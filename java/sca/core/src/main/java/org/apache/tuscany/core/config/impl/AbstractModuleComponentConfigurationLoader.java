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
package org.apache.tuscany.core.config.impl;

import org.apache.tuscany.common.resource.ResourceLoader;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.core.config.ModuleComponentConfigurationLoader;
import org.apache.tuscany.core.config.ComponentTypeIntrospector;
import org.apache.tuscany.core.config.processor.ProcessorUtils;
import org.apache.tuscany.core.system.context.SystemCompositeContextImpl;
import org.apache.tuscany.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.apache.tuscany.model.assembly.ModuleFragment;
import org.apache.tuscany.model.assembly.ComponentType;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractModuleComponentConfigurationLoader implements ModuleComponentConfigurationLoader {
    private static final String SCA_MODULE_FILE_NAME = "sca.module";
    //FIXME can fragments have a variable prefix name?
    private static final String SCA_FRAGMENT_FILE_NAME = "sca.fragment";
    private static final String SYSTEM_MODULE_FILE_NAME = "system.module";
    //FIXME can fragments have a variable prefix name?
    private static final String SYSTEM_FRAGMENT_FILE_NAME = "system.fragment";

    protected final AssemblyContext modelContext;
    protected final ResourceLoader resourceLoader;
    protected final AssemblyFactory assemblyFactory;

    // JFM HACK
    private ComponentTypeIntrospector introspector;

    private ComponentType systemType;

    private ComponentType compositeType;

    protected ComponentTypeIntrospector getIntrospector(){
        if (introspector == null){
            introspector =  ProcessorUtils.createCoreIntrospector(assemblyFactory);
        }
        return introspector;
    }

    protected ComponentType getSystemCompositeComponentType() throws ConfigurationLoadException {
        if (systemType == null){
            systemType = getIntrospector().introspect(SystemCompositeContextImpl.class);
        }
        return systemType;
    }

    protected ComponentType getCompositeComponentType() throws ConfigurationLoadException {
        if (compositeType == null){
            compositeType = getIntrospector().introspect(CompositeContextImpl.class);
        }
        return compositeType;
    }
    ///  JFM HACK

    protected AbstractModuleComponentConfigurationLoader(AssemblyContext modelContext) {
        this.modelContext = modelContext;
        resourceLoader = modelContext.getApplicationResourceLoader();
        assemblyFactory = modelContext.getAssemblyFactory();
    }

    public ModuleComponent loadSystemModuleComponent(String name, String uri) throws ConfigurationLoadException {
        ModuleComponent mc = loadModuleComponent(SYSTEM_MODULE_FILE_NAME, SYSTEM_FRAGMENT_FILE_NAME, name, uri);
        //JFM HACK  - this is completely gross since it overwrites existing component type
        mc.getImplementation().setImplementationClass(SystemCompositeContextImpl.class);
        mc.getImplementation().setComponentType(getSystemCompositeComponentType());
        //END HACK
        return mc;
    }

    public ModuleComponent loadModuleComponent(String name, String uri) throws ConfigurationLoadException {
        ModuleComponent mc = loadModuleComponent(SCA_MODULE_FILE_NAME, SCA_FRAGMENT_FILE_NAME, name, uri);
        //JFM HACK
        mc.getImplementation().setImplementationClass(CompositeContextImpl.class);
        mc.getImplementation().setComponentType(getCompositeComponentType());
        //END HACK
        return mc;
    }

    protected ModuleComponent loadModuleComponent(String moduleFileName, String fragmentFileName, String name, String uri) throws ConfigurationLoadException {

        // Load the sca.module file
        URL moduleUrl;
        moduleUrl = resourceLoader.getResource(moduleFileName);


        if (moduleUrl == null) {
            throw new ConfigurationLoadException(moduleFileName);
        }

        // Load the sca.fragment files
        Iterator<URL> i;
        try {
            i = resourceLoader.getResources(fragmentFileName);
        } catch (IOException e) {
            throw new ConfigurationLoadException(fragmentFileName, e);
        }
        List<URL> moduleFragmentUris=new ArrayList<URL>();
        while (i.hasNext()) {
            URL url=i.next();
            moduleFragmentUris.add(url);
        }

        return loadModuleComponent(name, uri, moduleUrl, moduleFragmentUris);
    }

    public ModuleComponent loadModuleComponent(String name, String uri, URL url) throws ConfigurationLoadException {
        return loadModuleComponent( name, uri, url, null);
    }

    public ModuleComponent loadModuleComponent(String name, String uri, URL moduleUri, Collection<URL> moduleFragmentUris) throws ConfigurationLoadException {

        // Load the module file
        Module module=loadModule(moduleUri);
        // Load the sca.fragment files
        if (moduleFragmentUris!=null) {
            for (URL moduleFragmentUri : moduleFragmentUris) {
                ModuleFragment moduleFragment=loadModuleFragment(moduleFragmentUri);
                module.getModuleFragments().add(moduleFragment);
            }
        }

        // Create the module component
        ModuleComponent moduleComponent=assemblyFactory.createModuleComponent();
        moduleComponent.setName(name);
        moduleComponent.setURI(uri);
        moduleComponent.setImplementation(module);
        moduleComponent.initialize(modelContext);

        return moduleComponent;
    }

    public abstract Module loadModule(URL url) throws ConfigurationLoadException;

    public abstract ModuleFragment loadModuleFragment(URL url) throws ConfigurationLoadException;
}
