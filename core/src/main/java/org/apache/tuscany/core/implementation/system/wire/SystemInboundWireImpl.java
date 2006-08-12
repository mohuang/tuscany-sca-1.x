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
package org.apache.tuscany.core.implementation.system.wire;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.apache.tuscany.spi.component.Component;
import org.apache.tuscany.spi.component.TargetException;
import org.apache.tuscany.spi.wire.InboundInvocationChain;
import org.apache.tuscany.spi.wire.OutboundWire;

/**
 * Default implementation of an inbound system wire
 *
 * @version $$Rev$$ $$Date$$
 */
public class SystemInboundWireImpl<T> implements SystemInboundWire<T> {
    private String serviceName;
    private Class<T> businessInterface;
    private Component<?> component;
    private SystemOutboundWire<T> wire;

    /**
     * Constructs a new inbound wire
     *
     * @param serviceName       the name of the service the inbound wire represents
     * @param businessInterface the service interface
     * @param target            the target context the wire is connected to
     */
    public SystemInboundWireImpl(String serviceName, Class<T> businessInterface, Component<?> target) {
        this.serviceName = serviceName;
        this.businessInterface = businessInterface;
        this.component = target;
    }

    public SystemInboundWireImpl(String serviceName, Class<T> businessInterface) {
        this.serviceName = serviceName;
        this.businessInterface = businessInterface;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public T getTargetService() throws TargetException {
        if (wire != null) {
            return wire.getTargetService();
        }
        return (T) component.getServiceInstance(serviceName);
    }

    public Class<T> getBusinessInterface() {
        return businessInterface;
    }

    public void setBusinessInterface(Class<T> businessInterface) {
        this.businessInterface = businessInterface;
    }

    public Class[] getImplementedInterfaces() {
        return new Class[0];
    }

    public Map<Method, InboundInvocationChain> getInvocationChains() {
        return Collections.emptyMap();
    }

    public void addInvocationChain(Method method, InboundInvocationChain chain) {
        throw new UnsupportedOperationException();
    }

    public void addInvocationChains(Map chains) {
        throw new UnsupportedOperationException();
    }

    public void addInterface(Class claz) {
        throw new UnsupportedOperationException();
    }

    public String getCallbackReferenceName() {
        return null;
    }

    public void setCallbackReferenceName(String callbackReferenceName) {
        throw new UnsupportedOperationException();
    }

    public boolean isOptimizable() {
        return true;  // system wires are always optimizable
    }

    public void setTargetWire(OutboundWire<T> wire) {
        assert wire instanceof SystemOutboundWire : "wire must be a " + SystemOutboundWireImpl.class.getName();
        this.wire = (SystemOutboundWire<T>) wire;
    }

}
