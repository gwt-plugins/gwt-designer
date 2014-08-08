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
public final class BoolConstant implements Term {
	
	public static final BoolConstant TRUE = new BoolConstant(true);
	public static final BoolConstant FALSE = new BoolConstant(false);

	public boolean value;
	
	private BoolConstant(boolean value) {
		this.value = value;
	}
	
	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return BOOL;
	}

	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		return this;
	}

	public boolean compare(BoolConstant other, int op) {
		int cmp = this == other ? 0 : (this == FALSE ? -1 : +1);
		return Number.compare(cmp, op);
	}

}
