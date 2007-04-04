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
package org.apache.tuscany.services.spi.contribution;

import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of an artifact processor registry.
 * 
 * @version $Rev$ $Date$
 */
abstract class DefaultArtifactProcessorRegistry<S, M, K> implements ArtifactProcessorRegistry<S, M, K> {
    private final Map<K, ArtifactProcessor<S, M, K>> processorsByArtifactType = new HashMap<K, ArtifactProcessor<S, M, K>>();
    private final Map<Class<?>, ArtifactProcessor<S, M, K>> processorsByModelType = new HashMap<Class<?>, ArtifactProcessor<S, M, K>>();

    /**
     * Constructs a new loader registry.
     */
    public DefaultArtifactProcessorRegistry() {
    }

    /**
     * Returns the processor associated with the given artifact type.
     * @param artifactType an artifact type
     * @return the processor associated with the given artifact type
     */
    protected ArtifactProcessor<S, M, K> getProcessor(K artifactType) {
        return processorsByArtifactType.get(artifactType);
    }

    /**
     * Returns the processor associated with the given model type.
     * @param modelType a model type
     * @return the processor associated with the given model type
     */
    protected ArtifactProcessor<S, M, K> getProcessor(Class<M> modelType) {
        Class<?>[] classes = modelType.getClasses();
        for (Class<?> c: classes) {
            ArtifactProcessor<S, M, K> processor = processorsByModelType.get(c);
            if (processor != null)
                return processor;
        }
        return null;
    }

    public void addArtifactProcessor(ArtifactProcessor<S, M, K> artifactProcessor) {
        processorsByArtifactType.put(artifactProcessor.getArtifactType(), artifactProcessor);
    }
    
    public K getArtifactType() {
        // Will never match
        return null;
    }
    
    public Class<M> getModelType() {
        // Will never match
        return null;
    }
}
