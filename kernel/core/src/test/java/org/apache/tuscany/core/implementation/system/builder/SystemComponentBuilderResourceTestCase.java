package org.apache.tuscany.core.implementation.system.builder;

import java.lang.reflect.Field;
import java.net.URI;

import org.apache.tuscany.spi.component.AtomicComponent;
import org.apache.tuscany.spi.component.Component;
import org.apache.tuscany.spi.component.ScopeContainer;
import org.apache.tuscany.spi.component.ScopeRegistry;
import org.apache.tuscany.spi.deployer.DeploymentContext;
import org.apache.tuscany.spi.host.ResourceHost;
import org.apache.tuscany.spi.implementation.java.ConstructorDefinition;
import org.apache.tuscany.spi.implementation.java.PojoComponentType;
import org.apache.tuscany.spi.implementation.java.Resource;
import org.apache.tuscany.spi.model.ComponentDefinition;
import org.apache.tuscany.spi.model.Property;
import org.apache.tuscany.spi.model.ReferenceDefinition;
import org.apache.tuscany.spi.model.Scope;
import org.apache.tuscany.spi.model.ServiceDefinition;
import org.apache.tuscany.spi.wire.Wire;

import junit.framework.TestCase;
import org.apache.tuscany.core.implementation.system.model.SystemImplementation;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class SystemComponentBuilderResourceTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testResourceInjection() throws Exception {
        ScopeContainer container = EasyMock.createNiceMock(ScopeContainer.class);
        DeploymentContext ctx = EasyMock.createNiceMock(DeploymentContext.class);
        ScopeRegistry registry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(registry.getScopeContainer(Scope.STATELESS)).andReturn(container);
        EasyMock.replay(registry);
        ResourceHost host = EasyMock.createMock(ResourceHost.class);
        EasyMock.expect(host.resolveResource(EasyMock.eq(String.class))).andReturn("result");
        EasyMock.replay(host);
        SystemComponentBuilder builder = new SystemComponentBuilder();
        builder.setScopeRegistry(registry);
        builder.setHost(host);
        ConstructorDefinition<Foo> ctorDef = new ConstructorDefinition<SystemComponentBuilderResourceTestCase.Foo>(
            SystemComponentBuilderResourceTestCase.Foo.class.getConstructor());
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        Field member = Foo.class.getDeclaredField("resource");
        Resource<String> resource = new Resource<String>("resource", String.class, member);
        type.add(resource);
        type.setImplementationScope(Scope.STATELESS);
        type.setConstructorDefinition(ctorDef);
        SystemImplementation impl = new SystemImplementation();
        impl.setImplementationClass(SystemComponentBuilderResourceTestCase.Foo.class);
        impl.setComponentType(type);
        ComponentDefinition<SystemImplementation> definition =
            new ComponentDefinition<SystemImplementation>(URI.create("foo"), impl);

        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getTargetInstance()).andReturn("result");
        EasyMock.replay(wire);

        Component parent = EasyMock.createMock(Component.class);
        EasyMock.replay(parent);
        AtomicComponent component = builder.build(definition, ctx);
        SystemComponentBuilderResourceTestCase.Foo foo =
            (SystemComponentBuilderResourceTestCase.Foo) component.createInstance();
        assertEquals("result", foo.resource);
        EasyMock.verify(parent);
    }

    private static class Foo {

        protected String resource;

        public Foo() {
        }

    }
}
