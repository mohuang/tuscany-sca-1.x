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
package org.apache.tuscany.model.assembly;

import org.apache.tuscany.common.resource.ResourceLoader;
import org.apache.tuscany.model.assembly.loader.AssemblyModelLoader;

import commonj.sdo.helper.TypeHelper;

/**
 * Context object supplied by visitors that are processing the model.
 */
public interface AssemblyContext {

    /**
     * Returns a factory that can be used to create other model objects
     *
     * @return a factory for model objects
     */
    AssemblyFactory getAssemblyFactory();

    /**
     * Returns a loader for resources in the application environment.
     *
     * @return a loader for resources in the system environment
     */
    ResourceLoader getApplicationResourceLoader();

    /**
     * Returns a loader that can be used to load sub-models.
     *
     * @return a loader for sub-models
     */
    AssemblyModelLoader getAssemblyLoader();

    /**
     * Returns an SDO type helper.
     * 
     * @return an SDO type helper
     */
    TypeHelper getTypeHelper();
    
    /**
     * Returns the Web application module URI
     * 
     * @return the module name of the Web app
     */
    String getWebAppName();
    
}
