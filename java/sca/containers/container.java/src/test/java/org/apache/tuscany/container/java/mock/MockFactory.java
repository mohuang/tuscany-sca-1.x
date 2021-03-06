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
package org.apache.tuscany.container.java.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import org.apache.tuscany.common.monitor.MonitorFactory;
import org.apache.tuscany.common.monitor.impl.NullMonitorFactory;
import org.apache.tuscany.container.java.assembly.JavaAssemblyFactory;
import org.apache.tuscany.container.java.assembly.JavaImplementation;
import org.apache.tuscany.container.java.assembly.impl.JavaAssemblyFactoryImpl;
import org.apache.tuscany.container.java.assembly.mock.HelloWorldImpl;
import org.apache.tuscany.container.java.assembly.mock.HelloWorldService;
import org.apache.tuscany.container.java.builder.JavaContextFactoryBuilder;
import org.apache.tuscany.container.java.builder.JavaTargetWireBuilder;
import org.apache.tuscany.container.java.context.JavaAtomicContext;
import org.apache.tuscany.container.java.mock.binding.foo.FooBinding;
import org.apache.tuscany.container.java.mock.binding.foo.FooBindingBuilder;
import org.apache.tuscany.container.java.mock.binding.foo.FooBindingWireBuilder;
import org.apache.tuscany.container.java.mock.components.GenericComponent;
import org.apache.tuscany.container.java.mock.components.HelloWorldClient;
import org.apache.tuscany.container.java.mock.components.ModuleScopeComponent;
import org.apache.tuscany.container.java.mock.components.ModuleScopeComponentImpl;
import org.apache.tuscany.container.java.mock.components.OtherTarget;
import org.apache.tuscany.container.java.mock.components.OtherTargetImpl;
import org.apache.tuscany.container.java.mock.components.Source;
import org.apache.tuscany.container.java.mock.components.SourceImpl;
import org.apache.tuscany.container.java.mock.components.Target;
import org.apache.tuscany.container.java.mock.components.TargetImpl;
import org.apache.tuscany.core.builder.BuilderException;
import org.apache.tuscany.core.builder.ContextFactory;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.builder.ContextFactoryBuilderRegistry;
import org.apache.tuscany.core.builder.WireBuilder;
import org.apache.tuscany.core.builder.impl.DefaultWireBuilder;
import org.apache.tuscany.core.builder.system.DefaultPolicyBuilderRegistry;
import org.apache.tuscany.core.builder.system.PolicyBuilderRegistry;
import org.apache.tuscany.core.client.BootstrapHelper;
import org.apache.tuscany.core.config.ComponentTypeIntrospector;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.core.config.JavaIntrospectionHelper;
import org.apache.tuscany.core.config.processor.ProcessorUtils;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.Context;
import org.apache.tuscany.core.context.SystemCompositeContext;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.core.injection.EventInvoker;
import org.apache.tuscany.core.injection.FieldInjector;
import org.apache.tuscany.core.injection.Injector;
import org.apache.tuscany.core.injection.MethodEventInvoker;
import org.apache.tuscany.core.injection.MethodInjector;
import org.apache.tuscany.core.injection.PojoObjectFactory;
import org.apache.tuscany.core.injection.SingletonObjectFactory;
import org.apache.tuscany.core.message.MessageFactory;
import org.apache.tuscany.core.message.impl.MessageFactoryImpl;
import org.apache.tuscany.core.runtime.RuntimeContext;
import org.apache.tuscany.core.runtime.RuntimeContextImpl;
import org.apache.tuscany.core.system.assembly.SystemAssemblyFactory;
import org.apache.tuscany.core.system.assembly.SystemBinding;
import org.apache.tuscany.core.system.assembly.impl.SystemAssemblyFactoryImpl;
import org.apache.tuscany.core.system.builder.SystemContextFactoryBuilder;
import org.apache.tuscany.core.system.builder.SystemEntryPointBuilder;
import org.apache.tuscany.core.system.builder.SystemExternalServiceBuilder;
import org.apache.tuscany.core.system.context.SystemCompositeContextImpl;
import org.apache.tuscany.core.wire.WireFactoryFactory;
import org.apache.tuscany.core.wire.jdk.JDKWireFactoryFactory;
import org.apache.tuscany.core.wire.service.DefaultWireFactoryService;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.AtomicComponent;
import org.apache.tuscany.model.assembly.Component;
import org.apache.tuscany.model.assembly.ComponentType;
import org.apache.tuscany.model.assembly.ConfiguredReference;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.Multiplicity;
import org.apache.tuscany.model.assembly.Reference;
import org.apache.tuscany.model.assembly.Scope;
import org.apache.tuscany.model.assembly.Service;
import org.apache.tuscany.model.assembly.impl.AssemblyContextImpl;
import org.apache.tuscany.model.types.java.JavaServiceContract;
import org.osoa.sca.annotations.ComponentName;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;

/**
 * Generates test components, modules, and runtime artifacts
 *
 * @version $Rev$ $Date$
 */
public class MockFactory {

    public static final String JAVA_BUILDER = "java.runtime.builder";
    public static final String MESSAGE_FACTORY = "java.runtime.messageFactory";
    public static final String PROXY_FACTORY_FACTORY = "java.runtime.wireFactoryFactory";
    public static final String WIRE_FACTORY_SERVICE = "java.runtime.wireFactoryservice";
    public static final String JAVA_WIRE_BUILDER = "java.wire.builder";
    public static final String FOO_BUILDER = "foo.binding.builder";
    public static final String FOO_WIRE_BUILDER = "foo.binding.wire.builder";
    public static final String POLICY_BUILDER_REGISTRY = "foo.binding.policy.registry";
    public static final String SYSTEM_CHILD = "tuscany.system.child";

    private static JavaAssemblyFactory factory = new JavaAssemblyFactoryImpl();
    private static SystemAssemblyFactory systemFactory = new SystemAssemblyFactoryImpl();
    private static AssemblyContext assemblyContext = new AssemblyContextImpl(null, null);
    private static ComponentTypeIntrospector introspector;
    private static ComponentType systemComponentType;
    private static ComponentType compositeComponentType;

    public static ComponentType getComponentType() throws ConfigurationLoadException {
        if (systemComponentType == null) {
            systemComponentType = getIntrospector().introspect(SystemCompositeContextImpl.class);
        }
        return systemComponentType;
    }

    public static ComponentType getCompositeComponentType() throws ConfigurationLoadException {
        if (compositeComponentType == null) {
            compositeComponentType = getIntrospector().introspect(CompositeContextImpl.class);
        }
        return compositeComponentType;
    }

    public static ComponentTypeIntrospector getIntrospector() {
        if (introspector == null) {
            introspector = ProcessorUtils.createCoreIntrospector(systemFactory);
        }
        return introspector;
    }

    /**
     * Creates an initialized simple component
     *
     * @param name  the component name
     * @param type  the implementation type
     * @param scope the component scope
     */
    public static AtomicComponent createComponent(String name, Class type, Scope scope) throws ConfigurationLoadException {
        AtomicComponent sc = factory.createSimpleComponent();
        JavaImplementation impl = factory.createJavaImplementation();
        impl.setComponentType(getIntrospector().introspect(type));
        impl.setImplementationClass(type);
        sc.setImplementation(impl);
        Service s = factory.createService();
        JavaServiceContract ji = factory.createJavaServiceContract();
        ji.setInterface(type);
        s.setServiceContract(ji);
        ji.setScope(scope);
        impl.getComponentType().getServices().add(s);
        sc.setName(name);
        sc.setImplementation(impl);
        return sc;
    }

    public static AtomicComponent createNonIntrospectedComponent(String name, Class service, Class type, Scope scope) throws ConfigurationLoadException {
        AtomicComponent sc = factory.createSimpleComponent();
        JavaImplementation impl = factory.createJavaImplementation();
        impl.setComponentType(factory.createComponentType());
        impl.setImplementationClass(type);
        sc.setImplementation(impl);
        Service s = factory.createService();
        JavaServiceContract ji = factory.createJavaServiceContract();
        ji.setInterface(service);
        s.setServiceContract(ji);
        ji.setScope(scope);
        impl.getComponentType().getServices().add(s);
        sc.setName(name);
        sc.setImplementation(impl);
        return sc;
    }


    /**
     * Creates an composite component with the given name
     */
    public static Component createCompositeComponent(String name) throws ConfigurationLoadException {
        Component sc = systemFactory.createModuleComponent();
        Module impl = systemFactory.createModule();
        impl.setName(name);
        //impl.setImplementationClass(CompositeContextImpl.class);
        sc.setImplementation(impl);
        Service s = systemFactory.createService();
        JavaServiceContract ji = systemFactory.createJavaServiceContract();
        s.setServiceContract(ji);
        ji.setScope(Scope.AGGREGATE);
        //impl.setComponentType(systemFactory.createComponentType());
        impl.setImplementationClass(CompositeContextImpl.class);
        impl.setComponentType(getCompositeComponentType());
        impl.getComponentType().getServices().add(s);
        sc.setName(name);
        sc.setImplementation(impl);

        return sc;
    }

    /**
     * Creates a system composite component with the given name
     */
    public static Component createSystemCompositeComponent(String name) throws ConfigurationLoadException {
        Component sc = systemFactory.createModuleComponent();
        Module impl = systemFactory.createSystemModule();
        impl.setName(name);
        //impl.setImplementationClass(SystemCompositeContextImpl.class);
        sc.setImplementation(impl);
        Service s = systemFactory.createService();
        JavaServiceContract ji = systemFactory.createJavaServiceContract();
        s.setServiceContract(ji);
        ji.setScope(Scope.AGGREGATE);
        impl.setComponentType(getComponentType());
        //impl.setComponentType(systemFactory.createComponentType());
        impl.getComponentType().getServices().add(s);
        sc.setName(name);
        sc.setImplementation(impl);
        return sc;
    }

    /**
     * Creates an external service configured with the 'Foo' test binding
     */
    public static ExternalService createFooBindingExternalService(String name, Class interfaz) {
        ExternalService es = factory.createExternalService();
        es.setName(name);
        Service s = factory.createService();
        JavaServiceContract ji = factory.createJavaServiceContract();
        ji.setScope(Scope.MODULE);
        ji.setInterface(interfaz);
        s.setServiceContract(ji);
        ConfiguredService configuredService = factory.createConfiguredService();
        es.setConfiguredService(configuredService);

        FooBinding binding = new FooBinding();
        es.getBindings().add(binding);
        return es;
    }

    /**
     * Creates an entry point with the given name configured with the given interface and the {@link
     * FooBinding}
     */
    public static EntryPoint createFooBindingEntryPoint(String name, Class interfaz) {
        EntryPoint ep = factory.createEntryPoint();
        ep.setName(name);
        Service s = factory.createService();
        JavaServiceContract ji = factory.createJavaServiceContract();
        ji.setScope(Scope.MODULE);
        ji.setInterface(interfaz);
        s.setServiceContract(ji);
        ConfiguredService configuredService = factory.createConfiguredService();
        configuredService.setPort(s);
        ep.setConfiguredService(configuredService);
        FooBinding binding = new FooBinding();
        ep.getBindings().add(binding);
        return ep;
    }

    /**
     * Creates an external service configured with a {@link SystemBinding}
     */
    public static ExternalService createESSystemBinding(String name, String refName) {
        ExternalService es = systemFactory.createExternalService();
        es.setName(name);
        ConfiguredService configuredService = systemFactory.createConfiguredService();
        es.setConfiguredService(configuredService);
        SystemBinding binding = systemFactory.createSystemBinding();
        binding.setTargetName(refName);
        es.getBindings().add(binding);
        es.initialize(null);
        return es;
    }

    /**
     * Creates a module with a Java-based "target" module-scoped component wired to a module-scoped "source"
     */
    public static Module createModule() throws ConfigurationLoadException {
        return createModule(Scope.MODULE, Scope.MODULE);
    }

    /**
     * Creates a module with a Java-based "target" component wired to a "source"
     */
    public static Module createModule(Scope sourceScope, Scope targetScope) throws ConfigurationLoadException {
        Component sourceComponent = createNonIntrospectedComponent("source", ModuleScopeComponent.class, ModuleScopeComponentImpl.class, sourceScope);
        Component targetComponent = createNonIntrospectedComponent("target", ModuleScopeComponent.class, ModuleScopeComponentImpl.class, targetScope);

        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(GenericComponent.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("GenericComponent");
        targetContract.setScope(targetScope);
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        cTargetService.initialize(assemblyContext);
        targetComponent.getConfiguredServices().add(cTargetService);
        targetComponent.initialize(assemblyContext);

        Reference ref = factory.createReference();
        ref.setName("setGenericComponent");
        JavaServiceContract inter = factory.createJavaServiceContract();
        inter.setInterface(GenericComponent.class);
        ref.setServiceContract(inter);
        sourceComponent.getImplementation().getComponentType().getReferences().add(ref);

        ConfiguredReference cref = factory.createConfiguredReference("setGenericComponent", "target");
        cref.initialize(assemblyContext);
        sourceComponent.getConfiguredReferences().add(cref);
        sourceComponent.initialize(assemblyContext);

        Module module = factory.createModule();
        module.setName("test.module");
        module.getComponents().add(sourceComponent);
        module.getComponents().add(targetComponent);
        module.initialize(assemblyContext);
        return module;
    }

    /**
     * Creates a module with a Java-based source component wired to a "target" external service configured
     * with the {@link FooBinding}
     */
    public static Module createModuleWithExternalService() throws ConfigurationLoadException {
        Component sourceComponent = createComponent("source", HelloWorldClient.class, Scope.MODULE);
        ExternalService targetES = createFooBindingExternalService("target", HelloWorldService.class);

        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(HelloWorldService.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("HelloWorld");
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        targetES.setConfiguredService(cTargetService);
        targetES.initialize(assemblyContext);

        Reference ref = factory.createReference();
        ref.setName("setHelloWorldService");
        JavaServiceContract inter = factory.createJavaServiceContract();
        inter.setInterface(HelloWorldService.class);
        ref.setServiceContract(inter);
        sourceComponent.getImplementation().getComponentType().getReferences().add(ref);

        ConfiguredReference cref = factory.createConfiguredReference(ref.getName(), "target");
        cref.initialize(assemblyContext);
        sourceComponent.getConfiguredReferences().add(cref);
        sourceComponent.initialize(assemblyContext);

        Module module = factory.createModule();
        module.setName("test.module");
        module.getComponents().add(sourceComponent);
        module.getExternalServices().add(targetES);
        module.initialize(assemblyContext);
        return module;
    }

    /**
     * Creates a module with an entry point named "source" configured with the {@link FooBinding} wired to a
     * service offered by a Java-based component named "target"
     *
     * @param scope the scope of the target service
     */
    public static Module createModuleWithEntryPoint(Scope scope) throws ConfigurationLoadException {
        Component targetComponent = createComponent("target", HelloWorldImpl.class, scope);

        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(HelloWorldService.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("HelloWorldService");
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        targetComponent.getConfiguredServices().add(cTargetService);
        targetComponent.initialize(assemblyContext);

        Reference ref = factory.createReference();
        ConfiguredReference cref = factory.createConfiguredReference();
        ref.setName("setHelloWorldService");
        JavaServiceContract inter = factory.createJavaServiceContract();
        inter.setInterface(HelloWorldService.class);
        ref.setServiceContract(inter);
        cref.setPort(ref);
        cref.getTargetConfiguredServices().add(cTargetService);
        cref.initialize(assemblyContext);

        EntryPoint sourceEP = createFooBindingEntryPoint("source", HelloWorldService.class);
        sourceEP.setConfiguredReference(cref);
        sourceEP.getConfiguredService().getPort().setName("HelloWorldService");
        sourceEP.initialize(assemblyContext);

        Module module = factory.createModule();
        module.setName("test.module");
        module.getEntryPoints().add(sourceEP);
        module.getComponents().add(targetComponent);
        module.setImplementationClass(CompositeContextImpl.class);
        module.setComponentType(getCompositeComponentType());
        module.initialize(assemblyContext);
        return module;
    }

    /**
     * Creates a module with an entry point wired to a "target" external service configured with the {@link
     * FooBinding}
     */
    public static Module createModuleWithEntryPointToExternalService() {
        //Component sourceComponent = createComponent("source", HelloWorldClient.class, Scope.MODULE);

        EntryPoint sourceEP = createFooBindingEntryPoint("source", HelloWorldService.class);
        sourceEP.getConfiguredService().getPort().setName("HelloWorldService");
        sourceEP.initialize(assemblyContext);


        ExternalService targetES = createFooBindingExternalService("target", HelloWorldService.class);

        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(HelloWorldService.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("HelloWorld");
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        targetES.setConfiguredService(cTargetService);
        targetES.initialize(assemblyContext);

        Reference ref = factory.createReference();
        ref.setName("setHelloWorldService");
        JavaServiceContract inter = factory.createJavaServiceContract();
        inter.setInterface(HelloWorldService.class);
        ref.setServiceContract(inter);


        ConfiguredReference cref = factory.createConfiguredReference(ref.getName(), "target");
        cref.setPort(ref);
        cref.initialize(assemblyContext);
        sourceEP.setConfiguredReference(cref);
        sourceEP.initialize(assemblyContext);

        Module module = factory.createModule();
        module.setName("test.module");
        module.getEntryPoints().add(sourceEP);
        module.getExternalServices().add(targetES);
        module.initialize(assemblyContext);
        return module;
    }


    /**
     * Creates a test system module with source and target components wired together.
     *
     * @see org.apache.tuscany.core.mock.component.Source
     * @see org.apache.tuscany.core.mock.component.Target
     */

    public static Module createModuleWithWiredComponents(Scope sourceScope, Scope targetScope) {

        // create the target component
        AtomicComponent target = factory.createSimpleComponent();
        target.setName("target");
        JavaImplementation targetImpl = factory.createJavaImplementation();
        targetImpl.setComponentType(factory.createComponentType());
        targetImpl.setImplementationClass(TargetImpl.class);
        target.setImplementation(targetImpl);
        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(Target.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("Target");
        targetImpl.getComponentType().getServices().add(targetService);
        targetContract.setScope(targetScope);
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        cTargetService.initialize(assemblyContext);
        target.getConfiguredServices().add(cTargetService);
        target.initialize(assemblyContext);

        // create the source component
        AtomicComponent source = factory.createSimpleComponent();
        ComponentType componentType = factory.createComponentType();
        source.setName("source");
        JavaImplementation impl = factory.createJavaImplementation();
        impl.setComponentType(componentType);
        impl.setImplementationClass(SourceImpl.class);
        source.setImplementation(impl);
        Service s = systemFactory.createService();
        JavaServiceContract contract = systemFactory.createJavaServiceContract();
        contract.setInterface(Source.class);
        s.setServiceContract(contract);
        contract.setScope(sourceScope);
        impl.getComponentType().getServices().add(s);
        source.setImplementation(impl);

        // wire source to target
        JavaServiceContract refContract = systemFactory.createJavaServiceContract();
        refContract.setInterface(Target.class);
        Reference reference = systemFactory.createReference();
        reference.setName("setTarget");
        reference.setServiceContract(refContract);
        componentType.getReferences().add(reference);
        ConfiguredReference cReference = systemFactory.createConfiguredReference(reference.getName(), "target");
        cReference.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference);

        // wire multiplicity using a setter
        JavaServiceContract refContract2 = systemFactory.createJavaServiceContract();
        refContract2.setInterface(Target.class);
        Reference reference2 = systemFactory.createReference();
        reference2.setName("setTargets");
        reference2.setServiceContract(refContract2);
        reference2.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference2);
        ConfiguredReference cReference2 = systemFactory.createConfiguredReference(reference2.getName(), "target");
        cReference2.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference2);

        // wire multiplicity using a field
        JavaServiceContract refContract3 = systemFactory.createJavaServiceContract();
        refContract3.setInterface(Target.class);
        Reference reference3 = systemFactory.createReference();
        reference3.setName("targetsThroughField");
        reference3.setServiceContract(refContract3);
        reference3.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference3);
        ConfiguredReference cReference3 = systemFactory.createConfiguredReference(reference3.getName(), "target");
        cReference3.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference3);

        // wire multiplicity using a array
        JavaServiceContract refContract4 = systemFactory.createJavaServiceContract();
        refContract4.setInterface(Target.class);
        Reference reference4 = systemFactory.createReference();
        reference4.setName("setArrayOfTargets");
        reference4.setServiceContract(refContract4);
        reference4.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference4);
        ConfiguredReference cReference4 = systemFactory.createConfiguredReference(reference4.getName(), "target");
        cReference4.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference4);

        source.initialize(assemblyContext);

        Module module = systemFactory.createModule();
        module.setName("system.module");

        module.getComponents().add(source);
        module.getComponents().add(target);
        module.initialize(assemblyContext);
        return module;
    }


    /**
     * Creates a test system module with source and target components wired together.
     *
     * @see org.apache.tuscany.core.mock.component.Source
     * @see org.apache.tuscany.core.mock.component.Target
     */

    public static Module createModuleWithWiredComponentsOfDifferentInterface(Scope sourceScope, Scope targetScope) {

        // create the target component
        AtomicComponent target = factory.createSimpleComponent();
        target.setName("target");
        JavaImplementation targetImpl = factory.createJavaImplementation();
        targetImpl.setComponentType(factory.createComponentType());
        targetImpl.setImplementationClass(OtherTargetImpl.class);
        target.setImplementation(targetImpl);
        Service targetService = factory.createService();
        JavaServiceContract targetContract = factory.createJavaServiceContract();
        targetContract.setInterface(OtherTarget.class);
        targetService.setServiceContract(targetContract);
        targetService.setName("Target");
        targetImpl.getComponentType().getServices().add(targetService);
        targetContract.setScope(targetScope);
        ConfiguredService cTargetService = factory.createConfiguredService();
        cTargetService.setPort(targetService);
        cTargetService.initialize(assemblyContext);
        target.getConfiguredServices().add(cTargetService);
        target.initialize(assemblyContext);

        // create the source component
        AtomicComponent source = factory.createSimpleComponent();
        ComponentType componentType = factory.createComponentType();
        source.setName("source");
        JavaImplementation impl = factory.createJavaImplementation();
        impl.setComponentType(componentType);
        impl.setImplementationClass(SourceImpl.class);
        source.setImplementation(impl);
        Service s = systemFactory.createService();
        JavaServiceContract contract = systemFactory.createJavaServiceContract();
        contract.setInterface(Source.class);
        s.setServiceContract(contract);
        contract.setScope(sourceScope);
        impl.getComponentType().getServices().add(s);
        source.setImplementation(impl);

        // wire source to target
        JavaServiceContract refContract = systemFactory.createJavaServiceContract();
        refContract.setInterface(Target.class);
        Reference reference = systemFactory.createReference();
        reference.setName("setTarget");
        reference.setServiceContract(refContract);
        componentType.getReferences().add(reference);
        ConfiguredReference cReference = systemFactory.createConfiguredReference(reference.getName(), "target");
        cReference.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference);

        // wire multiplicity using a setter
        JavaServiceContract refContract2 = systemFactory.createJavaServiceContract();
        refContract2.setInterface(Target.class);
        Reference reference2 = systemFactory.createReference();
        reference2.setName("setTargets");
        reference2.setServiceContract(refContract2);
        reference2.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference2);
        ConfiguredReference cReference2 = systemFactory.createConfiguredReference(reference2.getName(), "target");
        cReference2.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference2);

        // wire multiplicity using a field
        JavaServiceContract refContract3 = systemFactory.createJavaServiceContract();
        refContract3.setInterface(Target.class);
        Reference reference3 = systemFactory.createReference();
        reference3.setName("targetsThroughField");
        reference3.setServiceContract(refContract3);
        reference3.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference3);
        ConfiguredReference cReference3 = systemFactory.createConfiguredReference(reference3.getName(), "target");
        cReference3.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference3);

        // wire multiplicity using a array
        JavaServiceContract refContract4 = systemFactory.createJavaServiceContract();
        refContract4.setInterface(Target.class);
        Reference reference4 = systemFactory.createReference();
        reference4.setName("setArrayOfTargets");
        reference4.setServiceContract(refContract4);
        reference4.setMultiplicity(Multiplicity.ONE_N);
        componentType.getReferences().add(reference4);
        ConfiguredReference cReference4 = systemFactory.createConfiguredReference(reference4.getName(), "target");
        cReference4.initialize(assemblyContext);
        source.getConfiguredReferences().add(cReference4);

        source.initialize(assemblyContext);

        Module module = systemFactory.createModule();
        module.setName("system.module");

        module.getComponents().add(source);
        module.getComponents().add(target);
        module.initialize(assemblyContext);
        return module;
    }


    /**
     * Returns a collection of bootstrap configuration builders
     */
    public static List<ContextFactoryBuilder> createSystemBuilders() {
        List<ContextFactoryBuilder> builders = new ArrayList<ContextFactoryBuilder>();
        builders.add((new SystemContextFactoryBuilder(null)));
        builders.add(new SystemEntryPointBuilder());
        builders.add(new SystemExternalServiceBuilder());
        return builders;
    }

    /**
     * Creates an composite context faxtory
     *
     * @param name the name of the component
     * @throws BuilderException
     * @see ContextFactory
     */
    public static ContextFactory<Context> createCompositeConfiguration(String name
    ) throws BuilderException, ConfigurationLoadException {

        Component sc = createCompositeComponent(name);
        SystemContextFactoryBuilder builder = new SystemContextFactoryBuilder(null);
        builder.build(sc);
        return (ContextFactory<Context>) sc.getContextFactory();
    }

    /**
     * Creates a Java POJO component context
     *
     * @param name                   the name of the context
     * @param implType               the POJO class
     * @param scope                  the component scope
     * @param moduleComponentContext the containing composite context
     * @throws NoSuchMethodException if the POJO does not have a default noi-args constructor
     */
    public static JavaAtomicContext createPojoContext(String name, Class implType, Scope scope,
                                                      CompositeContext moduleComponentContext) throws NoSuchMethodException, ConfigurationLoadException {
        AtomicComponent component = createComponent(name, implType, scope);

        Set<Field> fields = JavaIntrospectionHelper.getAllFields(implType);
        Set<Method> methods = JavaIntrospectionHelper.getAllUniqueMethods(implType);
        List<Injector> injectors = new ArrayList<Injector>();

        EventInvoker initInvoker = null;
        boolean eagerInit = false;
        EventInvoker destroyInvoker = null;
        for (Field field : fields) {
            ComponentName compName = field.getAnnotation(ComponentName.class);
            if (compName != null) {
                Injector injector = new FieldInjector(field, new SingletonObjectFactory(name));
                injectors.add(injector);
            }
            org.osoa.sca.annotations.Context context = field.getAnnotation(org.osoa.sca.annotations.Context.class);
            if (context != null) {
                Injector injector = new FieldInjector(field, new SingletonObjectFactory(moduleComponentContext));
                injectors.add(injector);
            }
        }
        for (Method method : methods) {
            // FIXME Java5
            Init init = method.getAnnotation(Init.class);
            if (init != null && initInvoker == null) {
                initInvoker = new MethodEventInvoker(method);
                eagerInit = init.eager();
                continue;
            }
            Destroy destroy = method.getAnnotation(Destroy.class);
            if (destroy != null && destroyInvoker == null) {
                destroyInvoker = new MethodEventInvoker(method);
                continue;
            }
            ComponentName compName = method.getAnnotation(ComponentName.class);
            if (compName != null) {
                Injector injector = new MethodInjector(method, new SingletonObjectFactory(name));
                injectors.add(injector);
            }
            org.osoa.sca.annotations.Context context = method.getAnnotation(org.osoa.sca.annotations.Context.class);
            if (context != null) {
                Injector injector = new MethodInjector(method, new SingletonObjectFactory(moduleComponentContext));
                injectors.add(injector);
            }
        }
        boolean stateless = (scope == Scope.INSTANCE);
        return new JavaAtomicContext("foo", new PojoObjectFactory(JavaIntrospectionHelper
                .getDefaultConstructor(implType), null, injectors), eagerInit, initInvoker, destroyInvoker, stateless);
    }

    /**
     * Creates a default {@link RuntimeContext} configured with support for Java component implementations
     *
     * @throws ConfigurationException
     */
    public static RuntimeContext createJavaRuntime() throws ConfigurationException {
        MonitorFactory monitorFactory = new NullMonitorFactory();
        ContextFactoryBuilderRegistry builderRegistry = BootstrapHelper.bootstrapContextFactoryBuilders(monitorFactory);
        DefaultWireBuilder wireBuilder = new DefaultWireBuilder();
        RuntimeContext runtime = new RuntimeContextImpl(monitorFactory, builderRegistry, wireBuilder);
        runtime.start();
        runtime.getSystemContext().registerModelObject(createSystemCompositeComponent(SYSTEM_CHILD));
        SystemCompositeContext ctx = (SystemCompositeContext) runtime.getSystemContext().getContext(SYSTEM_CHILD);
        Component comp = systemFactory.createSystemComponent(POLICY_BUILDER_REGISTRY, PolicyBuilderRegistry.class, DefaultPolicyBuilderRegistry.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(DefaultPolicyBuilderRegistry.class));
        ctx.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(MESSAGE_FACTORY, MessageFactory.class, MessageFactoryImpl.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(MessageFactoryImpl.class));
        ctx.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(PROXY_FACTORY_FACTORY, WireFactoryFactory.class, JDKWireFactoryFactory.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(JDKWireFactoryFactory.class));
        ctx.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(WIRE_FACTORY_SERVICE, org.apache.tuscany.core.wire.service.WireFactoryService.class, DefaultWireFactoryService.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(DefaultWireFactoryService.class));
        ctx.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(JAVA_BUILDER, ContextFactoryBuilder.class, JavaContextFactoryBuilder.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(JavaContextFactoryBuilder.class));
        ctx.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(JAVA_WIRE_BUILDER, WireBuilder.class, JavaTargetWireBuilder.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(JavaTargetWireBuilder.class));
        ctx.registerModelObject(comp);
        ctx.publish(new ModuleStart(new Object()));
        return runtime;
    }

    /**
     * Registers the {@link FooBinding} builders with a given runtime
     *
     * @throws ConfigurationException
     */
    public static RuntimeContext registerFooBinding(RuntimeContext runtime) throws ConfigurationException {
        CompositeContext child = (CompositeContext) runtime.getSystemContext().getContext(MockFactory.SYSTEM_CHILD);
        child.getContext(MockFactory.JAVA_BUILDER).getInstance(null);
        Component comp = systemFactory.createSystemComponent(FOO_BUILDER, ContextFactoryBuilder.class, FooBindingBuilder.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(FooBindingBuilder.class));
        child.registerModelObject(comp);
        comp = systemFactory.createSystemComponent(FOO_WIRE_BUILDER, WireBuilder.class, FooBindingWireBuilder.class, Scope.MODULE);
        comp.getImplementation().setComponentType(getIntrospector().introspect(FooBindingWireBuilder.class));
        child.registerModelObject(comp);
        // since the child context is already started, we need to manually retrieve the components to init them
        Assert.assertNotNull(child.getContext(FOO_BUILDER).getInstance(null));
        Assert.assertNotNull(child.getContext(FOO_WIRE_BUILDER).getInstance(null));
        return runtime;
    }


}
