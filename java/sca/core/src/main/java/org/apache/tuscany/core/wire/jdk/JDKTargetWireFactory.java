/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.tuscany.core.wire.jdk;

import org.apache.tuscany.core.wire.MethodHashMap;
import org.apache.tuscany.core.wire.WireFactoryInitException;
import org.apache.tuscany.core.wire.TargetInvocationConfiguration;
import org.apache.tuscany.core.wire.TargetWireFactory;
import org.apache.tuscany.core.wire.WireTargetConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Creates proxies that are returned to non-SCA clients using JDK dynamic proxy facilities and front a wire. The proxies implement
 * the business interface associated with the target service of the wire and are typically returned by a locate operation.
 *
 * @version $Rev: 394431 $ $Date: 2006-04-15 21:27:44 -0700 (Sat, 15 Apr 2006) $
 */
public class JDKTargetWireFactory implements TargetWireFactory {

    private static final int UNINITIALIZED = 0;

    private static final int INITIALIZED = 1;

    private int state = UNINITIALIZED;

    private Class[] businessInterfaceArray;

    private Map<Method, TargetInvocationConfiguration> methodToInvocationConfig;

    private WireTargetConfiguration configuration;

    public void initialize() throws WireFactoryInitException {
        if (state != UNINITIALIZED) {
            throw new IllegalStateException("Proxy factory in wrong state [" + state + "]");
        }
        Map<Method, TargetInvocationConfiguration> invocationConfigs = configuration.getInvocationConfigurations();
        methodToInvocationConfig = new MethodHashMap<TargetInvocationConfiguration>(invocationConfigs.size());
        for (Map.Entry<Method, TargetInvocationConfiguration> entry : invocationConfigs.entrySet()) {
            Method method = entry.getKey();
            methodToInvocationConfig.put(method, entry.getValue());
        }
        state = INITIALIZED;
    }

    public Object createProxy() {
        if (state != INITIALIZED) {
            throw new IllegalStateException("Proxy factory not INITIALIZED [" + state + "]");
        }
        InvocationHandler handler = new JDKInvocationHandler(configuration.getMessageFactory(), methodToInvocationConfig);
        return Proxy.newProxyInstance(configuration.getProxyClassLoader(), businessInterfaceArray, handler);
    }

    public WireTargetConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(WireTargetConfiguration config) {
        configuration = config;
    }

    public void setBusinessInterface(Class interfaze) {
        businessInterfaceArray = new Class[]{interfaze};
    }

    public Class getBusinessInterface() {
        return businessInterfaceArray[0];
    }

    public void addInterface(Class claz) {
        throw new UnsupportedOperationException("Additional proxy interfaces not yet supported");
    }

    public Class[] getImplementatedInterfaces() {
        return businessInterfaceArray;
    }

}
