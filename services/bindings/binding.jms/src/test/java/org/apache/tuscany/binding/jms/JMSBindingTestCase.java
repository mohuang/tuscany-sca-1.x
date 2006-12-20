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
package org.apache.tuscany.binding.jms;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.activemq.broker.BrokerContainer;
import org.activemq.broker.impl.BrokerContainerImpl;
import org.activemq.store.vm.VMPersistenceAdapter;
import org.apache.tuscany.test.SCATestCase;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;
import org.xml.sax.SAXException;

public class JMSBindingTestCase extends SCATestCase {

    private IntroService introService;
    private BrokerContainer broker;

    protected static final String REQUEST_XML = "<ns:getGreetings xmlns:ns=\"http://helloworld\"><ns:name>petra</ns:name></ns:getGreetings>";
    protected static final String REPLY_XML = "<ns1:getGreetingsResponse xmlns:ns1=\"http://helloworld\"><ns1:getGreetingsReturn>Hello petra</ns1:getGreetingsReturn></ns1:getGreetingsResponse>";

    public void testJMSBinding() throws InvocationTargetException, SAXException, IOException, ParserConfigurationException {
        String reply = introService.greet("Rajith");
        assertEquals("Hello Rajith", reply);

        // TODO: the rest should be in a seperate test method but that doesn't work as you get broker conflicts
        JMSBinding binding = new JMSBinding();
        binding.setInitialContextFactoryName("org.activemq.jndi.ActiveMQInitialContextFactory");
        binding.setConnectionFactoryName("ConnectionFactory");
        binding.setJNDIProviderURL("tcp://localhost:61616");
        binding.setDestinationName("dynamicQueues/HelloworldServiceQueue");
        binding.setTimeToLive(3000);
        binding.setOperationSelectorPropertyName("scaOperationName");
        JMSTargetInvoker invoker = new JMSTargetInvoker(new SimpleJMSResourceFactory(binding), binding, "getGreetings", new DefaultOperationSelector(binding), true);

        String response = (String)invoker.invokeTarget(REQUEST_XML, (short)0);

        Diff diff = XMLUnit.compareXML(REPLY_XML, response);
        assertTrue(diff.toString(), diff.similar());
    
    }

    protected void setUp() throws Exception {
        startBroker();
        setApplicationSCDL(IntroService.class, "META-INF/sca/default.scdl");
        addExtension("jms.binding", getClass().getClassLoader().getResource("META-INF/sca/jms.system.scdl"));
        addExtension("idl.wsdl", getClass().getClassLoader().getResource("META-INF/sca/idl.wsdl.scdl"));
        addExtension("databinding.axiom", getClass().getResource("/META-INF/sca/databinding.axiom.scdl"));
        super.setUp();
        CompositeContext context = CurrentCompositeContext.getContext();
        introService = context.locateService(IntroService.class, "IntroServiceComponent");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        broker.stop();
    }

    public static void main(String[] args) {
        JMSBindingTestCase test = new JMSBindingTestCase();
        try {
            test.setUp();
            test.testJMSBinding();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBroker() throws Exception {
        broker = new BrokerContainerImpl("JMS Binding Test");
        // configure the broker
        broker.addConnector("tcp://localhost:61616");
        broker.setPersistenceAdapter(new VMPersistenceAdapter());
        broker.start();
    }
}
