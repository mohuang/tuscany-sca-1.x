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
package loanappconversation;

import org.apache.tuscany.spi.component.TargetNotFoundException;
import org.apache.tuscany.test.SCATestCase;
import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;

public class LoanAppConversationTestCase extends SCATestCase {

    private LoanClient loanClient;

    protected void setUp() throws Exception {
        try {
            setApplicationSCDL(LoanClient.class, "META-INF/sca/default.scdl");
            super.setUp();
    
            CompositeContext context = CurrentCompositeContext.getContext();
            loanClient = context.locateService(LoanClient.class, "LoanClientComponent");
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public void test() throws Exception {
        try {
            loanClient.applyForLoan("John Doe", 1000.0f);
            System.out.println("Applied: " + loanClient.displayLoan());
            System.out.println("Loan approved: " + loanClient.isApproved());
            loanClient.cancelLoan();
            System.out.println("Sleeping to let cancel complete ...");
            Thread.sleep(500);
            System.out.println("Cancelled: " + loanClient.displayLoan());
            loanClient.closeLoan();
            try {
                System.out.println("Trying to use the closed loan in the ended conversation ...");            
                System.out.println("Closed: " + loanClient.displayLoan());
                fail("Target should not be found");
            } catch(Exception e) {
                System.out.println("Target not found as expected");            
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
