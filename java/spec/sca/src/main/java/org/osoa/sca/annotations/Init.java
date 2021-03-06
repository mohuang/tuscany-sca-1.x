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
package org.osoa.sca.annotations;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate a method that will be called by the container when the
 * scope defined for the local service begins.
 *
 * @version $Rev$ $Date$
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Init {
    /**
     * Instructs the container when a component instance should be instantiated.
     * If true, then the component will be instantiated when its scope begins;
     * if false it will be instantiated when first referenced.
     */
    public boolean eager() default false;
}
