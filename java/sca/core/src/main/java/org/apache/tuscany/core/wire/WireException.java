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
package org.apache.tuscany.core.wire;

import org.apache.tuscany.common.TuscanyException;

/**
 * Denotes a top-level exception dealing with a wire
 */
public abstract class WireException extends TuscanyException {

    public WireException() {
        super();
    }

    public WireException(String message) {
        super(message);
    }

    public WireException(String message, Throwable cause) {
        super(message, cause);
    }

    public WireException(Throwable cause) {
        super(cause);
    }

}

