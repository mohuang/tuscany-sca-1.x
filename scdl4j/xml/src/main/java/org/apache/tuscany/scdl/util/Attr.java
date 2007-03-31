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

package org.apache.tuscany.scdl.util;

import javax.xml.namespace.QName;

import org.xml.sax.helpers.AttributesImpl;

public class Attr {
	
	String uri;
	String name;
	Object value;
	
	public Attr(String uri, String name, String value) {
		this.uri = uri;
		this.name = name;
		this.value = value;
	}

	public Attr(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Attr(String uri, String name, boolean value) {
		this.uri = uri;
		this.name = name;
		this.value = value;
	}

	public Attr(String name, boolean value) {
		this.name = name;
		this.value = value;
	}
	
	public Attr(String uri, String name, QName value) {
		this.uri = uri;
		this.name = name;
		this.value = value;
	}

	public Attr(String name, QName value) {
		this.name = name;
		this.value = value;
	}
	
	void write(AttributesImpl attrs) {
		if (value != null) {
			attrs.addAttribute(uri, name, name, "CDATA", String.valueOf(value));
		}
	}
	
}
