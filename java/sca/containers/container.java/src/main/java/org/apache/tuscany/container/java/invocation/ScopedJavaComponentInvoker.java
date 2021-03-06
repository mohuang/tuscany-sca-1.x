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
package org.apache.tuscany.container.java.invocation;

import org.apache.tuscany.core.context.QualifiedName;
import org.apache.tuscany.core.context.ScopeContext;
import org.apache.tuscany.core.context.TargetException;

import java.lang.reflect.Method;

/**
 * Uses a scope container to resolve an implementation instance based on the current thread context
 *
 * @version $Rev$ $Date$
 */
public class ScopedJavaComponentInvoker extends AbstractJavaComponentInvoker {

    private ScopeContext container;

    private QualifiedName name;

    private Object target;

    public boolean cacheable;


    /**
     * Creates a new invoker
     *
     * @param serviceName the name of the component/service pair to invoke
     * @param operation   the operation the invoker is associated with
     * @param scopeContext the scope context the component is resolved in
     * @param cacheable   Sets whether the target service instance may be cached by the invoker. This is a possible optimization
     *                    when a wire is configured for a "down-scope" reference, i.e. a reference from a source of a shorter
     *                    lifetime to a source of greater lifetime.
     */
    public ScopedJavaComponentInvoker(QualifiedName serviceName, Method operation, ScopeContext scopeContext, boolean cacheable) {
        super(operation);
        assert (serviceName != null) : "No service name specified";
        assert (scopeContext != null) : "No scope scopeContext specified";
        name = serviceName;
        this.container = scopeContext;
        this.cacheable = cacheable;
    }

    /**
     * Returns whether the target is cacheable.
     */
    public boolean isCacheable() {
        return cacheable;
    }

    /**
     * Resolves the target service instance or returns a cached one
     */
    protected Object getInstance() throws TargetException {
        if (!cacheable) {
            return container.getInstance(name);
        } else {
            if (target == null) {
                target = container.getInstance(name);
            }
            return target;
        }
    }

    public Object clone() throws CloneNotSupportedException {
        ScopedJavaComponentInvoker invoker = (ScopedJavaComponentInvoker) super.clone();
        invoker.target = null;
        invoker.cacheable = this.cacheable;
        invoker.container = this.container;
        invoker.name = this.name;
        return invoker;
    }
}
