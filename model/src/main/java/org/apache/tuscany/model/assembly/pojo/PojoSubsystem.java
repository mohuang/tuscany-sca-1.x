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
package org.apache.tuscany.model.assembly.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuscany.model.assembly.AssemblyModelContext;
import org.apache.tuscany.model.assembly.AssemblyModelVisitor;
import org.apache.tuscany.model.assembly.Component;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.assembly.Part;
import org.apache.tuscany.model.assembly.ServiceURI;
import org.apache.tuscany.model.assembly.Subsystem;

import commonj.sdo.Sequence;

public class PojoSubsystem implements Subsystem {

    private boolean frozen;

    // ----------------------------------
    // Constructors
    // ----------------------------------

    public PojoSubsystem() {
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    private String uri;

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        check();
        this.name = name;
    }

    private List<EntryPoint> entryPoints = new ArrayList();

    private List<EntryPoint> unModifiableEntryPoints;

    private Map<String, EntryPoint> entryPointMap = new HashMap();

    public List<EntryPoint> getEntryPoints() {
        if (frozen) {
            if (unModifiableEntryPoints == null) {
                unModifiableEntryPoints = Collections.unmodifiableList(entryPoints);
            }
            return unModifiableEntryPoints;
        } else {
            return entryPoints;
        }
    }

    public EntryPoint getEntryPoint(String epName) {
        return entryPointMap.get(epName);
    }

    public void addEntryPoint(EntryPoint ep) {
        check();
        entryPoints.add(ep);
        entryPointMap.put(ep.getName(), ep);
    }

    private List<Component> components = new ArrayList();

    private List<Component> unModifiableComponents;

    private Map<String, Component> componentMap = new HashMap();

    public List<Component> getComponents() {
        if (frozen) {
            if (unModifiableComponents == null) {
                unModifiableComponents = Collections.unmodifiableList(components);
            }
            return unModifiableComponents;
        } else {
            return components;
        }
    }

    public Component getComponent(String compName) {
        return componentMap.get(compName);
    }

    public void addComponent(Component component) {
        check();
        components.add(component);
        componentMap.put(component.getName(), component);
    }

    private List<ExternalService> externalServices = new ArrayList();

    private List<ExternalService> unModifiableExternalServices;

    private Map<String, ExternalService> externalServiceMap = new HashMap();

    public List<ExternalService> getExternalServices() {
        if (frozen) {
            if (unModifiableExternalServices == null) {
                unModifiableExternalServices = Collections.unmodifiableList(externalServices);
            }
            return unModifiableExternalServices;
        } else {
            return externalServices;
        }
    }

    public ExternalService getExternalService(String serviceName) {
        return externalServiceMap.get(serviceName);
    }

    public void addExternalService(ExternalService service) {
        check();
        externalServices.add(service);
        externalServiceMap.put(service.getName(), service);
    }

    private Map<ServiceURI, ConfiguredService> configuredServicesMap = new HashMap();

    public Map<ServiceURI, ConfiguredService> getConfiguredServices() {
        if (frozen) {
            return Collections.unmodifiableMap(configuredServicesMap);
        } else {
            return configuredServicesMap;
        }
    }

    public ConfiguredService getConfiguredService(ServiceURI serviceName) {
        return configuredServicesMap.get(serviceName);
    }

    public void addConfiguredService(ServiceURI serviceUri, ConfiguredService service) {
        check();
        configuredServicesMap.put(serviceUri, service);
    }

    private List<Part> parts = new ArrayList();

    private List<Part> unModifiableParts;

    public List<Part> getParts() {
        if (frozen) {
            if (unModifiableParts == null) {
                unModifiableParts = Collections.unmodifiableList(parts);
            }
            return unModifiableParts;
        } else {
            return parts;
        }
    }

    public void addPart(Part part) {
        check();
        parts.add(part);
    }

    public Sequence getAny() {
        throw new UnsupportedOperationException();
    }

    public Sequence getAnyAttribute() {
        throw new UnsupportedOperationException();
    }

    public void initialize(AssemblyModelContext modelContext) {
        check();
    }

    public void freeze() {
        frozen = true;
    }

    public boolean accept(AssemblyModelVisitor visitor) {
        if (!visitor.visit(this)) {
            return false;
        }
        for (EntryPoint ep : entryPoints) {
            if (!ep.accept(visitor)) {
                return false;
            }
        }
        for (Component component : components) {
            if (!component.accept(visitor)) {
                return false;
            }
        }

        for (ExternalService service : externalServices) {
            if (!service.accept(visitor)) {
                return false;
            }
        }

        for (ConfiguredService service : configuredServicesMap.values()) {
            if (!service.accept(visitor)) {
                return false;
            }
        }
        for (Part part : parts) {
            if (!part.accept(visitor)) {
                return false;
            }
        }
        return true;
    }

    private void check() {
        if (frozen == true) {
            throw new IllegalStateException("Attempt to modify a frozen configuration");
        }
    }
}
