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
package org.apache.tuscany.container.rhino.context;

import org.apache.tuscany.container.rhino.rhino.RhinoScript;
import org.apache.tuscany.core.context.AtomicContext;
import org.apache.tuscany.core.context.CoreRuntimeException;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.context.TargetException;
import org.apache.tuscany.core.context.event.InstanceCreated;
import org.apache.tuscany.core.context.impl.AbstractContext;
import org.apache.tuscany.core.wire.ProxyCreationException;
import org.apache.tuscany.core.wire.WireFactory;
import org.apache.tuscany.core.wire.SourceWireFactory;
import org.apache.tuscany.core.wire.TargetWireFactory;
import org.osoa.sca.ServiceRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaScriptComponentContext extends AbstractContext implements AtomicContext {

    private Map<String, Class> services;

    private RhinoScript rhinoInvoker;

    private Map<String, Object> properties;

    private List<SourceWireFactory> sourceProxyFactories;

    private Map<String, TargetWireFactory> targetProxyFactories;

    private Object instance;

    public JavaScriptComponentContext(String name, Map<String, Class> services, Map<String, Object> properties,
                                      List<SourceWireFactory> sourceProxyFactories, Map<String, TargetWireFactory> targetProxyFactories, RhinoScript invoker) {
        super(name);
        assert (services != null) : "No service interface mapping specified";
        assert (properties != null) : "No properties specified";
        this.services = services;
        this.properties = properties;
        this.rhinoInvoker = invoker;
        this.sourceProxyFactories = sourceProxyFactories;
        this.targetProxyFactories = targetProxyFactories;
    }

    public Object getInstance(QualifiedName qName) throws TargetException {
        return getInstance(qName, true);
    }

    public void init() throws TargetException {
        getInstance(null, false);
    }

    public void destroy() throws TargetException {

    }

    private synchronized Object getInstance(QualifiedName qName, boolean notify) throws TargetException {
        String portName = qName.getPortName();
        WireFactory targetFactory;
        if (portName != null) {
            targetFactory = targetProxyFactories.get(portName);
        } else {
            //FIXME The port name is null here, either locateService needs more information (the expected interface) to
            // select the correct port, or we need to return a factory that matches the whole set of services exposed by
            // the component.
            targetFactory = targetProxyFactories.values().iterator().next();
        }
        if (targetFactory == null) {
            TargetException e = new TargetException("Target service not found");
            e.setIdentifier(qName.getPortName());
            e.addContextName(getName());
            throw e;
        }
        try {
            Object proxy = targetFactory.createProxy(); //createProxy(new Class[] { iface });
            if (notify) {
                publish(new InstanceCreated(this));
            }
            return proxy;
        } catch (ProxyCreationException e) {
            TargetException te = new TargetException("Error returning target", e);
            e.setIdentifier(qName.getPortName());
            e.addContextName(getName());
            throw te;
        }
    }

    public Object getTargetInstance() throws TargetException {
        rhinoInvoker.updateScriptScope(createServiceReferences()); // create references
        rhinoInvoker.updateScriptScope(properties); // create prop values
        return rhinoInvoker;
    }

    /**
     * Creates a map containing any ServiceReferences
     */
    private Map createServiceReferences() {
        try {
            Map<String, Object> context = new HashMap<String, Object>();
            for (SourceWireFactory proxyFactory : sourceProxyFactories) {
                context.put(proxyFactory.getConfiguration().getReferenceName(), proxyFactory.createProxy());
            }
            return context;
        } catch (ProxyCreationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public boolean isEagerInit() {
        return false;
    }

    public boolean isDestroyable() {
        return false;
    }

    public void start() throws CoreRuntimeException {
    }

    public void stop() throws CoreRuntimeException {
    }

}
