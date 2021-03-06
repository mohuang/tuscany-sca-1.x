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

import org.apache.tuscany.core.builder.ObjectFactory;
import org.apache.tuscany.core.injection.ObjectCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves targets configured in a multiplicity by delegating to object factories and returning an
 * <code>List</code> containing object instances 
 * 
 * @version $Rev$ $Date$
 */
public class ListMultiplicityObjectFactory implements ObjectFactory<List> {

    private ObjectFactory[] factories;

    public ListMultiplicityObjectFactory(List<ObjectFactory> factories) {
        assert (factories != null) : "Object factories were null";
        this.factories = factories.toArray(new ObjectFactory[factories.size()]);
    }

    public List getInstance() throws ObjectCreationException {
        List<Object> list = new ArrayList<Object>();
        for (ObjectFactory factory : factories) {
            list.add(factory.getInstance());
        }
        return list;
    }

}
