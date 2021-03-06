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
package org.apache.tuscany.binding.axis2.assembly.tests.bigbank.account.services.account;


import org.apache.tuscany.binding.axis2.assembly.tests.bigbank.account.services.accountdata.AccountDataService;
import org.apache.tuscany.binding.axis2.assembly.tests.bigbank.account.services.stockquote.StockQuoteService;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

public class AccountServiceImpl implements AccountService {

    @Property
    private String currency = "USD";
    @Reference
    private AccountDataService accountDataService;
    @Reference
    private StockQuoteService stockQuoteService;

    public AccountServiceImpl() {
    }

    public AccountReport getAccountReport(String customerID) {
        return null;
    } 
    
    
    //methods to access the fields
    protected StockQuoteService getStockQuoteService() {
        return stockQuoteService;
    }
    protected AccountDataService getAccountDataService() {
        return accountDataService;
    }
    protected String getCurrency() {
        return currency;
    }

}
