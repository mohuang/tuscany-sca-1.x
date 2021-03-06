/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.tuscany.core.loader;

import org.apache.tuscany.core.builder.ObjectFactory;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.model.assembly.Property;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A factory that will create an ObjectFactory for a property by parsing a StAX XMLStreamReader.
 *
 * @version $Rev$ $Date$
 */
public interface StAXPropertyFactory<T> {
    /**
     * Return an ObjectFactory for instances of a property defined in an XML stream.
     *
     * @param reader the reader to use to access the XML stream
     * @param property the Property definition that the resulting ObjectFactory must be able to assign to
     * @return an ObjectFactory that can produce instances that can be assigned to the supplied Property
     * @throws XMLStreamException if there is a problem reading the stream
     * @throws ConfigurationLoadException if there is a problem creating the ObjectFactory
     */
    ObjectFactory<T> createObjectFactory(XMLStreamReader reader, Property property) throws XMLStreamException, ConfigurationLoadException;
}
