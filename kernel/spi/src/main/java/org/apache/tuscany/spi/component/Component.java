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
package org.apache.tuscany.spi.component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.spi.model.ComponentDefinition;
import org.apache.tuscany.spi.model.Implementation;
import org.apache.tuscany.spi.model.Operation;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.OutboundWire;
import org.apache.tuscany.spi.wire.TargetInvoker;

/**
 * The runtime instantiation of an SCA component
 *
 * @version $$Rev$$ $$Date$$
 */
public interface Component extends SCAObject {

    /**
     * Adds a target-side wire. Target-side wire factories contain the invocation chains associated with the destination
     * service of a wire
     */
    void addInboundWire(InboundWire wire);

    /**
     * Returns a map of inbound wires for a service.
     */
    Collection<InboundWire> getInboundWires();

    /**
     * Adds a source-side wire for the given reference. Source-side wires contain the invocation chains for a reference
     * in the implementation associated with the instance wrapper created by this configuration.
     */
    void addOutboundWire(OutboundWire wire);

    /**
     * Adds a set of source-side multiplicity wires for the given reference. Source-side wires contain the invocation
     * chains for a reference in the implementation associated with the instance wrapper created by this configuration.
     */
    void addOutboundWires(Class<?> multiplicityClass, List<OutboundWire> wires);

    /**
     * Returns a map of source-side wires for references. There may be 1..n wires per reference.
     */
    Map<String, List<OutboundWire>> getOutboundWires();

    /**
     * Callback to create a {@link org.apache.tuscany.spi.wire.TargetInvoker} which dispatches to a service offered by
     * the component
     *
     * @param targetName   the service name
     * @param operation    the operation to invoke
     * @param callbackWire the callback wire or null if the associated wire is unidirectional
     * @throws TargetInvokerCreationException
     */
    TargetInvoker createTargetInvoker(String targetName, Operation operation, InboundWire callbackWire)
        throws TargetInvokerCreationException;
    
    /**
     * Gets the component definition associated with the component.
     * @return Definition associated with the component.
     */
    ComponentDefinition<Implementation<?>> getComponentDefinition();
    
    /**
     * Gets the component definition associated with the component.
     * @param definition Definition associated with the component.
     * TODO This shouldn't be mutable, rather set during construction. However,
     * passing it in the constructor of the implementing class has implications 
     * in the usage of various types in the inheritance tree.
     * @deprecated Remove this when immutability is enforced in the implemenation tree.
     */
    void setComponentDefinition(ComponentDefinition<Implementation<?>> definition);

}
