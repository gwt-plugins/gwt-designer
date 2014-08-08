/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop.ant;

import org.apache.tools.ant.Task;

/**
 * @author Bob Foster
 */
public class Var {

	private String fName;
	private String fValue;
	
	public String getName() {
		return fName;
	}

	public String getValue() {
		return fValue;
	}

	public void setName(String string) {
		fName = string;
	}

	public void setValue(String string) {
		fValue = string;
	}

}
