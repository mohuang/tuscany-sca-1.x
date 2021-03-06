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
package org.apache.tuscany.test.helloworldws;

import helloworld.HelloWorldService;
import junit.framework.TestCase;

import org.apache.tuscany.core.client.TuscanyRuntime;
import org.apache.tuscany.core.config.ConfigurationException;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;


public class HelloWorldWebServiceTestCase extends TestCase {
    
    String getGreetings(String name) throws ConfigurationException {
        // Obtain Tuscany runtime
        TuscanyRuntime tuscany = new TuscanyRuntime("hello", null);

        // Start the runtime
        tuscany.start();

        // Obtain SCA module context.
        ModuleContext moduleContext = CurrentModuleContext.getContext();

        // Locate the HelloWorld service component and invoke it
        HelloWorldService helloworldService = (HelloWorldService) moduleContext.locateService("HelloWorldService");

        String value = helloworldService.getGreetings(name);
        
        // Stop the runtime
        tuscany.stop();
        
        return value;
    }

	public void testHelloWorldDefault() throws Exception {
		final String name= "World";
		String greeting= getGreetings(name);
		assertEquals(greeting, "Hello " + name);
		
		final String name2= "SCA World!";
		greeting= getGreetings(name2);
		assertEquals(greeting, "Hello " + name2);

	}

}
