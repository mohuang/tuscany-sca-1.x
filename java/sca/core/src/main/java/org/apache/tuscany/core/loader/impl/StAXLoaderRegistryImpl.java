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
package org.apache.tuscany.core.loader.impl;

import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.core.loader.StAXElementLoader;
import org.apache.tuscany.core.loader.StAXLoaderRegistry;
import org.apache.tuscany.core.loader.LoaderContext;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.AssemblyObject;
import org.osoa.sca.annotations.Scope;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
@Scope("MODULE")
public class StAXLoaderRegistryImpl implements StAXLoaderRegistry {
    private final Map<QName, StAXElementLoader<? extends AssemblyObject>> loaders = new HashMap<QName, StAXElementLoader<? extends AssemblyObject>>();

    private Monitor monitor;

    @org.apache.tuscany.core.system.annotation.Monitor
    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public <T extends AssemblyObject> void registerLoader(QName element, StAXElementLoader<T> loader) {
        monitor.registeringLoader(element);
        loaders.put(element, loader);
    }

    public <T extends AssemblyObject> void unregisterLoader(QName element, StAXElementLoader<T> loader) {
        monitor.unregisteringLoader(element);
        loaders.remove(element);
    }

    public AssemblyObject load(XMLStreamReader reader, LoaderContext loaderContext) throws XMLStreamException, ConfigurationLoadException {
        QName name = reader.getName();
        monitor.elementLoad(name);
        StAXElementLoader<? extends AssemblyObject> loader = loaders.get(name);
        if (loader == null) {
            ConfigurationLoadException e = new ConfigurationLoadException("Unrecognized element");
            e.setIdentifier(name.toString());
            throw e;
        } else {
            return loader.load(reader, loaderContext);
        }
    }


    private final ThreadLocal<AssemblyContext> modelContext = new ThreadLocal<AssemblyContext>();

    @Deprecated
    public AssemblyContext getContext() {
        return modelContext.get();
    }

    @Deprecated
    public void setContext(AssemblyContext context) {
        modelContext.set(context);
    }

    public static interface Monitor {
        /**
         * Event emitted when a StAX element loader is registered.
         *
         * @param xmlType the QName of the element the loader will handle
         */
        void registeringLoader(QName xmlType);

        /**
         * Event emitted when a StAX element loader is unregistered.
         *
         * @param xmlType the QName of the element the loader will handle
         */
        void unregisteringLoader(QName xmlType);

        /**
         * Event emitted when a request is made to load an element.
         *
         * @param xmlType the QName of the element that should be loaded
         */
        void elementLoad(QName xmlType);
    }
}
