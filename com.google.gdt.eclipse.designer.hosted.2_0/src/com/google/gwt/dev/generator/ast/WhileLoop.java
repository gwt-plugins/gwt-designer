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

/**
 * A Node that represents a Java <code>while</code> loop.
 */
public class WhileLoop implements Statements {

  private final StatementsList body = new StatementsList();

  private final String test;

  /**
   * Creates a new <code>while</code> loop with <code>test</code> as the test
   * {@link Expression}. The <code>WhileLoop</code> has an empty body.
   *
   * @param test A textual <code>boolean</code> {@link Expression}. Must not be
   * <code>null</code>.
   */
  public WhileLoop(String test) {
    this.test = test;
  }

  public List<Statements> getStatements() {
    return body.getStatements();
  }

  public String toCode() {
    return "while ( " + test + " ) {\n" +
        body.toCode() + "\n" +
        "}\n";
  }
}
