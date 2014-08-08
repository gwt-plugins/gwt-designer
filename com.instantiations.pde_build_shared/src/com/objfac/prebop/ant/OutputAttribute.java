/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop.ant;

import org.apache.tools.ant.types.EnumeratedAttribute;


public class OutputAttribute extends EnumeratedAttribute {
	/**
	 * @see org.apache.tools.ant.types.EnumeratedAttribute#getValues()
	 */
	public String[] getValues() {
		return new String[] {"create", "replace", "merge"};
	}
}