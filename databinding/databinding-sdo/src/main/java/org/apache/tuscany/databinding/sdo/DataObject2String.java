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
package org.apache.tuscany.databinding.sdo;

import org.apache.tuscany.databinding.TransformationContext;
import org.apache.tuscany.databinding.TransformationException;
import org.apache.tuscany.databinding.PullTransformer;

import commonj.sdo.DataObject;
import commonj.sdo.helper.XMLHelper;

public class DataObject2String implements PullTransformer<DataObject, String> {

    public String transform(DataObject source, TransformationContext context) {
        try {
            return XMLHelper.INSTANCE.save(source, "", "");
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    public Class<DataObject> getSourceType() {
        return DataObject.class;
    }

    public Class<String> getTargetType() {
        return String.class;
    }

    public int getWeight() {
        return 40;
    }

}
