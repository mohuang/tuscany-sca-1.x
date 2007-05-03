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
package org.apache.tuscany.core;

/**
 * Acts as a delegating <code>WireProcessorExtensionPoint</code>, delegating
 * processing of wires after policies have been applied and source an targets
 * have been connected.
 * 
 * @version $Rev$ $Date$
 */
public interface WireProcessorExtensionPoint extends RuntimeWireProcessorExtension {

    /**
     * Registers a wire-processor in the runtime
     * 
     * @param processor the processor to register
     */
    void register(RuntimeWireProcessorExtension processor);

    /**
     * De-registers a wire-processor in the runtime
     * 
     * @param processor the processor to de-register
     */
    void unregister(RuntimeWireProcessorExtension processor);

}
