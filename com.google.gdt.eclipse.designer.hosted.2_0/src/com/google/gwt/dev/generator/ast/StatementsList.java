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

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link Statements} that is composed of a list of
 * {@link Statements}.
 */
public class StatementsList extends BaseNode implements Statements {

  private final List<Statements> statements = new ArrayList<Statements>();

  /**
   * Creates a new <code>StatementsList</code> with no {@link Statements}.
   */
  public StatementsList() {
  }

  /**
   * Returns the {@link Statements} that are in this list.
   */
  public List<Statements> getStatements() {
    return statements;
  }

  @Override
  public String toCode() {
    StringBuffer code = new StringBuffer();
    for (Statements stmts : statements) {
      code.append(stmts.toCode()).append("\n");
    }
    return code.toString();
  }
}
