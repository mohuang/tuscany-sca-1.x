/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.core.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.tuscany.spi.QualifiedName;
import org.apache.tuscany.spi.component.TargetException;
import org.apache.tuscany.spi.wire.InboundInvocationChain;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.Interceptor;
import org.apache.tuscany.spi.wire.MessageHandler;
import org.apache.tuscany.spi.wire.OutboundInvocationChain;
import org.apache.tuscany.spi.wire.OutboundWire;

import org.apache.tuscany.core.util.MethodHashMap;

/**
 * Default implementation of an outbound wire
 *
 * @version $Rev$ $Date$
 */
public class OutboundWireImpl<T> implements OutboundWire<T> {

    private Class<T>[] businessInterfaces;
    private Class<T>[] callbackInterfaces;
    private Map<Method, OutboundInvocationChain> chains = new MethodHashMap<OutboundInvocationChain>();
    private Map<Method, InboundInvocationChain> callbackTargetChains = new MethodHashMap<InboundInvocationChain>();
    private Map<Method, OutboundInvocationChain> callbackSourceChains = new MethodHashMap<OutboundInvocationChain>();
    private String referenceName;
    private QualifiedName targetName;
    private InboundWire<T> targetWire;

    @SuppressWarnings("unchecked")
    public T getTargetService() throws TargetException {
        if (targetWire != null) {
            // optimized, no interceptors or handlers on either end
            return targetWire.getTargetService();
        }
        throw new TargetException("Target wire not optimized");
    }

    @SuppressWarnings("unchecked")
    public void setBusinessInterface(Class<T> interfaze) {
        businessInterfaces = new Class[]{interfaze};
    }

    public Class<T> getBusinessInterface() {
        return businessInterfaces[0];
    }

    public void addInterface(Class<?> claz) {
        throw new UnsupportedOperationException("Additional proxy interfaces not yet supported");
    }

    public Class[] getImplementedInterfaces() {
        return businessInterfaces;
    }

    @SuppressWarnings("unchecked")
    public void setCallbackInterface(Class<T> interfaze) {
        callbackInterfaces = new Class[]{interfaze};
    }

    public Class<T> getCallbackInterface() {
        return callbackInterfaces[0];
    }

    public void addCallbackInterface(Class<?> claz) {
        throw new UnsupportedOperationException("Additional callback interfaces not yet supported");
    }

    public Class[] getImplementedCallbackInterfaces() {
        return callbackInterfaces;
    }

    public void setTargetWire(InboundWire<T> wire) {
        this.targetWire = wire;
    }

    public Map<Method, OutboundInvocationChain> getInvocationChains() {
        return chains;
    }

    public void addInvocationChains(Map<Method, OutboundInvocationChain> chains) {
        this.chains.putAll(chains);
    }

    public void addInvocationChain(Method method, OutboundInvocationChain chain) {
        chains.put(method, chain);
    }

    public Map<Method, InboundInvocationChain> getTargetCallbackInvocationChains() {
        return callbackTargetChains;
    }

    public void addTargetCallbackInvocationChains(Map<Method, InboundInvocationChain> chains) {
        callbackTargetChains.putAll(chains);
    }

    public void addTargetCallbackInvocationChain(Method method, InboundInvocationChain chain) {
        callbackTargetChains.put(method, chain);
    }

    public Map<Method, OutboundInvocationChain> getSourceCallbackInvocationChains() {
        return callbackSourceChains;
    }

    public void addSourceCallbackInvocationChains(Map<Method, OutboundInvocationChain> chains) {
        callbackSourceChains.putAll(chains);
    }

    public void addSourceCallbackInvocationChain(Method method, OutboundInvocationChain chain) {
        callbackSourceChains.put(method, chain);
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public QualifiedName getTargetName() {
        return targetName;
    }

    public void setTargetName(QualifiedName targetName) {
        this.targetName = targetName;
    }

    public boolean isOptimizable() {
        for (OutboundInvocationChain chain : chains.values()) {
            if (chain.getHeadInterceptor() != null || !chain.getRequestHandlers().isEmpty()
                || !chain.getResponseHandlers().isEmpty()) {
                Interceptor current = chain.getHeadInterceptor();
                while (current != null && current != chain.getTargetInterceptor()) {
                    if (!current.isOptimizable()) {
                        return false;
                    }
                    current = current.getNext();
                }
                if (chain.getRequestHandlers() != null) {
                    for (MessageHandler handler : chain.getRequestHandlers()) {
                        if (!handler.isOptimizable()) {
                            return false;
                        }
                    }
                }
                if (chain.getResponseHandlers() != null) {
                    for (MessageHandler handler : chain.getResponseHandlers()) {
                        if (!handler.isOptimizable()) {
                            return false;
                        }
                    }
                }
            }
        }

        for (InboundInvocationChain chain : callbackTargetChains.values()) {
            if (chain.getTargetInvoker() != null && !chain.getTargetInvoker().isOptimizable()) {
                return false;
            }
            if (chain.getHeadInterceptor() != null) {
                Interceptor current = chain.getHeadInterceptor();
                while (current != null) {
                    if (!current.isOptimizable()) {
                        return false;
                    }
                    current = current.getNext();
                }
            }
            if (chain.getRequestHandlers() != null && !chain.getRequestHandlers().isEmpty()) {
                for (MessageHandler handler : chain.getRequestHandlers()) {
                    if (!handler.isOptimizable()) {
                        return false;
                    }
                }
            }
            if (chain.getResponseHandlers() != null && !chain.getResponseHandlers().isEmpty()) {
                for (MessageHandler handler : chain.getResponseHandlers()) {
                    if (!handler.isOptimizable()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
