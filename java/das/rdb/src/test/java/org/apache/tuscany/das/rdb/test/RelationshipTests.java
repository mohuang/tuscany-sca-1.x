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
package org.apache.tuscany.das.rdb.test;

/*
 * 
 * 
 */

import org.apache.tuscany.das.rdb.ApplyChangesCommand;
import org.apache.tuscany.das.rdb.Command;
import org.apache.tuscany.das.rdb.SDODataTypes;
import org.apache.tuscany.das.rdb.test.data.CustomerData;
import org.apache.tuscany.das.rdb.test.data.OrderData;
import org.apache.tuscany.das.rdb.test.framework.DasTest;

import commonj.sdo.DataObject;


public class RelationshipTests extends DasTest {

	protected void setUp() throws Exception {
		super.setUp();
		
		new CustomerData(getAutoConnection()).refresh();
		new OrderData(getAutoConnection()).refresh();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test ability to read a compound graph
	 */
	public void testRead() throws Exception {

		String statement = "SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID WHERE CUSTOMER.ID = 1";

		// Read some customers and related orders
		Command select = Command.FACTORY.createCommand(statement, getConfig("customerOrderRelationshipMapping.xml"));
		select.setConnection(getConnection());

		DataObject root = select.executeQuery();
		DataObject customer = root.getDataObject("CUSTOMER[1]");

		assertEquals(2, customer.getList("orders").size());

	}

	/**
	 * Same as above except uses xml file for relationhip and key information.
	 * Employs CUD generation.
	 */
	public void testRelationshipModification2() throws Exception {

		// Read some customers and related orders
		Command select = Command.FACTORY
				.createCommand(
						"SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID", getConfig("basicCustomerOrderMapping.xml"));
		select.setConnection(getConnection());

		DataObject root = select.executeQuery();

		DataObject cust1 = root.getDataObject("CUSTOMER[1]");
		DataObject cust2 = root.getDataObject("CUSTOMER[2]");

		// Save IDs
		Integer cust1ID = (Integer) cust1.get("ID");
		Integer cust2ID = (Integer) cust2.get("ID");
		// save order count
		Integer cust1OrderCount = new Integer(cust1.getList("orders").size());
		Integer cust2OrderCount = new Integer(cust2.getList("orders").size());

		// Move an order to cust1 from cust2
		DataObject order = (DataObject) cust2.getList("orders").get(0);
		cust1.getList("orders").add(order);
		
		// Build apply changes command
		ApplyChangesCommand apply = Command.FACTORY.createApplyChangesCommand(getConfig("basicCustomerOrderMapping.xml"));
		apply.setConnection(getConnection());

		// Flush changes
		apply.execute(root);

		// verify cust1 relationship updates
		select = Command.FACTORY
				.createCommand(
						"SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID where CUSTOMER.ID = :ID",
						getConfig("basicCustomerOrderMapping.xml"));
		select.setConnection(getConnection());
		select.setParameterValue("ID", cust1ID);

		root = select.executeQuery();
		assertEquals(cust1OrderCount.intValue() + 1, root.getList(
				"CUSTOMER[1]/orders").size());

		// verify cust2 relationship updates
		select.setParameterValue("ID", cust2ID);
		root = select.executeQuery();
		assertEquals(cust2OrderCount.intValue() - 1, root.getList(
				"CUSTOMER[1]/orders").size());

	}

}
