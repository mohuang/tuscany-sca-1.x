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
package org.apache.tuscany.binding.axis2.config;

import org.apache.tuscany.core.builder.ObjectFactory;
import org.apache.tuscany.core.extension.ExternalServiceContextFactory;

/**
 * Creates instances of {@link org.apache.tuscany.core.context.ExternalServiceContext} configured with the appropriate wire chains and bindings. This
 * implementation serves as a marker for {@link org.apache.tuscany.binding.axis2.builder.ExternalWebServiceWireBuilder}
 * 
 * @version $Rev$ $Date$
 */
public class WSExternalServiceContextFactory extends ExternalServiceContextFactory {

    public WSExternalServiceContextFactory(String name, ObjectFactory objectFactory) {
        super(name, objectFactory);
    }

}
