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
package com.google.gwt.dev.javac;

import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsProgram;

/**
 * Represents a single JsniMethod in a compiled class file.
 */
public abstract class JsniMethod {
  /**
   * If non-null, an anonymous function containing the parameters and body of
   * this JSNI method.
   */
  public abstract JsFunction function();

  /**
   * Starting line number of the method.
   */
  public abstract int line();

  /**
   * Location of the containing compilation unit.
   */
  public abstract String location();

  /**
   * The mangled method name (a jsni signature).
   */
  public abstract String name();

  /**
   * The parameter names.
   */
  public abstract String[] paramNames();

  /**
   * Gets the JsProgram in which {@link #function(TreeLogger)} is located.
   */
  public abstract JsProgram program();
}
