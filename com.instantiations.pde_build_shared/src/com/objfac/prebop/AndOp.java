/*
 * Copyright (c) 2003 Object Factory Inc. All rights reserved.
 */
package com.objfac.prebop;

/**
 * @author Bob Foster
 */
public class AndOp extends BinOp {

	public AndOp(Term t1) {
		super(t1);
	}

	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return ANDOP;
	}

	public Term eval() throws PreprocessorError {
		Term r1 = t1.eval();
		if (r1.getKind() != BOOL)
			throw new PreprocessorError("Left operand of && is not boolean");
		if (!((BoolConstant)r1).value)
			return BoolConstant.FALSE;
		Term r2 = t2.eval();
		if (r2.getKind() != BOOL)
			throw new PreprocessorError("Right operand of && is not boolean");
		return r2;
	}
}
