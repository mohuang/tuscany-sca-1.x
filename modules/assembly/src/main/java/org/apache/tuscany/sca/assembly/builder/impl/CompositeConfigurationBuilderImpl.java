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

package org.apache.tuscany.sca.assembly.builder.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.sca.assembly.AssemblyFactory;
import org.apache.tuscany.sca.assembly.Binding;
import org.apache.tuscany.sca.assembly.Component;
import org.apache.tuscany.sca.assembly.ComponentProperty;
import org.apache.tuscany.sca.assembly.ComponentReference;
import org.apache.tuscany.sca.assembly.ComponentService;
import org.apache.tuscany.sca.assembly.Composite;
import org.apache.tuscany.sca.assembly.CompositeService;
import org.apache.tuscany.sca.assembly.Implementation;
import org.apache.tuscany.sca.assembly.Multiplicity;
import org.apache.tuscany.sca.assembly.Property;
import org.apache.tuscany.sca.assembly.Reference;
import org.apache.tuscany.sca.assembly.SCABinding;
import org.apache.tuscany.sca.assembly.Service;
import org.apache.tuscany.sca.assembly.WireableBinding;
import org.apache.tuscany.sca.assembly.builder.ComponentPreProcessor;
import org.apache.tuscany.sca.assembly.builder.CompositeBuilderMonitor;
import org.apache.tuscany.sca.assembly.builder.Problem.Severity;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;

public class CompositeConfigurationBuilderImpl {
    
    private AssemblyFactory assemblyFactory;
    private CompositeBuilderMonitor monitor;
    private InterfaceContractMapper interfaceContractMapper;
    
    public CompositeConfigurationBuilderImpl(AssemblyFactory assemblyFactory, InterfaceContractMapper interfaceContractMapper, CompositeBuilderMonitor monitor) {
        this.assemblyFactory = assemblyFactory;
        this.interfaceContractMapper = interfaceContractMapper;
        this.monitor = monitor;
    }

    /**
     * Configure components in the composite.
     * 
     * @param composite
     * @param problems
     */
    public void configureComponents(Composite composite) {
        configureComponents(composite, null);
    }

    /**
     * Configure components in the composite.
     * 
     * @param composite
     * @param uri
     * @param problems
     */
    private void configureComponents(Composite composite, String uri) {
    
        // Process nested composites recursively
        for (Component component : composite.getComponents()) {
    
            // Initialize component URI
            String componentURI;
            if (uri == null) {
                componentURI = component.getName();
            } else {
                componentURI = uri + "/" + component.getName();
            }
            component.setURI(componentURI);
    
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
    
                // Process nested composite
                configureComponents((Composite)implementation, componentURI);
            }
        }
    
        // Set default binding names
        for (Service service : composite.getServices()) {
            for (Binding binding : service.getBindings()) {
                if (binding.getName() == null) {
                    binding.setName(service.getName());
                }
            }
            if (service.getCallback() != null) {
                for (Binding binding : service.getCallback().getBindings()) {
                    if (binding.getName() == null) {
                        binding.setName(service.getName());
                    }
                }
            }
        }
        for (Reference reference : composite.getReferences()) {
            for (Binding binding : reference.getBindings()) {
                if (binding.getName() == null) {
                    binding.setName(reference.getName());
                }
            }
            if (reference.getCallback() != null) {
                for (Binding binding : reference.getCallback().getBindings()) {
                    if (binding.getName() == null) {
                        binding.setName(reference.getName());
                    }
                }
            }
        }
    
        // Initialize all component services and references
        Map<String, Component> components = new HashMap<String, Component>();
        for (Component component : composite.getComponents()) {
    
            // Index all components and check for duplicates
            if (components.containsKey(component.getName())) {
                warning("Duplicate component name: " + composite.getName() + " : " + component.getName(), composite);
            } else {
                components.put(component.getName(), component);
            }
    
            // Propagate the autowire flag from the composite to components
            if (composite.isAutowire()) {
                component.setAutowire(true);
            }
    
            if (component.getImplementation() instanceof ComponentPreProcessor) {
                ((ComponentPreProcessor)component.getImplementation()).preProcess(component);
            }
    
            Map<String, Service> services = new HashMap<String, Service>();
            Map<String, Reference> references = new HashMap<String, Reference>();
            Map<String, Property> properties = new HashMap<String, Property>();
            //Index properties, services and references
            indexImplementationPropertiesServicesAndReferences(component, services, references, properties);
    
            Map<String, ComponentService> componentServices = new HashMap<String, ComponentService>();
            Map<String, ComponentReference> componentReferences = new HashMap<String, ComponentReference>();
            Map<String, ComponentProperty> componentProperties = new HashMap<String, ComponentProperty>();
            //Index component services, references and properties
            // Also check for duplicates
            indexComponentPropertiesServicesAndReferences(component,
                                                          componentServices,
                                                          componentReferences,
                                                          componentProperties);
    
            // Reconcile component services/references/properties and
            // implementation
            // services/references and create component
            // services/references/properties
            // for the services/references declared by the implementation
            reconcileServices(component, services, componentServices);
            reconcileReferences(component, references, componentReferences);
            reconcileProperties(component, properties, componentProperties);
    
            // Create self references to the component's services
            if (!(component.getImplementation() instanceof Composite)) {
                createSelfReferences(component);
            }
        }
    }

    /**
     * Report a warning.
     * 
     * @param problems
     * @param message
     * @param model
     */
    private void warning(String message, Object model) {
        monitor.problem(new ProblemImpl(Severity.WARNING, message, model));
    }

    /**
     * Reconcile component properties and the properties defined by the
     * component type.
     * 
     * @param component
     * @param properties
     * @param componentProperties
     * @param problems
     */
    private void reconcileProperties(Component component,
                                     Map<String, Property> properties,
                                     Map<String, ComponentProperty> componentProperties) {
    
        // Connect component properties to their properties
        for (ComponentProperty componentProperty : component.getProperties()) {
            Property property = properties.get(componentProperty.getName());
            if (property != null) {
                componentProperty.setProperty(property);
            } else {
                warning("Property not found for component property: " + component.getName()
                    + "/"
                    + componentProperty.getName(), component);
            }
        }
    
        // Create component properties for all properties
        if (component.getImplementation() != null) {
            for (Property property : component.getImplementation().getProperties()) {
                if (!componentProperties.containsKey(property.getName())) {
                    ComponentProperty componentProperty = assemblyFactory.createComponentProperty();
                    componentProperty.setName(property.getName());
                    componentProperty.setMany(property.isMany());
                    componentProperty.setXSDElement(property.getXSDElement());
                    componentProperty.setXSDType(property.getXSDType());
                    componentProperty.setProperty(property);
                    component.getProperties().add(componentProperty);
                }
            }
        }
    
        // Reconcile component properties and their properties
        for (ComponentProperty componentProperty : component.getProperties()) {
            Property property = componentProperty.getProperty();
            if (property != null) {
    
                // Check that a component property does not override the
                // mustSupply attribute
                if (!property.isMustSupply() && componentProperty.isMustSupply()) {
                    warning("Component property mustSupply attribute incompatible with property: " + component
                        .getName()
                        + "/"
                        + componentProperty.getName(), component);
                }
    
                // Default to the mustSupply attribute specified on the property
                if (!componentProperty.isMustSupply())
                    componentProperty.setMustSupply(property.isMustSupply());
    
                // Default to the value specified on the property
                if (componentProperty.getValue() == null) {
                    componentProperty.setValue(property.getValue());
                }
    
                // Check that a value is supplied
                if (componentProperty.getValue() == null && property.isMustSupply()) {
                    warning("No value configured on a mustSupply property: " + component.getName()
                        + "/"
                        + componentProperty.getName(), component);
                }
    
                // Check that a a component property does not override the
                // many attribute
                if (!property.isMany() && componentProperty.isMany()) {
                    warning("Component property many attribute incompatible with property: " + component.getName()
                        + "/"
                        + componentProperty.getName(), component);
                }
    
                // Default to the many attribute defined on the property
                componentProperty.setMany(property.isMany());
    
                // Default to the type and element defined on the property
                if (componentProperty.getXSDType() == null) {
                    componentProperty.setXSDType(property.getXSDType());
                }
                if (componentProperty.getXSDElement() == null) {
                    componentProperty.setXSDElement(property.getXSDElement());
                }
    
                // Check that a type or element are specified
                if (componentProperty.getXSDElement() == null && componentProperty.getXSDType() == null) {
                    warning("No type specified on component property: " + component.getName()
                        + "/"
                        + componentProperty.getName(), component);
                }
            }
        }
    }

    /**
     * Reconcile component references with the references defined on the
     * component type.
     * 
     * @param component
     * @param references
     * @param componentReferences
     * @param problems
     */
    private void reconcileReferences(Component component,
                                     Map<String, Reference> references,
                                     Map<String, ComponentReference> componentReferences) {
    
        // Connect each component reference to the corresponding reference
        for (ComponentReference componentReference : component.getReferences()) {
            Reference reference = references.get(componentReference.getName());
            if (reference != null) {
                componentReference.setReference(reference);
            } else {
                if (!componentReference.getName().startsWith("$self$.")) {
                    warning("Reference not found for component reference: " + component.getName()
                        + "/"
                        + componentReference.getName(), component);
                }
            }
        }
    
        // Create a component reference for each reference
        if (component.getImplementation() != null) {
            for (Reference reference : component.getImplementation().getReferences()) {
                if (!componentReferences.containsKey(reference.getName())) {
                    ComponentReference componentReference = assemblyFactory.createComponentReference();
                    componentReference.setName(reference.getName());
                    componentReference.setReference(reference);
                    component.getReferences().add(componentReference);
                }
            }
        }
    
        // Reconcile each component reference with its reference
        for (ComponentReference componentReference : component.getReferences()) {
            Reference reference = componentReference.getReference();
            if (reference != null) {
    
                // Reconcile multiplicity
                if (componentReference.getMultiplicity() != null) {
                    if (!ReferenceUtil.isValidMultiplicityOverride(reference.getMultiplicity(), componentReference
                        .getMultiplicity())) {
                        warning("Component reference multiplicity incompatible with reference multiplicity: " + component
                                    .getName()
                                    + "/"
                                    + componentReference.getName(),
                                component);
                    }
                } else {
                    componentReference.setMultiplicity(reference.getMultiplicity());
                }
    
                // Reconcile interface
                if (componentReference.getInterfaceContract() != null) {
                    if (!componentReference.getInterfaceContract().equals(reference.getInterfaceContract())) {
                        if (!interfaceContractMapper.isCompatible(reference.getInterfaceContract(), componentReference
                            .getInterfaceContract())) {
                            warning("Component reference interface incompatible with reference interface: " + component
                                .getName()
                                + "/"
                                + componentReference.getName(), component);
                        }
                    }
                } else {
                    componentReference.setInterfaceContract(reference.getInterfaceContract());
                }
    
                // Reconcile bindings
                if (componentReference.getBindings().isEmpty()) {
                    componentReference.getBindings().addAll(reference.getBindings());
                }
    
                // Reconcile callback bindings
                if (componentReference.getCallback() == null) {
                    componentReference.setCallback(reference.getCallback());
                    if (componentReference.getCallback() == null) {
                        // Create an empty callback to avoid null check
                        componentReference.setCallback(assemblyFactory.createCallback());
                    }
    
                } else if (componentReference.getCallback().getBindings().isEmpty() && reference.getCallback() != null) {
                    componentReference.getCallback().getBindings().addAll(reference.getCallback().getBindings());
                }
    
                // Propagate autowire setting from the component
                if (component.isAutowire()) {
                    componentReference.setAutowire(true);
                }
    
                // Reconcile targets
                if (componentReference.getTargets().isEmpty()) {
                    componentReference.getTargets().addAll(reference.getTargets());
                }
            }
        }
    }

    /**
     * Reconcile component services and services defined on the component type.
     * 
     * @param component
     * @param services
     * @param componentServices
     * @param problems
     */
    private void reconcileServices(Component component,
                                   Map<String, Service> services,
                                   Map<String, ComponentService> componentServices) {
    
        // Connect each component service to the corresponding service
        for (ComponentService componentService : component.getServices()) {
            Service service = services.get(componentService.getName());
            if (service != null) {
                componentService.setService(service);
            } else {
                warning("Service not found for component service: " + component.getName()
                    + "/"
                    + componentService.getName(), component);
            }
        }
    
        // Create a component service for each service
        if (component.getImplementation() != null) {
            for (Service service : component.getImplementation().getServices()) {
                if (!componentServices.containsKey(service.getName())) {
                    ComponentService componentService = assemblyFactory.createComponentService();
                    componentService.setName(service.getName());
                    componentService.setService(service);
                    component.getServices().add(componentService);
                }
            }
        }
    
        // Reconcile each component service with its service
        for (ComponentService componentService : component.getServices()) {
            Service service = componentService.getService();
            if (service != null) {
    
                // Reconcile interface
                if (componentService.getInterfaceContract() != null) {
                    if (!componentService.getInterfaceContract().equals(service.getInterfaceContract())) {
                        if (!interfaceContractMapper.isCompatible(componentService.getInterfaceContract(), service
                            .getInterfaceContract())) {
                            warning("Component service interface incompatible with service interface: " + component
                                .getName()
                                + "/"
                                + componentService.getName(), component);
                        }
                    }
                } else {
                    componentService.setInterfaceContract(service.getInterfaceContract());
                }
    
                // Reconcile bindings
                if (componentService.getBindings().isEmpty()) {
                    componentService.getBindings().addAll(service.getBindings());
                }
    
                // Reconcile callback bindings
                if (componentService.getCallback() == null) {
                    componentService.setCallback(service.getCallback());
                    if (componentService.getCallback() == null) {
                        // Create an empty callback to avoid null check
                        componentService.setCallback(assemblyFactory.createCallback());
                    }
                } else if (componentService.getCallback().getBindings().isEmpty() && service.getCallback() != null) {
                    componentService.getCallback().getBindings().addAll(service.getCallback().getBindings());
                }
            }
    
        }
    }

    private void indexComponentPropertiesServicesAndReferences(Component component,
                                                               Map<String, ComponentService> componentServices,
                                                               Map<String, ComponentReference> componentReferences,
                                                               Map<String, ComponentProperty> componentProperties) {
        for (ComponentService componentService : component.getServices()) {
            if (componentServices.containsKey(componentService.getName())) {
                warning("Duplicate component service name: " + component.getName() + "/" + componentService.getName(),
                        component);
            } else {
                componentServices.put(componentService.getName(), componentService);
            }
    
            // Initialize binding names
            for (Binding binding : componentService.getBindings()) {
                if (binding.getName() == null) {
                    binding.setName(componentService.getName());
                }
            }
            if (componentService.getCallback() != null) {
                for (Binding binding : componentService.getCallback().getBindings()) {
                    if (binding.getName() == null) {
                        binding.setName(componentService.getName());
                    }
                }
            }
        }
        for (ComponentReference componentReference : component.getReferences()) {
            if (componentReferences.containsKey(componentReference.getName())) {
                warning("Duplicate component reference name: " + component.getName()
                    + "/"
                    + componentReference.getName(), component);
            } else {
                componentReferences.put(componentReference.getName(), componentReference);
            }
    
            // Initialize binding names
            for (Binding binding : componentReference.getBindings()) {
                if (binding.getName() == null) {
                    binding.setName(componentReference.getName());
                }
            }
            if (componentReference.getCallback() != null) {
                for (Binding binding : componentReference.getCallback().getBindings()) {
                    if (binding.getName() == null) {
                        binding.setName(componentReference.getName());
                    }
                }
            }
        }
        for (ComponentProperty componentProperty : component.getProperties()) {
            if (componentProperties.containsKey(componentProperty.getName())) {
                warning("Duplicate component property name: " + component.getName() + "/" + componentProperty.getName(),
                        component);
            } else {
                componentProperties.put(componentProperty.getName(), componentProperty);
            }
        }
    
    }

    private void indexImplementationPropertiesServicesAndReferences(Component component,
                                                                    Map<String, Service> services,
                                                                    Map<String, Reference> references,
                                                                    Map<String, Property> properties) {
        // First check that the component has a resolved implementation
        Implementation implementation = component.getImplementation();
        if (implementation == null) {
    
            // A component must have an implementation
            warning("No implementation for component: " + component.getName(), component);
    
        } else if (implementation.isUnresolved()) {
    
            // The implementation must be fully resolved
            warning("Component implementation not found: " + component.getName() + " : " + implementation.getURI(),
                    component);
    
        } else {
    
            // Index properties, services and references, also check for
            // duplicates
            for (Property property : implementation.getProperties()) {
                if (properties.containsKey(property.getName())) {
                    warning("Duplicate property name: " + component.getName() + "/" + property.getName(), component);
                } else {
                    properties.put(property.getName(), property);
                }
            }
            for (Service service : implementation.getServices()) {
                if (services.containsKey(service.getName())) {
                    warning("Duplicate service name: " + component.getName() + "/" + service.getName(), component);
                } else {
                    services.put(service.getName(), service);
                }
            }
            for (Reference reference : implementation.getReferences()) {
                if (references.containsKey(reference.getName())) {
                    warning("Duplicate reference name: " + component.getName() + "/" + reference.getName(), component);
                } else {
                    references.put(reference.getName(), reference);
                }
            }
        }
    
    }

    /**
     * Create a self-reference for a component service
     * @param component
     * @param service
     */
    private ComponentReference createSelfReference(Component component, ComponentService service) {
        ComponentReference componentReference = assemblyFactory.createComponentReference();
        componentReference.setName("$self$." + service.getName());
        componentReference.getBindings().addAll(service.getBindings());
        componentReference.setCallback(service.getCallback());
        /*
        ComponentService componentService = assemblyFactory.createComponentService();
        componentService.setName(component.getName() + "/" + service.getName());
        componentService.setUnresolved(true);
        componentReference.getTargets().add(componentService);
        */
        componentReference.getTargets().add(service);
        componentReference.getPolicySets().addAll(service.getPolicySets());
        componentReference.getRequiredIntents().addAll(service.getRequiredIntents());
        
        // FIXME: What interface contract should be used?
        InterfaceContract interfaceContract = service.getInterfaceContract();
        Service componentTypeService = service.getService();
        if (componentTypeService != null && componentTypeService.getInterfaceContract() != null) {
            interfaceContract = componentTypeService.getInterfaceContract();
        }
        componentReference.setInterfaceContract(interfaceContract);
        componentReference.setMultiplicity(Multiplicity.ONE_ONE);
        component.getReferences().add(componentReference);
        return componentReference;
    }

    /**
     * For all the services, create a corresponding self-reference.
     * 
     * @param component
     */
    private void createSelfReferences(Component component) {
        for (ComponentService service : component.getServices()) {
            createSelfReference(component, service);
        }
    }

    /**
     * Activate composite services in nested composites.
     * 
     * @param composite
     * @param problems
     */
    public void activateCompositeServices(Composite composite) {
    
        // Process nested composites recursively
        for (Component component : composite.getComponents()) {
            Implementation implementation = component.getImplementation();
            if (implementation instanceof Composite) {
    
                // First process nested composites
                activateCompositeServices((Composite)implementation);
    
                // Process the component services declared on components
                // in this composite
                for (ComponentService componentService : component.getServices()) {
                    CompositeService compositeService = (CompositeService)componentService.getService();
                    if (compositeService != null) {
    
                        // Get the inner most promoted service
                        ComponentService promotedService = getPromotedComponentService(compositeService);
                        if (promotedService != null) {
    
                            // Default to use the interface from the promoted service
                            if (compositeService.getInterfaceContract() == null) {
                                compositeService.setInterfaceContract(promotedService.getInterfaceContract());
                            }
                            if (componentService.getInterfaceContract() == null) {
                                componentService.setInterfaceContract(promotedService.getInterfaceContract());
                            }
    
                            // Create a new component service to represent this composite
                            // service on the promoted component
                            SCABinding scaBinding = promotedService.getBinding(SCABinding.class);
                            if (scaBinding != null) {
                                Component promotedComponent = scaBinding.getComponent();
                                ComponentService newComponentService = assemblyFactory.createComponentService();
                                newComponentService.setName("$promoted$." + compositeService.getName());
                                //newComponentService.setService(compositeService);
                                promotedComponent.getServices().add(newComponentService);
                                newComponentService.getBindings().add(scaBinding);
                                newComponentService.getBindings().addAll(compositeService.getBindings());
                                newComponentService.setInterfaceContract(compositeService.getInterfaceContract());
                                if (compositeService.getInterfaceContract() != null && // can be null in unit tests
                                compositeService.getInterfaceContract().getCallbackInterface() != null) {
                                    SCABinding scaCallbackBinding =
                                        promotedService.getCallbackBinding(SCABinding.class);
                                    newComponentService.setCallback(assemblyFactory.createCallback());
                                    if (scaCallbackBinding != null) {
                                        newComponentService.getCallback().getBindings().add(scaCallbackBinding);
                                    }
                                    if (compositeService.getCallback() != null) {
                                        newComponentService.getCallback().getBindings().addAll(compositeService
                                            .getCallback().getBindings());
                                    }
                                }
    
                                // FIXME: [rfeng] Set the service to promoted
                                newComponentService.setService(promotedService.getService());
                                
                                // Create a self-reference for the promoted service
                                ComponentReference selfRef = createSelfReference(promotedComponent, newComponentService);
                                Binding binding = BindingUtil.resolveBindings(selfRef, newComponentService);
                                selfRef.getBindings().clear();
                                selfRef.getBindings().add(binding);
                                selfRef.getTargets().clear();
    
                                // Change the composite service to now promote the newly
                                // created component service directly
                                compositeService.setPromotedService(newComponentService);
    
                            } else {
                                warning("Promoted component service not found: " + promotedService.getName(), composite);
                            }
                        }
                    }
                }
            }
        }
    
        // Process composite services declared in this composite
        for (Service service : composite.getServices()) {
            CompositeService compositeService = (CompositeService)service;
    
            // Get the inner most promoted service
            ComponentService promotedService = getPromotedComponentService(compositeService);
            if (promotedService != null) {
    
                // Default to use the interface from the promoted service
                if (compositeService.getInterfaceContract() == null && promotedService.getInterfaceContract() != null) {
                    compositeService.setInterfaceContract(promotedService.getInterfaceContract());
                }
    
                // Create a new component service to represent this composite
                // service on the promoted component
                SCABinding scaBinding = promotedService.getBinding(SCABinding.class);
                if (scaBinding != null) {
                    Component promotedComponent = scaBinding.getComponent();
                    ComponentService newComponentService = assemblyFactory.createComponentService();
                    newComponentService.setName("$promoted$." + compositeService.getName());
                    //newComponentService.setService(compositeService);
                    promotedComponent.getServices().add(newComponentService);
                    newComponentService.getBindings().add(scaBinding);
                    newComponentService.getBindings().addAll(compositeService.getBindings());
                    newComponentService.setInterfaceContract(compositeService.getInterfaceContract());
                    if (compositeService.getInterfaceContract() != null && // can be null in unit tests
                    compositeService.getInterfaceContract().getCallbackInterface() != null) {
                        SCABinding scaCallbackBinding = promotedService.getCallbackBinding(SCABinding.class);
                        newComponentService.setCallback(assemblyFactory.createCallback());
                        if (scaCallbackBinding != null) {
                            newComponentService.getCallback().getBindings().add(scaCallbackBinding);
                        }
                        if (compositeService.getCallback() != null) {
                            newComponentService.getCallback().getBindings().addAll(compositeService.getCallback()
                                .getBindings());
                        }
                    }
    
                    // FIXME: [rfeng] Set the service to promoted
                    newComponentService.setService(promotedService.getService());
    
                    // Change the composite service to now promote the newly
                    // created component service directly
                    compositeService.setPromotedService(newComponentService);
                } else {
                    warning("Promoted component service not found: " + promotedService.getName(), composite);
                }
            }
        }
    }

    /**
     * Follow a service promotion chain down to the inner most (non composite)
     * component service.
     * 
     * @param topCompositeService
     * @return
     */
    private ComponentService getPromotedComponentService(CompositeService compositeService) {
        ComponentService componentService = compositeService.getPromotedService();
        if (componentService != null) {
            Service service = componentService.getService();
            if (componentService.getName() != null && service instanceof CompositeService) {
    
                // Continue to follow the service promotion chain
                return getPromotedComponentService((CompositeService)service);
    
            } else {
    
                // Found a non-composite service
                return componentService;
            }
        } else {
    
            // No promoted service
            return null;
        }
    }

}
