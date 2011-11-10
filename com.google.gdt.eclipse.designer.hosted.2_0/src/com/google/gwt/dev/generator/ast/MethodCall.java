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
package com.google.gwt.dev.generator.ast;

import java.util.List;

/**
 * An {@link Expression} that represents a method call, for example,
 * <code>foo( a, b, c )</code>.
 */
public class MethodCall extends Expression {

  /**
   * Creates a new MethodCall Expression.
   * 
   * @param name The name of the method. This must contain the qualified target
   *            expression if it is not implicitly this. For example, "foo.bar".
   * 
   * @param arguments The list of Expressions that are the arguments for the
   *            call.
   */
  public MethodCall(String name, List<String> arguments) {
    StringBuffer call = new StringBuffer(name + "(");

    if (arguments != null) {
      call.append(" ");
      for (int i = 0; i < arguments.size(); ++i) {
        call.append(arguments.get(i));
        if (i < arguments.size() - 1) {
          call.append(", ");
        }
      }
      call.append(" ");
    }

    call.append(")");
    super.code = call.toString();
  }
}
