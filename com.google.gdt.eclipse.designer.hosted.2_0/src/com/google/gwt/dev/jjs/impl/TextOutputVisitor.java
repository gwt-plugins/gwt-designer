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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.JVisitor;
import com.google.gwt.dev.util.TextOutput;

/**
 * A convenience base class that combines a {@link JVisitor} with a
 * {@link TextOutput}.
 */
public class TextOutputVisitor extends JVisitor implements TextOutput {

  private final TextOutput textOutput;

  public TextOutputVisitor(TextOutput textOutput) {
    this.textOutput = textOutput;
  }

  public int getPosition() {
    return textOutput.getPosition();
  }

  public void indentIn() {
    textOutput.indentIn();
  }

  public void indentOut() {
    textOutput.indentOut();
  }

  public void newline() {
    textOutput.newline();
  }

  public void newlineOpt() {
    textOutput.newlineOpt();
  }

  public void print(char c) {
    textOutput.print(c);
  }

  public void print(char[] s) {
    textOutput.print(s);
  }

  public void print(String s) {
    textOutput.print(s);
  }

  public void printOpt(char c) {
    textOutput.printOpt(c);
  }

  public void printOpt(char[] s) {
    textOutput.printOpt(s);
  }

  public void printOpt(String s) {
    textOutput.printOpt(s);
  }
}
