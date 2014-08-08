/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop;

/**
 * @author Bob Foster
 */
public abstract class BinOp implements Term {
	
	public Term t1;
	public Term t2;
	
	public BinOp(Term t1) {
		this.t1 = t1;
	}

}
