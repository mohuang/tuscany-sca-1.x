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
package calculator;

import junit.framework.TestCase;

import org.apache.tuscany.core.client.TuscanyRuntime;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;

/**
 * This shows how to test the HelloWorld service component.
 */
public class CalculatorTestCase extends TestCase {
    
    private TuscanyRuntime tuscany;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        // Create a Tuscany runtime for the sample module component
        tuscany = new TuscanyRuntime("CalculatorModuleComponent", "http://calculator");

        // Start the Tuscany runtime and associate it with this thread
        tuscany.start();
    }
    
    public void testCalculator() throws Exception {

        // Get the SCA module context.
        ModuleContext moduleContext = CurrentModuleContext.getContext();

        // Locate the Calculator service
        CalculatorService calculatorService = (CalculatorService) moduleContext.locateService("CalculatorServiceComponent");
        
        // Calculate
        assertEquals(calculatorService.add(3, 2), 5.0);
        assertEquals(calculatorService.subtract(3, 2), 1.0);
        assertEquals(calculatorService.multiply(3, 2), 6.0);
        assertEquals(calculatorService.divide(3, 2), 1.5);

    }
    
    protected void tearDown() throws Exception {
        
        // Stop the Tuscany runtime
        tuscany.stop();
        
        super.tearDown();
    }
}
