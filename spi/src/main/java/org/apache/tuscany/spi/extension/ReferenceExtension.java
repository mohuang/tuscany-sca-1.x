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
package org.apache.tuscany.spi.extension;

import java.lang.reflect.Method;

import org.apache.tuscany.spi.component.AbstractSCAObject;
import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.component.Reference;
import org.apache.tuscany.spi.component.TargetException;
import org.apache.tuscany.spi.model.Scope;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.OutboundWire;
import org.apache.tuscany.spi.wire.TargetInvoker;
import org.apache.tuscany.spi.wire.WireInvocationHandler;
import org.apache.tuscany.spi.wire.WireService;

/**
 * The default implementation of an SCA reference
 *
 * @version $Rev$ $Date$
 */
public abstract class ReferenceExtension<T> extends AbstractSCAObject<T> implements Reference<T> {

    protected InboundWire<T> inboundWire;
    protected OutboundWire<T> outboundWire;
    protected Class<T> referenceInterface;
    protected WireService wireService;

    protected ReferenceExtension(String name, 
                                 Class<T> referenceInterface,
                                 CompositeComponent<?> parent,
                                 WireService wireService) {
        super(name, parent);
        this.referenceInterface = referenceInterface;
        this.wireService = wireService;
    }

    public Scope getScope() {
        return Scope.COMPOSITE;
    }

    public void setInboundWire(InboundWire<T> wire) {
        this.inboundWire = wire;
    }

    public InboundWire<T> getInboundWire() {
        return inboundWire;
    }

    public OutboundWire<T> getOutboundWire() {
        return outboundWire;
    }

    public void setOutboundWire(OutboundWire<T> outboundWire) {
        this.outboundWire = outboundWire;
    }

    public Class<T> getInterface() {
        return referenceInterface;
    }

    public void setInterface(Class<T> referenceInterface) {
        this.referenceInterface = referenceInterface;
    }

    public T getServiceInstance() throws TargetException {
        return wireService.createProxy(inboundWire);
    }

    public WireInvocationHandler getHandler() throws TargetException {
        return wireService.createHandler(inboundWire);
    }

    public TargetInvoker createAsyncTargetInvoker(Method operation, OutboundWire wire) {
        throw new UnsupportedOperationException();
    }

}
