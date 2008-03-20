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

package org.apache.tuscany.sca.interfacedef.java.jaxws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.FaultExceptionMapper;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.interfacedef.java.JavaOperation;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceVisitor;
import org.apache.tuscany.sca.interfacedef.util.ElementInfo;
import org.apache.tuscany.sca.interfacedef.util.WrapperInfo;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * Introspect the java class/interface with JSR-181 and JAXWS annotations
 * 
 * @version $Rev$ $Date$
 */
public class JAXWSJavaInterfaceProcessor implements JavaInterfaceVisitor {
    private static final String JAXB_DATABINDING = "javax.xml.bind.JAXBElement";
    private FaultExceptionMapper faultExceptionMapper;

    public JAXWSJavaInterfaceProcessor(FaultExceptionMapper faultExceptionMapper) {
        super();
        this.faultExceptionMapper = faultExceptionMapper;
    }

    public JAXWSJavaInterfaceProcessor() {
        super();
    }

    public void visitInterface(JavaInterface contract) throws InvalidInterfaceException {

        Class<?> clazz = contract.getJavaClass();
        WebService webService = clazz.getAnnotation(WebService.class);
        String tns = "";
        if (webService != null) {
            tns = webService.targetNamespace();
            // Mark SEI as Remotable
            contract.setRemotable(true);
        }
        if (!contract.isRemotable()) {
            return;
        }

        // SOAP binding (doc/lit/wrapped|bare or rpc/lit)
        SOAPBinding soapBinding = clazz.getAnnotation(SOAPBinding.class);

        for (Operation op : contract.getOperations()) {
            JavaOperation operation = (JavaOperation)op;
            Method method = operation.getJavaMethod();
            introspectFaultTypes(operation);

            // SOAP binding (doc/lit/wrapped|bare or rpc/lit)
            SOAPBinding methodSOAPBinding = method.getAnnotation(SOAPBinding.class);
            if (methodSOAPBinding == null) {
                methodSOAPBinding = soapBinding;
            }

            boolean documentStyle = true;
            if (methodSOAPBinding != null) {
                operation.setWrapperStyle(methodSOAPBinding.parameterStyle() == SOAPBinding.ParameterStyle.WRAPPED);
                documentStyle = methodSOAPBinding.style() == Style.DOCUMENT;
            }

            // WebMethod
            WebMethod webMethod = method.getAnnotation(WebMethod.class);
            if (webMethod == null) {
                continue;
            }
            String operationName = getValue(webMethod.operationName(), operation.getName());
            operation.setName(operationName);

            // Is one way?
            Oneway oneway = method.getAnnotation(Oneway.class);
            if (oneway != null) {
                // JSR 181
                assert method.getReturnType() == void.class;
                operation.setNonBlocking(true);
            }

            // Handle BARE mapping
            if (!operation.isWrapperStyle()) {
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    WebParam param = getAnnotation(method, i, WebParam.class);
                    if (param != null) {
                        String ns = getValue(param.targetNamespace(), tns);
                        // Default to <operationName> for doc-bare
                        String name = getValue(param.name(), documentStyle ? operationName : "arg" + i);
                        QName element = new QName(ns, name);
                        Object logical = operation.getInputType().getLogical().get(i).getLogical();
                        if (logical instanceof XMLType) {
                            ((XMLType)logical).setElementName(element);
                        }
                    }
                }
                WebResult result = method.getAnnotation(WebResult.class);
                if (result != null) {
                    String ns = getValue(result.targetNamespace(), tns);
                    // Default to <operationName>Response for doc-bare
                    String name = getValue(result.name(), documentStyle ? operationName + "Response" : "return");
                    QName element = new QName(ns, name);
                    Object logical = operation.getOutputType().getLogical();
                    if (logical instanceof XMLType) {
                        ((XMLType)logical).setElementName(element);
                    }
                }
            }

            RequestWrapper requestWrapper = method.getAnnotation(RequestWrapper.class);
            ResponseWrapper responseWrapper = method.getAnnotation(ResponseWrapper.class);
            if (requestWrapper == null) {
                continue;
            }

            String ns = getValue(requestWrapper.targetNamespace(), tns);
            String name = getValue(requestWrapper.localName(), operationName);
            QName inputWrapper = new QName(ns, name);

            ns = getValue(responseWrapper.targetNamespace(), tns);
            name = getValue(responseWrapper.localName(), operationName + "Response");

            QName outputWrapper = new QName(ns, name);

            List<ElementInfo> inputElements = new ArrayList<ElementInfo>();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                WebParam param = getAnnotation(method, i, WebParam.class);
                assert param != null;
                // Default to "" for doc-lit-wrapped && non-header
                ns = getValue(param.targetNamespace(), documentStyle && !param.header() ? "" : tns);
                name = getValue(param.name(), "arg" + i);
                QName element = new QName(ns, name);
                inputElements.add(new ElementInfo(element, null));
            }

            List<ElementInfo> outputElements = new ArrayList<ElementInfo>();
            WebResult result = method.getAnnotation(WebResult.class);
            // Default to "" for doc-lit-wrapped && non-header
            ns = getValue(result.targetNamespace(), documentStyle && !result.header() ? "" : tns);
            name = getValue(result.name(), "return");
            QName element = new QName(ns, name);
            outputElements.add(new ElementInfo(element, null));

            WrapperInfo wrapperInfo =
                new WrapperInfo(JAXB_DATABINDING, new ElementInfo(inputWrapper, null), new ElementInfo(outputWrapper,
                                                                                                       null),
                                inputElements, outputElements);
            operation.setWrapper(wrapperInfo);
            // operation.setDataBinding(JAXB_DATABINDING); // could be JAXB or SDO

        }
    }

    @SuppressWarnings("unchecked")
    private void introspectFaultTypes(Operation operation) {
        for (DataType exceptionType : operation.getFaultTypes()) {
            faultExceptionMapper.introspectFaultDataType(exceptionType);
        }
    }

    private <T extends Annotation> T getAnnotation(Method method, int index, Class<T> annotationType) {
        Annotation[] annotations = method.getParameterAnnotations()[index];
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationType) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    private static String getValue(String value, String defaultValue) {
        return "".equals(value) ? defaultValue : value;
    }

}
