/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.tuscany.tomcat;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;
import org.osoa.sca.SCA;

import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.event.HttpSessionBound;
import org.apache.tuscany.core.context.event.RequestStart;
import org.apache.tuscany.core.context.event.RequestEnd;
import org.apache.tuscany.core.webapp.LazyHTTPSessionId;

/**
 * Valve that can be added to a pipeline to automatically set the SCA environment as each request is processed.
 *
 * @version $Rev$ $Date$
 */
public class TuscanyValve extends ValveBase {
    /**
     * Name of the note that contains the request id
     */
    private static final String REQUEST_ID = "org.apache.tuscany.tomcat.REQUEST_ID";

    private static final ContextBinder BINDER = new ContextBinder();

    private CompositeContext moduleComponentContext;

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setContext(CompositeContext moduleComponentContext) {
        this.moduleComponentContext = moduleComponentContext;
    }

    public TuscanyValve(CompositeContext moduleComponentContext) {
        this.moduleComponentContext = moduleComponentContext;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (!enabled){
            return;
        }
        Object oldRequestId = request.getNote(REQUEST_ID);
        ModuleContext oldContext = CurrentModuleContext.getContext();
        // bind the current module context to the thread for use by CurrentModuleContext
        BINDER.setContext((ModuleContext) moduleComponentContext);
        try {
            if (oldRequestId != null) {
                // the request has already been started, just invoke the next valve
                next.invoke(request, response);
            } else {
                // tell the runtime a new request is starting
                Object requestId = new Object();

                HttpSession session = request.getSession(false);
                if (session != null) {
                    // A session is already active
                    moduleComponentContext .publish(new HttpSessionBound(this,session));
                } else {
                    // Create a lazy wrapper since a session is not yet active
                    moduleComponentContext.publish(new HttpSessionBound(this, new LazyHTTPSessionId(request)));
                }

                try {
                    moduleComponentContext.publish(new RequestStart(this, requestId));
                } catch (Exception e) {
                    throw new ServletException(e.getMessage(), e);
                }
                request.setNote(REQUEST_ID, requestId);

                try {
                    // invoke the next valve in the pipeline
                    next.invoke(request, response);
                } finally {
                    // notify the runtime the request is ending
                    request.removeNote(REQUEST_ID);
                    try {
                        moduleComponentContext.publish(new RequestEnd(this, requestId));
                    } catch (Exception e) {
                        // the application already did its work, log and ignore
                        // todo log this exception
                    }
                }
            }
        } finally {
            // restore the previous module context onto the thread
            BINDER.setContext(oldContext);
        }
    }

    private static class ContextBinder extends SCA {
        public void setContext(ModuleContext context) {
            setModuleContext(context);
        }

        public void start() {
            throw new AssertionError();
        }

        public void stop() {
            throw new AssertionError();
        }
    }
}
