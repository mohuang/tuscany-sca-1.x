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


/**
 * The binding of an entry point or external service to a transport.
 * This model object will typically be extended by binding implementations to allow
 * specification of binding/transport specific information.
 */
public interface Binding extends AssemblyObject {
    /**
     * Returns the binding URI.
     * @return the binding uri
     * TODO do we need this?
     */
    String getURI();

    /**
     * Sets binding URI.
     * @param value the binding uri
     * TODO do we need this?
     */
    void setURI(String value);

}
