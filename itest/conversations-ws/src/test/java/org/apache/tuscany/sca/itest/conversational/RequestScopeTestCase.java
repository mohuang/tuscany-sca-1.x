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

package org.apache.tuscany.sca.itest.conversational;

import junit.framework.Assert;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatefulImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatefulNonConversationalCallbackImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalClientStatelessImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceRequestImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceStatefulImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceStatefulNonConversationalCallbackImpl;
import org.apache.tuscany.sca.itest.conversational.impl.ConversationalServiceStatelessImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RequestScopeTestCase {

    private static SCADomain domain;
    private static ConversationalClient conversationalStatelessClientStatelessService;
    private static ConversationalClient conversationalStatelessClientStatefulService;
    private static ConversationalClient conversationalStatefulClientStatelessService;
    private static ConversationalClient conversationalStatefulClientStatefulService; 
    private static ConversationalClient conversationalStatelessClientRequestService;
    private static ConversationalClient conversationalStatefulClientNonConversationalCallbackStatelessService;    

    @BeforeClass
    public static void setUp() throws Exception {
    	try {
	        domain = SCADomain.newInstance("conversational.composite");
	
	        conversationalStatelessClientStatelessService = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatelessClientStatelessService");
	
	        conversationalStatelessClientStatefulService  = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatelessClientStatefulService");
	
	        conversationalStatefulClientStatelessService  = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatefulClientStatelessService");
	
	        conversationalStatefulClientStatefulService   = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatefulClientStatefulService");
	        conversationalStatelessClientRequestService    = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatelessClientRequestService");
	        conversationalStatefulClientNonConversationalCallbackStatelessService    = domain.getService(ConversationalClient.class,
	                                                                          "ConversationalStatefulClientNonConversationalCallbackStatefulService");
	        
	        // reset the place where we record the sequence of calls passing
	        // through each component instance
	        ConversationalServiceStatelessImpl.calls = new StringBuffer();
	        ConversationalServiceStatefulImpl.calls  = new StringBuffer();
	        ConversationalClientStatelessImpl.calls  = new StringBuffer();         
	        ConversationalClientStatefulImpl.calls   = new StringBuffer();
        
    	} catch(Exception ex) {
    		System.err.println(ex.toString());
    	}
               
    }

    @AfterClass
    public static void tearDown() throws Exception {
        domain.close();
    }
    
    private static void resetCallStack() {
        
        // reset the place where we record the sequence of calls passing
        // through each component instance
        ConversationalServiceStatelessImpl.calls = new StringBuffer();
        ConversationalServiceStatefulImpl.calls  = new StringBuffer();
        ConversationalClientStatelessImpl.calls  = new StringBuffer();         
        ConversationalClientStatefulImpl.calls   = new StringBuffer();    
        ConversationalClientStatefulNonConversationalCallbackImpl.calls = new StringBuffer();
        
    }    

    // stateless client request scope service tests
    // ============================================
    @Test
    public void testStatelessRequestConversationFromInjectedReference() {
        int count = conversationalStatelessClientRequestService.runConversationFromInjectedReference();
        Assert.assertEquals(2, count);
    } 
    
    //@Test
    public void testStatelessRequestConversationFromInjectedReference2() {
        int count = conversationalStatelessClientRequestService.runConversationFromInjectedReference2();
        Assert.assertEquals(1, count);
    }     
    
    @Test
    public void testStatelessRequestConversationFromServiceReference() {
        int count = conversationalStatelessClientRequestService.runConversationFromServiceReference();
        Assert.assertEquals(2, count);
    }          
   
    @Test
    public void testStatelessRequestConversationWithUserDefinedConversationId() {
        int count = conversationalStatelessClientRequestService.runConversationWithUserDefinedConversationId();
        Assert.assertEquals(2, count);
    }   
    
    //@Test
    public void testStatelessRequestConversationCheckUserDefinedConversationId() {
        String conversationId = conversationalStatelessClientRequestService.runConversationCheckUserDefinedConversationId();
        Assert.assertEquals("MyConversation2", conversationId);
    } 
    
    //@Test
    public void testStatelessRequestConversationCheckingScope() {
        resetCallStack();
    	ConversationalServiceRequestImpl.calls = new StringBuffer();
    	conversationalStatelessClientRequestService.runConversationCheckingScope();
        Assert.assertEquals("init,initializeCount,incrementCount,retrieveCount,endConversation,", 
        		            ConversationalServiceRequestImpl.calls.toString());
        //init,initializeCount,init,incrementCount,init,retrieveCount,init,endConversation,
    }     

    @Test
    public void testStatelessRequestConversationWithCallback() {
        resetCallStack();
    	ConversationalClientStatelessImpl.calls = new StringBuffer();    	
        int count = conversationalStatelessClientRequestService.runConversationWithCallback();
        Assert.assertEquals(0, count);
               
        Assert.assertEquals("init,runConversationWithCallback,init,initializeCount,destroy,init,incrementCount,destroy,init,retrieveCount,destroy,init,endConversation,destroy,destroy,", 
                            ConversationalClientStatelessImpl.calls.toString());        
    }  
    
    //@Test
    public void testStatelessRequestConversationHavingPassedReference() {
        int count = conversationalStatelessClientRequestService.runConversationHavingPassedReference();
        Assert.assertEquals(3, count);
    }    
  
    //@Test
    public void testStatelessRequestConversationBusinessException() {
        String message = conversationalStatelessClientRequestService.runConversationBusinessException();
        Assert.assertEquals("Business Exception", message);
    }     
    
    //@Test
    public void testStatelessRequestConversationBusinessExceptionCallback() {
        String message = conversationalStatelessClientRequestService.runConversationBusinessExceptionCallback();
        Assert.assertEquals("Business Exception", message);
    }  
    
    @Test
    public void testStatelessRequestConversationCallingEndedConversation() {
        int count = conversationalStatelessClientRequestService.runConversationCallingEndedConversation();
        Assert.assertEquals(-999, count);
    }     
    
    @Test
    public void testStatelessRequestConversationCallingEndedConversationCallback() {
        int count = conversationalStatelessClientRequestService.runConversationCallingEndedConversationCallback();
        Assert.assertEquals(0, count);
    }  
    
    @Test
    public void testStatelessRequestConversationCallingEndedConversationCheckConversationId() {
        String id = conversationalStatelessClientRequestService.runConversationCallingEndedConversationCheckConversationId();
        Assert.assertEquals(null, id);
    }     
    
    //@Test
    public void testStatelessRequestConversationCallingEndedConversationCallbackCheckConversationId() {
        String id = conversationalStatelessClientRequestService.runConversationCallingEndedConversationCallbackCheckConversationId();
        Assert.assertEquals(null, id);
    }    
        
}
