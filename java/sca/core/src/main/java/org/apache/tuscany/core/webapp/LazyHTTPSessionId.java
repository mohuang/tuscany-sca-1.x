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

import org.apache.tuscany.core.context.ScopeIdentifier;

import javax.servlet.http.HttpServletRequest;

/**
 * Implements a <code>ScopeIdentifier</code> for a Servlet-based transport.
 * Wraps an <code>HttpServletRequest</code> so that the session id associated
 * with the current request may be lazily retrieved by the module context - i.e.
 * if a session context or session-scoped component is not accessed, no session
 * is created.
 *
 * @version $Rev$ $Date$
 */
public class LazyHTTPSessionId implements ScopeIdentifier {

    private HttpServletRequest request;

    //----------------------------------
    // Constructors
    //----------------------------------

    public LazyHTTPSessionId(HttpServletRequest request) {
        this.request = request;
    }

    //----------------------------------
    // Methods
    //----------------------------------

    /**
     * Returns the session identifier
     *
     * @see org.apache.tuscany.core.context.ScopeIdentifier#getIdentifier()
     */
    public Object getIdentifier() {
        return request.getSession(true);
    }
}
