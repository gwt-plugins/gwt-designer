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
public interface Term {
	
	public static final int EMPTY = 0;
	public static final int CMPOP = 1;
	public static final int VAR = 2;
	public static final int STRING = 3;
	public static final int NUMBER = 4;
	public static final int BOOL = 5;
	public static final int ANDOP = 6;
	public static final int OROP = 7;
	public static final int NOTOP = 8;

	int getKind();
	
	Term eval() throws PreprocessorError;
}
