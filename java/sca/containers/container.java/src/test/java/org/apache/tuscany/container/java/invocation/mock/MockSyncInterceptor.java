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
package org.apache.tuscany.container.java.invocation.mock;

import org.apache.tuscany.core.wire.Interceptor;
import org.apache.tuscany.core.message.Message;

public class MockSyncInterceptor implements Interceptor {

    private int count;

    private Interceptor next;

    public MockSyncInterceptor() {
    }

    public Message invoke(Message msg) {
        ++count;
        //System.out.println("Invoking interceptor");
        return next.invoke(msg);
    }

    public int getCount() {
        return count;
    }
    
    public void setNext(Interceptor next) {
        this.next=next;
    }
}

