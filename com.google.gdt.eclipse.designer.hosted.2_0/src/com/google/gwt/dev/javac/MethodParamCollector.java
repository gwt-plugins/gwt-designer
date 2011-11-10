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

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

/**
 * Collects method parameter names.
 */
public class MethodParamCollector {

  private static class Visitor extends MethodVisitor {

    private final MethodArgNamesLookup methodArgs;

    public Visitor(MethodArgNamesLookup methodArgs) {
      this.methodArgs = methodArgs;
    }

    @Override
    protected boolean interestingMethod(AbstractMethodDeclaration method) {
      return method.arguments != null && method.arguments.length > 0
          && method.isAbstract();
    }

    @Override
    protected void processMethod(TypeDeclaration typeDecl,
        AbstractMethodDeclaration method, String enclosingType, String loc) {
      methodArgs.store(enclosingType, method);
    }
  }

  /**
   * Returns an unmodifiable MethodArgNamesLookup containing the method argument
   * names for the supplied compilation unit.
   * 
   * @param cud
   * @return MethodArgNamesLookup instance
   */
  public static MethodArgNamesLookup collect(CompilationUnitDeclaration cud) {
    MethodArgNamesLookup methodArgs = new MethodArgNamesLookup();
    new Visitor(methodArgs).collect(cud);
    methodArgs.freeze();
    return methodArgs;
  }
}
