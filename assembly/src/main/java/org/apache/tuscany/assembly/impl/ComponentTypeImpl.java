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
package org.apache.tuscany.assembly.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.assembly.ComponentType;
import org.apache.tuscany.assembly.ConstrainingType;
import org.apache.tuscany.assembly.Property;
import org.apache.tuscany.assembly.Reference;
import org.apache.tuscany.assembly.Service;
import org.apache.tuscany.assembly.util.Visitor;
import org.apache.tuscany.policy.Intent;
import org.apache.tuscany.policy.PolicySet;

/**
 * Represents a component type.
 * 
 * @version $Rev$ $Date$
 */
public class ComponentTypeImpl extends BaseImpl implements ComponentType {
    private ConstrainingType constrainingType;
    private List<Property> properties = new ArrayList<Property>();
    private List<Reference> references = new ArrayList<Reference>();
    private List<Service> services = new ArrayList<Service>();
    private List<Intent> requiredIntents = new ArrayList<Intent>();
    private List<PolicySet> policySets = new ArrayList<PolicySet>();
    
    /**
     * Constructs a new component type.
     */
    public ComponentTypeImpl() {
    }
    
    /**
     * Copy constructor.
     * @param other
     */
    public ComponentTypeImpl(ComponentType other) {
        super(other);
        constrainingType = other.getConstrainingType();
        getServices().clear();
        for (Service service: other.getServices()) {
            getServices().add(new ServiceImpl(service));
        }
        getReferences().clear();
        for (Reference reference: other.getReferences()) {
            getReferences().add(new ReferenceImpl(reference));
        }
        for (Property property: other.getProperties()) {
            getProperties().add(new PropertyImpl(property));
        }
        requiredIntents.addAll(other.getRequiredIntents());
        policySets.addAll(other.getPolicySets());
    }

    public ConstrainingType getConstrainingType() {
        return constrainingType;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setConstrainingType(ConstrainingType constrainingType) {
        this.constrainingType = constrainingType;
    }

    public List<Intent> getRequiredIntents() {
        return requiredIntents;
    }

    public List<PolicySet> getPolicySets() {
        return policySets;
    }

    public boolean accept(Visitor visitor) {
        if (!super.accept(visitor)) {
            return false;
        }
        for (Property property : properties) {
            if (!visitor.visit(property)) {
                return false;
            }
        }
        for (Reference reference : references) {
            if (!visitor.visit(reference)) {
                return false;
            }
        }
        for (Service service : services) {
            if (!visitor.visit(service)) {
                return false;
            }
        }
        return true;
    }
}
