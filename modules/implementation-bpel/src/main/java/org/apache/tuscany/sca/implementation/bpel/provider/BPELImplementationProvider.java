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
package org.apache.tuscany.sca.implementation.bpel.provider;

import java.io.File;
import java.net.URL;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tuscany.sca.implementation.bpel.BPELImplementation;
import org.apache.tuscany.sca.implementation.bpel.ode.EmbeddedODEServer;
import org.apache.tuscany.sca.implementation.bpel.ode.ODEDeployment;
import org.apache.tuscany.sca.implementation.bpel.ode.ODEInitializationException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.provider.ImplementationProvider;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;

/**
 * BPEL Implementation provider
 * 
 * @version $Rev$ $Date$
 */
public class BPELImplementationProvider implements ImplementationProvider {
    private final Log __log = LogFactory.getLog(getClass());
    
    private RuntimeComponent component;
    private BPELImplementation implementation;
    
    private EmbeddedODEServer odeServer;
    private TransactionManager txMgr;
    
    /**
     * Constructs a new BPEL Implementation.
     */
    public BPELImplementationProvider(RuntimeComponent component,
                                      BPELImplementation implementation,
                                      EmbeddedODEServer odeServer,
                                      TransactionManager txMgr) {
        this.component = component;
        this.implementation = implementation;
        this.odeServer = odeServer;
        this.txMgr = txMgr;
    }

    public Invoker createInvoker(RuntimeComponentService service, Operation operation) {
        BPELInvoker invoker = new BPELInvoker(component, service, operation, odeServer, txMgr);
        return invoker;
    }
    
    public boolean supportsOneWayInvocation() {
        return false;
    }

    public void start() {
        if(__log.isInfoEnabled()) {
            __log.info("Starting " + component.getName());
        }
        
        try {
            if (!odeServer.isInitialized()) {
                // start ode server
                odeServer.init();
            }

            URL deployURL = this.implementation.getProcessDefinition().getLocation();
            
            File deploymentDir = new File(deployURL.toURI().getPath()).getParentFile();
            
            if(__log.isInfoEnabled()) {
                __log.info(">>> Deploying : " + deploymentDir.toString());
            }

            // deploy the process
            if (odeServer.isInitialized()) {
                try {
                    txMgr.begin();
                    odeServer.registerTuscanyRuntimeComponent(implementation.getProcess(), component);
                    odeServer.deploy(new ODEDeployment(deploymentDir));
                    txMgr.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    txMgr.rollback();
                }
            }
            
        } catch (ODEInitializationException inite) {
            throw new RuntimeException("BPEL Component Type Implementation : Error initializing embedded ODE server " + inite.getMessage(), inite);
        } catch(Exception e) {
            throw new RuntimeException("BPEl Component Type Implementation initialization failure : " + e.getMessage(), e);
        }
    }

    public void stop() {
        if(__log.isInfoEnabled()) {
            __log.info("Stopping " + component.getName());
        }
        
        if (odeServer.isInitialized()) {
            // start ode server
            odeServer.stop();
        }
        
        txMgr = null;

        if(__log.isInfoEnabled()) {
            __log.info("Stopped !!!");
        }
    }

}
