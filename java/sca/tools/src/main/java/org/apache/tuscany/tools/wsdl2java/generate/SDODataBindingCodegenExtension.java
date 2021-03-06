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
package org.apache.tuscany.tools.wsdl2java.generate;

import org.apache.axis2.wsdl.codegen.extension.AbstractDBProcessingExtension;
import org.apache.axis2.wsdl.databinding.TypeMapper;

/**
 * SDO data binding codegen extension.
 */
public class SDODataBindingCodegenExtension extends AbstractDBProcessingExtension {

    private TypeMapper typeMapper;
    
    public SDODataBindingCodegenExtension(TypeMapper typeMapper) {
        this.typeMapper=typeMapper;
    }
    
    protected boolean testFallThrough(String dbFrameworkName) {
        return !dbFrameworkName.equals("sdo");
    }

    public void engage() {
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }
        
        // Set the type mapper into the config
        configuration.setTypeMapper(typeMapper);

    }

}
