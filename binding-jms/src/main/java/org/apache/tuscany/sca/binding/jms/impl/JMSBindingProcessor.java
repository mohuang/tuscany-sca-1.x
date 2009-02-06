/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.binding.jms.impl;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.sca.assembly.OperationSelector;
import org.apache.tuscany.sca.assembly.WireFormat;
import org.apache.tuscany.sca.assembly.builder.impl.ProblemImpl;
import org.apache.tuscany.sca.assembly.xml.Constants;
import org.apache.tuscany.sca.assembly.xml.PolicyAttachPointProcessor;
import org.apache.tuscany.sca.binding.jms.operationselector.jmsdefault.OperationSelectorJMSDefault;
import org.apache.tuscany.sca.binding.jms.wireformat.jmsobject.WireFormatJMSObject;
import org.apache.tuscany.sca.binding.jms.wireformat.jmstext.WireFormatJMSText;
import org.apache.tuscany.sca.binding.jms.wireformat.jmstextxml.WireFormatJMSTextXML;
import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.contribution.processor.StAXArtifactProcessor;
import org.apache.tuscany.sca.contribution.resolver.DefaultModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ExtensibleModelResolver;
import org.apache.tuscany.sca.contribution.resolver.ModelResolver;
import org.apache.tuscany.sca.contribution.service.ContributionReadException;
import org.apache.tuscany.sca.contribution.service.ContributionResolveException;
import org.apache.tuscany.sca.contribution.service.ContributionWriteException;
import org.apache.tuscany.sca.monitor.Monitor;
import org.apache.tuscany.sca.monitor.Problem;
import org.apache.tuscany.sca.monitor.Problem.Severity;
import org.apache.tuscany.sca.policy.PolicyFactory;

/**
 * A processor to read the XML that describes the JMS binding...
 * 
 * <binding.jms correlationScheme="string"?
 *              initialContextFactory="xs:anyURI"?
 *              jndiURL="xs:anyURI"?
 *              requestConnection="QName"?
 *              responseConnection="QName"?
 *              operationProperties="QName"?
 *              ...>
 * 
 *     <destination name="xs:anyURI" type="string"? create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </destination>?
 * 
 *     <connectionFactory name="xs:anyURI" create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </connectionFactory>?
 * 
 *     <activationSpec name="xs:anyURI" create="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </activationSpec>?
 * 
 *     <response>
 *         <destination name="xs:anyURI" type="string"? create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </destination>?
 * 
 *         <connectionFactory name="xs:anyURI" create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </connectionFactory>?
 * 
 *         <activationSpec name="xs:anyURI" create="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </activationSpec>?
 *     </response>?
 * 
 *     <complexType name="SubscriptionHeaders"> 
 *         <attribute name="JMSSelector" type="string"/> 
 *     </complexType>
 *
 *     <resourceAdapter name="NMTOKEN">?
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </resourceAdapter>?
 * 
 *     <headers JMSType="string"?
 *              JMSCorrelationId="string"?
 *              JMSDeliveryMode="string"?
 *              JMSTimeToLive="int"?
 *              JMSPriority="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *     </headers>?
 * 
 *     <operationProperties name="string" nativeOperation="string"?>
 *         <property name="NMTOKEN" type="NMTOKEN">*
 *         <headers JMSType="string"?
 *                  JMSCorrelationId="string"?
 *                  JMSDeliveryMode="string"?
 *                  JMSTimeToLive="int"?
 *                  JMSPriority="string"?>
 *             <property name="NMTOKEN" type="NMTOKEN">*
 *         </headers>?
 *     </operationProperties>*
 * </binding.jms>
 *
 * Parsing error messages are recorded locally and reported as validation exceptions. Parsing
 * warnings do not cause validation exceptions.
 *
 * @version $Rev$ $Date$
 */

public class JMSBindingProcessor implements StAXArtifactProcessor<JMSBinding> {

    private PolicyFactory policyFactory;
    private PolicyAttachPointProcessor policyProcessor;
    protected StAXArtifactProcessor<Object> extensionProcessor;
    private Monitor monitor;
    protected String validationMessage;

    public JMSBindingProcessor(ModelFactoryExtensionPoint modelFactories, StAXArtifactProcessor<Object> extensionProcessor, Monitor monitor) {
        this.policyFactory = modelFactories.getFactory(PolicyFactory.class);
        this.policyProcessor = new PolicyAttachPointProcessor(policyFactory);
        this.extensionProcessor = extensionProcessor;
        this.monitor = monitor;
        this.validationMessage = null;
    }
    
    /**
     * Report a error.
     * 
     * @param problems
     * @param message
     * @param model
    */
    private void warning(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = new ProblemImpl(this.getClass().getName(), "binding-jms-validation-messages", Severity.WARNING, model, message, (Object[])messageParameters);
    	    monitor.problem(problem);
        }        
    }
     
    /**
      * Report an error.
      * One side effect is that error messages are saved for future validation calls.
      * 
      * @param problems
      * @param message
      * @param model
    */
    private void error(String message, Object model, Object... messageParameters) {
        if (monitor != null) {
            Problem problem = new ProblemImpl(this.getClass().getName(), "binding-jms-validation-messages", Severity.ERROR, model, message, (Object[])messageParameters);
            validationMessage = problem.toString(); // Record error message for use in validation.
     	    monitor.problem(problem);
        }        
    }

    public QName getArtifactType() {
        return JMSBindingConstants.BINDING_JMS_QNAME;
    }

    public Class<JMSBinding> getModelType() {
        return JMSBinding.class;
    }

    public JMSBinding read(XMLStreamReader reader) throws ContributionReadException, XMLStreamException {
        JMSBinding jmsBinding = new JMSBinding();
        // Reset validation message to keep track of validation issues.
        this.validationMessage = null;

        // Read policies
        policyProcessor.readPolicies(jmsBinding, reader);

        // Read binding name
        String name = reader.getAttributeValue(null, "name");
        if (name != null) {
            jmsBinding.setName(name);
        }

        // Read binding URI
        String uri = reader.getAttributeValue(null, "uri");
        if (uri != null && uri.length() > 0) {
            parseURI(uri, jmsBinding);
        }

        // Read correlation scheme
        String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null && correlationScheme.length() > 0) {
            if (JMSBindingConstants.VALID_CORRELATION_SCHEMES.contains(correlationScheme.toLowerCase())) {
                jmsBinding.setCorrelationScheme(correlationScheme);
            } else {
            	error("InvalidCorrelationScheme", reader, correlationScheme);
            }
        }

        // Read initial context factory
        String initialContextFactory = reader.getAttributeValue(null, "initialContextFactory");
        if (initialContextFactory != null && initialContextFactory.length() > 0) {
            jmsBinding.setInitialContextFactoryName(initialContextFactory);
        }

        // Read JNDI URL
        String jndiURL = reader.getAttributeValue(null, "jndiURL");
        if (jndiURL != null && jndiURL.length() > 0) {
            jmsBinding.setJndiURL(jndiURL);
        }

        // Read message processor class name
        // TODO - maintain this for the time being but move over to 
        //        configuring wire formats instead of message processors
        String messageProcessorName = reader.getAttributeValue(null, "messageProcessor");
        if (messageProcessorName != null && messageProcessorName.length() > 0) {
            if ("XMLTextMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSTextXML());
                jmsBinding.setResponseWireFormat(new WireFormatJMSTextXML());
            } else if ("TextMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSText());
                jmsBinding.setResponseWireFormat(new WireFormatJMSText());
            } else if ("ObjectMessage".equalsIgnoreCase(messageProcessorName)) {
                // may be overwritten be real wire format later
                jmsBinding.setRequestWireFormat(new WireFormatJMSObject());
                jmsBinding.setResponseWireFormat(new WireFormatJMSObject());
            } else {
                jmsBinding.setRequestMessageProcessorName(messageProcessorName);
                jmsBinding.setResponseMessageProcessorName(messageProcessorName);
            }
        }

        String requestConnectionName = reader.getAttributeValue(null, "requestConnection");
        if (requestConnectionName != null && requestConnectionName.length() > 0) {
            jmsBinding.setRequestConnectionName(requestConnectionName);
        }
        String responseConnectionName = reader.getAttributeValue(null, "responseConnection");
        if (responseConnectionName != null && responseConnectionName.length() > 0) {
            jmsBinding.setResponseConnectionName(responseConnectionName);
        }

        // Read sub-elements of binding.jms
        boolean endFound = false;
        while (!endFound) {
            int fg = reader.next();
            switch (fg) {
                case START_ELEMENT:
                    String elementName = reader.getName().getLocalPart();
                    if ("destination".equals(elementName)) {
                        parseDestination(reader, jmsBinding);
                    } else if ("connectionFactory".equals(elementName)) {
                        parseConnectionFactory(reader, jmsBinding);
                    } else if ("activationSpec".equals(elementName)) {
                        parseActivationSpec(reader, jmsBinding);
                    } else if ("response".equals(elementName)) {
                        parseResponse(reader, jmsBinding);
                    } else if ("resourceAdapter".equals(elementName)) {
                        parseResourceAdapter(reader, jmsBinding);
                    } else if ("headers".equals(elementName)) {
                        parseHeaders(reader, jmsBinding);
                    } else if ("operationProperties".equals(elementName)) {
                        parseOperationProperties(reader, jmsBinding);
                    } else if ("SubscriptionHeaders".equals(elementName)) {
                        parseSubscriptionHeaders(reader, jmsBinding);
                    } else {
                        Object extension = extensionProcessor.read(reader);
                        if (extension != null) {
                            if (extension instanceof WireFormat) {
                                jmsBinding.setRequestWireFormat((WireFormat)extension);
                            } else if (extension instanceof OperationSelector) {
                                jmsBinding.setOperationSelector((OperationSelector)extension);
                            } else {
                                error("UnexpectedElement", reader, extension.toString());
                            }
                        }
                    }
                    reader.next();
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.equals(JMSBindingConstants.BINDING_JMS_QNAME)) {
                        endFound = true;
                    } else {
                    	error("UnexpectedElement", reader, x.toString());
                    }
            }
        }
        
        // if no operation selector is specified then assume the default
        if (jmsBinding.getOperationSelector() == null){
            jmsBinding.setOperationSelector(new OperationSelectorJMSDefault());
        }
        
        // if no request wire format specified then assume the default
        if (jmsBinding.getRequestWireFormat() == null){
            jmsBinding.setRequestWireFormat(new WireFormatJMSTextXML());
        }
        
        // if no response wire format specific then assume the default
        if (jmsBinding.getResponseWireFormat() == null){
            jmsBinding.setResponseWireFormat(jmsBinding.getRequestWireFormat());
         }

        validate( jmsBinding );

        return jmsBinding;
    }

    protected void parseURI(String uri, JMSBinding jmsBinding) {
        if (!uri.startsWith("jms:")) {
        	error("MustStartWithSchema", jmsBinding, uri);
        	return;
        }
        int i = uri.indexOf('?');            
        if (i >= 0) {
        	StringTokenizer st = new StringTokenizer(uri.substring(i+1),"&");
        	while (st.hasMoreTokens()) {
        	    String s = st.nextToken();
        	    if (s.startsWith("connectionFactoryName=")) {
        	        jmsBinding.setConnectionFactoryName(s.substring(22));
        	    } else {
        	        error("UnknownTokenInURI", jmsBinding, s, uri);
                 	return;
        	     }
        	}
        	jmsBinding.setDestinationName(uri.substring(4, i));
        } else {
           jmsBinding.setDestinationName(uri.substring(4));
        }
    }

    public void resolve(JMSBinding model, ModelResolver resolver) throws ContributionResolveException {
        if (model.getRequestConnectionName() != null) {
            model.setRequestConnectionBinding(getConnectionBinding(model.getRequestConnectionName(), resolver));
        }
        if (model.getResponseConnectionName() != null) {
            model.setResponseConnectionBinding(getConnectionBinding(model.getResponseConnectionName(), resolver));
        }
    }

    @SuppressWarnings("unchecked")
    private JMSBinding getConnectionBinding(String bindingName, ModelResolver resolver) {
        if (resolver instanceof ExtensibleModelResolver) {
            DefaultModelResolver dr = (DefaultModelResolver)((ExtensibleModelResolver) resolver).getDefaultModelResolver();
            Map models = dr.getModels();
            for (Object o : models.keySet()) {
                if (o instanceof JMSBinding) {
                    JMSBinding binding = (JMSBinding) o;
                    if (bindingName.equals(binding.getName())) {
                        return binding;
                    }
                }
            }
        }
        return null;
    }

    public void write(JMSBinding rmiBinding, XMLStreamWriter writer) throws ContributionWriteException,
        XMLStreamException {
        // Write a <binding.jms>
        writer.writeStartElement(Constants.SCA10_NS, JMSBindingConstants.BINDING_JMS);

        // FIXME Implement

        writer.writeEndElement();
    }

    private void parseDestination(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setDestinationName(name);
        }

        String type = reader.getAttributeValue(null, "type");                
        if (type != null && type.length() > 0) {
            warning("DoesntProcessDestinationType", jmsBinding);
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(type)) {
                jmsBinding.setDestinationType(JMSBindingConstants.DESTINATION_TYPE_QUEUE);
            } else if (JMSBindingConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type)) {
                jmsBinding.setDestinationType(JMSBindingConstants.DESTINATION_TYPE_TOPIC);
            } else {
            	error("InvalidDestinationType", reader, type);
            }            
        }

        String create = reader.getAttributeValue(null, "create");
        if (create != null && create.length() > 0) {
            jmsBinding.setDestinationCreate(create);
        }

        jmsBinding.getDestinationProperties().putAll(parseBindingProperties(reader));
    }

    private void parseConnectionFactory(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setConnectionFactoryName(name);
        } else {
            error("MissingConnectionFactoryName", reader);
        }
        jmsBinding.getConnectionFactoryProperties().putAll(parseBindingProperties(reader));
    }

    private void parseActivationSpec(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");        
        if (name != null && name.length() > 0) {
            jmsBinding.setActivationSpecName(name);            
        } else {
            warning("MissingActivationSpecName", reader);
        }
        jmsBinding.getActivationSpecProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseDestination(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setResponseDestinationName(name);
        }

        String type = reader.getAttributeValue(null, "type");        
        if (type != null && type.length() > 0) {
            warning("DoesntProcessResponseDestinationType", jmsBinding);
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(type)) {
                jmsBinding.setResponseDestinationType(JMSBindingConstants.DESTINATION_TYPE_QUEUE);
            } else if (JMSBindingConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type)) {
                jmsBinding.setResponseDestinationType(JMSBindingConstants.DESTINATION_TYPE_TOPIC);
            } else {
                error("InvalidResponseDestinationType", reader, type);
            }
        }

        String create = reader.getAttributeValue(null, "create");
        if (create != null && create.length() > 0) {
            jmsBinding.setResponseDestinationCreate(create);
        }

        jmsBinding.getResponseDestinationProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseConnectionFactory(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            warning("DoesntProcessResponseConnectionFactory", jmsBinding);
            jmsBinding.setResponseConnectionFactoryName(name);            
        } else {
            warning("MissingResponseConnectionFactory", reader);
        }
        jmsBinding.getResponseConnectionFactoryProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponseActivationSpec(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name != null && name.length() > 0) {
            jmsBinding.setResponseActivationSpecName(name);            
        } else {
            warning("MissingResponseActivationSpec", reader);
        }
        jmsBinding.getResponseActivationSpecProperties().putAll(parseBindingProperties(reader));
    }

    private void parseResponse(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        // Read sub-elements of response
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    String elementName = reader.getName().getLocalPart();
                    if ("destination".equals(elementName)) {
                        parseResponseDestination(reader, jmsBinding);
                    } else if ("connectionFactory".equals(elementName)) {
                        parseResponseConnectionFactory(reader, jmsBinding);
                    } else if ("activationSpec".equals(elementName)) {
                        parseResponseActivationSpec(reader, jmsBinding);
                    }
                    reader.next();
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("response")) {
                        return;
                    } else {
                    	error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }

    private void parseResourceAdapter(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
    	warning("DoesntProcessResourceAdapter", jmsBinding);
    }

    /**
     * <headers JMSType=�string�?
     *          JMSCorrelationID=�string�?
     *          JMSDeliveryMode=�PERSISTENT or NON_PERSISTENT�?
     *          JMSTimeToLive=�long�?      
     *          JMSPriority=�0 .. 9�?>
     *     <property name=�NMTOKEN� type=�NMTOKEN�?>*    
     * </headers>?
     */
    private void parseHeaders(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        
        String jmsType = reader.getAttributeValue(null, "JMSType");
        if (jmsType != null && jmsType.length() > 0) {
            jmsBinding.setJMSType(jmsType);
        }

        String jmsCorrelationId = reader.getAttributeValue(null, "JMSCorrelationID");
        if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
            jmsBinding.setJMSCorrelationId(jmsCorrelationId);
        }

        String jmsDeliveryMode = reader.getAttributeValue(null, "JMSDeliveryMode");
        if (jmsDeliveryMode != null && jmsDeliveryMode.length() > 0) {
            if ("PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(true);
            } else if ("NON_PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(false);
            } else {
                error("InvalidJMSDeliveryMode", jmsBinding, jmsDeliveryMode);
            }
        }

        String jmsTimeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (jmsTimeToLive != null && jmsTimeToLive.length() > 0) {
            jmsBinding.setJMSTimeToLive(Long.parseLong(jmsTimeToLive));
        }

        String jmsPriority = reader.getAttributeValue(null, "JMSPriority");
        if (jmsPriority != null && jmsPriority.length() > 0) {
            int p = Integer.parseInt(jmsPriority);
            if (p >= 0 && p <= 9) {
                jmsBinding.setJMSPriority(p);
            } else {
                warning("InvalidJMSPriority", jmsBinding, jmsPriority);
            }
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("property")) {
                        parseProperty(reader, jmsBinding);
                    }
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("headers")) {
                        return;
                    } else {
                        error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }
    
    private void parseProperty(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");
        if (name != null && name.length() > 0) {
            Object value = reader.getElementText();
            if ("boolean".equalsIgnoreCase(type)) {
                value = Boolean.parseBoolean((String)value);
            } else if ("byte".equalsIgnoreCase(type)) {
                value = Byte.parseByte(((String)value));
            } else if ("short".equalsIgnoreCase(type)) {
                value = Short.parseShort((String)value);
            } else if ("int".equalsIgnoreCase(type)) {
                value = Integer.parseInt((String)value);
            } else if ("long".equalsIgnoreCase(type)) {
                value = Long.parseLong((String)value);
            } else if ("float".equalsIgnoreCase(type)) {
                value = Float.parseFloat((String)value);
            } else if ("double".equalsIgnoreCase(type)) {
                value = Double.parseDouble((String)value);
            } else if ("String".equalsIgnoreCase(type)) {
                // its already a string
            }
            jmsBinding.setProperty(name, value);
        }
    }

    /**
     * <operationProperties name=�string� nativeOperation=�string�?>
     *   <property name=�NMTOKEN� type=�NMTOKEN�?>*
     *   <headers JMSType=�string�?
     *            JMSCorrelationId=�string�?
     *            JMSDeliveryMode=�PERSISTENT or NON_PERSISTENT�?
     *            JMSTimeToLive=�long�?
     *            JMSPriority=�0 .. 9�?>
     *       <property name=�NMTOKEN� type=�NMTOKEN�?>*
     *   </headers>?
     * </operationProperties>*
     */
    private void parseOperationProperties(XMLStreamReader reader, JMSBinding jmsBinding) throws XMLStreamException {
        String opName = reader.getAttributeValue(null, "name");
        if (opName == null || opName.length() < 1) {
            warning("MissingJMSOperationPropertyName", jmsBinding);
            return;
        }
        String nativeOpName = reader.getAttributeValue(null, "nativeOperation");
        if (nativeOpName != null && nativeOpName.length() > 0) {
            jmsBinding.setNativeOperationName(opName, nativeOpName);
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("headers")) {
                        parseOperationHeaders(reader, jmsBinding, opName);
                    }
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("operationProperties")) {
                        return;
                    } else {
                        error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }

    private void parseOperationHeaders(XMLStreamReader reader, JMSBinding jmsBinding, String opName) throws XMLStreamException {
        String jmsType = reader.getAttributeValue(null, "JMSType");
        if (jmsType != null && jmsType.length() > 0) {
            jmsBinding.setOperationJMSType(opName, jmsType);
        }

        String jmsCorrelationId = reader.getAttributeValue(null, "JMSCorrelationID");
        if (jmsCorrelationId != null && jmsCorrelationId.length() > 0) {
            jmsBinding.setOperationJMSCorrelationId(opName, jmsCorrelationId);
        }

        String jmsDeliveryMode = reader.getAttributeValue(null, "JMSDeliveryMode");
        if (jmsDeliveryMode != null && jmsDeliveryMode.length() > 0) {
            if ("PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setJMSDeliveryMode(true);
            } else if ("NON_PERSISTENT".equalsIgnoreCase(jmsDeliveryMode)) {
                jmsBinding.setOperationJMSDeliveryMode(opName, false);
            } else {
                error("InvalidOPJMSDeliveryMode", jmsBinding, jmsDeliveryMode);
            }
        }

        String jmsTimeToLive = reader.getAttributeValue(null, "JMSTimeToLive");
        if (jmsTimeToLive != null && jmsTimeToLive.length() > 0) {
            jmsBinding.setOperationJMSTimeToLive(opName, Long.parseLong(jmsTimeToLive));
        }

        String jmsPriority = reader.getAttributeValue(null, "JMSPriority");
        if (jmsPriority != null && jmsPriority.length() > 0) {
            int p = Integer.parseInt(jmsPriority);
            if (p >= 0 && p <= 9) {
                jmsBinding.setOperationJMSPriority(opName, p);
            } else {
                warning("InvalidOPJMSPriority", jmsBinding, jmsPriority);
            }
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("property")) {
                        parseOperationPropertyProperties(reader, jmsBinding, opName);
                    }
                    break;
                case END_ELEMENT:
                    QName x = reader.getName();
                    if (x.getLocalPart().equals("headers")) {
                        return;
                    } else {
                        error("UnexpectedResponseElement", reader, x.toString());
                    }
            }
        }
    }

    private void parseOperationPropertyProperties(XMLStreamReader reader, JMSBinding jmsBinding, String opName) throws XMLStreamException {
        String propName = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");
        if (propName != null && propName.length() > 0) {
            Object value = reader.getElementText();
            if ("boolean".equalsIgnoreCase(type)) {
                value = Boolean.parseBoolean((String)value);
            } else if ("byte".equalsIgnoreCase(type)) {
                value = Byte.parseByte(((String)value));
            } else if ("short".equalsIgnoreCase(type)) {
                value = Short.parseShort((String)value);
            } else if ("int".equalsIgnoreCase(type)) {
                value = Integer.parseInt((String)value);
            } else if ("long".equalsIgnoreCase(type)) {
                value = Long.parseLong((String)value);
            } else if ("float".equalsIgnoreCase(type)) {
                value = Float.parseFloat((String)value);
            } else if ("double".equalsIgnoreCase(type)) {
                value = Double.parseDouble((String)value);
            } else if ("String".equalsIgnoreCase(type)) {
                // its already a string
            }
            jmsBinding.setOperationProperty(opName, propName, value);
        }
    }

    private void parseSubscriptionHeaders(XMLStreamReader reader, JMSBinding jmsBinding) {
        String jmsSelector = reader.getAttributeValue(null, "JMSSelector");
        if (jmsSelector != null && jmsSelector.length() > 0) {
            jmsBinding.setJMSSelector(jmsSelector);
        }
    }

    private Map<String, BindingProperty> parseBindingProperties(XMLStreamReader reader) throws XMLStreamException {
        Map<String, BindingProperty> props = new HashMap<String, BindingProperty>();
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    String elementName = reader.getName().getLocalPart();
                    if ("property".equals(elementName)) {
                        String name = reader.getAttributeValue(null, "name");
                        if (name == null || name.length() < 1) {
                            error("InvalidPropertyElement", reader, elementName);
                        }
                        String type = reader.getAttributeValue(null, "type");
                        String value = reader.getElementText();
                        props.put(name, new BindingProperty(name, type, value));
                    }
                    break;
                case END_ELEMENT:
                    return props;
            }
        }
    }

    /**
     * Preserve an existing public method. The method validate() is a legacy method 
     * that was called from reading am XML stream via the read(XMLStreamReader) method above.
     * However read(XMLStreamReader) now calls validate(JMSBinding jmsBinding) and
     * passes in the jmsBinding model.
     * The older validate() now calls validate(JMSBinding jmsBinding) with a null model. 
     */
    public void validate() throws JMSBindingException {
        validate( null );
    }
    
    /**
     * Validates JMS parsing and JMSBinding model.
     * Validation rules are taken from the binding schema and the OSOA and OASIS specs:
     *    http://www.oasis-open.org/committees/documents.php?wg_abbrev=sca-bindings
     *    (sca-binding-jms-1.1-spec-cd01-rev4.pdf)
     *    http://www.osoa.org/display/Main/Service+Component+Architecture+Specifications
     *    (SCA JMS Binding V1.00 )
     * @param jmsBinding an optional JMS binding model to check for validity.  
     * @since 1.4
     */
    protected void validate( JMSBinding jmsBinding ) {
        // Check validation message for issues that arise from parsing errors.
        if ( validationMessage != null ) {
            throw new JMSBindingException( validationMessage );
        }
        
        // If no JMSBinding model is provided, that is all the validation we can do.
        if ( jmsBinding == null ) {
            return;
        }

        // Connection factory should not contradict destination type.
        String connectionFactoryName = jmsBinding.getConnectionFactoryName();
        if (( connectionFactoryName != null ) && ( connectionFactoryName.length() > 0 )) {
            if (JMSBindingConstants.DESTINATION_TYPE_QUEUE == jmsBinding.getDestinationType()) {
                if ( connectionFactoryName.contains( "topic" )) {
                    error("DestinationQueueContradiction", jmsBinding, connectionFactoryName );
                }
            }
            if (JMSBindingConstants.DESTINATION_TYPE_TOPIC == jmsBinding.getDestinationType()) {
                if ( connectionFactoryName.contains( "queue" )) {
                    error("DestinationTopicContradiction", jmsBinding, connectionFactoryName );
                }
            }
        }
        
        // Connection factory and activation Specification are mutually exclusive.
        if (( connectionFactoryName != null ) && ( connectionFactoryName.length() > 0 ) 
            && !JMSBindingConstants.DEFAULT_CONNECTION_FACTORY_NAME.equals(connectionFactoryName) ) {
            String activationSpecName = jmsBinding.getActivationSpecName();
            if ((activationSpecName != null) && (activationSpecName.length() > 0 )) {
                error("ConnectionFactoryActivationSpecContradiction", jmsBinding, connectionFactoryName, activationSpecName );                
            }
        }

        // Given a response connection name attribute, there must not be a response element.
        // 156 � /binding.jms/@responseConnection � identifies a binding.jms element that is present in a
        // 157 definition document, whose response child element is used to define the values for this binding. In
        // 158 this case this binding.jms element MUST NOT contain a response element.
        String responseConnectionName = jmsBinding.getResponseConnectionName();
        if (( responseConnectionName != null ) && ( responseConnectionName.length() > 0 )) {
            String responseDestinationName = jmsBinding.getResponseDestinationName();
            if (( responseDestinationName != null ) && (responseDestinationName.length() > 0)) {
                error("ResponseAttrElement", jmsBinding, responseConnectionName, responseDestinationName );                               
            }
        }

        // Other jmsBinding model validation may be added here.
        
        // Check validation message for issues that arise from internal model validation errors.
        if ( validationMessage != null ) {
            throw new JMSBindingException( validationMessage );
        }

    }
}
