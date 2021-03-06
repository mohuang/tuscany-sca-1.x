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
package org.apache.tuscany.tomcat;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.StringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tuscany.common.resource.ResourceLoader;
import org.apache.tuscany.common.resource.impl.ResourceLoaderImpl;
import org.apache.tuscany.core.client.BootstrapHelper;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.config.ModuleComponentConfigurationLoader;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.EventException;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.context.event.ModuleStop;
import org.apache.tuscany.core.runtime.RuntimeContext;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.apache.tuscany.model.assembly.impl.AssemblyContextImpl;
import org.apache.tuscany.model.assembly.loader.AssemblyModelLoader;

/**
 * @version $Rev$ $Date$
 */
public class TuscanyContextListener implements LifecycleListener {
    private static final Log log = LogFactory.getLog(TuscanyContextListener.class);
    private static final StringManager sm = StringManager.getManager("org.apache.tuscany.tomcat");
    private static final String TUSCANY_RUNTIME_NAME = RuntimeContext.class.getName();
    public static final String MODULE_COMPONENT_NAME = "org.apache.tuscany.core.webapp.ModuleComponentContext";

    private final AssemblyFactory modelFactory;
    private final AssemblyModelLoader modelLoader;
    private final RuntimeContext runtime;
    private CompositeContext moduleContext;
    private TuscanyValve valve;

    public TuscanyContextListener(RuntimeContext runtimeContext, AssemblyFactory modelFactory, AssemblyModelLoader modelLoader) {
        this.runtime = runtimeContext;
        this.modelFactory = modelFactory;
        this.modelLoader = modelLoader;
    }

    public void lifecycleEvent(LifecycleEvent event) {
        String type = event.getType();
        if (Lifecycle.AFTER_START_EVENT.equals(type)) {
            startContext((Context) event.getLifecycle());
        } else if (Lifecycle.STOP_EVENT.equals(type)) {
            stopContext((Context) event.getLifecycle());
        }
    }

    private void startContext(Context ctx) {
        ClassLoader appLoader = ctx.getLoader().getClassLoader();
        if (appLoader.getResource("sca.module") == null) {
            return;
        }

        log.info(sm.getString("context.configLoad", ctx.getName()));
        try {
            loadContext(ctx);
        } catch (ConfigurationException e) {
            log.error(sm.getString("context.configError"), e);
            // todo mark application as unavailable
            return;
        }

        try {
            moduleContext.publish(new ModuleStart(this));
        } catch (EventException e) {
            log.error(sm.getString("context.moduleStartError"), e);
            // todo unload module component from runtime
            // todo mark application as unavailable
            return;
        } catch (RuntimeException e) {
            log.error(sm.getString("context.unknownRuntimeException"), e);
            // todo unload module component from runtime
            throw e;
        }

        // add a valve to this context's pipeline that will associate the request with the runtime
        if (valve == null) {
            valve = new TuscanyValve(moduleContext);
        } else {
            valve.setContext(moduleContext);
            valve.setEnabled(true);
        }
        ctx.getPipeline().addValve(valve);
        // add the RuntimeContext in as a servlet context parameter
        ServletContext servletContext = ctx.getServletContext();
        servletContext.setAttribute(TUSCANY_RUNTIME_NAME, runtime);
        servletContext.setAttribute(MODULE_COMPONENT_NAME, moduleContext);
    }

    private void loadContext(Context ctx) throws ConfigurationException {
        ResourceLoader resourceLoader = new ResourceLoaderImpl(ctx.getLoader().getClassLoader());
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            AssemblyContextImpl modelContext = new AssemblyContextImpl(modelFactory, modelLoader, resourceLoader, ctx.getName());

            ModuleComponentConfigurationLoader loader = BootstrapHelper.getConfigurationLoader(runtime.getSystemContext(), modelContext);

            // Load the SCDL configuration of the application module
            ModuleComponent moduleComponent = loader.loadModuleComponent(ctx.getName(), ctx.getPath());

            // Register it under the root application context
            CompositeContext rootContext = runtime.getRootContext();
            rootContext.registerModelObject(moduleComponent);
            moduleContext = (CompositeContext) rootContext.getContext(moduleComponent.getName());
            //TODO remove the hack below
            moduleContext.setAssemblyContext(modelContext);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void stopContext(Context ctx) {
        if (moduleContext != null) {
            moduleContext.publish(new ModuleStop(this));
        }
        CompositeContext rootContext = runtime.getRootContext();
        rootContext.removeContext(moduleContext.getName());
        valve.setEnabled(false);
        //ctx.getPipeline().removeValve(valve);
        //valve = null;
        moduleContext.stop();
        moduleContext = null;
        // todo unload module component from runtime
    }

}
