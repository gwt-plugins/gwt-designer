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
import com.google.gwt.dev.jjs.SourceOrigin;

/**
 * Java boolean literal expression.
 */
public class JBooleanLiteral extends JValueLiteral {

  public static final JBooleanLiteral FALSE = new JBooleanLiteral(
      SourceOrigin.UNKNOWN, false);

  private static final JBooleanLiteral TRUE = new JBooleanLiteral(
      SourceOrigin.UNKNOWN, true);

  public static JBooleanLiteral get(boolean value) {
    return value ? TRUE : FALSE;
  }

  private final boolean value;

  private JBooleanLiteral(SourceInfo sourceInfo, boolean value) {
    super(sourceInfo);
    this.value = value;
  }

  @Override
  public JValueLiteral cloneFrom(JValueLiteral value) {
    return value instanceof JBooleanLiteral ? value : null;
  }

  public JType getType() {
    return JPrimitiveType.BOOLEAN;
  }

  public boolean getValue() {
    return value;
  }

  public Object getValueObj() {
    return Boolean.valueOf(value);
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  private Object readResolve() {
    return get(value);
  }
}
