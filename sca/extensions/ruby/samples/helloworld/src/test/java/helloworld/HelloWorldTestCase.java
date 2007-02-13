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
package helloworld;

import java.net.URL;

import org.apache.tuscany.test.SCATestCase;
import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;

/**
 * This shows how to test the HelloWorld service component.
 */
public class HelloWorldTestCase extends SCATestCase {

    private HelloWorldService helloWorldService;

    protected void setUp() throws Exception {
        URL scdl = getClass().getClassLoader().getResource("META-INF/sca/container.ruby.scdl");
        addExtension("RubyContainer", scdl);
        setApplicationSCDL(HelloWorldService.class, "META-INF/sca/default.scdl");
        super.setUp();

        CompositeContext context = CurrentCompositeContext.getContext();
        helloWorldService = context.locateService(HelloWorldService.class, "HelloWorldComponent");
    }

    public void testHelloWorld() throws Exception {
        assertEquals("Hello to petra from the Ruby World!", helloWorldService.sayHello("petra"));
    }
}
