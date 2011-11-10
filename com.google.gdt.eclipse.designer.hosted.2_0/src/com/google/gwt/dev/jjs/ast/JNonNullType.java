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

import com.google.gwt.dev.jjs.InternalCompilerException;

/**
 * A type including all the values in some other type except for
 * <code>null</code>.
 */
public class JNonNullType extends JReferenceType {

  private final JReferenceType ref;

  JNonNullType(JReferenceType ref) {
    super(ref.getSourceInfo(), ref.getName());
    assert ref.canBeNull();
    this.ref = ref;
  }

  @Override
  public boolean canBeNull() {
    return false;
  }

  @Override
  public String getClassLiteralFactoryMethod() {
    return ref.getClassLiteralFactoryMethod();
  }

  @Override
  public JClassType getSuperClass() {
    return ref.getSuperClass();
  }

  @Override
  public JReferenceType getUnderlyingType() {
    return ref;
  }

  public boolean isAbstract() {
    return ref.isAbstract();
  }

  public boolean isFinal() {
    return ref.isFinal();
  }

  @Override
  public void setSuperClass(JClassType superClass) {
    throw new InternalCompilerException("should not be called");
  }

  public void traverse(JVisitor visitor, Context ctx) {
    visitor.accept(ref);
  }
}
