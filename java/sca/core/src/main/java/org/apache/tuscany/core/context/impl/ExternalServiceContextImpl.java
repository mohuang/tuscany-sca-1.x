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
package org.apache.tuscany.core.context.impl;

import org.apache.tuscany.core.builder.ObjectFactory;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.context.TargetException;
import org.apache.tuscany.core.context.CoreRuntimeException;
import org.apache.tuscany.core.context.ExternalServiceContext;
import org.apache.tuscany.core.wire.ProxyCreationException;
import org.apache.tuscany.core.wire.TargetWireFactory;

/**
 * The default implementation of an external service context
 *
 * @version $Rev$ $Date$
 */
public class ExternalServiceContextImpl extends AbstractContext implements ExternalServiceContext {

    private TargetWireFactory<?> targetWireFactory;

    private ObjectFactory targetInstanceFactory;

    /**
     * Creates an external service context
     *
     * @param name the name of the external service
     * @param targetWireFactory the factory which creates proxies implementing the configured service interface for the
     *        external service. There is always only one proxy factory as an external service is configured with one
     *        service
     * @param targetInstanceFactory the object factory that creates an artifact capabile of communicating over the
     *        binding transport configured on the external service. The object factory may implement a caching strategy.
     */
    public ExternalServiceContextImpl(String name, TargetWireFactory targetWireFactory, ObjectFactory targetInstanceFactory) {
        super(name);
        assert (targetInstanceFactory != null) : "Target instance factory was null";
        this.targetWireFactory = targetWireFactory;
        this.targetInstanceFactory = targetInstanceFactory;
    }

    public void start() throws CoreRuntimeException {
        lifecycleState = RUNNING;
    }

    public void stop() throws CoreRuntimeException {
        lifecycleState = STOPPED;
    }


    public Object getInstance(QualifiedName qName) throws TargetException {
        try {
            return targetWireFactory.createProxy();
            // TODO do we cache the proxy, (assumes stateful capabilities will be provided in an interceptor)
        } catch (ProxyCreationException e) {
            TargetException te = new TargetException(e);
            te.addContextName(getName());
            throw te;
        }
    }

    public Object getHandler() throws TargetException {
        return targetInstanceFactory.getInstance();
    }
}
