/**
 * 
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.tuscany.core.system.builder;

import org.apache.tuscany.core.builder.BuilderException;
import org.apache.tuscany.core.builder.BuilderInitException;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.injection.FactoryInitException;
import org.apache.tuscany.core.system.assembly.SystemBinding;
import org.apache.tuscany.core.system.config.SystemEntryPointContextFactory;
import org.apache.tuscany.model.assembly.AssemblyObject;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.EntryPoint;

/**
 * Decorates the logical model with entry point context configuration builders
 * 
 * @version $Rev: 385747 $ $Date: 2006-03-13 22:12:53 -0800 (Mon, 13 Mar 2006) $
 */
public class SystemEntryPointBuilder implements ContextFactoryBuilder {

    public SystemEntryPointBuilder() {
    }

    public void build(AssemblyObject modelObject) throws BuilderException {
        if (!(modelObject instanceof EntryPoint)) {
            return;
        }
        EntryPoint entryPoint = (EntryPoint) modelObject;
        if (!(entryPoint.getBindings().get(0) instanceof SystemBinding)
                || entryPoint.getContextFactory() != null) {
            return;
        }
        try {
            String targetName;
            ConfiguredService targetService = entryPoint.getConfiguredReference().getTargetConfiguredServices().get(0);
            Class serviceInterface = entryPoint.getConfiguredReference().getPort().getServiceContract().getInterface();
            if (targetService.getPart() == null) {
                // FIXME not correct
                if (targetService.getPort() == null) {
                    BuilderInitException e = new BuilderInitException("No target service specified on ");
                    e.setIdentifier(entryPoint.getName());
                }
                targetName = targetService.getPort().getName();
            } else {
                targetName = targetService.getPart().getName();
            }
            SystemEntryPointContextFactory contextFactory = new SystemEntryPointContextFactory(entryPoint.getName(),
                    targetName, serviceInterface);
            entryPoint.setContextFactory(contextFactory);
        } catch (FactoryInitException e) {
            e.addContextName(entryPoint.getName());
            throw e;
        }
    }
}
