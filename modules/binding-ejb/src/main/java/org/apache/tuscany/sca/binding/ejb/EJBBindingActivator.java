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

package org.apache.tuscany.sca.binding.ejb;

import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentReference;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.spi.BindingActivator;
import org.apache.tuscany.sca.spi.ComponentLifecycle;
import org.apache.tuscany.sca.spi.InvokerFactory;
import org.osoa.sca.ServiceRuntimeException;

public class EJBBindingActivator implements BindingActivator<EJBBinding> {

    public InvokerFactory createInvokerFactory(RuntimeComponent rc, RuntimeComponentReference rcr, final Binding b, final EJBBinding binding) {
        // TODO: assumes a Java interface, need to support tuscany generic Interface
        final Class si = ((JavaInterface)rcr.getInterfaceContract().getInterface()).getJavaClass();
        return new InvokerFactory() {
            public Invoker createInvoker(Operation operation) {
                return new EJBTargetInvoker((EJBBinding)binding, si, operation);
            }
         };
    }

    public ComponentLifecycle createService(RuntimeComponent rc, RuntimeComponentService rcs, Binding b, EJBBinding binding) {
        throw new ServiceRuntimeException("services not yet implemented for binding.ejb");
    }

    public Class<EJBBinding> getBindingClass() {
        return EJBBinding.class;
    }
}
