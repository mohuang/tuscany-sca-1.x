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
package org.apache.tuscany.container.java.scopes;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.tuscany.container.java.builder.JavaContextFactoryBuilder;
import org.apache.tuscany.container.java.mock.MockFactory;
import org.apache.tuscany.container.java.mock.components.SessionScopeDestroyOnlyComponent;
import org.apache.tuscany.container.java.mock.components.SessionScopeInitDestroyComponent;
import org.apache.tuscany.container.java.mock.components.SessionScopeInitOnlyComponent;
import org.apache.tuscany.core.builder.BuilderException;
import org.apache.tuscany.core.builder.ContextFactory;
import org.apache.tuscany.core.builder.system.DefaultPolicyBuilderRegistry;
import org.apache.tuscany.core.context.EventContext;
import org.apache.tuscany.core.context.Context;
import org.apache.tuscany.core.context.event.HttpSessionEnd;
import org.apache.tuscany.core.context.event.HttpSessionEvent;
import org.apache.tuscany.core.context.impl.EventContextImpl;
import org.apache.tuscany.core.context.scope.SessionScopeContext;
import org.apache.tuscany.core.wire.service.WireFactoryService;
import org.apache.tuscany.core.wire.service.DefaultWireFactoryService;
import org.apache.tuscany.core.wire.jdk.JDKWireFactoryFactory;
import org.apache.tuscany.core.message.impl.MessageFactoryImpl;
import org.apache.tuscany.core.config.ComponentTypeIntrospector;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.model.assembly.Scope;
import org.apache.tuscany.model.assembly.AtomicComponent;
import org.apache.tuscany.model.assembly.ComponentType;

/**
 * Lifecycle unit tests for the Http session scope container
 * 
 * @version $Rev$ $Date$
 */
public class SessionScopeLifecycleTestCase extends TestCase {

    /**
     * Tests instance identity is properly maintained
     */
    public void testInitDestroy() throws Exception {
        EventContext ctx = new EventContextImpl();
        SessionScopeContext scope = new SessionScopeContext(ctx);
        scope.registerFactories(createComponents());
        scope.start();
        Object session = new Object();
        // first request, no need to notify scope container since sessions are
        // evaluated lazily
        ctx.setIdentifier(HttpSessionEvent.HTTP_IDENTIFIER,session);
        SessionScopeInitDestroyComponent initDestroy = (SessionScopeInitDestroyComponent) scope.getContext(
                "TestServiceInitDestroy").getInstance(null);
        Assert.assertNotNull(initDestroy);
        SessionScopeInitOnlyComponent initOnly = (SessionScopeInitOnlyComponent) scope.getContext("TestServiceInitOnly")
                .getInstance(null);
        Assert.assertNotNull(initOnly);
        SessionScopeDestroyOnlyComponent destroyOnly = (SessionScopeDestroyOnlyComponent) scope.getContext(
                "TestServiceDestroyOnly").getInstance(null);
        Assert.assertNotNull(destroyOnly);

        Assert.assertTrue(initDestroy.isInitialized());
        Assert.assertTrue(initOnly.isInitialized());
        Assert.assertFalse(initDestroy.isDestroyed());
        Assert.assertFalse(destroyOnly.isDestroyed());
        // end request
        ctx.clearIdentifier(HttpSessionEvent.HTTP_IDENTIFIER);
        // expire session
        scope.onEvent(new HttpSessionEnd(this,session));
        Assert.assertTrue(initDestroy.isDestroyed());
        Assert.assertTrue(destroyOnly.isDestroyed());

        scope.stop();
    }

    /**
     * Test instances destroyed in proper (i.e. reverse) order
     */
    public void testDestroyOrder() throws Exception {
        EventContext ctx = new EventContextImpl();
        SessionScopeContext scope = new SessionScopeContext(ctx);
        scope.registerFactories(createOrderedInitComponents());
        scope.start();
        Object session = new Object();
        // request start
        ctx.setIdentifier(HttpSessionEvent.HTTP_IDENTIFIER,session);

        SessionScopedOrderedInitPojo one = (SessionScopedOrderedInitPojo) scope.getContext("one").getInstance(null);
        Assert.assertNotNull(one);
        Assert.assertEquals(1, one.getNumberInstantiated());
        Assert.assertEquals(1, one.getInitOrder());

        SessionScopedOrderedInitPojo two = (SessionScopedOrderedInitPojo) scope.getContext("two").getInstance(null);
        Assert.assertNotNull(two);
        Assert.assertEquals(2, two.getNumberInstantiated());
        Assert.assertEquals(2, two.getInitOrder());

        SessionScopedOrderedInitPojo three = (SessionScopedOrderedInitPojo) scope.getContext("three").getInstance(null);
        Assert.assertNotNull(three);
        Assert.assertEquals(3, three.getNumberInstantiated());
        Assert.assertEquals(3, three.getInitOrder());

        // end request
        ctx.clearIdentifier(HttpSessionEvent.HTTP_IDENTIFIER);

        // expire session
        scope.onEvent(new HttpSessionEnd(this, session));
        Assert.assertEquals(0, one.getNumberInstantiated());
        scope.stop();
    }

    JavaContextFactoryBuilder builder;

    private List<ContextFactory<Context>> createComponents() throws BuilderException, ConfigurationLoadException {
        AtomicComponent[] ca = new AtomicComponent[3];
        ca[0] = MockFactory.createComponent("TestServiceInitDestroy", SessionScopeInitDestroyComponent.class,
                Scope.SESSION);
        ca[1] = MockFactory.createComponent("TestServiceInitOnly", SessionScopeInitOnlyComponent.class, Scope.SESSION);
        ca[2] = MockFactory.createComponent("TestServiceDestroyOnly", SessionScopeDestroyOnlyComponent.class,
                Scope.SESSION);
        ComponentTypeIntrospector introspector = MockFactory.getIntrospector();
        ca[0].getImplementation().setComponentType(introspector.introspect(SessionScopeInitDestroyComponent.class));
        ca[1].getImplementation().setComponentType(introspector.introspect(SessionScopeInitOnlyComponent.class));
        ca[2].getImplementation().setComponentType(introspector.introspect(SessionScopeDestroyOnlyComponent.class));
        List<ContextFactory<Context>> configs = new ArrayList<ContextFactory<Context>>();
        for (AtomicComponent aCa : ca) {
            builder.build(aCa);
            configs.add((ContextFactory<Context>) aCa.getContextFactory());

        }
        return configs;
    }

    private List<ContextFactory<Context>> createOrderedInitComponents() throws
            BuilderException, ConfigurationLoadException {
        AtomicComponent[] ca = new AtomicComponent[3];
        ca[0] = MockFactory.createComponent("one", SessionScopedOrderedInitPojo.class, Scope.SESSION);
        ca[1] = MockFactory.createComponent("two", SessionScopedOrderedInitPojo.class, Scope.SESSION);
        ca[2] = MockFactory.createComponent("three", SessionScopedOrderedInitPojo.class, Scope.SESSION);
        ComponentTypeIntrospector introspector = MockFactory.getIntrospector();
        ComponentType type = introspector.introspect(SessionScopedOrderedInitPojo.class);
        ca[0].getImplementation().setComponentType(type);
        ca[1].getImplementation().setComponentType(type);
        ca[2].getImplementation().setComponentType(type);
        List<ContextFactory<Context>> configs = new ArrayList<ContextFactory<Context>>();
        for (AtomicComponent aCa : ca) {
            builder.build(aCa);
            configs.add((ContextFactory<Context>) aCa.getContextFactory());

        }
        return configs;
    }

    protected void setUp() throws Exception {
        super.setUp();
        WireFactoryService wireService = new DefaultWireFactoryService(new MessageFactoryImpl(), new JDKWireFactoryFactory(),new DefaultPolicyBuilderRegistry());
        builder = new JavaContextFactoryBuilder(wireService);
    }

}
