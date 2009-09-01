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

package org.apache.tuscany.sca.binding.gdata;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gdata.client.Query;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;

public class GoogleCalendarServiceTestCase extends TestCase{

    private SCADomain scaDomainConsumer = null;
    private CustomerClient testService = null;    
    
    public GoogleCalendarServiceTestCase(){

    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        System.out.println("Method Test Start-----------------------------------------------------------------------");
        
        //Initialize the GData client service (Reference Binding test)
        scaDomainConsumer = SCADomain.newInstance("org/apache/tuscany/sca/binding/gdata/ConsumerGoogleCalendar.composite");
        testService = scaDomainConsumer.getService(CustomerClient.class, "CustomerClient");  
    }

    @After
    @Override
    public void tearDown(){
        System.out.println("Method Test End------------------------------------------------------------------------");
        System.out.println("\n\n");
    }        
    
    @Test
    public void testClientGetFeed() throws Exception {
        Feed feed = testService.clientGetFeed();
        System.out.println("feed title: " + feed.getTitle().getPlainText());        
        assertEquals("gsoc gosc", feed.getTitle().getPlainText());
     }
    
    
    @Test
    public void testClientGetEntry() throws Exception {
        String entryID = "1c76lcl70jg9r0fm18rcbneea8";
        Entry blogEntry = testService.clientGetEntry(entryID);
        System.out.println("Entry ID: " + blogEntry.getId());
        assertTrue(blogEntry.getId().endsWith(entryID));
        System.out.println("------------------------------------------------------------\n\n");
    }
    
    
    @Test
    public void testClientPut() throws Exception {  
        String entryID = "1c76lcl70jg9r0fm18rcbneea8";          
        String newBlogEntryTitle = "updatedTitleByGoogleContactsConsumerTestCase";
        testService.clientPut(entryID, newBlogEntryTitle);      //update the title
        Thread.sleep(Constants.SLEEP_INTERVAL);            
        Entry updatedEntry = testService.clientGetEntry(entryID);         
        assertEquals(newBlogEntryTitle, updatedEntry.getTitle().getPlainText());
    }
    
    

    @Test
    public void testClientPost() throws Exception {
        String blogEntryTitle = "titleByGoogleCalendarTestcase";
        Entry newEntry = new Entry();
        newEntry.setTitle(new PlainTextConstruct(blogEntryTitle));
        newEntry.setContent(new PlainTextConstruct("contentByGoogleCalendarTestCase"));
        Entry postedEntry = testService.clientPost(newEntry);        
        assertEquals(blogEntryTitle, postedEntry.getTitle().getPlainText());
    }

    
    @Test
    public void testClientDelete() throws Exception {
        
        //This test case might fail
        //because Google blogger service has limitation on new posts allowed everyday/every hour?
        
        System.out.println("testClientDelete");
        //We create a new post, and then delete it
        Entry newEntry = new Entry();
        newEntry.setTitle(new PlainTextConstruct("calendarEntryShouldNotApear"));
        newEntry.setContent(new PlainTextConstruct("calendarByBloggerShouldNotAppear"));
        Entry postedEntry = testService.clientPost(newEntry);
        Thread.sleep(Constants.SLEEP_INTERVAL); 
        
        System.out.println("ID: " + postedEntry.getId());
        
        int idStartPosition = postedEntry.getId().lastIndexOf("/");
        String postedEntryID = postedEntry.getId().substring(idStartPosition+1);        
        System.out.println("postedEntryID: " + postedEntryID );
        
        //Before deletion
        Entry entry00 = testService.clientGetEntry(postedEntryID);
        System.out.println("Before Deleteion: " + entry00.getId());
        
        //Delete this entry
        testService.clientDelete(postedEntryID);

        //Worked: this newly posted entry did not appear in the contact list
        //But we need a Junit assertion here
        //Link:  http://haibotuscany.blogspot.com/feeds/posts/default/
        //FIXME: Need an assertion here
        //Assert(....);
    }
    
    
    @Test
    public void testClientQuery() throws Exception {
        Query myQuery = new Query(new URL("http://www.google.com/calendar/feeds/haibotuscany@gmail.com/private/full"));
        myQuery.setMaxResults(100);
        //myQuery.setUpdatedMin(startTime);
        myQuery.setUpdatedMax(DateTime.now());
        
        //Google Calendar service supports full-text search
        String queryString = "event0";
        myQuery.setFullTextQuery(queryString);
        
        Feed resultFeed = testService.clientQuery(myQuery);        
        System.out.println("Query result feed title: " + resultFeed.getTitle().getPlainText());    
        System.out.println("Query result entry number: "+ resultFeed.getEntries().size());
        //assertEquals("gdata binding tuscany test", resultFeed.getTitle().getPlainText());
     }


}
