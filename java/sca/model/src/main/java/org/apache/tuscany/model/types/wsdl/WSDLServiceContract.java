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
package org.apache.tuscany.model.types.wsdl;

import javax.wsdl.PortType;

import org.apache.tuscany.model.assembly.ServiceContract;

/**
 * A WSDL service contract.
 */
public interface WSDLServiceContract extends ServiceContract {
    
    /**
     * Returns the WSDL portType.
     * @return the WSDL portType
     */
    PortType getPortType();
    
    /**
     * Sets the WSDL portType.
     * @param portType
     */
    void setPortType(PortType portType);
    
    /**
     * Returns the callback WSDL portType.
     * @return the callback WSDL portType
     */
    PortType getCallbackPortType();
    
    /**
     * Sets the callback WSDL portType.
     * @param portType
     */
    void setCallbackPortType(PortType portType);
}
