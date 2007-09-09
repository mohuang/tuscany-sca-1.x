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

package org.apache.tuscany.sca.core.invocation;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.tuscany.sca.core.context.CallableReferenceImpl;
import org.apache.tuscany.sca.core.context.ConversationImpl;
import org.apache.tuscany.sca.core.context.InstanceWrapper;
import org.apache.tuscany.sca.core.scope.Scope;
import org.apache.tuscany.sca.core.scope.ScopeContainer;
import org.apache.tuscany.sca.core.scope.ScopedRuntimeComponent;
import org.apache.tuscany.sca.interfacedef.ConversationSequence;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.runtime.EndpointReference;
import org.apache.tuscany.sca.runtime.RuntimeComponent;
import org.apache.tuscany.sca.runtime.RuntimeWire;
import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class JDKInvocationHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -3366410500152201371L;

    protected boolean conversational;
    protected ConversationImpl conversation;
    protected boolean conversationStarted;
    protected MessageFactory messageFactory;
    protected EndpointReference endpoint;
    protected Object callbackID;
    protected Object callbackObject;
    protected RuntimeWire wire;
    protected CallableReference<?> callableReference;
    protected Class<?> businessInterface;

    protected boolean fixedWire = true;
    protected transient Map<Method, InvocationChain> chains = new HashMap<Method, InvocationChain>();

    public JDKInvocationHandler(MessageFactory messageFactory, Class<?> businessInterface, RuntimeWire wire) {
        this.messageFactory = messageFactory;
        this.wire = wire;
        this.businessInterface = businessInterface;
        setConversational(wire);
    }

    public JDKInvocationHandler(MessageFactory messageFactory, CallableReference<?> callableReference) {
        this.messageFactory = messageFactory;
        this.callableReference = callableReference;
        if (callableReference != null) {
            this.businessInterface = callableReference.getBusinessInterface();
            this.callbackID = callableReference.getCallbackID();
            this.conversation = (ConversationImpl)callableReference.getConversation();
            this.wire = ((CallableReferenceImpl<?>)callableReference).getRuntimeWire();
            if (callableReference instanceof ServiceReference) {
                this.callbackObject = ((ServiceReference)callableReference).getCallback();
            }
            if (wire != null) {
                setConversational(wire);
            }
        }
    }

    protected void setConversational(RuntimeWire wire) {
        InterfaceContract contract = wire.getSource().getInterfaceContract();
        this.conversational = contract.getInterface().isConversational();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            return invokeObjectMethod(method, args);
        }
        if (wire == null) {
            //FIXME: need better exception
            throw new RuntimeException("Destination for call is not known");
        }
        InvocationChain chain = getInvocationChain(method, wire);
        if (chain == null) {
            throw new IllegalArgumentException("No matching operation is found: " + method);
        }

        // send the invocation down the wire
        Object result = invoke(chain, args, wire);

        return result;
    }

    /**
     * @param method
     * @param args
     */
    private Object invokeObjectMethod(Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("toString".equals(name)) {
            return "[Proxy - " + toString() + "]";
        } else if ("equals".equals(name)) {
            Object obj = args[0];
            if (obj == null) {
                return false;
            }
            if (!Proxy.isProxyClass(obj.getClass())) {
                return false;
            }
            return equals(Proxy.getInvocationHandler(obj));
        } else if ("hashCode".equals(name)) {
            return hashCode();
        } else {
            return method.invoke(this);
        }
    }

    /**
     * Determines if the given operation matches the given method
     * 
     * @return true if the operation matches, false if does not
     */
    @SuppressWarnings("unchecked")
    private static boolean match(Operation operation, Method method) {
        Class<?>[] params = method.getParameterTypes();
        DataType<List<DataType>> inputType = operation.getInputType();
        List<DataType> types = inputType.getLogical();
        boolean matched = true;
        if (types.size() == params.length && method.getName().equals(operation.getName())) {
            for (int i = 0; i < params.length; i++) {
                Class<?> clazz = params[i];
                if (!operation.getInputType().getLogical().get(i).getPhysical().isAssignableFrom(clazz)) {
                    matched = false;
                }
            }
        } else {
            matched = false;
        }
        return matched;

    }

    protected synchronized InvocationChain getInvocationChain(Method method, RuntimeWire wire) {
        if (fixedWire && chains.containsKey(method)) {
            return chains.get(method);
        }
        InvocationChain found = null;
        for (InvocationChain chain : wire.getInvocationChains()) {
            Operation operation = chain.getSourceOperation();
            if (operation.isDynamic()) {
                operation.setName(method.getName());
                found = chain;
                break;
            } else if (match(operation, method)) {
                found = chain;
                break;
            }
        }
        if (fixedWire) {
            chains.put(method, found);
        }
        return found;
    }

    public void setEndpoint(EndpointReference endpoint) {
        this.endpoint = endpoint;
    }

    public void setCallbackID(Object callbackID) {
        this.callbackID = callbackID;
    }

    protected Object invoke(InvocationChain chain, Object[] args, RuntimeWire wire) throws Throwable {

        Message msgContext = ThreadMessageContext.getMessageContext();
        Object msgContextConversationId = msgContext.getConversationID();

        Message msg = messageFactory.createMessage();

        // make sure that the conversation id is set so it can be put in the 
        // outgoing messages.        
        if (conversational) {
            Object conversationId = conversation.getConversationID();

            // create a conversation id if one doesn't exist 
            // already, i.e. the conversation is just starting
            // If this is a callback the conversation id will have been
            // set to the conversation from the message context already
            if (conversationId == null) {
                // create a new conversation Id
                conversationId = createConversationID();
                conversation.setConversationID(conversationId);
            }

            msg.setConversationID(conversationId);
            
            // If we are passing out a callback target register the calling component instance against 
            // this new conversation id so that stateful callbacks will be able to find it
            // we don't check if the callback has conversation scope here as non-conversational
            // scoped components still need to have the conversation ids on the calling reference set
            // to null
            if (wire.getSource().getCallbackEndpoint() != null && callbackObject == null) {
                // the component instance will already registered by now so add another registration
                ScopeContainer<Object> scopeContainer = getConversationalScopeContainer(wire);

                if (scopeContainer != null) {
                    scopeContainer.addWrapperReference(msgContextConversationId, conversation);
                }
            }
        }
        
        Invoker headInvoker = chain.getHeadInvoker();
        msg.setCorrelationID(callbackID);
        Operation operation = chain.getTargetOperation();
        msg.setOperation(operation);
        Interface contract = operation.getInterface();
        if (contract != null && contract.isConversational()) {
            ConversationSequence sequence = operation.getConversationSequence();
            if (sequence == ConversationSequence.CONVERSATION_END) {
                msg.setConversationSequence(ConversationSequence.CONVERSATION_END);
                conversationStarted = false;
            } else if (sequence == ConversationSequence.CONVERSATION_CONTINUE) {
                if (conversationStarted) {
                    msg.setConversationSequence(ConversationSequence.CONVERSATION_CONTINUE);
                } else {
                    conversationStarted = true;
                    msg.setConversationSequence(ConversationSequence.CONVERSATION_START);
                }
            }
        }
        msg.setCallableReference(callableReference);
        msg.setBody(args);
        if (wire.getSource() != null) {
            EndpointReference callbackEndpoint = wire.getSource().getCallbackEndpoint();
            if (callbackEndpoint != null) {
                if (callbackObject != null) {
                    if (callbackObject instanceof ServiceReference) {
                        msg.setFrom(((CallableReferenceImpl)callbackObject).getRuntimeWire().getTarget());
                    } else {
                        if (contract != null) {
                            if (!contract.isConversational()) {
                                throw new IllegalArgumentException
                                        ("Callback object for stateless callback is not a ServiceReference");
                            } else {
                                ScopeContainer<Object> scopeContainer = getConversationalScopeContainer(wire);
                                if (scopeContainer != null) {
                                    InstanceWrapper<Object> wrapper = new CallbackObjectWrapper(callbackObject);
                                    scopeContainer.registerWrapper(wrapper, conversation.getConversationID());
                                }
                                msg.setFrom(callbackEndpoint);
                            }
                        }
                    }
                } else {
                    msg.setFrom(callbackEndpoint);
                }
            }
        }
        if (endpoint != null) {
            msg.setTo(endpoint);
        } else {
            msg.setTo(wire.getTarget());
        }

        ThreadMessageContext.setMessageContext(msg);
        try {
            // dispatch the wire down the chain and get the response
            Message resp = headInvoker.invoke(msg);
            Object body = resp.getBody();
            
            // Mark the object instance for removal if the conversation has ended
            if (contract != null && contract.isConversational()) {
                ConversationSequence sequence = operation.getConversationSequence();
                if (sequence == ConversationSequence.CONVERSATION_END) {
                    if (conversation != null) {

                        // remove conversation id from scope container
                        ScopeContainer<Object> scopeContainer = getConversationalScopeContainer(wire);

                        if (scopeContainer != null) {
                            scopeContainer.remove(conversation.getConversationID());
                        }

                        conversation.setConversationID(null);
                    }
                }
            }               
            
            if (resp.isFault()) {
                throw (Throwable)body;
            }
            return body;
        } finally {
            ThreadMessageContext.setMessageContext(msgContext);
        }
        

    }

    private ScopeContainer<Object> getConversationalScopeContainer(RuntimeWire wire) {
        ScopeContainer<Object> scopeContainer = null;

        RuntimeComponent runtimeComponent = wire.getSource().getComponent();

        if (runtimeComponent instanceof ScopedRuntimeComponent) {
            ScopedRuntimeComponent scopedRuntimeComponent = (ScopedRuntimeComponent)runtimeComponent;
            ScopeContainer<Object> tmpScopeContainer = scopedRuntimeComponent.getScopeContainer();

            if ((tmpScopeContainer != null) && (tmpScopeContainer.getScope() == Scope.CONVERSATION)) {
                scopeContainer = tmpScopeContainer;
            }
        }

        return scopeContainer;
    }

    /**
     * Creates a new conversational id
     * 
     * @return the conversational id
     */
    private String createConversationID() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return the callableReference
     */
    public CallableReference<?> getCallableReference() {
        return callableReference;
    }

    /**
     * @param callableReference the callableReference to set
     */
    public void setCallableReference(CallableReference<?> callableReference) {
        this.callableReference = callableReference;
    }

    /**
     * Minimal wrapper for a callback object contained in a ServiceReference
     */
    private static class CallbackObjectWrapper<T> implements InstanceWrapper<T> {

        private T instance;

        private CallbackObjectWrapper(T instance) {
            this.instance = instance;
        }

        public T getInstance() {
            return instance;
        }

        public void start() {
            // do nothing
        }
        
        public void stop() {
            // do nothing
        }

    }

}
