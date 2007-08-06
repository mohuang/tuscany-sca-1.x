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

package org.apache.tuscany.sca.interfacedef.java.module;

import org.apache.tuscany.sca.contribution.ModelFactoryExtensionPoint;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.ModuleActivator;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.introspect.DefaultJavaInterfaceIntrospectorExtensionPoint;
import org.apache.tuscany.sca.interfacedef.java.introspect.JavaInterfaceIntrospectorExtensionPoint;

/**
 * @version $Rev$ $Date$
 */
public class JavaInterfaceRuntimeModuleActivator implements ModuleActivator {
    
    private JavaInterfaceFactory javaFactory;
    private JavaInterfaceIntrospectorExtensionPoint visitors;
    
    public JavaInterfaceRuntimeModuleActivator() {
        visitors = new DefaultJavaInterfaceIntrospectorExtensionPoint();
        javaFactory = new DefaultJavaInterfaceFactory(visitors);
    }

    public Object[] getExtensionPoints() {
        return new Object[]{ visitors };
    }

    public void start(ExtensionPointRegistry registry) {
        
        // Register the Java interface factory
        ModelFactoryExtensionPoint modelFactories = registry.getExtensionPoint(ModelFactoryExtensionPoint.class); 
        modelFactories.addFactory(javaFactory);
    }

    public void stop(ExtensionPointRegistry registry) {
    }

}
