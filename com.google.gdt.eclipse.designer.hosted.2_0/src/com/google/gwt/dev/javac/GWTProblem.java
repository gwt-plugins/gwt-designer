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

import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.dev.jjs.SourceInfo;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * A problem specific to compiling for GWT.
 */
public class GWTProblem extends DefaultProblem {

  public static void recordError(ASTNode node, CompilationUnitDeclaration cud,
      String message, HelpInfo helpInfo) {
    recordProblem(node, cud.compilationResult(), message, helpInfo,
        ProblemSeverities.Error);
  }

  public static void recordError(SourceInfo info, int startColumn,
      CompilationResult compResult, String message, HelpInfo helpInfo) {
    recordProblem(info, startColumn, compResult, message, helpInfo,
        ProblemSeverities.Error);
  }

  public static void recordProblem(ASTNode node, CompilationResult compResult,
      String message, HelpInfo helpInfo, int problemSeverity) {
    int[] lineEnds = compResult.getLineSeparatorPositions();
    int startLine = Util.getLineNumber(node.sourceStart(), lineEnds, 0,
        lineEnds.length - 1);
    int startColumn = Util.searchColumnNumber(lineEnds, startLine,
        node.sourceStart());
    recordProblem(node.sourceStart(), node.sourceEnd(), startLine, startColumn,
        compResult, message, helpInfo, problemSeverity);
  }

  public static void recordProblem(SourceInfo info, int startColumn,
      CompilationResult compResult, String message, HelpInfo helpInfo,
      int problemSeverity) {
    recordProblem(info.getStartPos(), info.getEndPos(), info.getStartLine(),
        startColumn, compResult, message, helpInfo, problemSeverity);
  }

  private static void recordProblem(int startPos, int endPos, int startLine,
      int startColumn, CompilationResult compResult, String message,
      HelpInfo helpInfo, int problemSeverity) {
    DefaultProblem problem = new GWTProblem(compResult.fileName, startPos,
        endPos, startLine, startColumn, message, helpInfo, problemSeverity);
    compResult.record(problem, null);
  }

  private HelpInfo helpInfo;

  private GWTProblem(char[] originatingFileName, int startPosition,
      int endPosition, int line, int column, String message, HelpInfo helpInfo,
      int problemSeverity) {
    super(originatingFileName, message, IProblem.ExternalProblemNotFixable,
        null, problemSeverity, startPosition, endPosition, line, column);
    this.helpInfo = helpInfo;
  }

  public HelpInfo getHelpInfo() {
    return helpInfo;
  }
}
