package org.apache.tuscany.core.loader;

import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.apache.tuscany.spi.implementation.java.PojoComponentType;
import org.apache.tuscany.spi.loader.InvalidReferenceException;
import org.apache.tuscany.spi.loader.LoaderException;
import org.apache.tuscany.spi.loader.LoaderRegistry;
import org.apache.tuscany.spi.loader.MissingReferenceException;
import org.apache.tuscany.spi.loader.PropertyObjectFactory;
import org.apache.tuscany.spi.model.ComponentDefinition;
import org.apache.tuscany.spi.model.Implementation;
import org.apache.tuscany.spi.model.Multiplicity;
import org.apache.tuscany.spi.model.Property;
import org.apache.tuscany.spi.model.ReferenceDefinition;
import org.apache.tuscany.spi.model.ServiceDefinition;
import org.apache.tuscany.spi.model.ReferenceTarget;

import junit.framework.TestCase;
import org.apache.tuscany.core.implementation.java.JavaImplementation;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class ComponentLoaderValidationTestCase extends TestCase {

    private ComponentLoaderValidationTestCase.TestLoader loader;

    public void testValidation() throws LoaderException, XMLStreamException {
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ReferenceDefinition refDefinition = new ReferenceDefinition();
        refDefinition.setName("name");
        type.add(refDefinition);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<Implementation<?>> defn = new ComponentDefinition<Implementation<?>>(impl);
        ReferenceTarget target = new ReferenceTarget();
        target.setReferenceName("name");
        defn.add(target);
        loader.validate(defn);
    }

    public void testReferenceNotSet() throws LoaderException, XMLStreamException {
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ReferenceDefinition refDefinition = new ReferenceDefinition();
        refDefinition.setName("name");
        refDefinition.setRequired(true);
        type.add(refDefinition);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<Implementation<?>> defn = new ComponentDefinition<Implementation<?>>(impl);
        try {
            loader.validate(defn);
            fail();
        } catch (MissingReferenceException e) {
            // expected
        }
    }

    public void testNotRequiredReference() throws LoaderException, XMLStreamException {
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ReferenceDefinition refDefinition = new ReferenceDefinition();
        refDefinition.setName("name");
        refDefinition.setRequired(false);
        type.add(refDefinition);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<Implementation<?>> defn = new ComponentDefinition<Implementation<?>>(impl);
        loader.validate(defn);
    }

    public void testReferenceMultiplicity() throws LoaderException, XMLStreamException {
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ReferenceDefinition refDefinition = new ReferenceDefinition();
        refDefinition.setName("r1");
        refDefinition.setRequired(true);
        refDefinition.setMultiplicity(Multiplicity.ONE_N);
        type.add(refDefinition);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<Implementation<?>> defn = new ComponentDefinition<Implementation<?>>(impl);
        ReferenceTarget target = new ReferenceTarget();
        target.setReferenceName("r1");
        target.addTarget(URI.create("c1"));
        target.addTarget(URI.create("c2"));
        defn.add(target);
        loader.validate(defn);

        refDefinition.setMultiplicity(Multiplicity.ZERO_ONE);
        try {
            loader.validate(defn);
            fail();
        } catch (InvalidReferenceException e) {
            // Expected
        }

        refDefinition.setMultiplicity(Multiplicity.ZERO_N);
        loader.validate(defn);

        refDefinition.setMultiplicity(Multiplicity.ONE_ONE);
        try {
            loader.validate(defn);
            fail();
        } catch (InvalidReferenceException e) {
            // Expected
        }

        target.getTargets().clear();
        refDefinition.setMultiplicity(Multiplicity.ONE_ONE);
        try {
            loader.validate(defn);
            fail();
        } catch (InvalidReferenceException e) {
            // Expected
        }
        refDefinition.setMultiplicity(Multiplicity.ONE_N);
        try {
            loader.validate(defn);
            fail();
        } catch (InvalidReferenceException e) {
            // Expected
        }
        refDefinition.setMultiplicity(Multiplicity.ZERO_N);
        loader.validate(defn);
        refDefinition.setMultiplicity(Multiplicity.ZERO_ONE);
        loader.validate(defn);

    }    
    public void testAutowire() throws LoaderException, XMLStreamException {
        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ReferenceDefinition refDefinition = new ReferenceDefinition();
        refDefinition.setName("name");
        refDefinition.setAutowire(true);
        type.add(refDefinition);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<Implementation<?>> defn = new ComponentDefinition<Implementation<?>>(impl);
        loader.validate(defn);
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        PropertyObjectFactory mockPropertyFactory = EasyMock.createMock(PropertyObjectFactory.class);
        loader = new ComponentLoaderValidationTestCase.TestLoader(mockRegistry, mockPropertyFactory);
    }

    private class TestLoader extends ComponentLoader {

        public TestLoader(LoaderRegistry registry, PropertyObjectFactory propertyFactory) {
            super(registry, propertyFactory);
        }

        @Override
        protected void validate(ComponentDefinition<Implementation<?>> definition) throws LoaderException {
            super.validate(definition);
        }
    }
}
