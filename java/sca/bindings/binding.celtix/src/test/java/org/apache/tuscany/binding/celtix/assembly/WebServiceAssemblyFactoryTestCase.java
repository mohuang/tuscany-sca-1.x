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
package org.apache.tuscany.binding.celtix.assembly;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.framework.TestCase;

import org.apache.tuscany.binding.celtix.assembly.impl.WebServiceAssemblyFactoryImpl;
import org.apache.tuscany.binding.celtix.assembly.impl.WebServiceBindingImpl;
import org.apache.tuscany.core.loader.WSDLDefinitionRegistry;
import org.apache.tuscany.model.assembly.AssemblyContext;
import org.easymock.EasyMock;

public class WebServiceAssemblyFactoryTestCase extends TestCase {

    
    private void setupMocks(WSDLDefinitionRegistry reg,
                            List<Definition> wsdlList) {
        EasyMock.reset(new Object[] {reg});
        
        //FIXME pass the current ResourceLoader
        reg.getDefinitionsForNamespace("http://objectweb.org/hello_world_soap_http", null);
        EasyMock.expectLastCall().andReturn(wsdlList);
        
        EasyMock.replay(new Object[] {reg});
    }
    
    public void testCreate() throws Exception {
        WSDLDefinitionRegistry reg = EasyMock.createNiceMock(WSDLDefinitionRegistry.class);
        
        WebServiceAssemblyFactoryImpl impl = new WebServiceAssemblyFactoryImpl();
        WebServiceBinding bind = impl.createWebServiceBinding(reg);
        assertNotNull("Did not create the binding", bind);
        assertTrue("bind object wrong class: " + bind.getClass(),
                   bind instanceof WebServiceBindingImpl);
        
        assertNull("Should be initialized with null WSDL", bind.getWSDLDefinition());
        assertNull("Should be initialized with null port", bind.getWSDLPort());
        assertNull("Should be initialized with null service", bind.getWSDLService());
        assertNull("Should be initialized with null URI", bind.getURI());
        assertNull("Should be initialized with null TypeHelper",
                   bind.getTypeHelper());
        assertNull("Should be initialized with null ResourceLoader",
                   bind.getResourceLoader());
        
        bind.setURI("http://objectweb.org/hello_world_soap_http");
        bind.setPortURI("http://objectweb.org/hello_world_soap_http#SoapPort");
        
        AssemblyContext modelContext = EasyMock.createNiceMock(AssemblyContext.class);
        
        WSDLReader reader =  WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        Definition definition = reader.readWSDL(url.toString());

        List<Definition> wsdlList = new ArrayList<Definition>();
        
        setupMocks(reg, wsdlList);
        try {
            bind.initialize(modelContext);
            fail("Should have failed getting the wsdl");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        
        setupMocks(reg, wsdlList);

        
        wsdlList.add(definition);
        bind = impl.createWebServiceBinding(reg);
        bind.setURI("http://objectweb.org/hello_world_soap_http");
        bind.setPortURI("http://objectweb.org/hello_world_soap_http#SoapPort");
        bind.initialize(modelContext);
    
        setupMocks(reg, wsdlList);
        
        wsdlList.add(definition);
        bind = impl.createWebServiceBinding(reg);
        bind.setURI("http://objectweb.org/hello_world_soap_http");
        bind.setPortURI("http://objectweb.org/hello_world_soap_http#FooPort");
        
        try {
            bind.initialize(modelContext);
            fail("Should have failed finding the port");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }
}
