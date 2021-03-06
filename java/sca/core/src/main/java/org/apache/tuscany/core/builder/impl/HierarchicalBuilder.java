/**
 * 
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.core.builder.impl;

import org.apache.tuscany.core.builder.BuilderException;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.model.assembly.AssemblyObject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A builder that contains nested builders. Used for synchronizing parts of the buildSource process, such as references.
 * 
 * @version $Rev$ $Date$
 */
public class HierarchicalBuilder implements ContextFactoryBuilder {
    private List<ContextFactoryBuilder> builders = new CopyOnWriteArrayList<ContextFactoryBuilder>();
    
    private List<ContextFactoryBuilder> readOnlyBuilders = Collections.unmodifiableList(builders); 
    
    public HierarchicalBuilder() {
    }

    public void addBuilder(ContextFactoryBuilder builder) {
        builders.add(builder);
    }

    public void removeBuilder(ContextFactoryBuilder builder){
        builders.remove(builder);
    }
    
    public List getBuilders(){
        return readOnlyBuilders;
    }
    
    public void build(AssemblyObject object) throws BuilderException {
        for (ContextFactoryBuilder builder : builders) {
            builder.build(object);
        }

    }

    
}
