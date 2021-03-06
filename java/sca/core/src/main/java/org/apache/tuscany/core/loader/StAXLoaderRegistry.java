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
package org.apache.tuscany.core.loader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.apache.tuscany.model.assembly.AssemblyObject;

/**
 * Registry for XML loaders that can parse a StAX input stream and return model objects.
 * <p/>
 * Loaders will typically be contributed to the system by any extension that needs to
 * handle extension specific information contained in some XML configuration file.
 * The loader can be contributed as a system component with an autowire reference
 * to this registry which is used during initialization to actually register.
 * </p>
 * This registry can also be used to parse an input stream, dispatching to the
 * appropriate loader for each element accepted. Loaders can call back to the
 * registry to load sub-elements that they are not able to handle directly.
 *
 * @version $Rev$ $Date$
 */
public interface StAXLoaderRegistry {
    /**
     * Register a loader. This operation will typically be called by a loader
     * during its initialization.
     *
     * @param element the element that should be delegated to the contibuted loader
     * @param loader  a loader that is being contributed to the system
     */
    <T extends AssemblyObject> void registerLoader(QName element, StAXElementLoader<T> loader);

    /**
     * Unregister a loader. This will typically be called by a loader as it is being destroyed.
     *
     * @param element the element that was being delegated to the contibuted loader
     * @param loader  a loader that should no longer be used
     */
    <T extends AssemblyObject> void unregisterLoader(QName element, StAXElementLoader<T> loader);

    /**
     * Parse the supplied XML stream dispatching to the appropriate registered loader
     * for each element encountered in the stream.
     * <p/>
     * This method must be called with the XML cursor positioned on a START_ELEMENT event.
     * When this method returns, the stream will be positioned on the corresponding
     * END_ELEMENT event.
     *
     * @param reader        the XML stream to parse
     * @param loaderContext
     * @return the model object obtained by parsing the current element on the stream
     * @throws XMLStreamException if there was a problem reading the stream
     */
    AssemblyObject load(XMLStreamReader reader, LoaderContext loaderContext) throws XMLStreamException, ConfigurationLoadException;

    /**
     * Hack to allow loaders to initialize model objects on the fly.
     * Remove when initialization has been moved from the model implementation to the loader.
     *
     * @return the model context for this load operation
     */
    @Deprecated
    AssemblyContext getContext();

    /**
     * Hack to allow loaders to initialize model objects on the fly.
     * Remove when initialization has been moved from the model implementation to the loader.
     *
     * @param context the model context for this load operation
     */
    @Deprecated
    void setContext(AssemblyContext context);
}
