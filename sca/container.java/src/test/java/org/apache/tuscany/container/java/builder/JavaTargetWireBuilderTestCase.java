package org.apache.tuscany.container.java.builder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.tuscany.container.java.config.JavaComponentRuntimeConfiguration;
import org.apache.tuscany.container.java.invocation.mock.MockHandler;
import org.apache.tuscany.container.java.invocation.mock.MockJavaOperationType;
import org.apache.tuscany.container.java.invocation.mock.MockScopeContext;
import org.apache.tuscany.container.java.invocation.mock.MockSyncInterceptor;
import org.apache.tuscany.container.java.invocation.mock.SimpleTarget;
import org.apache.tuscany.container.java.invocation.mock.SimpleTargetImpl;
import org.apache.tuscany.core.builder.impl.DefaultWireBuilder;
import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.invocation.InvocationConfiguration;
import org.apache.tuscany.core.invocation.ProxyConfiguration;
import org.apache.tuscany.core.invocation.impl.InvokerInterceptor;
import org.apache.tuscany.core.invocation.jdk.JDKProxyFactory;
import org.apache.tuscany.core.invocation.spi.ProxyFactory;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.core.message.impl.PojoMessageFactory;
import org.apache.tuscany.core.message.impl.PojoMessageImpl;
import org.apache.tuscany.model.types.OperationType;

public class JavaTargetWireBuilderTestCase extends TestCase {

    private Method hello;

    private Method goodbye;

    public JavaTargetWireBuilderTestCase() {
    }

    public JavaTargetWireBuilderTestCase(String arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        hello = SimpleTarget.class.getMethod("hello", new Class[] { String.class });
        goodbye = SimpleTarget.class.getMethod("goodbye", new Class[] { String.class });
    }
    
    
    /**
     * Tests basic wiring of a source to a target, including handlers and interceptors
     */
    public void testInvocation() throws Exception {
        MessageFactory msgFactory = new PojoMessageFactory();
        OperationType operation = new MockJavaOperationType(hello);

        InvocationConfiguration source = new InvocationConfiguration(operation);
        MockHandler sourceRequestHandler = new MockHandler();
        MockHandler sourceResponseHandler = new MockHandler();
        MockSyncInterceptor sourceInterceptor = new MockSyncInterceptor();
        source.addRequestHandler(sourceRequestHandler);
        source.addResponseHandler(sourceResponseHandler);
        source.addSourceInterceptor(sourceInterceptor);

        ProxyFactory sourceFactory = new JDKProxyFactory();
        Map<OperationType, InvocationConfiguration> sourceInvocationConfigs = new HashMap();
        sourceInvocationConfigs.put(operation, source);
        ProxyConfiguration sourceConfig = new ProxyConfiguration(new QualifiedName("target/SimpleTarget"),
                sourceInvocationConfigs, Thread.currentThread().getContextClassLoader(), null, msgFactory);
        sourceFactory.setProxyConfiguration(sourceConfig);
        sourceFactory.setBusinessInterface(SimpleTarget.class);
        
        InvocationConfiguration target = new InvocationConfiguration(operation);
        MockHandler targetRequestHandler = new MockHandler();
        MockHandler targetResponseHandler = new MockHandler();
        MockSyncInterceptor targetInterceptor = new MockSyncInterceptor();
        target.addRequestHandler(targetRequestHandler);
        target.addResponseHandler(targetResponseHandler);
        target.addTargetInterceptor(targetInterceptor);
        target.addTargetInterceptor(new InvokerInterceptor());

        ProxyFactory targetFactory = new JDKProxyFactory();
        Map<OperationType, InvocationConfiguration> targetInvocationConfigs = new HashMap();
        targetInvocationConfigs.put(operation, target);
        ProxyConfiguration targetConfig = new ProxyConfiguration(new QualifiedName("target/SimpleTarget"),
                targetInvocationConfigs, Thread.currentThread().getContextClassLoader(), null, msgFactory);
        targetFactory.setProxyConfiguration(targetConfig);
        targetFactory.setBusinessInterface(SimpleTarget.class);

        // bootstrap a scope container with the target in it
        Map<String,Object> instances = new HashMap();
        SimpleTarget simpleTarget = new SimpleTargetImpl();
        instances.put("target",simpleTarget);
        MockScopeContext scopeCtx = new MockScopeContext(instances);

        // connect the source to the target
        DefaultWireBuilder builder = new DefaultWireBuilder();
        builder.addWireBuilder(new JavaTargetWireBuilder());
        
        builder.wire(sourceFactory, targetFactory, JavaComponentRuntimeConfiguration.class, true, scopeCtx);
        source.build();
        target.build();
        Assert.assertNotNull(source.getTargetInvoker());
        
        Message msg = new PojoMessageImpl();
        msg.setPayload("foo");
        msg.setTargetInvoker(source.getTargetInvoker());
        Message response = (Message) source.getSourceInterceptor().invoke(msg);
        Assert.assertEquals("foo", response.getPayload());
        Assert.assertEquals(1, sourceRequestHandler.getCount());
        Assert.assertEquals(1, sourceResponseHandler.getCount());
        Assert.assertEquals(1, sourceInterceptor.getCount());
        Assert.assertEquals(1, targetRequestHandler.getCount());
        Assert.assertEquals(1, targetResponseHandler.getCount());
        Assert.assertEquals(1, targetInterceptor.getCount());
    }


}

