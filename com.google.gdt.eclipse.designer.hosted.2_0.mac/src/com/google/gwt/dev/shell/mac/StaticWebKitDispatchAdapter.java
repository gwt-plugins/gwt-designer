/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gwt.dev.shell.mac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gwt.dev.javac.CompilationUnit;
import com.google.gwt.dev.javac.JsniMethod;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.Jsni;
import com.google.gwt.dev.util.JsniRef;

/**
 * The implementation of {@link WebKitDispatchAdapter} which able to provide the references to static fields
 * in jsni code.
 * 
 * @author mitin_aa
 */
final class StaticWebKitDispatchAdapter extends WebKitDispatchAdapter {
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private final Stack<String> m_executionStack = new Stack<String>();
	private final Map<Integer, String[]> m_fieldsMap = new HashMap<Integer, String[]>();
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	StaticWebKitDispatchAdapter(CompilingClassLoader cl) {
		super(cl);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Fields list support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String[] getFields() {
		try {
			if (m_executionStack.empty()) {
				return EMPTY_STRING_ARRAY;
			}
			String currentMethod = m_executionStack.peek();
			int dispId = classLoader.getDispId(currentMethod);
			if (dispId == -1) {
				return EMPTY_STRING_ARRAY;
			}
			String[] fieldReferences = m_fieldsMap.get(dispId);
			if (fieldReferences == null) {
				String[] fieldReferencesGot = getJsniFieldReferences(currentMethod);
				fieldReferences = new String[fieldReferencesGot.length];
				int index = 0;
				for (int i = 0; i < fieldReferencesGot.length; i++) {
					int fieldDispId = classLoader.getDispId(fieldReferencesGot[i]);
					if (fieldDispId > 0) {
						fieldReferences[index++] = Jsni.WBP_MEMBER + String.valueOf(fieldDispId);
					}
				}
				System.arraycopy(fieldReferences, 0, fieldReferences = new String[index], 0, index);
				m_fieldsMap.put(dispId, fieldReferences);
			}
			return fieldReferences;
		} catch (Throwable e) {
			// capture all exceptions to prevent them to go into native code
			e.printStackTrace();
			return EMPTY_STRING_ARRAY;
		}
	}
	/**
	 * Searches for method body for any references to static fields and return the array of references found.
	 * 
	 * @param methodSignature
	 *            the method jsni signature which body will be searched for field references.
	 */
	private String[] getJsniFieldReferences(String methodSignature) {
		JsniRef parsed = JsniRef.parse(methodSignature);
		if (parsed != null) {
			String lookupClassName = parsed.className().replace('.', '/');
			CompilationUnit compilationUnit = classLoader.getUnitForClassName(lookupClassName);
			if (compilationUnit != null) {
				for (JsniMethod jsniMethod : compilationUnit.getJsniMethods()) {
					if (jsniMethod.name().equals(methodSignature)) {
						String source = jsniMethod.function().toSource();
						return parseJsniFieldReferences(source);
					}
				}
			}
		}
		return EMPTY_STRING_ARRAY;
	}
	/**
	 * Parses the method body and looks up for static field references.
	 * 
	 * @return the field references as the array of {@link String}.
	 */
	private String[] parseJsniFieldReferences(String code) {
		Set<String> fieldRefs = new HashSet<String>();
		if (code != null && code.indexOf('@') != -1) {
			while (true) {
				int atIndex = code.indexOf('@');
				if (atIndex == -1) {
					break;
				}
				//
				int closeIndex;
				String memberSignature;
				{
					closeIndex = atIndex;
					for (;; closeIndex++) {
						char c = code.charAt(closeIndex);
						if (c == '(') {
							closeIndex = code.indexOf(')', closeIndex) + 1;
							break;
						}
						if (c != '@'
							&& c != '.'
							&& c != ':'
							&& !Character.isLetter(c)
							&& !Character.isDigit(c)) {
							break;
						}
					}
					memberSignature = code.substring(atIndex, closeIndex);
				}
				// check for 'name.@XXX' and skip it
				if (code.charAt(atIndex - 1) != '.') {
					// do additional check using Jsni.parse() method
					JsniRef parsedSignature = JsniRef.parse(memberSignature);
					if (parsedSignature != null && parsedSignature.isField()) {
						String fieldName = parsedSignature.memberName();
						int referenceIndex = fieldName.indexOf('.');
						if (referenceIndex != -1) {
							fieldName = fieldName.substring(0, referenceIndex);
						}
						fieldRefs.add("@" + parsedSignature.className() + "::" + fieldName);
					}
				}
				code = code.substring(closeIndex, code.length());
			}
		}
		return fieldRefs.toArray(new String[fieldRefs.size()]);
	}
	public void push(String method) {
		m_executionStack.push(method);
	}
	public String pop() {
		return m_executionStack.pop();
	}
}
