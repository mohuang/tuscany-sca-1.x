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
package org.apache.tuscany.core.loader.assembly;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.core.loader.LoaderContext;
import org.apache.tuscany.model.assembly.Wire;
import org.osoa.sca.annotations.Scope;

/**
 * @version $Rev$ $Date$
 */
@Scope("MODULE")
public class WireLoader extends AbstractLoader {
    private static final String XSD = "http://www.w3.org/2001/XMLSchema";

    private static final Map<QName, Class<?>> TYPE_MAP;

    static {
        // todo support more XSD types, or remove if we store the QName
        TYPE_MAP = new HashMap<QName, Class<?>>(17);
        TYPE_MAP.put(new QName(XSD, "string"), String.class);
    }

    public QName getXMLType() {
        return AssemblyConstants.WIRE;
    }

    public Wire load(XMLStreamReader reader, LoaderContext loaderContext) throws XMLStreamException, ConfigurationLoadException {
        assert AssemblyConstants.WIRE.equals(reader.getName());
        Wire wire = factory.createWire();
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (AssemblyConstants.WIRE_SOURCE.equals(qname)) {
                        String uri = reader.getElementText();
                        int pos = uri.indexOf('/');
                        if (pos < 1) {
                            throw new ConfigurationLoadException("Invalid source wire");
                        }
                        String partName = uri.substring(0, pos);
                        String portName = uri.substring(pos + 1);
                        wire.setSource(factory.createServiceURI(null, partName, portName));
                    } else if (AssemblyConstants.WIRE_TARGET.equals(qname)) {
                        String uri = reader.getElementText();
                        int pos = uri.indexOf('/');
                        if (pos < 1) {
                            wire.setTarget(factory.createServiceURI(null, uri));
                        }else{
                            String partName = uri.substring(0, pos);
                            String portName = uri.substring(pos + 1);
                            wire.setTarget(factory.createServiceURI(null, partName, portName));
                        }
                    }
                    break;
                case END_ELEMENT:
                    return wire;
            }
        }
    }
}
