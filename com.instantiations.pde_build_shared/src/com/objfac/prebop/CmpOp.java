/*
 * Copyright (c) 2003 Object Factory Inc. All rights reserved.
 */
package com.objfac.prebop;

/**
 * @author Bob Foster
 */
public class CmpOp extends BinOp {

	public static final int EQ = 0;
	public static final int NE = 1;
	public static final int LT = 2;
	public static final int LE = 3;
	public static final int GT = 4;
	public static final int GE = 5;

	public int op;
	
	public CmpOp(int op, Term t1) {
		super(t1);
		this.op = op;
	}
	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return CMPOP;
	}
	
	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		Term r1 = t1.eval();
		Term r2 = t2.eval();
		int kind1 = r1.getKind();
		int kind2 = r2.getKind();
		if (kind1 == kind2) {
			if (kind1 == BOOL)
				return ((BoolConstant)r1).compare((BoolConstant)r2, op) ? BoolConstant.TRUE : BoolConstant.FALSE;
			if (kind1 == NUMBER)
				return ((Number)r1).compare((Number)r2, op) ? BoolConstant.TRUE : BoolConstant.FALSE;
			if (kind1 == STRING)
				return ((StringConstant)r1).compare((StringConstant)r2, op) ? BoolConstant.TRUE : BoolConstant.FALSE;
		}
		throw new PreprocessorError("Comparison operands are not comparable");
	}

}
