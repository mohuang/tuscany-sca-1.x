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
package org.apache.tuscany.core.system.builder;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.tuscany.core.builder.ContextFactory;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.Context;
import org.apache.tuscany.core.context.AtomicContext;
import org.apache.tuscany.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.core.context.impl.EventContextImpl;
import org.apache.tuscany.core.context.scope.DefaultScopeStrategy;
import org.apache.tuscany.core.mock.MockConfigContext;
import org.apache.tuscany.core.mock.MockFactory;
import org.apache.tuscany.core.system.assembly.SystemAssemblyFactory;
import org.apache.tuscany.core.system.assembly.SystemImplementation;
import org.apache.tuscany.core.system.assembly.impl.SystemAssemblyFactoryImpl;
import org.apache.tuscany.model.assembly.Component;
import org.apache.tuscany.model.assembly.ConfiguredProperty;
import org.apache.tuscany.model.assembly.Property;
import org.apache.tuscany.model.assembly.Scope;
import org.apache.tuscany.model.assembly.Service;
import org.apache.tuscany.model.assembly.ComponentType;
import org.apache.tuscany.model.types.java.JavaServiceContract;

/**
 * Tests to system components are built properly
 * 
 * @version $Rev$ $Date$
 */
public class SystemContextFactoryBuilderTestCase extends TestCase {

    private SystemAssemblyFactory factory = new SystemAssemblyFactoryImpl();

    public void testComponentContextBuilder() throws Exception {
        SystemContextFactoryBuilder builder = new SystemContextFactoryBuilder(null);
        Component component = factory.createSystemComponent("test", null, SystemComponentImpl.class, Scope.AGGREGATE);
        component.getImplementation().setComponentType(MockFactory.getIntrospector().introspect(SystemComponentImpl.class));
        ConfiguredProperty cProp = factory.createConfiguredProperty();
        Property prop = factory.createProperty();
        prop.setName("testInt");
        cProp.setValue(1);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testString");
        cProp.setValue("test");
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testDouble");
        cProp.setValue(1d);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testFloat");
        cProp.setValue(1f);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testShort");
        cProp.setValue((short) 1);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testByte");
        cProp.setValue((byte) 1);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testBoolean");
        cProp.setValue(Boolean.TRUE);
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        cProp = factory.createConfiguredProperty();
        prop = factory.createProperty();
        prop.setName("testChar");
        cProp.setValue('1');
        cProp.setProperty(prop);
        component.getConfiguredProperties().add(cProp);

        builder.build(component);
        ContextFactory<AtomicContext> contextFactory = (ContextFactory<AtomicContext>) component.getContextFactory();
        Assert.assertNotNull(contextFactory);
        contextFactory.prepare(createContext());
        AtomicContext ctx = contextFactory.createContext();

        ctx.start();
        SystemComponentImpl instance = (SystemComponentImpl) ctx.getInstance(null);
        Assert.assertNotNull(instance.getConfigContext());
        Assert.assertNotNull(instance.getParentContext());
        Assert.assertNotNull(instance.getAutowireContext());
        Assert.assertNotNull(instance.getConfigContextSetter());
        Assert.assertNotNull(instance.getParentContextSetter());
        Assert.assertNotNull(instance.getAutowireContextSetter());
        Assert.assertEquals(1, instance.getTestInt());
        Assert.assertEquals(1d, instance.getTestDouble());
        Assert.assertEquals(1f, instance.getTestFloat());
        Assert.assertEquals((short) 1, instance.getTestShort());
        Assert.assertTrue(instance.getTestBoolean());
        Assert.assertEquals('1', instance.getTestChar());
        Assert.assertEquals((byte) 1, instance.getTestByte());
        Assert.assertEquals("test", instance.getTestString());

        Assert.assertTrue(instance.initialized());
        ctx.destroy();
        ctx.stop();
        Assert.assertTrue(instance.destroyed());
    }


    public void testDefaultScopeIsModuleScope() throws Exception {
        SystemContextFactoryBuilder builder = new SystemContextFactoryBuilder(null);
        Component component = createSystemComponentWithNoScope("test", null, SystemComponentImpl.class);
        builder.build(component);
        ContextFactory<AtomicContext> contextFactory = (ContextFactory<AtomicContext>) component.getContextFactory();
        Assert.assertEquals(Scope.MODULE, contextFactory.getScope());
    }




    private static CompositeContext createContext() {
        return new CompositeContextImpl("test.parent", null, new DefaultScopeStrategy(), new EventContextImpl(),
                new MockConfigContext(null));
    }

    private <T> Component createSystemComponentWithNoScope(String name, Class<T> service, Class<? extends T> impl) {
        JavaServiceContract jsc = factory.createJavaServiceContract();
        jsc.setInterface(service);
        Service s = factory.createService();
        s.setServiceContract(jsc);

        ComponentType componentType = factory.createComponentType();
        componentType.getServices().add(s);

        SystemImplementation sysImpl = factory.createSystemImplementation();
        sysImpl.setImplementationClass(impl);
        sysImpl.setComponentType(componentType);

        Component sc = factory.createSimpleComponent();
        sc.setName(name);
        sc.setImplementation(sysImpl);
        return sc;
    }


}
