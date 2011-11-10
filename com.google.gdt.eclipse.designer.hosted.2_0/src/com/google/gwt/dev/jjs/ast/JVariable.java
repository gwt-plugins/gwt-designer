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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Base class for any storage location.
 */
public abstract class JVariable extends JNode implements CanBeSetFinal,
    CanHaveInitializer, HasName, HasType {

  protected JDeclarationStatement declStmt = null;
  private boolean isFinal;
  private final String name;
  private JType type;

  JVariable(SourceInfo info, String name, JType type, boolean isFinal) {
    super(info);
    assert type != null;
    this.name = name;
    this.type = type;
    this.isFinal = isFinal;
  }

  public JLiteral getConstInitializer() {
    JExpression initializer = getInitializer();
    if (isFinal() && initializer instanceof JLiteral) {
      return (JLiteral) initializer;
    }
    return null;
  }

  public JDeclarationStatement getDeclarationStatement() {
    return declStmt;
  }

  public JExpression getInitializer() {
    if (declStmt != null) {
      return declStmt.getInitializer();
    }
    return null;
  }

  public String getName() {
    return name;
  }

  public JType getType() {
    return type;
  }

  public boolean hasInitializer() {
    return declStmt != null;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public void setFinal() {
    isFinal = true;
  }

  public void setType(JType newType) {
    assert newType != null;
    type = newType;
  }

}
