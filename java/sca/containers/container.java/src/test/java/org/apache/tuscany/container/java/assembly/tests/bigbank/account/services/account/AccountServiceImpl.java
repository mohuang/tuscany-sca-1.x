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
package org.apache.tuscany.container.java.assembly.tests.bigbank.account.services.account;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.apache.tuscany.container.java.assembly.tests.bigbank.account.services.accountdata.AccountDataService;
import org.apache.tuscany.container.java.assembly.tests.bigbank.account.services.stockquote.StockQuoteService;

public class AccountServiceImpl implements AccountService {

    @Property
    public String currency = "USD";

    @Reference
    public AccountDataService accountDataService;
    @Reference
    public StockQuoteService stockQuoteService;

    public AccountServiceImpl() {
    }

    public AccountReport getAccountReport(String customerID) {
        return null;
    }

}
