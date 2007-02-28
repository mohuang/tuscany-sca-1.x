package org.apache.tuscany.test.binding;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Constructor;

import org.apache.tuscany.spi.annotation.Autowire;
import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.deployer.DeploymentContext;
import org.apache.tuscany.spi.extension.LoaderExtension;
import org.apache.tuscany.spi.loader.LoaderException;
import org.apache.tuscany.spi.loader.LoaderRegistry;
import org.apache.tuscany.spi.model.ModelObject;

/**
 * @version $Rev$ $Date$
 */
public class TestSocketBindingLoader extends LoaderExtension<TestSocketBindingDefinition> {

    public static final QName BINDING_TEST = new QName(Constants.SCA_NS, "binding.socket");

    @Constructor
    public TestSocketBindingLoader(@Autowire LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return TestSocketBindingLoader.BINDING_TEST;
    }

    public TestSocketBindingDefinition load(CompositeComponent parent,
                                  ModelObject object, XMLStreamReader reader,
                                  DeploymentContext context) throws XMLStreamException, LoaderException {
        String host = reader.getAttributeValue(null, "host");
        int port;
        try {
            port = Integer.parseInt(reader.getAttributeValue(null, "port"));
        } catch (NumberFormatException e) {
            throw new LoaderException("Invalid port specified", e);
        }
        return new TestSocketBindingDefinition(host, port);
    }
}
