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
package org.apache.tuscany.service.discovery.jxta;

import java.io.IOException;
import java.net.URI;

import net.jxta.endpoint.Message;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

/**
 * Class for receiving information on a JXTA pipe.
 * 
 * @version $Revision$ $Date$
 *
 */
public final class PipeReceiver implements PipeMsgListener {
    
    /** Message listener. */
    private MessageListener messageListener;
    
    /** Net peer group. */
    private PeerGroup peerGroup;
    
    /** Pipe service. */
    private PipeService pipeService;
    
    /** Input pipe */
    private InputPipe inputPipe;
    
    /**
     * Initializes the message listener.
     * @param messageListener Message listener.
     * @throws PeerGroupException If unable to create Peer group.
     */
    private PipeReceiver(MessageListener messageListener) throws PeerGroupException {
        
        this.messageListener = messageListener;

        peerGroup = PeerGroupFactory.newNetPeerGroup();
        pipeService = peerGroup.getPipeService();
        
    }
    
    /**
     * Creates a new instance of the pipe receiver.
     * @param messageListener Message lsitener.
     * @throws PeerGroupException If unable to create Peer group.
     */
    public static PipeReceiver newInstance(MessageListener messageListener) throws PeerGroupException {
        
        if(messageListener == null) {
            throw new IllegalArgumentException("Message listener is null");
        }
        
        return new PipeReceiver(messageListener);
        
    }
    
    /**
     * Start receiving messages.
     * @param domain Domain in which runtime is participating.
     * @param profile Profile name of the runtime.
     * @throws IOException In case of unexpected IO error.
     */
    public void start(URI domain, String profile) throws IOException {  
        
        if(domain == null) {
            throw new IllegalArgumentException("Domain is null");
        }
        if(profile == null) {
            throw new IllegalArgumentException("Profile is null");
        }    
        PipeAdvertisement pipeAdvertisement = AdvertismentHelper.getDomainAdvertisment(domain, profile);
        inputPipe = pipeService.createInputPipe(pipeAdvertisement, this);
        
    }
    
    /**
     * Stop receiving messages.
     */
    public void stop() {
        inputPipe.close();
    }

    /**
     * Callback when messages are received.
     */
    public void pipeMsgEvent(PipeMsgEvent event) {        
        Message message = event.getMessage();
        messageListener.onMessage(message);
    }

}
