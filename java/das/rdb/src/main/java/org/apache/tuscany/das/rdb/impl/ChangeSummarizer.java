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
package org.apache.tuscany.das.rdb.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.tuscany.das.rdb.Command;
import org.apache.tuscany.das.rdb.Key;
import org.apache.tuscany.das.rdb.config.Column;
import org.apache.tuscany.das.rdb.config.Relationship;
import org.apache.tuscany.das.rdb.config.Table;
import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
import org.apache.tuscany.das.rdb.config.wrapper.QualifiedColumn;
import org.apache.tuscany.das.rdb.config.wrapper.RelationshipWrapper;
import org.apache.tuscany.das.rdb.config.wrapper.TableWrapper;
import org.apache.tuscany.das.rdb.util.DebugUtil;
import org.apache.tuscany.sdo.impl.ChangeSummaryImpl;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class ChangeSummarizer {

	private static final boolean debug = false;

	private Changes changes = new Changes();

	private FactoryRegistry registry;

	private MappingWrapper mapping = new MappingWrapper();

	private ConnectionImpl connection;

	private HashMap generatedKeys = new HashMap();

	public ChangeSummarizer() {
		// Empty Constructor
	}

	public Changes loadChanges(DataObject root) {
		ChangeSummary changeSummary = root.getDataGraph().getChangeSummary();
		if (changeSummary.isLogging())
			((ChangeSummaryImpl) changeSummary).summarize();

		List changedObjects = changeSummary.getChangedDataObjects();
		DebugUtil.debugln(getClass(), debug,
				"List of changed objects contains " + changedObjects.size()
						+ " object(s)");

		changes.setInsertOrder(mapping.getInsertOrder());
		changes.setDeleteOrder(mapping.getDeleteOrder());

		Iterator i = changedObjects.iterator();
		while (i.hasNext()) {
			DataObject o = (DataObject) i.next();
			
			if (!(o.equals(root))) {
				createChange(changeSummary, o);
			}
		}

		return changes;
	}

	public void createChange(ChangeSummary changeSummary,
			DataObject changedObject) {

		if (changeSummary.isCreated(changedObject)) {
			DebugUtil.debugln(getClass(), debug, "Change is a create");
			if (!changeSummary.isDeleted(changedObject)) {
				ChangeFactory factory = getRegistry().getFactory(
						changedObject.getType());
				String propagatedID = (String) generatedKeys.get(changedObject
						.getType().getName());
				changes.addInsert(factory.createInsertOperation(changedObject,
						propagatedID));
			}
		} else if (changeSummary.isDeleted(changedObject)) {
			ChangeFactory factory = getRegistry().getFactory(
					changedObject.getType());
			DebugUtil.debugln(getClass(), debug, "Change is a delete");
			changes.addDelete(factory.createDeleteOperation(changedObject));
		} else {
			// bumpCollisionField(changedObject);
			DebugUtil.debugln(getClass(), debug, "Change is a modify");
			List attrList = changeSummary.getOldValues(changedObject);
			if (hasAttributeChange(attrList)) {
				ChangeFactory factory = getRegistry().getFactory(
						changedObject.getType());
				DebugUtil.debugln(getClass(), debug, "Attribute Change for "
						+ changedObject.getType().getName());
				String propagatedID = (String) generatedKeys.get(changedObject
						.getType().getName());
				changes.addUpdate(factory.createUpdateOperation(changedObject,
						propagatedID));
			} else {
				// Reference change
				List values = changeSummary.getOldValues(changedObject);
				Iterator i = values.iterator();
				while (i.hasNext()) {
					ChangeSummary.Setting setting = (ChangeSummary.Setting) i
							.next();

					if (!setting.getProperty().getType().isDataType()) {
						DebugUtil.debugln(getClass(), debug,
								"Reference change for "
										+ changedObject.getType().getName());

						Property ref = setting.getProperty();

						DebugUtil.debugln(getClass(), debug, ref.getName());
						if (hasState(ref, changedObject) ) {                     
							ChangeFactory factory = getRegistry().getFactory(
									changedObject.getType());							
							changes.addUpdate(factory
									.createUpdateOperation(changedObject));
						}

					}
				}
			}
		}

	}

	private boolean hasState(Property ref, DataObject changedObject) {
		if ( ref.getOpposite().isMany() ) {
			return true;
		} else {
			MappingWrapper mw = this.mapping;
			if ( mw.getConfig() == null ) 
				mw = registry.getFactory(changedObject.getType()).getConfig();
			if ( mw.getConfig() == null )
				return false;
			
			Relationship rel = mw.getRelationshipByReference(ref);			
			
			if ( !rel.isMany()) {
				// This is a one-one relationship
				Table t = mapping.getTableByPropertyName(changedObject.getType().getName());
				TableWrapper tw = new TableWrapper(t);
				RelationshipWrapper rw = new RelationshipWrapper(rel);
				if (( rel.getForeignKeyTable().equals(t.getName())) &&
						( Collections.disjoint(tw.getPrimaryKeyProperties(),rw.getForeignKeys()) ))
					return true;
				
			}
		
		}
		return false;
	}

	private boolean hasAttributeChange(List theChanges) {
		Iterator i = theChanges.iterator();
		while (i.hasNext()) {
			ChangeSummary.Setting setting = (ChangeSummary.Setting) i.next();
			if (setting.getProperty().getType().isDataType())
				return true;
		}
		return false;
	}

	public void addCreateCommand(Type type, Command cmd) {
		ChangeFactory cf = getRegistry().getFactory(type);
		cf.setCreateCommand((InsertCommandImpl) cmd);
		((CommandImpl) cmd).setConnection(connection);
	}

	public void addUpdateCommand(Type type, Command cmd) {
		ChangeFactory cf = getRegistry().getFactory(type);
		cf.setUpdateCommand((UpdateCommandImpl) cmd);
		((CommandImpl) cmd).setConnection(connection);
	}

	public void addDeleteCommand(Type type, Command cmd) {
		ChangeFactory cf = getRegistry().getFactory(type);
		cf.setDeleteCommand((DeleteCommandImpl) cmd);
		 ((CommandImpl) cmd).setConnection(connection);

	}

	private FactoryRegistry getRegistry() {
		if (this.registry == null) {
			this.registry = new FactoryRegistry(mapping, connection);
		}
		return this.registry;
	}

	public void setConnection(ConnectionImpl connection) {
		this.connection = connection;
	}

	public void setMapping(MappingWrapper map) {
		this.mapping = map;
		
		if ( mapping.getConfig() == null ) 
			return;
	
		Iterator i = mapping.getConfig().getTable().iterator();
		while (i.hasNext()) {
			Table t = (Table) i.next();
			Iterator columns = t.getColumn().iterator();
			while ( columns.hasNext()) {
				Column c = (Column) columns.next();
				if ( c.isPrimaryKey() && c.isGenerated()) {
					DebugUtil.debugln(getClass(), debug, "adding generated key "
							+ t.getName() + "."
							+ c.getName());
					generatedKeys.put(t.getName(), c.getName());
				}
			}
		}
	}

	public void addRelationship(String parentName, String childName) {
		mapping.addRelationship(parentName, childName);
	}

	public void addPrimaryKey(String columnName) {
		mapping.addPrimaryKey(columnName);
	}

	public void addCollisionColumn(String columnName) {
		mapping.addCollisionColumn(columnName);
	}

	public void addPrimarykey(Key key) {
		mapping.addPrimaryKey(key);
	}

	public void addGeneratedPrimaryKey(String columnName) {
		QualifiedColumn col = new QualifiedColumn(columnName);
		generatedKeys.put(col.getTableName(), col.getColumnName());
		mapping.addGeneratedPrimaryKey(columnName);
	}

	public void addConverter(String name, String converterName) {
		mapping.addConverter(name, converterName);
	}

	public ConnectionImpl getConnection() {
		return this.connection;	
	}

    
    
}
