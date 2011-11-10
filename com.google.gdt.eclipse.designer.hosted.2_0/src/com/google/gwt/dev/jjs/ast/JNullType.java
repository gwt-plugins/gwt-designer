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
import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;

/**
 * Java null reference type.
 */
public class JNullType extends JReferenceType {

  public static final JNullType INSTANCE = new JNullType(SourceOrigin.UNKNOWN);

  private JNullType(SourceInfo sourceInfo) {
    super(sourceInfo, "<null>");
  }

  @Override
  public String getClassLiteralFactoryMethod() {
    throw new InternalCompilerException(
        "Cannot get class literal for null type");
  }

  @Override
  public String getJavahSignatureName() {
    return "N";
  }

  @Override
  public String getJsniSignatureName() {
    return "N";
  }

  public boolean isAbstract() {
    return false;
  }

  public boolean isFinal() {
    return true;
  }

  @Override
  public void setSuperClass(JClassType superClass) {
    throw new InternalCompilerException("should not be called");
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  private Object readResolve() {
    return INSTANCE;
  }
}
