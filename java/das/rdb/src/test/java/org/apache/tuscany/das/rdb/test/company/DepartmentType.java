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
package org.apache.tuscany.das.rdb.test.company;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Department Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getEmployees <em>Employees</em>}</li>
 *   <li>{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getLocation <em>Location</em>}</li>
 *   <li>{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getNumber <em>Number</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public interface DepartmentType
{
  /**
   * Returns the value of the '<em><b>Employees</b></em>' containment reference list.
   * The list contents are of type {@link org.apache.tuscany.das.rdb.test.company.EmployeeType}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Employees</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Employees</em>' containment reference list.
   * @generated
   */
  List getEmployees();

  /**
   * Returns the value of the '<em><b>Location</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Location</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Location</em>' attribute.
   * @see #setLocation(String)
   * @generated
   */
  String getLocation();

  /**
   * Sets the value of the '{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getLocation <em>Location</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Location</em>' attribute.
   * @see #getLocation()
   * @generated
   */
  void setLocation(String value);

  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Number</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Number</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Number</em>' attribute.
   * @see #isSetNumber()
   * @see #unsetNumber()
   * @see #setNumber(int)
   * @generated
   */
  int getNumber();

  /**
   * Sets the value of the '{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getNumber <em>Number</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Number</em>' attribute.
   * @see #isSetNumber()
   * @see #unsetNumber()
   * @see #getNumber()
   * @generated
   */
  void setNumber(int value);

  /**
   * Unsets the value of the '{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getNumber <em>Number</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isSetNumber()
   * @see #getNumber()
   * @see #setNumber(int)
   * @generated
   */
  void unsetNumber();

  /**
   * Returns whether the value of the '{@link org.apache.tuscany.das.rdb.test.company.DepartmentType#getNumber <em>Number</em>}' attribute is set.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return whether the value of the '<em>Number</em>' attribute is set.
   * @see #unsetNumber()
   * @see #getNumber()
   * @see #setNumber(int)
   * @generated
   */
  boolean isSetNumber();

} // DepartmentType
