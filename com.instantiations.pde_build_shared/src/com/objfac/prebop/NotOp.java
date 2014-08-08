/*
 * Copyright (c) 2003 Object Factory Inc. All rights reserved.
 */
package com.objfac.prebop;

/**
 * @author Bob Foster
 */
public class NotOp implements Term {

	public Term t;
	
	public NotOp(Term t) {
		this.t = t;
	}

	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return NOTOP;
	}

	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		Term r = t.eval();
		if (r.getKind() != BOOL)
			throw new PreprocessorError("Left operand of && is not boolean");
		return ((BoolConstant)r).value ? BoolConstant.FALSE : BoolConstant.TRUE;
	}

}
