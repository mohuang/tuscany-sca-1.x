/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.tuscany.core.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;

import org.apache.tuscany.common.monitor.MonitorFactory;
import org.apache.tuscany.common.resource.ResourceLoader;
import org.apache.tuscany.common.resource.impl.ResourceLoaderImpl;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.builder.ContextFactoryBuilderRegistry;
import org.apache.tuscany.core.builder.impl.ContextFactoryBuilderRegistryImpl;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.config.ModuleComponentConfigurationLoader;
import org.apache.tuscany.core.config.impl.StAXModuleComponentConfigurationLoaderImpl;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.SystemCompositeContext;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.loader.StAXLoaderRegistry;
import org.apache.tuscany.core.loader.StAXUtil;
import org.apache.tuscany.core.system.assembly.impl.SystemAssemblyFactoryImpl;
import org.apache.tuscany.core.system.builder.SystemContextFactoryBuilder;
import org.apache.tuscany.core.system.builder.SystemEntryPointBuilder;
import org.apache.tuscany.core.system.builder.SystemExternalServiceBuilder;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.apache.tuscany.model.assembly.impl.AssemblyContextImpl;
import org.apache.tuscany.model.assembly.loader.AssemblyModelLoader;
import org.apache.tuscany.model.scdl.loader.impl.SCDLAssemblyModelLoaderImpl;

/**
 * @version $Rev$ $Date$
 */
public final class BootstrapHelper {
    private BootstrapHelper() {
    }

    /**
     * Returns a default AssemblyModelContext.
     *
     * @param classLoader the classloader to use for application artifacts
     * @return a default AssemblyModelContext
     */
    public static AssemblyContext getModelContext(ClassLoader classLoader) {
        // Create an assembly model factory
        AssemblyFactory modelFactory = new SystemAssemblyFactoryImpl();

        // Create a default assembly model loader
        AssemblyModelLoader modelLoader = new SCDLAssemblyModelLoaderImpl();

        // Create a resource loader from the supplied classloader
        ResourceLoader resourceLoader = new ResourceLoaderImpl(classLoader);

        // Create an assembly model context
        return new AssemblyContextImpl(modelFactory, modelLoader, resourceLoader);
    }

    /**
     * Returns a default list of configuration builders.
     *
     * @param monitorFactory
     * @return a default list of configuration builders
     */
    public static List<ContextFactoryBuilder> getBuilders(MonitorFactory monitorFactory) {
        List<ContextFactoryBuilder> configBuilders = new ArrayList<ContextFactoryBuilder>();
        configBuilders.add((new SystemContextFactoryBuilder(monitorFactory)));
        configBuilders.add(new SystemEntryPointBuilder());
        configBuilders.add(new SystemExternalServiceBuilder());
        return configBuilders;
    }

    /**
     * Returns a ContextFactoryBuilderRegistry with default builders registered for system contexts.
     *
     * @param monitorFactory a monitorFactory that will be used to obtain monitors for system components
     * @return a default ContextFactoryBuilderRegistry
     */
    public static ContextFactoryBuilderRegistry bootstrapContextFactoryBuilders(MonitorFactory monitorFactory) {
        ContextFactoryBuilderRegistryImpl registry = new ContextFactoryBuilderRegistryImpl();
        registry.register(new SystemContextFactoryBuilder(monitorFactory));
        registry.register(new SystemEntryPointBuilder());
        registry.register(new SystemExternalServiceBuilder());
        return registry;
    }

    public static final String SYSTEM_LOADER_COMPONENT = "tuscany.loader";

    /**
     * Returns the default module configuration loader.
     *
     * @param systemContext the runtime's system context
     * @param modelContext  the model context the loader will use
     * @return the default module configuration loader
     */
    public static ModuleComponentConfigurationLoader getConfigurationLoader(SystemCompositeContext systemContext, AssemblyContext modelContext) {
        return new StAXModuleComponentConfigurationLoaderImpl(modelContext, XMLInputFactory.newInstance(), systemContext.resolveInstance(StAXLoaderRegistry.class));
    }

    /**
     * Bootstrap the StAX-based loader.
     *
     * @param parentContext the parent system context
     * @param modelContext
     * @return the system context for the loader
     * @throws ConfigurationException
     */
    public static CompositeContext bootstrapStaxLoader(SystemCompositeContext parentContext, AssemblyContext modelContext) throws ConfigurationException {
        ModuleComponent loaderComponent = StAXUtil.bootstrapLoader(SYSTEM_LOADER_COMPONENT, modelContext);
        CompositeContext loaderContext = registerModule(parentContext, loaderComponent);
        loaderContext.publish(new ModuleStart(loaderComponent));
        return loaderContext;
    }

    public static CompositeContext registerModule(CompositeContext parent, ModuleComponent component) throws ConfigurationException {
        // register the component
        parent.registerModelObject(component);

        // Get the composite context representing the component
        return (CompositeContext) parent.getContext(component.getName());
    }
}
