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
 * Base class for any reference type.
 */
public abstract class JReferenceType extends JType implements CanBeAbstract {

  /**
   * This type's super class.
   */
  private JClassType superClass;

  public JReferenceType(SourceInfo info, String name) {
    super(info, name, JNullLiteral.INSTANCE);
  }

  /**
   * Returns <code>true</code> if it's possible for this type to be
   * <code>null</code>.
   * 
   * @see JNonNullType
   */
  public boolean canBeNull() {
    return true;
  }

  @Override
  public String getJavahSignatureName() {
    return "L" + name.replaceAll("_", "_1").replace('.', '_') + "_2";
  }

  @Override
  public String getJsniSignatureName() {
    return "L" + name.replace('.', '/') + ';';
  }

  public String getShortName() {
    int dotpos = name.lastIndexOf('.');
    return name.substring(dotpos + 1);
  }

  /**
   * Returns this type's super class, or <code>null</code> if this type is
   * {@link Object} or the {@link JNullType}.
   */
  public JClassType getSuperClass() {
    return superClass;
  }

  /**
   * If this type is a non-null type, returns the underlying (original) type.
   */
  public JReferenceType getUnderlyingType() {
    return this;
  }

  /**
   * Sets this type's super class.
   */
  public void setSuperClass(JClassType superClass) {
    this.superClass = superClass;
  }
}
