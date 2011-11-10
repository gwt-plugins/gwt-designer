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
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsProgram;

/**
 * Resolves any unresolved JsNameRefs.
 */
public class JsSymbolResolver {

  /**
   * Resolves any unresolved JsNameRefs.
   */
  private class JsResolveSymbolsVisitor extends JsAbstractSymbolResolver {

    @Override
    protected void resolve(JsNameRef x) {
      JsName name;
      String ident = x.getIdent();
      if (x.getQualifier() == null) {
        name = getScope().findExistingName(ident);
        if (name == null) {
          // No clue what this is; create a new unobfuscatable name
          name = program.getRootScope().declareName(ident);
          name.setObfuscatable(false);
        }
      } else {
        name = program.getObjectScope().findExistingName(ident);
        if (name == null) {
          // No clue what this is; create a new unobfuscatable name
          name = program.getObjectScope().declareName(ident);
          name.setObfuscatable(false);
        }
      }
      x.resolve(name);
    }
  }

  public static void exec(JsProgram program) {
    new JsSymbolResolver(program).execImpl();
  }

  private final JsProgram program;

  private JsSymbolResolver(JsProgram program) {
    this.program = program;
  }

  private void execImpl() {
    JsResolveSymbolsVisitor resolver = new JsResolveSymbolsVisitor();
    resolver.accept(program);
  }
}
