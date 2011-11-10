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

import java.util.List;

/**
 * Java try statement.
 */
public class JTryStatement extends JStatement {

  private final List<JLocalRef> catchArgs;
  private final List<JBlock> catchBlocks;
  private final JBlock finallyBlock;
  private final JBlock tryBlock;

  public JTryStatement(SourceInfo info, JBlock tryBlock,
      List<JLocalRef> catchArgs, List<JBlock> catchBlocks, JBlock finallyBlock) {
    super(info);
    assert (catchArgs.size() == catchBlocks.size());
    this.tryBlock = tryBlock;
    this.catchArgs = catchArgs;
    this.catchBlocks = catchBlocks;
    this.finallyBlock = finallyBlock;
  }

  public List<JLocalRef> getCatchArgs() {
    return catchArgs;
  }

  public List<JBlock> getCatchBlocks() {
    return catchBlocks;
  }

  public JBlock getFinallyBlock() {
    return finallyBlock;
  }

  public JBlock getTryBlock() {
    return tryBlock;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      visitor.accept(tryBlock);
      visitor.accept(catchArgs);
      visitor.accept(catchBlocks);
      // TODO: normalize this so it's never null?
      if (finallyBlock != null) {
        visitor.accept(finallyBlock);
      }
    }
    visitor.endVisit(this, ctx);
  }
}
