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

package org.apache.tuscany.core.implementation.composite;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tuscany.spi.implementation.java.PojoComponentType;
import org.apache.tuscany.spi.loader.InvalidWireException;
import org.apache.tuscany.spi.model.Binding;
import org.apache.tuscany.spi.model.BindlessServiceDefinition;
import org.apache.tuscany.spi.model.BoundServiceDefinition;
import org.apache.tuscany.spi.model.ComponentDefinition;
import org.apache.tuscany.spi.model.CompositeComponentType;
import org.apache.tuscany.spi.model.Property;
import org.apache.tuscany.spi.model.ReferenceDefinition;
import org.apache.tuscany.spi.model.ServiceDefinition;
import org.apache.tuscany.spi.model.WireDefinition;

import junit.framework.TestCase;
import org.apache.tuscany.core.implementation.java.JavaImplementation;

/**
 * This class tests the wire resolution function of the composite loader
 */
public class CompositeLoaderWireResolutionTestCase extends TestCase {
    private CompositeComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> componentType;
    private CompositeLoader compositeLoader = new CompositeLoader(null, null);

    public void setUp() throws Exception {
        componentType = new CompositeComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        componentType.setName("TestComposite");
        //add a service to the composite
        ServiceDefinition serviceDefn = new ServiceDefinition("compositeService1", null, true);
        BindlessServiceDefinition bindlessSvcDefn = new BindlessServiceDefinition("bindlessSvc", null, true, null);
        BoundServiceDefinition<Binding> boundSvcDefn =
            new BoundServiceDefinition<Binding>("boundSvc", null, true, null, null);
        BoundServiceDefinition<Binding> boundSvcDefnWithTarget =
            new BoundServiceDefinition<Binding>("boundSvcWithTarget", null, true, null, new URI("orgTarget"));
        componentType.add(serviceDefn);
        componentType.add(boundSvcDefn);
        componentType.add(bindlessSvcDefn);
        componentType.add(boundSvcDefnWithTarget);

        ReferenceDefinition compositeReference = new ReferenceDefinition("compositeReference", null);
        componentType.add(compositeReference);

        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> pojoComponentType1 =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ServiceDefinition pojoSvc1 = new ServiceDefinition("pojoSvc1", null, false);
        pojoComponentType1.add(pojoSvc1);
        ReferenceDefinition pojoRef1 = new ReferenceDefinition("pojoRef1", null);
        pojoComponentType1.add(pojoRef1);
        JavaImplementation pojoImpl1 = new JavaImplementation();
        pojoImpl1.setComponentType(pojoComponentType1);

        ComponentDefinition<JavaImplementation> component1 =
            new ComponentDefinition<JavaImplementation>("Component1", pojoImpl1);
        componentType.add(component1);

        PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> pojoComponentType2 =
            new PojoComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        ServiceDefinition pojoSvc2 = new ServiceDefinition("pojoSvc2", null, false);
        pojoComponentType2.add(pojoSvc2);
        ServiceDefinition pojoSvc3 = new ServiceDefinition("pojoSvc3", null, false);
        pojoComponentType2.add(pojoSvc3);
        ReferenceDefinition pojoRef2 = new ReferenceDefinition("pojoRef2", null);
        pojoComponentType2.add(pojoRef2);
        ReferenceDefinition pojoRef3 = new ReferenceDefinition("pojoRef3", null);
        pojoComponentType2.add(pojoRef3);
        JavaImplementation pojoImpl2 = new JavaImplementation();
        pojoImpl2.setComponentType(pojoComponentType2);

        ComponentDefinition<JavaImplementation> component2 =
            new ComponentDefinition<JavaImplementation>("Component2", pojoImpl2);
        componentType.add(component2);
    }

    public void testCompositeSvc2CompositeReferenceWire() throws Exception {
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component1"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentQualifiedValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2/pojoSvc3"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2/pojoSvc5"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testCompositeSvc2ComponentUnQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testComponent2CompositeReferenceValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2CompositeReferenceQualifiedValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component2/pojoRef3"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2CompositeReferenceUnQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component2"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);

        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testComponent2ComponentQualifedValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1"));
        wireDefn.setTarget(new URI("Component2/pojoSvc3"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2ComponentUnQualifedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1"));
        wireDefn.setTarget(new URI("Component2"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testInvalidWireDefinitions() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("undefinedSource"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);

        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }

        try {
            wireDefn.setSource(new URI("compositeService1"));
            wireDefn.setTarget(new URI("undefinedTarget"));
            componentType.add(wireDefn);
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }
}
