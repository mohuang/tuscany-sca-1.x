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
package org.apache.tuscany.core.marshaller;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tuscany.spi.marshaller.MarshalException;
import org.apache.tuscany.spi.marshaller.ModelMarshaller;
import org.apache.tuscany.spi.model.ComponentDefinition;

/**
 * Marshaller used for marshalling and unmarshalling component definition.
 * 
 * @version $Rev$ $Date$
 *
 */
public class ComponentDefinitionMarshaller implements ModelMarshaller<ComponentDefinition<?>> {

    /**
     * Marshalls the component definition object to the specified stream writer.
     * 
     * @param modelObject Component definition object to be serialized.
     * @param writer Stream writer to which the infoset is serialized.
     * @throws MarshalException In case of any marshalling error.
     */
    public void marshall(ComponentDefinition<?> modelObject, XMLStreamWriter writer) throws MarshalException {

    }

    /**
     * Unmarshalls the component definition object from an XML stream.
     * 
     * @param reader XML stream from where the marshalled XML is read.
     * @param upconvert Whether to upconvert the object is the current runtime
     * supports a higher version of the model object.
     * @return Hydrated component definition object.
     * @throws MarshalException In case of any unmarshalling error.
     */
    public ComponentDefinition<?> unmarshall(XMLStreamReader reader, boolean upconvert) throws MarshalException {
        return null;
    }

}
