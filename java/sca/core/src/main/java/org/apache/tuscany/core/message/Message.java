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
package org.apache.tuscany.core.message;

import org.apache.tuscany.core.wire.MessageChannel;
import org.apache.tuscany.core.wire.TargetInvoker;

/**
 * Represents a request, response, or exception flowing through a wire
 *
 * @version $Rev $Date
 */
public interface Message {

    /**
     * Returns the body of the message, which will be the payload or parameters associated with the wire
     */
    Object getBody();

    /**
     * Sets the body of the message.
     */
    void setBody(Object body);

    /**
     * Sets the target invoker to dispatch to when the message passes through the request side of the invocation chain
     */
    public void setTargetInvoker(TargetInvoker invoker);

    /**
     * Sets the target invoker to dispatch to when the message passes through the request side of the invocation chain
     */
    public TargetInvoker getTargetInvoker();

    /**
     * Returns the callback channel
     */
    public MessageChannel getCallbackChannel();

    /**
     * 
     */
    public Message getRelatedCallbackMessage();
}
