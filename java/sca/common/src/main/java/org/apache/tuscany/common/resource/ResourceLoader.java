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
package org.apache.tuscany.common.resource;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Interface which abstracts the implementation of something that is able to
 * load resources (such as a ClassLoader). All Tuscany code should use this
 * API rather than a ClassLoader directly in order to reduce the risk of
 * memory leaks due to ClassLoader references.
 *
 * @version $Rev: 379878 $ $Date: 2006-02-22 12:45:50 -0800 (Wed, 22 Feb 2006) $
 */
public interface ResourceLoader {

    /**
     * Returns the parent resource loaders.
     *
     * @return resource loaders that are parents to this one
     */
    List<ResourceLoader> getParents();

    /**
     * Loads the class with the specified binary name.
     *
     * @param name the binary name of the class
     * @return the resulting Class object
     * @throws ClassNotFoundException if the class was not found
     * @see ClassLoader#loadClass(String)
     */
    Class<?> loadClass(String name) throws ClassNotFoundException;

    /**
     * Converts an array of bytes into a Class.
     *
     * @param bytes the bytecode for the class; must match the class file format
     * @return a Class defined from the supplied bytecode
     */
    Class<?> addClass(byte[] bytes);

    /**
     * Finds the first resource with the given name.
     * <p/>
     * Each parent is searched first (in the order returned by {@link #getParents()})
     * and the first resource located is found. If no parent returns a resource then
     * the first resource defined by this ResourceLoader is returned.
     *
     * @param name the resource name
     * @return a {@link URL} that can be used to read the resource, or null if no resource could be found
     */
    URL getResource(String name);

    /**
     * Find resources with the given name that are available from this
     * ResourceLoader or any of its parents.
     *
     * @param name the resource name
     * @return an Iterator of {@link URL} objects for the resource
     * @throws IOException if there was a problem locating the resources
     */
    Iterator<URL> getResources(String name) throws IOException;

    //FIXME this is temporary to work around classloader problems with SDO when running in Tomcat

    /**
     * Returns the underlying classloader this loader is wrapping.
     *
     * @return the underlying classloader this loader is wrapping
     */
    ClassLoader getClassLoader();

}
