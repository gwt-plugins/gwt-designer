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
 * A kind of {@link Statements} that represents a <code>for</code> loop.
 */
public class ForLoop implements Statements {

  private final StatementsList body;

  private final String initializer;

  private String label;

  private final String step;

  private final String test;

  /**
   * Creates a {@link ForLoop#ForLoop(String,String,String,Statements)} with a
   * null body.
   */
  public ForLoop(String initializer, String test, String step) {
    this(initializer, test, step, null);
  }

  /**
   * Constructs a new <code>ForLoop</code> {@link Node}.
   * 
   * @param initializer The textual initializer {@link Expression}.
   * @param test The textual test {@link Expression}.
   * @param step The textual step {@link Expression}. May be <code>null</code>.
   * @param statements The {@link Statements} for the body of the loop. May be
   *            <code>null</code>.
   */
  public ForLoop(String initializer, String test, String step,
      Statements statements) {
    this.initializer = initializer;
    this.test = test;
    this.step = step;
    this.body = new StatementsList();

    if (statements != null) {
      body.getStatements().add(statements);
    }
  }

  public List<Statements> getStatements() {
    return body.getStatements();
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String toCode() {
    String loop = "for ( " + initializer + "; " + test + "; " + step + " ) {\n"
        + body.toCode() + "\n" + "}\n";

    return label != null ? label + ": " + loop : loop;
  }
}
