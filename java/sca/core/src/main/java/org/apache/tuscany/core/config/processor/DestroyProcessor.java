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
package org.apache.tuscany.core.config.processor;

import java.lang.reflect.Method;

import org.apache.tuscany.core.extension.config.extensibility.DestroyInvokerExtensibilityElement;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.ComponentType;
import org.osoa.sca.annotations.Destroy;

/**
 * Processes a {@link Destroy}
 *
 * @version $$Rev$$ $$Date$$
 */
public class DestroyProcessor extends ImplementationProcessorSupport {

    public DestroyProcessor() {
    }

    public void visitMethod(Method method, ComponentType type) throws ConfigurationLoadException {
        Destroy destroy = method.getAnnotation(Destroy.class);
        if (destroy == null) {
            return;
        }
        if (method.getParameterTypes().length != 0) {
            throw new ConfigurationLoadException("Destroy methods cannot take parameters");
        }
        type.getExtensibilityElements().add(new DestroyInvokerExtensibilityElement(method));
    }
}
