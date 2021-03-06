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
package org.apache.tuscany.das.rdb.test.commands;

import org.apache.tuscany.das.rdb.ResultSetShape;
import org.apache.tuscany.das.rdb.SDODataTypes;
import org.apache.tuscany.das.rdb.impl.ReadCommandImpl;

import commonj.sdo.Type;

public class ReadCustomersWithShapeCommand extends ReadCommandImpl {

	// This sql string ensures that we won't have resultset metadata
	private static final String sqlString = "Select * from customer union select * from customer";

	private static final String[] columns = {"ID", "LASTNAME", "ADDRESS"};
	private static final String[] tables = {"CUSTOMER", "CUSTOMER", "CUSTOMER"};
	private static final Type[] types = {SDODataTypes.INT, SDODataTypes.STRING, SDODataTypes.STRING};

	public ReadCustomersWithShapeCommand() {
		super(sqlString);
		ResultSetShape shape = new ResultSetShape(tables, columns, types);
		setResultSetShape(shape);
	}

}
