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
package org.apache.tuscany.binding.axis2;

import java.util.LinkedList;
import java.util.Map;

import org.apache.tuscany.spi.model.Operation;
import org.apache.tuscany.spi.wire.AbstractOutboundInvocationHandler;
import org.apache.tuscany.spi.wire.InboundWire;
import org.apache.tuscany.spi.wire.OutboundInvocationChain;
import org.apache.tuscany.spi.wire.TargetInvoker;

public class Axis2CallbackInvocationHandler extends AbstractOutboundInvocationHandler {

    private InboundWire inboundWire;

    public Axis2CallbackInvocationHandler(InboundWire inboundWire) {
        this.inboundWire = inboundWire;
    }

    public Object invoke(Operation operation, Object[] args, LinkedList<Object> callbackRoutingChain) throws Throwable {
        Object targetAddress = callbackRoutingChain.removeFirst();
        if (targetAddress == null) {
            throw new AssertionError("Popped a null from address from stack");
        }
        //TODO optimize as this is slow in local invocations
        Map<Operation<?>, OutboundInvocationChain> sourceCallbackInvocationChains =
            inboundWire.getSourceCallbackInvocationChains(targetAddress);
        OutboundInvocationChain chain = sourceCallbackInvocationChains.get(operation);
        TargetInvoker invoker = chain.getTargetInvoker();
        return invoke(chain, invoker, args, null, callbackRoutingChain);
    }
}
