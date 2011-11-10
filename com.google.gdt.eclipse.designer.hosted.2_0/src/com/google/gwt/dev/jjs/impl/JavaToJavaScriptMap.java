/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsStatement;

/**
 * A map between chunks of JavaScript to chunks of Java.
 */
public interface JavaToJavaScriptMap {
  /**
   * Return the JavaScript name corresponding to a Java method.
   */
  JsName nameForMethod(JMethod method);

  /**
   * Return the JavaScript name corresponding to a Java type.
   */
  JsName nameForType(JClassType type);

  /**
   * If <code>name</code> is the name of a
   * <code>var<code> that corresponds to a Java
   * static field, then return that field. Otherwise, return null.
   */
  JField nameToField(JsName name);

  /**
   * If <code>name</code> is the name of a function that corresponds to a Java
   * method, then return that method. Otherwise, return null.
   */
  JMethod nameToMethod(JsName name);

  /**
   * If <code>name</code> is the name of a constructor function corresponding to
   * a Java type, then return that type. Otherwise, return <code>null</code>.
   */
  JClassType nameToType(JsName name);

  /**
   * If <code>stat</code> is used to set up the definition of some class, return
   * that class. Otherwise, return null.
   */
  JClassType typeForStatement(JsStatement stat);

  /**
   * If <code>stat</code> is used to set up a vtable entry for a method, then
   * return that method. Otherwise return null.
   */
  JMethod vtableInitToMethod(JsStatement stat);
}
