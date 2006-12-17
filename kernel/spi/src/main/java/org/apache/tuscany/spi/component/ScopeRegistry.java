/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.spi.component;

import org.apache.tuscany.spi.ObjectFactory;
import org.apache.tuscany.spi.model.Scope;

/**
 * Manages {@link ScopeContainer}s in the runtime
 *
 * @version $$Rev$$ $$Date$$
 */
public interface ScopeRegistry {

    /**
     * Returns the scope container for the given scope or null if one not found
     *
     * @param scope the scope
     * @return the scope container for the given scope or null if one not found
     */
    ScopeContainer getScopeContainer(Scope scope);

    <T extends ScopeContainer> void registerFactory(Scope scope, ObjectFactory<T> factory);

    void deregisterFactory(Scope scope);

}
