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

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate the characteristics of a session.
 *
 * @version $Rev$ $Date$
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Session {
    /**
     * The maximum time that can pass between operations in a single conversation.
     * If this time is exceeded the container may end the conversation.
     */
    public String maxIdleTime() default "";

    /**
     * The maximum time that a conversation may remain active.
     * If this time is exceeded the container may end the conversation.
     */
    public String maxAge() default "";

    /**
     * If true, indicates that only the user that initiated the conversation
     * has the authority to continue it.
     */
    public boolean singlePrincipal() default false;
}
