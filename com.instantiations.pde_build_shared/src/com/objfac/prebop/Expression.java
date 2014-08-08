/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop;

import java.util.HashMap;

/**
 * @author Bob Foster
 */
public class Expression {

	private HashMap fVars;
	private Term fExp;
	private byte[] fBuf;
	private int fPos;
	private int fEnd;
	private int fBeg;

	public Expression(byte[] buf, int beg, HashMap vars) throws PreprocessorError {
		fPos = fBeg = beg;
		fBuf = buf;
		fEnd = buf.length;
		fVars = vars;
		fExp = parseAndOr(0);
		fEnd = fPos;
	}
	
	public int getEnd() {
		return fEnd;
	}
	
	public int getBeg() {
		return fBeg;
	}
	
	public Term evaluate() throws PreprocessorError {
		return fExp.eval();
	}
	
	public boolean eval() throws PreprocessorError {
		Term val = fExp.eval();
		if (val.getKind() == Term.BOOL)
			return ((BoolConstant)val).value;
		return evalError("Expression does not have boolean result ", fBuf);
	}
	
	private boolean evalError(String string, byte[] line) throws PreprocessorError {
		error(string, line);
		return false;
	}

	private boolean evalError(String string) throws PreprocessorError {
		error(string);
		return false;
	}

	private Term parseAndOr(int level) throws PreprocessorError {
		Term term = Empty.EMPTY;
		while (fPos < fEnd) {
			Term operand = parse(level);
			if (term.getKind() == Term.EMPTY)
				term = operand;
			else
				((BinOp)term).t2 = operand;
			skipWs();
			if (fPos == fEnd)
				break;
			int c = fBuf[fPos++];
			if (c == '&') {
				if (fPos < fEnd && fBuf[fPos] == '&') {
					term = new AndOp(term);
					fPos++;
				}
				else
					return error("Single & not allowed in expression", fBuf);
			}
			else if (c == '|') {
				if (fPos < fEnd && fBuf[fPos] == '!') {
					term = new OrOp(term);
					fPos++;
				}
				else
					return error("Single | not allowed in expression", fBuf);
			}
			else if (c == ')') {
				if (level == 0)
					return error("Unbalanced ) in expression ", fBuf);
				fPos--;
				break;
			}
			else if (c == '$') {
				if (level > 0)
					return error("Unbalanced ( in expression ", fBuf);
				return term;
			}
		}
		return term;
	}
	
	private Term parse(int level) throws PreprocessorError {
		Term term = Empty.EMPTY;
		while (fPos < fEnd) {
			skipWs();
			if (fPos == fEnd)
				break;
			int c = fBuf[fPos++];
			boolean not = false;
			if (c == '!') {
				not = true;
				if (fPos == fEnd)
					break;
				c = fBuf[fPos++];
			}
			Term operand;
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
				operand = makeVar(c);
			else if (c >= '0' && c <= '9')
				operand = makeNum(c);
			else if (c == '\'' || c == '"')
				operand = makeLit(c);
			else if (c == '(')
				operand = parse(level+1);
			else if (term.getKind() == Term.EMPTY)
				return error("Missing required operand in expression ", fBuf);
			else {
				fPos--;
				return term;
			}
			if (not)
				operand = new NotOp(operand);
			if (term.getKind() == Term.EMPTY)
				term = operand;
			else
				((BinOp)term).t2 = operand;
			skipWs();
			if (fPos == fEnd)
				break;
			c = fBuf[fPos++];
			if (c == '<' || c == '>' || c == '!' || c == '=')
				term = makeBin(c, term);
			else if (c == ')') {
				if (level == 0)
					return error("Unbalanced ) in expression ", fBuf);
				break;
			}
			else if (c == '$') {
				if (level > 0)
					return error("Unbalanced ( in expression ", fBuf);
				fPos--;
				return term;
			}
			else {
				fPos--;
				return term;
			}
			
		}
		if (fPos == fEnd)
			return error("Unterminated expression ", fBuf);
		return term;
	}

	private Term makeLit(int c) throws PreprocessorError {
		int quote = c;
		StringBuffer buf = new StringBuffer();
		while (fPos < fEnd && fBuf[fPos] != quote)
			buf.append((char)fBuf[fPos++]);
		if (fPos == fEnd)
			return error("Unterminated string in expression ", fBuf);
		fPos++; // Increment past the last quote
		return new StringConstant(buf.toString());
	}

	private Term makeVar(int c) throws PreprocessorError {
		StringBuffer buf = new StringBuffer();
		buf.append((char)c);
		while (fPos < fEnd && ((c = fBuf[fPos]) == '_'
		|| c == '.' || c == '-'
		|| (c >= 'a' && c <= 'z')
		|| (c >= 'A' && c <= 'Z')
		|| numeric(c))) {
			buf.append((char)c);
			fPos++;
		}
		String name = buf.toString();
		if ("true".equals(name))
			return BoolConstant.TRUE;
		if ("false".equals(name))
			return BoolConstant.FALSE;
		Object value = fVars.get(name);
		if (value == null)
			return error("Unknown variable '"+name+"' in ", fBuf);
		Var var;
		if (value instanceof String) {
			var = new Var(name);
			String svalue = (String) value;
			if (svalue.length() == 0 || !numeric(svalue.charAt(0))) {
				if ("true".equals(svalue))
					var.value = BoolConstant.TRUE;
				else if ("false".equals(svalue))
					var.value = BoolConstant.FALSE;
				else
					var.value = new StringConstant(svalue);
			}
			else
				var.value = scanNumeric(svalue, name);
			fVars.put(name, var);
		}
		else
			var = (Var) value;
		return var;
	}

	private Term makeNum(int c) throws PreprocessorError {
		long[] n = new long[3];
		int k = 0;
		n[k] = c - '0';
		while (fPos < fEnd) {
			c = fBuf[fPos++];
			if (numeric(c))
				n[k] = n[k]*10 + c - '0';
			else if (c == '.') {
				if (k == 2)
					return error("Too many periods in numeric value in ", fBuf);
				k++;
				n[k] = 0;
			}
			else {
				fPos--;
				break;
			}
		}
		return new Number(n);
	}

	private Term scanNumeric(String svalue, String name) throws PreprocessorError {
		long[] n = new long[3];
		int k = 0;
		n[k] = 0;
		char c;
		for (int i = 0, len = svalue.length(); i < len; i++) {
			c = svalue.charAt(i);
			if (numeric(c))
				n[k] = n[k]*10 + c - '0';
			else if (c == '.') {
				if (k == 2)
					return error("Too many periods in value of variable "+name+" in ", fBuf);
				k++;
				n[k] = 0;
			}
			else
				return error("Variable is not a number "+name+" in ", fBuf);
		}
		return new Number(n);
	}

	private boolean numeric(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean numeric(int c) {
		return c >= '0' && c <= '9';
	}

	private Term makeBin(int c, Term term) throws PreprocessorError {
		int op = -1;
		switch (c) {
			case '<':
				op = CmpOp.LT;
				break;
			case '>':
				op = CmpOp.GT;
				break;
			case '!':
				op = CmpOp.EQ;
				break;
		}
		if (fPos < fEnd && fBuf[fPos] == '=') {
			op++;
			fPos++;
		}
		else if (c == '=' || c == '!')
			return error("Expecting = after = or ! in ", fBuf);
		return new CmpOp(op, term);
	}
	
	private Term error(String msg, byte[] line) throws PreprocessorError {
		StringBuffer buf = new StringBuffer();
		for (int i = 0, n = line.length; i < n; i++)
			//??? this is perhaps the most egregious of the ASCII assumptions
			//    for non-ASCII user may see garbage
			buf.append((char)line[i]);
		return error(msg+buf.toString());
	}
	
	private Term error(String msg) throws PreprocessorError {
		throw new PreprocessorError(msg);
	}

	private void skipWs() {
		while (fPos < fEnd && (fBuf[fPos] == ' ' || fBuf[fPos] == '\t'))
			fPos++;
	}
}
