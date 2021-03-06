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
package org.apache.tuscany.core.webapp;

import org.apache.tuscany.common.monitor.MonitorFactory;
import org.apache.tuscany.common.monitor.impl.NullMonitorFactory;
import org.apache.tuscany.core.builder.ContextFactoryBuilderRegistry;
import org.apache.tuscany.core.builder.impl.DefaultWireBuilder;
import org.apache.tuscany.core.client.BootstrapHelper;
import org.apache.tuscany.core.config.ConfigurationException;
import org.apache.tuscany.core.config.ModuleComponentConfigurationLoader;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.SystemCompositeContext;
import org.apache.tuscany.core.context.event.ModuleStop;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.context.event.HttpSessionEnd;
import org.apache.tuscany.core.runtime.RuntimeContext;
import org.apache.tuscany.core.runtime.RuntimeContextImpl;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * ServletContextListener that can be added to a standard web application to boot
 * a Tuscany runtime inside that application. All implementation classes should
 * be located in the web application itself.
 *
 * @version $Rev: 380792 $ $Date: 2006-02-24 11:25:11 -0800 (Fri, 24 Feb 2006) $
 */
public class TuscanyServletListener implements ServletContextListener, HttpSessionListener {
    public static final String SCA_COMPONENT_NAME = "org.apache.tuscany.core.webapp.ModuleComponentName";
    public static final String MODULE_COMPONENT_NAME = "org.apache.tuscany.core.webapp.ModuleComponentContext";
    public static final String TUSCANY_RUNTIME_NAME = RuntimeContext.class.getName();

    private RuntimeContext runtime;
    private CompositeContext moduleContext;

    private static final String SYSTEM_MODULE_COMPONENT = "org.apache.tuscany.core.system";

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String name = servletContext.getInitParameter(SCA_COMPONENT_NAME);
        String uri = name; // todo get from context path
        MonitorFactory monitorFactory = new NullMonitorFactory(); // todo have one that writes to the servlet log

        try {
            bootRuntime(name, uri, monitorFactory);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        servletContext.setAttribute(TUSCANY_RUNTIME_NAME, runtime);
        servletContext.setAttribute(MODULE_COMPONENT_NAME, moduleContext);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        moduleContext.publish(new ModuleStop(this));
        moduleContext.stop();
        SystemCompositeContext systemContext = runtime.getSystemContext();
        systemContext.publish(new ModuleStop(this));
        systemContext.stop();
        runtime.stop();
        servletContextEvent.getServletContext().removeAttribute(MODULE_COMPONENT_NAME);
        servletContextEvent.getServletContext().removeAttribute(TUSCANY_RUNTIME_NAME);
    }

    public void sessionCreated(HttpSessionEvent event) {
        // do nothing since sessions are lazily created in {@link TuscanyRequestFilter}
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        // todo do we actually need to bind the module context to the thread to fire this event?
        ModuleContext oldContext = CurrentModuleContext.getContext();
        try {
            ContextBinder.BINDER.setContext((ModuleContext) moduleContext);
            moduleContext.publish(new HttpSessionEnd(this, event.getSession()));
        } finally{
            ContextBinder.BINDER.setContext(oldContext);
        }
    }

    private void bootRuntime(String name, String uri, MonitorFactory monitorFactory) throws ConfigurationException {

        // Create an assembly model context
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        AssemblyContext modelContext = BootstrapHelper.getModelContext(classLoader);

        // Create a runtime context and start it
        ContextFactoryBuilderRegistry builderRegistry = BootstrapHelper.bootstrapContextFactoryBuilders(monitorFactory);
        runtime = new RuntimeContextImpl(monitorFactory, builderRegistry, new DefaultWireBuilder());
        runtime.start();

        // Load and start the system configuration
        SystemCompositeContext systemContext = runtime.getSystemContext();
        BootstrapHelper.bootstrapStaxLoader(systemContext, modelContext);
        ModuleComponentConfigurationLoader loader = BootstrapHelper.getConfigurationLoader(systemContext, modelContext);
        ModuleComponent systemModuleComponent = loader.loadSystemModuleComponent(SYSTEM_MODULE_COMPONENT, SYSTEM_MODULE_COMPONENT);
        CompositeContext context = BootstrapHelper.registerModule(systemContext, systemModuleComponent);
        context.publish(new ModuleStart(this));

        // Load the SCDL configuration of the application module
        CompositeContext rootContext = runtime.getRootContext();
        ModuleComponent moduleComponent = loader.loadModuleComponent(name, uri);
        moduleContext = BootstrapHelper.registerModule(rootContext, moduleComponent);

        moduleContext.publish(new ModuleStart(this));
    }
}
