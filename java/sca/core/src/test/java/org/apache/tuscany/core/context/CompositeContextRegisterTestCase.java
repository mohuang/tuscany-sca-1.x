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
package org.apache.tuscany.core.context;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.core.context.impl.EventContextImpl;
import org.apache.tuscany.core.context.scope.DefaultScopeStrategy;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.context.event.ModuleStop;
import org.apache.tuscany.core.mock.MockConfigContext;
import org.apache.tuscany.core.mock.MockFactory;
import org.apache.tuscany.core.mock.component.GenericSystemComponent;
import org.apache.tuscany.core.mock.component.ModuleScopeSystemComponent;
import org.apache.tuscany.core.mock.component.ModuleScopeSystemComponentImpl;
import org.apache.tuscany.core.system.assembly.SystemAssemblyFactory;
import org.apache.tuscany.core.system.assembly.impl.SystemAssemblyFactoryImpl;
import org.apache.tuscany.model.assembly.Component;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.Scope;

import java.util.List;

/**
 * Tests registration of model objects for an composite context
 * 
 * @version $Rev$ $Date$
 */
public class CompositeContextRegisterTestCase extends TestCase {
    private SystemAssemblyFactory factory;

    public void testModuleRegistration() throws Exception {
        CompositeContext moduleContext = createContext();
        Module module = MockFactory.createSystemModule();
        moduleContext.registerModelObject(module);
        moduleContext.start();
        moduleContext.publish(new ModuleStart(this));
        GenericSystemComponent component = (GenericSystemComponent) moduleContext.getContext("TestService1").getInstance(null);
        Assert.assertNotNull(component);
        GenericSystemComponent ep = (GenericSystemComponent) moduleContext.getContext("TestService1EP").getInstance(null);
        Assert.assertNotNull(ep);
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
    }

    public void testModuleRegistrationAfterStart() throws Exception {
        CompositeContext moduleContext = createContext();
        moduleContext.start();
        Module module = MockFactory.createSystemModule();
        moduleContext.registerModelObject(module);
        moduleContext.publish(new ModuleStart(this));
        GenericSystemComponent component = (GenericSystemComponent) moduleContext.getContext("TestService1").getInstance(null);
        Assert.assertNotNull(component);
        GenericSystemComponent ep = (GenericSystemComponent) moduleContext.getContext("TestService1EP").getInstance(null);
        Assert.assertNotNull(ep);
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
    }

    public void testRegistration() throws Exception {
        CompositeContext moduleContext = createContext();
        Component component = factory.createSystemComponent("TestService1", ModuleScopeSystemComponent.class, ModuleScopeSystemComponentImpl.class, Scope.MODULE);
        moduleContext.registerModelObject(component);
        EntryPoint ep = MockFactory.createEPSystemBinding("TestService1EP", ModuleScopeSystemComponent.class, "TestService1", component);
        moduleContext.registerModelObject(ep);
        moduleContext.start();
        moduleContext.publish(new ModuleStart(this));
        GenericSystemComponent test = (GenericSystemComponent) moduleContext.getContext("TestService1").getInstance(null);
        Assert.assertNotNull(test);
        GenericSystemComponent testEP = (GenericSystemComponent) moduleContext.getContext("TestService1EP").getInstance(null);
        Assert.assertNotNull(testEP);
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
    }

    public void testRegistrationAfterStart() throws Exception {
        CompositeContext moduleContext = createContext();
        Component component = factory.createSystemComponent("TestService1", ModuleScopeSystemComponent.class, ModuleScopeSystemComponentImpl.class, Scope.MODULE);
        moduleContext.start();
        moduleContext.registerModelObject(component);
        EntryPoint ep = MockFactory.createEPSystemBinding("TestService1EP", ModuleScopeSystemComponent.class, "TestService1", component);
        moduleContext.registerModelObject(ep);
        moduleContext.publish(new ModuleStart(this));
        GenericSystemComponent test = (GenericSystemComponent) moduleContext.getContext("TestService1").getInstance(null);
        Assert.assertNotNull(test);
        GenericSystemComponent testEP = (GenericSystemComponent) moduleContext.getContext("TestService1EP").getInstance(null);
        Assert.assertNotNull(testEP);
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
    }

    public void testEPRegistrationAfterModuleStart() throws Exception {
        CompositeContext moduleContext = createContext();
        Component component = factory.createSystemComponent("TestService1", ModuleScopeSystemComponent.class, ModuleScopeSystemComponentImpl.class, Scope.MODULE);
        moduleContext.start();
        moduleContext.registerModelObject(component);
        moduleContext.publish(new ModuleStart(this));
        GenericSystemComponent test = (GenericSystemComponent) moduleContext.getContext("TestService1").getInstance(null);
        Assert.assertNotNull(test);
        EntryPoint ep = MockFactory.createEPSystemBinding("TestService1EP", ModuleScopeSystemComponent.class, "TestService1", component);
        moduleContext.registerModelObject(ep);
        GenericSystemComponent testEP = (GenericSystemComponent) moduleContext.getContext("TestService1EP").getInstance(null);
        Assert.assertNotNull(testEP);
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
    }

    protected CompositeContext createContext() {
        List<ContextFactoryBuilder> builders = MockFactory.createSystemBuilders();
        return new CompositeContextImpl("test.context", null, new DefaultScopeStrategy(), new EventContextImpl(),
                new MockConfigContext(builders));
    }

    protected void setUp() throws Exception {
        factory = new SystemAssemblyFactoryImpl();
        super.setUp();
    }
}
