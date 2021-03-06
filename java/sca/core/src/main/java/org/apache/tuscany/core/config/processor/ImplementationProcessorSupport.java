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
package org.apache.tuscany.core.config.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.tuscany.core.config.ComponentTypeIntrospector;
import org.apache.tuscany.core.config.ConfigurationLoadException;
import org.apache.tuscany.core.extension.config.ImplementationProcessor;
import org.apache.tuscany.core.system.annotation.Autowire;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.ComponentType;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Scope;

/**
 * A base implementation of an <code>ImplementationProcessor</code>
 *
 * @version $$Rev$$ $$Date$$
 */
@Scope("MODULE")
public abstract class ImplementationProcessorSupport implements ImplementationProcessor {

    protected ComponentTypeIntrospector introspector;
    protected AssemblyFactory factory;

    protected ImplementationProcessorSupport(AssemblyFactory factory) {
        this.factory = factory;
    }

    protected ImplementationProcessorSupport() {
    }

    @Init(eager = true)
    public void init() throws Exception {
        introspector.registerProcessor(this);
    }

    @Autowire
    public void setIntrospector(ComponentTypeIntrospector introspector) {
        this.introspector = introspector;
    }

    @Autowire
    public void setFactory(AssemblyFactory factory) {
        this.factory = factory;

    }

    public void visitClass(Class<?> clazz, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitSuperClass(Class<?> clazz, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitMethod(Method method, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitConstructor(Constructor<?> constructor, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitField(Field field, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitInterface(Class clazz, Annotation[] annotations, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitInterfaceMethod(Method method, Annotation[] annotations, ComponentType type) throws ConfigurationLoadException {

    }

    public void visitEnd(Class<?> clazz, ComponentType type) throws ConfigurationLoadException {

    }

//    /**
//     * Creates a {@link JavaExtensibilityElement} subclasses may update while processing annotations
//     */
//    protected JavaExtensibilityElement getExtensibilityElement(ComponentType type) {
//        JavaExtensibilityElement element = (JavaExtensibilityElement) type.getExtensibilityElements().get(JAVA_ELEMENT);
//        if (element == null) {
//            element = new JavaExtensibilityElementImpl();
//            type.getExtensibilityElements().put(JAVA_ELEMENT, element);
//        }
//        return element;
//    }
}
