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
public class Number implements Term {

	public long[] value;

	public Number(long[] value) {
		this.value = value;
	}
	
	public int compare(Number other) {
		int cmp = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] != other.value[i]) {
				cmp = value[i] < other.value[i] ? -1 : 1;
				break;
			}
		}
		return cmp;
	}
	
	public boolean compare(Number other, int op) {
		int cmp = compare(other);
		return compare(cmp, op);
	}
	
	public static boolean compare(int cmp, int op) {
		switch (op) {
			case CmpOp.EQ:
				return cmp == 0;
			case CmpOp.NE:
				return cmp != 0;
			case CmpOp.LT:
				return cmp < 0;
			case CmpOp.LE:
				return cmp <= 0;
			case CmpOp.GT:
				return cmp > 0;
			case CmpOp.GE:
				return cmp >= 0;
			default:
				return false;
		}
	}
	
	/**
	 * @see com.objfac.preprocessor.Term#getKind()
	 */
	public int getKind() {
		return NUMBER;
	}

	/**
	 * @see com.objfac.preprocessor.Term#eval()
	 */
	public Term eval() throws PreprocessorError {
		return this;
	}

}
