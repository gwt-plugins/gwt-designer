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
public class StringConstant implements Term {

	public String value;
	
	public StringConstant(String value) {
		this.value = value;
	}
	
	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return STRING;
	}

	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		return this;
	}
	
	public boolean compare(StringConstant other, int op) {
		int cmp = value.compareTo(other.value);
		return Number.compare(cmp, op);
	}

}
