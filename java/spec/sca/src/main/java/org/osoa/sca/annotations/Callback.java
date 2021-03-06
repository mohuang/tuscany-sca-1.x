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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * When placed on a service interface, this annotation specifies the interface
 * to be used for callbacks.
 * <p/>
 * When placed on a method or field, this annotation denotes the injection
 * site to be used for a callback reference.
 *
 * @version $Rev$ $Date$
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface Callback {
    /**
     * The Class of the callback interface.
     */
    Class<?> value() default Void.class;
}
