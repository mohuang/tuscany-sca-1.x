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
package org.apache.tuscany.das.rdb.graphbuilder.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.tuscany.das.rdb.config.Relationship;
import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
import org.apache.tuscany.das.rdb.graphbuilder.impl.GraphBuilderMetadata;
import org.apache.tuscany.das.rdb.graphbuilder.impl.ResultMetadata;
import org.apache.tuscany.das.rdb.util.DebugUtil;
import org.apache.tuscany.sdo.util.DataObjectUtil;
import org.apache.tuscany.sdo.util.SDOUtil;

import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.TypeHelper;

/**
 * 
 * An ESchemaMaker is used to create an EMF Schema from an instance of JDBC
 * Mediator Metadata.
 * 
 */
public class ESchemaMaker {

	private final GraphBuilderMetadata metadata;

	private boolean debug = false;

	/**
	 * Constructor for ESchemaMaker. Creates an SDO model based on the metadata
	 * passed in.
	 * 
	 * @param metadata
	 */
	public ESchemaMaker(GraphBuilderMetadata metadata) {
		this.metadata = metadata;
	}	

	/**
	 * Creates an EMF Schema by using the
	 * 
	 * @link TableMaker and
	 * @link RelationshipMaker to transform
	 * @link Metadata elements into EMF Schema elements.
	 */

	public Type createTypes() {
		TypeHelper types = SDOUtil.createTypeHelper();	
		
		DataObjectUtil.initRuntime();
		SDOUtil.createDataGraph();

	
		Type rootType = SDOUtil.createType(types, getURI(), "DataGraphRoot", false);	

		Iterator iter = metadata.getResultMetadata().iterator();
		while (iter.hasNext()) {

			ResultMetadata resultMetadata = (ResultMetadata) iter.next();

			Iterator names = resultMetadata.getAllTablePropertyNames()
					.iterator();
			while (names.hasNext()) {
				String tableName = (String) names.next();
	
				Type tableType = SDOUtil.createType(types, getURI(), tableName, false);										
				Property property = SDOUtil.createProperty(rootType, tableName, tableType);
				SDOUtil.setMany(property,true);		
				SDOUtil.setContainment(property, true);
			}
		
			// TODO tablePropertyMap is temporary until Tuscany-203 is fixed
			HashMap tablePropertyMap = new HashMap();
			
			for (int i = 1; i <= resultMetadata.getColumnNames().size(); i++) {

				Property ref = rootType.getProperty(resultMetadata.getTablePropertyName(i));
				
				// TODO Temporary code to check to see if a property has already been added.
				// Replace when Tuscany-203 is fixed
				ArrayList addedProperties = (ArrayList) tablePropertyMap.get(ref.getName());
				if ( addedProperties == null ) {
					addedProperties = new ArrayList();
					tablePropertyMap.put(ref.getName(), addedProperties);
				}
				
				if (ref == null)
					throw new RuntimeException("Could not find table "
							+ resultMetadata.getTablePropertyName(i)
							+ " in the SDO model");
			
				String columnName = resultMetadata.getColumnPropertyName(i);

				// TODO temporary check until Tuscany-203 is fixed
				if ( !addedProperties.contains(columnName)) {
					addedProperties.add(columnName);
					Type atype = (Type) resultMetadata.getDataType(i);					
	
					SDOUtil.createProperty(ref.getType(), columnName, atype);				

					DebugUtil.debugln(getClass(), debug, "Adding column "
							+ columnName + " to "
							+ resultMetadata.getTablePropertyName(i));	
				}

			}
		}

		if (metadata.hasMappingModel()) {
			MappingWrapper wrapper = new MappingWrapper(metadata.getMapping());
			Iterator i = metadata.getRelationships().iterator();
			while (i.hasNext()) {
				Relationship r = (Relationship) i.next();

				Type parent = rootType.getProperty(
						wrapper.getTablePropertyName(r.getPrimaryKeyTable())).getType();
				Type child = rootType.getProperty(
						wrapper.getTablePropertyName(r.getForeignKeyTable())).getType();
				if (parent == null) {
					throw new RuntimeException("The parent table ("
							+ r.getPrimaryKeyTable() + ") in relationship "
							+ r.getName()
							+ " was not found in the mapping information.");
				} else if (child == null) {
					throw new RuntimeException("The child table ("
							+ r.getForeignKeyTable() + ") in relationship "
							+ r.getName()
							+ " was not found in the mapping information.");
				}

			//	ReferenceImpl ref = refMaker.createReference(r, parent, child);	
				
				Property parentProp = SDOUtil.createProperty(parent, r.getName(), child);	
				Property childProp = SDOUtil.createProperty(child, r.getName() + "_opposite", parent);
				SDOUtil.setOpposite(parentProp, childProp);
				SDOUtil.setOpposite(childProp, parentProp);
				SDOUtil.setMany(parentProp, r.isMany());
				
								

			}

		}
		
		return rootType;
	}

	private String getURI() {
		return "http:///org.apache.tuscany.das.rdb/das";
	}	
	

}
