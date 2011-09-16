/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.typeinfo.JAbstractMethod;
import com.google.gwt.dev.javac.asm.CollectMethodData;
import com.google.gwt.dev.util.collect.Maps;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Keeps track of method argument names that cannot be read from just the
 * bytecode.
 */
public class MethodArgNamesLookup {

  private Map<String, String[]> methodArgs;

  public MethodArgNamesLookup() {
    this.methodArgs = new HashMap<String, String[]>();
  }

  /**
   * Prevent further modification to this object.  Calls to
   * {@link #store(String, AbstractMethodDeclaration)} or 
   * {@link #mergeFrom(MethodArgNamesLookup)} on this object will fail after
   * this method is called.
   */
  public void freeze() {
    methodArgs = Maps.normalizeUnmodifiable(methodArgs);
  }
  
  /**
   * Lookup the argument names for a given method.
   * 
   * @param method TypeOracle method
   * @param methodData method data collected from bytecode
   * @return an array of the argument names, or null if unavailable
   */
  public String[] lookup(JAbstractMethod method, CollectMethodData methodData) {
    StringBuilder buf = new StringBuilder();
    buf.append(method.getEnclosingType().getQualifiedBinaryName());
    buf.append('.').append(method.getName());
    buf.append(methodData.getDesc());
    String key = buf.toString();
    return methodArgs.get(key);
  }

  /**
   * Merge argument names from another lookup map into this one.
   * 
   * @param other
   */
  public void mergeFrom(MethodArgNamesLookup other) {
    methodArgs.putAll(other.methodArgs);
  }

  /**
   * Store the argument names for a method.
   * <p>
   * <b>Note: method must have non-zero arguments.<b>
   * 
   * @param enclosingType fully qualified binary name of the enclosing type
   * @param method JDT method
   */
  public void store(String enclosingType,
      AbstractMethodDeclaration method) {
    int n = method.arguments.length;
    String[] argNames = new String[n];
    for (int i = 0; i < n; ++i) {
      argNames[i] = String.valueOf(method.arguments[i].name);
    }
    StringBuilder buf = new StringBuilder();
    buf.append(enclosingType).append('.').append(method.selector);
    buf.append(method.binding.signature());
    String key = buf.toString();
    methodArgs.put(key, argNames);
  }
  // XXX >>> Instantiations
  // load/save cache
  public static void save(MethodArgNamesLookup argNamesLookup, DataOutputStream stream) throws Exception {
    // much faster than HashMap serialize
    stream.writeInt(argNamesLookup.methodArgs.size());
    if (argNamesLookup.methodArgs.isEmpty()) {
      return;
    }
    for (Entry<String, String[]> entry : argNamesLookup.methodArgs.entrySet()) {
      stream.writeUTF(entry.getKey());
      String[] values = entry.getValue();
      stream.writeInt(values.length);
      for (String value : values) {
		stream.writeUTF(value);
	  }
	}
  }
  public static MethodArgNamesLookup load(DataInputStream stream) throws Exception {
    MethodArgNamesLookup result = new MethodArgNamesLookup();
    int mapSize = stream.readInt();
    for (int i = 0; i < mapSize; ++i) {
      String key = stream.readUTF();
      int valuesSize = stream.readInt();
      String[] values = new String[valuesSize];
      for (int j = 0; j < valuesSize; ++j) {
		values[j] = stream.readUTF();
	  }
      result.methodArgs.put(key, values);
	}
    result.freeze();
    return result;
  }
  // XXX <<< Instantiations
}
