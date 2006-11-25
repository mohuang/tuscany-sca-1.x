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
package org.apache.tuscany.core.idl.java;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversation;
import org.osoa.sca.annotations.EndConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Scope;

import org.apache.tuscany.spi.idl.InvalidConversationalContractException;
import org.apache.tuscany.spi.idl.InvalidServiceContractException;
import org.apache.tuscany.spi.idl.OverloadedOperationException;
import org.apache.tuscany.spi.idl.java.JavaInterfaceProcessor;
import org.apache.tuscany.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.apache.tuscany.spi.idl.java.JavaServiceContract;
import org.apache.tuscany.spi.model.DataType;
import org.apache.tuscany.spi.model.InteractionScope;
import org.apache.tuscany.spi.model.Operation;
import static org.apache.tuscany.spi.model.Operation.CONVERSATION_END;
import static org.apache.tuscany.spi.model.Operation.NO_CONVERSATION;
import static org.apache.tuscany.spi.model.ServiceContract.UNDEFINED;

import static org.apache.tuscany.core.util.JavaIntrospectionHelper.getBaseName;

/**
 * Default implementation of an InterfaceJavaIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class JavaInterfaceProcessorRegistryImpl implements JavaInterfaceProcessorRegistry {
    public static final String IDL_INPUT = "idl:input";

    private static final String UNKNOWN_DATABINDING = null;
    private static final String SECONDS = " SECONDS";
    private static final String MINUTES = " MINUTES";
    private static final String HOURS = " HOURS";
    private static final String DAYS = " DAYS";
    private static final String YEARS = " YEARS";

    private List<JavaInterfaceProcessor> processors = new ArrayList<JavaInterfaceProcessor>();

    public JavaInterfaceProcessorRegistryImpl() {
    }

    public void registerProcessor(JavaInterfaceProcessor processor) {
        processors.add(processor);
    }

    public void unregisterProcessor(JavaInterfaceProcessor processor) {
        processors.remove(processor);
    }

    public <T> JavaServiceContract introspect(Class<T> type) throws InvalidServiceContractException {
        Class<?> callbackClass = null;
        Callback callback = type.getAnnotation(Callback.class);
        if (callback != null && !Void.class.equals(callback.value())) {
            callbackClass = callback.value();
        } else if (callback != null && Void.class.equals(callback.value())) {
            IllegalCallbackException e = new IllegalCallbackException("No callback interface specified on annotation");
            e.setIdentifier(type.getName());
            throw e;
        }
        return introspect(type, callbackClass);
    }

    public <I, C> JavaServiceContract introspect(Class<I> type, Class<C> callback)
        throws InvalidServiceContractException {
        JavaServiceContract contract = new JavaServiceContract();
        contract.setInterfaceName(getBaseName(type));
        contract.setInterfaceClass(type);
        boolean remotable = type.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);
        Scope interactionScope = type.getAnnotation(Scope.class);
        boolean conversational = false;
        if (interactionScope != null && "CONVERSATION".equalsIgnoreCase(interactionScope.value())) {
            contract.setInteractionScope(InteractionScope.CONVERSATIONAL);
            conversational = true;
        } else {
            contract.setInteractionScope(InteractionScope.NONCONVERSATIONAL);
        }
        Conversation conversation = type.getAnnotation(Conversation.class);
        if (conversation != null && !conversational) {
            InvalidConversationalContractException e = new InvalidConversationalContractException(
                "Service is marked with @Conversation but the scope is not @Scope(\"CONVERSATION\")");
            e.setIdentifier(type.getName());
            throw e;
        } else if (conversation != null) {
            long maxAge = UNDEFINED;
            long maxIdleTime = UNDEFINED;
            try {
                String maxAgeVal = conversation.maxAge();
                if (maxAgeVal != null && maxAgeVal.length() > 0) {
                    maxAge = convertTimeMillis(maxAgeVal);
                }
            } catch (NumberFormatException e) {
                InvalidConversationalContractException e2 =
                    new InvalidConversationalContractException("Invalid maximum age", e);
                e2.setIdentifier(type.getName());
            }
            try {
                String maxIdleTimeVal = conversation.maxIdleTime();
                if (maxIdleTimeVal != null && maxIdleTimeVal.length() > 0) {
                    maxIdleTime = convertTimeMillis(maxIdleTimeVal);
                }
            } catch (NumberFormatException e) {
                InvalidConversationalContractException e2 =
                    new InvalidConversationalContractException("Invalid maximum idle time", e);
                e2.setIdentifier(type.getName());
            }

            contract.setMaxAge(maxAge);
            contract.setMaxIdleTime(maxIdleTime);
        }
        contract.setOperations(getOperations(type, remotable, conversational));

        if (callback != null) {
            contract.setCallbackName(getBaseName(callback));
            contract.setCallbackClass(callback);
            contract.setCallbackOperations(getOperations(callback, remotable, conversational));
        }

        for (JavaInterfaceProcessor processor : processors) {
            processor.visitInterface(type, callback, contract);
        }
        return contract;
    }

    private <T> Map<String, Operation<Type>> getOperations(Class<T> type, boolean remotable, boolean conversational)
        throws InvalidServiceContractException {
        Method[] methods = type.getMethods();
        Map<String, Operation<Type>> operations = new HashMap<String, Operation<Type>>(methods.length);
        for (Method method : methods) {
            String name = method.getName();
            if (remotable && operations.containsKey(name)) {
                throw new OverloadedOperationException(method.toString());
            }

            Type returnType = method.getGenericReturnType();
            Type[] paramTypes = method.getGenericParameterTypes();
            Type[] faultTypes = method.getGenericExceptionTypes();
            boolean nonBlocking = method.isAnnotationPresent(OneWay.class);
            int conversationSequence = NO_CONVERSATION;
            if (method.isAnnotationPresent(EndConversation.class)) {
                if (!conversational) {
                    InvalidConversationalContractException e = new InvalidConversationalContractException(
                        "Method is marked as end conversation but contract is not conversational");
                    e.setIdentifier(method.getDeclaringClass().getName() + "." + method.getName());
                    throw e;
                }
                conversationSequence = CONVERSATION_END;
            } else if (conversational) {
                conversationSequence = Operation.CONVERSATION_CONTINUE;
            }

            DataType<Type> returnDataType = new DataType<Type>(UNKNOWN_DATABINDING, returnType, returnType);
            List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>(paramTypes.length);
            for (Type paramType : paramTypes) {
                paramDataTypes.add(new DataType<Type>(UNKNOWN_DATABINDING, paramType, paramType));
            }
            List<DataType<Type>> faultDataTypes = new ArrayList<DataType<Type>>(faultTypes.length);
            for (Type faultType : faultTypes) {
                faultDataTypes.add(new DataType<Type>(UNKNOWN_DATABINDING, faultType, faultType));
            }

            DataType<List<DataType<Type>>> inputType =
                new DataType<List<DataType<Type>>>(IDL_INPUT, Object[].class, paramDataTypes);
            Operation<Type> operation = new Operation<Type>(name,
                inputType,
                returnDataType,
                faultDataTypes,
                nonBlocking,
                UNKNOWN_DATABINDING,
                conversationSequence);
            operations.put(name, operation);
        }
        return operations;
    }


    protected long convertTimeMillis(String expr) throws NumberFormatException {
        expr = expr.trim().toUpperCase();
        int i = expr.lastIndexOf(SECONDS);
        if (i >= 0) {
            String units = expr.substring(0, i);
            return Long.parseLong(units) * 1000;
        }
        i = expr.lastIndexOf(MINUTES);
        if (i >= 0) {
            String units = expr.substring(0, i);
            return Long.parseLong(units) * 60000;
        }

        i = expr.lastIndexOf(HOURS);
        if (i >= 0) {
            String units = expr.substring(0, i);
            return Long.parseLong(units) * 3600000;
        }
        i = expr.lastIndexOf(DAYS);
        if (i >= 0) {
            String units = expr.substring(0, i);
            return Long.parseLong(units) * 86400000;
        }
        i = expr.lastIndexOf(YEARS);
        if (i >= 0) {
            String units = expr.substring(0, i);
            return Long.parseLong(units) * 31556926000L;
        }
        return Long.parseLong(expr) * 1000;  // assume seconds if no suffix specified
    }

}
