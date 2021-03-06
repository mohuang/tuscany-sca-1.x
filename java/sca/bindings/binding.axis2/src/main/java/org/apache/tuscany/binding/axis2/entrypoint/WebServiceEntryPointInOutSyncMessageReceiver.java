/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.binding.axis2.entrypoint;

import java.lang.reflect.Method;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.tuscany.binding.axis2.util.SDODataBinding;
import org.apache.tuscany.core.wire.InvocationRuntimeException;

public class WebServiceEntryPointInOutSyncMessageReceiver extends AbstractInOutSyncMessageReceiver {

    private Object entryPointProxy;

    protected Method operationMethod;

    protected SDODataBinding dataBinding;
    
    protected ClassLoader classLoader;

    public WebServiceEntryPointInOutSyncMessageReceiver(Object entryPointProxy, Method operationMethod, SDODataBinding dataBinding, ClassLoader classLoader) {
        this.entryPointProxy = entryPointProxy;
        this.operationMethod = operationMethod;
        this.dataBinding = dataBinding;
        this.classLoader = classLoader;
    }

    @Override
    public void invokeBusinessLogic(MessageContext inMC, MessageContext outMC) throws AxisFault {
        try {

            OMElement requestOM = inMC.getEnvelope().getBody().getFirstElement();
            Object[] request;
            if (requestOM != null) {
                request = dataBinding.fromOMElement(requestOM);
            } else {
            	request = new Object[0];
            }
            
            Object response;
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                if (tccl != classLoader) {
                    Thread.currentThread().setContextClassLoader(classLoader);
                }

                response = operationMethod.invoke(entryPointProxy, request);

            } finally {
                if (tccl != classLoader) {
                    Thread.currentThread().setContextClassLoader(tccl);
                }
            }

            SOAPEnvelope soapEnvelope = getSOAPFactory(inMC).getDefaultEnvelope();

            OMElement responseOM = null;
            if (response != null) {
                responseOM = dataBinding.toOMElement(new Object[] { response });
                soapEnvelope.getBody().addChild(responseOM);
            }

            outMC.setEnvelope(soapEnvelope);
            outMC.getOperationContext().setProperty(Constants.RESPONSE_WRITTEN, Constants.VALUE_TRUE);

        } catch (InvocationRuntimeException e) {
           // throw new InvocationRuntimeException(e);
            Throwable t = e.getCause();
            if(t instanceof Exception) {
               
                throw AxisFault.makeFault((Exception)t);
                
            }
            throw e;
        
        
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }
}
