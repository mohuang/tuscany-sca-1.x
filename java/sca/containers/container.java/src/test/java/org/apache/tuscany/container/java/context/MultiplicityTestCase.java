/**
 * 
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuscany.container.java.context;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.tuscany.container.java.builder.JavaContextFactoryBuilder;
import org.apache.tuscany.container.java.builder.JavaTargetWireBuilder;
import org.apache.tuscany.container.java.mock.MockConfigContext;
import org.apache.tuscany.container.java.mock.MockFactory;
import org.apache.tuscany.container.java.mock.components.Source;
import org.apache.tuscany.container.java.mock.components.Target;
import org.apache.tuscany.core.builder.ContextFactoryBuilder;
import org.apache.tuscany.core.builder.WireBuilder;
import org.apache.tuscany.core.builder.system.DefaultPolicyBuilderRegistry;
import org.apache.tuscany.core.context.CompositeContext;
import org.apache.tuscany.core.context.AtomicContext;
import org.apache.tuscany.core.context.event.ModuleStart;
import org.apache.tuscany.core.context.impl.CompositeContextImpl;
import org.apache.tuscany.core.wire.jdk.JDKWireFactoryFactory;
import org.apache.tuscany.core.wire.service.WireFactoryService;
import org.apache.tuscany.core.wire.service.DefaultWireFactoryService;
import org.apache.tuscany.core.message.impl.MessageFactoryImpl;
import org.apache.tuscany.model.assembly.Scope;

/**
 * Tests wires that are configured with a multiplicity
 * 
 * @version $Rev$ $Date$
 */
public class MultiplicityTestCase extends TestCase {

    public void testMultiplicity() throws Exception {
        CompositeContext context = createContext();
        context.start();
        context.registerModelObject(MockFactory.createModuleWithWiredComponents(Scope.MODULE, Scope.MODULE));
        context.publish(new ModuleStart(this));
        Source source = (Source) ((AtomicContext) context.getContext("source")).getTargetInstance();
        Assert.assertNotNull(source);
        Target target = (Target) ((AtomicContext)context.getContext("target")).getTargetInstance();
        Assert.assertNotNull(target);
        // test setter injection
        List<Target> targets = source.getTargets();
        Assert.assertEquals(1, targets.size());

        // test field injection
        targets = source.getTargetsThroughField();
        Assert.assertEquals(1, targets.size());
    }

    private CompositeContext createContext() {
        CompositeContextImpl context = new CompositeContextImpl();
        context.setName("system.context");
        List<ContextFactoryBuilder>builders = MockFactory.createSystemBuilders();
        WireFactoryService wireService = new DefaultWireFactoryService(new MessageFactoryImpl(), new JDKWireFactoryFactory(), new DefaultPolicyBuilderRegistry());
        builders.add(new JavaContextFactoryBuilder(wireService));
        List<WireBuilder> wireBuilders = new ArrayList<WireBuilder>();
        wireBuilders.add(new JavaTargetWireBuilder());
        context.setConfigurationContext(new MockConfigContext(builders,wireBuilders));
        return context;
    }
}
