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
public class Empty implements Term {

	public static final Empty EMPTY = new Empty();

	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return Term.EMPTY;
	}

	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		throw new PreprocessorError("Empty expression");
	}
}
