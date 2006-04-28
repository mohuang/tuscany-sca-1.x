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
package org.apache.tuscany.samples.bigbank.webclient.services.account;

import org.apache.tuscany.samples.bigbank.account.AccountReport;
import org.apache.tuscany.samples.bigbank.account.services.account.AccountService;
import org.osoa.sca.ServiceUnavailableException;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 */
@Service(AccountService.class)
public class AccountServiceComponentImpl implements AccountService {
    
    @Reference
    public AccountService accountService;

    /**
     *
     */
    public AccountServiceComponentImpl() {
        super();
    }

    /**
     * @see org.apache.tuscany.samples.bigbank.webclient.services.account.AccountService#getAccountReport(java.lang.String)
     */
    public AccountReport getAccountReport(String customerID) {
        try {
            return accountService.getAccountReport(customerID);
        } catch (Exception e) {
            throw new ServiceUnavailableException(e);
        }
    }

}
