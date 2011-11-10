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
import java.util.Arrays;

/**
 * A {@link Node} that represents a single Java statement.
 */
public class Statement extends BaseNode implements Statements {

  private String code;

  private Expression expression;

  private final List<Statements> list;

  /**
   * Creates a new <code>Statement</code> from a {@link String} of code
   * representing an {@link Expression}. Automatically appends a semicolon to
   * <code>code</code>.
   * 
   * @param code A textual {@link Expression}. Should not end with a semicolon.
   */
  public Statement(String code) {
    this.code = code;
    this.list = Arrays.asList((Statements) this);
  }

  /**
   * Creates a new <code>Statement</code> from an {@link Expression}.
   * 
   * @param expression A non <code>null</code> {@link Expression}.
   */
  public Statement(Expression expression) {
    this.expression = expression;
    this.list = Arrays.asList((Statements) this);
  }

  /**
   * Returns this single <code>Statement</code> as a {@link java.util.List} of
   * {@link Statement}s of size, one.
   */
  public List<Statements> getStatements() {
    return list;
  }

  @Override
  public String toCode() {
    if (expression != null) {
      return expression.toCode() + ";";
    } else {
      return code + ";";
    }
  }
}
