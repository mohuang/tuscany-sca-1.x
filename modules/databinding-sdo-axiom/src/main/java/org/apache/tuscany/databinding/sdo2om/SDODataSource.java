package org.apache.tuscany.databinding.sdo2om;

import static org.apache.tuscany.databinding.sdo.SDODataBinding.ROOT_ELEMENT;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.tuscany.sdo.helper.XMLStreamHelper;
import org.apache.tuscany.sdo.util.SDOUtil;

import commonj.sdo.DataObject;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

public class SDODataSource implements OMDataSource {
    private HelperContext helperContext;
    private XMLDocument sourceDocument;

    public SDODataSource(XMLDocument source, HelperContext helperContext) {
        this.sourceDocument = source;
        this.helperContext = helperContext;
    }

    public SDODataSource(DataObject obj, HelperContext helperContext) {
        this.helperContext = helperContext;
        this.sourceDocument =
            helperContext.getXMLHelper().createDocument(obj,
                                                        ROOT_ELEMENT.getNamespaceURI(),
                                                        ROOT_ELEMENT.getLocalPart());
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        XMLStreamHelper streamHelper = SDOUtil.createXMLStreamHelper(helperContext.getTypeHelper());
        return streamHelper.createXMLStreamReader(sourceDocument);
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        StreamingOMSerializer serializer = new StreamingOMSerializer();
        serializer.serialize(getReader(), xmlWriter);
    }

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        try {
            helperContext.getXMLHelper().save(sourceDocument, output, null);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        try {
            helperContext.getXMLHelper().save(sourceDocument, writer, null);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

}
