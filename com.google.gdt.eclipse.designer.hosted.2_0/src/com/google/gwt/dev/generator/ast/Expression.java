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

/**
 * A {@link Node} that represents a Java expression. An <code>Expression</code>
 * is a parsable value that is a subset of a {@link Statement}. For example,
 *
 * <ul> <li>foo( a, b )</li> <li>14</li> <li>11 / 3</li> <li>x</li> </ul>
 *
 * are all <code>Expressions</code>.
 */
public class Expression extends BaseNode {

  protected String code;

  public Expression() {
    code = "";
  }

  public Expression(String code) {
    this.code = code;
  }

  public String toCode() {
    return code;
  }
}
