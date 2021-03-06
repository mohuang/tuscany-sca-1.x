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
package org.apache.tuscany.core.builder.impl;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.wire.MethodHashMap;
import org.apache.tuscany.core.wire.WireSourceConfiguration;
import org.apache.tuscany.core.wire.WireTargetConfiguration;
import org.apache.tuscany.core.wire.SourceInvocationConfiguration;
import org.apache.tuscany.core.wire.TargetInvocationConfiguration;
import org.apache.tuscany.core.wire.SourceWireFactory;
import org.apache.tuscany.core.wire.WireFactoryFactory;
import org.apache.tuscany.core.wire.TargetWireFactory;
import org.apache.tuscany.core.wire.mock.SimpleTarget;
import org.apache.tuscany.core.wire.mock.MockHandler;
import org.apache.tuscany.core.wire.mock.MockSyncInterceptor;
import org.apache.tuscany.core.wire.mock.MockStaticInvoker;
import org.apache.tuscany.core.wire.mock.SimpleTargetImpl;
import org.apache.tuscany.core.wire.impl.InvokerInterceptor;
import org.apache.tuscany.core.wire.jdk.JDKWireFactoryFactory;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.core.message.impl.MessageFactoryImpl;

import java.lang.reflect.Method;
import java.util.Map;

public class DefaultWireBuilderTestCase extends TestCase {

    private Method hello;

    private WireFactoryFactory wireFactoryFactory = new JDKWireFactoryFactory();

    public DefaultWireBuilderTestCase() {
        super();
    }

    public DefaultWireBuilderTestCase(String arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        hello = SimpleTarget.class.getMethod("hello", String.class);
    }

    public void testWireWithInterceptorsAndHandlers() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        MockHandler sourceRequestHandler = new MockHandler();
        MockHandler sourceResponseHandler = new MockHandler();
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addRequestHandler(sourceRequestHandler);
        source.addResponseHandler(sourceResponseHandler);
        source.addInterceptor(sourceInterceptor);

        SourceWireFactory sourceFactory = wireFactoryFactory.createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockHandler targetRequestHandler = new MockHandler();
        MockHandler targetResponseHandler = new MockHandler();
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addRequestHandler(targetRequestHandler);
        target.addResponseHandler(targetResponseHandler);
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, sourceRequestHandler.getCount());
        Assert.assertEquals(1, sourceResponseHandler.getCount());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetRequestHandler.getCount());
        Assert.assertEquals(1, targetResponseHandler.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithSourceInterceptorTargetHandlersAndTargetInterceptor() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addInterceptor(sourceInterceptor);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockHandler targetRequestHandler = new MockHandler();
        MockHandler targetResponseHandler = new MockHandler();
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addRequestHandler(targetRequestHandler);
        target.addResponseHandler(targetResponseHandler);
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetRequestHandler.getCount());
        Assert.assertEquals(1, targetResponseHandler.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithInterceptorsAndRequestHandlers() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        MockHandler sourceRequestHandler = new MockHandler();
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addRequestHandler(sourceRequestHandler);
        source.addInterceptor(sourceInterceptor);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockHandler targetRequestHandler = new MockHandler();
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addRequestHandler(targetRequestHandler);
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, sourceRequestHandler.getCount());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetRequestHandler.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithSourceAndTargetInterceptors() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addInterceptor(sourceInterceptor);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithSourceInterceptorSourceHandlersAndTargetInterceptor() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        MockHandler sourceRequestHandler = new MockHandler();
        MockHandler sourceResponseHandler = new MockHandler();
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addRequestHandler(sourceRequestHandler);
        source.addResponseHandler(sourceResponseHandler);
        source.addInterceptor(sourceInterceptor);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, sourceRequestHandler.getCount());
        Assert.assertEquals(1, sourceResponseHandler.getCount());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithTargetInterceptorAndTargetHandlers() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockHandler targetRequestHandler = new MockHandler();
        MockHandler targetResponseHandler = new MockHandler();
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addRequestHandler(targetRequestHandler);
        target.addResponseHandler(targetResponseHandler);
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        // source.buildSource();
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, targetRequestHandler.getCount());
        Assert.assertEquals(1, targetResponseHandler.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    public void testWireWithTargetInterceptor() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addInterceptor(targetInterceptor);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }

    /**
     * When there are only {@link InvokerInterceptor}s in the source and target chain, we need to bypass one during
     * wire up so they are not chained together
     */
    public void testWireWithOnlyInvokerInterceptors() throws Exception {
        MessageFactory msgFactory = new MessageFactoryImpl();

        SourceInvocationConfiguration source = new SourceInvocationConfiguration(hello);
        source.setTargetInterceptor(new InvokerInterceptor());

        SourceWireFactory sourceFactory = new JDKWireFactoryFactory().createSourceWireFactory();
        Map<Method, SourceInvocationConfiguration> sourceInvocationConfigs = new MethodHashMap<SourceInvocationConfiguration>();
        sourceInvocationConfigs.put(hello, source);
        WireSourceConfiguration sourceConfig = new WireSourceConfiguration("foo",new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        sourceFactory.setConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);

        TargetInvocationConfiguration target = new TargetInvocationConfiguration(hello);
        target.addInterceptor(new InvokerInterceptor());

        TargetWireFactory targetFactory = wireFactoryFactory.createTargetWireFactory();
        Map<Method, TargetInvocationConfiguration> targetInvocationConfigs = new MethodHashMap<TargetInvocationConfiguration>();
        targetInvocationConfigs.put(hello, target);
        WireTargetConfiguration targetConfig = new WireTargetConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), msgFactory);
        targetFactory.setConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        // no need for scopes since we use a static invoker
        builder.connect(sourceFactory, targetFactory, null, true, null);
        target.build();
        // set a static invoker
        MockStaticInvoker invoker = new MockStaticInvoker(hello, new SimpleTargetImpl());
        source.setTargetInvoker(invoker);

        Message msg = msgFactory.createMessage();
        msg.setBody("foo");
        msg.setTargetInvoker(invoker);
        Message response = source.getHeadInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getBody());
    }

}
