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

package org.apache.tuscany.binding.ws.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.tuscany.assembly.AssemblyFactory;
import org.apache.tuscany.assembly.ComponentType;
import org.apache.tuscany.assembly.Composite;
import org.apache.tuscany.assembly.impl.DefaultAssemblyFactory;
import org.apache.tuscany.assembly.xml.ComponentTypeProcessor;
import org.apache.tuscany.assembly.xml.CompositeProcessor;
import org.apache.tuscany.assembly.xml.ConstrainingTypeProcessor;
import org.apache.tuscany.binding.ws.WebServiceBindingFactory;
import org.apache.tuscany.binding.ws.impl.DefaultWebServiceBindingFactory;
import org.apache.tuscany.contribution.processor.DefaultStAXArtifactProcessor;
import org.apache.tuscany.contribution.processor.DefaultStAXArtifactProcessorExtensionPoint;
import org.apache.tuscany.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.interfacedef.impl.DefaultInterfaceContractMapper;
import org.apache.tuscany.interfacedef.wsdl.WSDLFactory;
import org.apache.tuscany.interfacedef.wsdl.impl.DefaultWSDLFactory;
import org.apache.tuscany.interfacedef.wsdl.introspect.DefaultWSDLInterfaceIntrospector;
import org.apache.tuscany.interfacedef.wsdl.introspect.WSDLInterfaceIntrospector;
import org.apache.tuscany.policy.PolicyFactory;
import org.apache.tuscany.policy.impl.DefaultPolicyFactory;

/**
 * Test reading/write WSDL interfaces.
 * 
 * @version $Rev$ $Date$
 */
public class WriteTestCase extends TestCase {

    XMLInputFactory inputFactory;
    DefaultStAXArtifactProcessorExtensionPoint staxProcessors;
    DefaultStAXArtifactProcessor staxProcessor;
    private AssemblyFactory factory;
    private PolicyFactory policyFactory;
    private InterfaceContractMapper mapper;
    private WebServiceBindingFactory wsFactory;
    private WSDLInterfaceIntrospector introspector;
    private WSDLFactory wsdlFactory;

    public void setUp() throws Exception {
        factory = new DefaultAssemblyFactory();
        policyFactory = new DefaultPolicyFactory();
        mapper = new DefaultInterfaceContractMapper();
        inputFactory = XMLInputFactory.newInstance();
        staxProcessors = new DefaultStAXArtifactProcessorExtensionPoint();
        staxProcessor = new DefaultStAXArtifactProcessor(staxProcessors, XMLInputFactory.newInstance(), XMLOutputFactory.newInstance());
        wsFactory = new DefaultWebServiceBindingFactory();
        introspector = new DefaultWSDLInterfaceIntrospector(wsdlFactory);
        wsdlFactory = new DefaultWSDLFactory();

        staxProcessors.addArtifactProcessor(new CompositeProcessor(factory, policyFactory, mapper, staxProcessor));
        staxProcessors.addArtifactProcessor(new ComponentTypeProcessor(factory, policyFactory, staxProcessor));
        staxProcessors.addArtifactProcessor(new ConstrainingTypeProcessor(factory, policyFactory, staxProcessor));

        WebServiceBindingProcessor wsdlProcessor = new WebServiceBindingProcessor(
                                                                                  factory, policyFactory, wsFactory,
                                                                                  wsdlFactory, introspector);
        staxProcessors.addArtifactProcessor(wsdlProcessor);
    }

    public void tearDown() throws Exception {
        inputFactory = null;
        staxProcessors = null;
        policyFactory = null;
        factory = null;
        mapper = null;
    }

    public void testReadWriteComponentType() throws Exception {
        InputStream is = getClass().getResourceAsStream("CalculatorImpl.componentType");
        ComponentType componentType = staxProcessor.read(is, ComponentType.class);
        assertNotNull(componentType);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(componentType, bos);
    }

    public void testReadWriteComposite() throws Exception {
        InputStream is = getClass().getResourceAsStream("Calculator.composite");
        Composite composite = staxProcessor.read(is, Composite.class);
        assertNotNull(composite);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        staxProcessor.write(composite, bos);
    }

}
