/*
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.google.gdt.eclipse.designer.copied;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * @coverage gwtHosted
 */
public class Util {
	public static int getLineNumber(int position, int[] lineEnds, int g, int d) {
		if (lineEnds == null)
			return 1;
		if (d == -1)
			return 1;
		int m = g, start;
		while (g <= d) {
			m = g + (d - g) / 2;
			if (position < (start = lineEnds[m])) {
				d = m - 1;
			} else if (position > start) {
				g = m + 1;
			} else {
				return m + 1;
			}
		}
		if (position < lineEnds[m]) {
			return m + 1;
		}
		return m + 2;
	}
	/**
	 * INTERNAL USE-ONLY
	 * Search the column number corresponding to a specific position
	 */
	public static final int searchColumnNumber(int[] startLineIndexes, int lineNumber, int position) {
		switch (lineNumber) {
			case 1 :
				return position + 1;
			case 2 :
				return position - startLineIndexes[0];
			default :
				int line = lineNumber - 2;
				int length = startLineIndexes.length;
				if (line >= length) {
					return position - startLineIndexes[length - 1];
				}
				return position - startLineIndexes[line];
		}
	}
	public static ReferenceBinding genericType(ParameterizedTypeBinding ptBinding) {
		return ptBinding.genericType(); 
	}
}
