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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.WidgetTopBoundsSupport;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link TopBoundsSupport} for {@link WindowInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.top
 */
public class WindowTopBoundsSupport extends WidgetTopBoundsSupport {
  private final WindowInfo m_window;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WindowTopBoundsSupport(WindowInfo window) {
    super(window);
    m_window = window;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    if (hasMethodInvocations(new String[]{
        "setSize(int,int)",
        "setSize(java.lang.String,java.lang.String)",
        "setWidth(int)",
        "setWidth(java.lang.String)",
        "setHeight(int)",
        "setHeight(java.lang.String)"})) {
      m_window.putArbitraryValue("wbp-Window-noSize", "true");
    } else {
      m_window.removeArbitraryValue("wbp-Window-noSize");
    }
    super.apply();
    // We set size of Window before changing size of Shell with browser,
    // so when Window centers itself in RootPanel, Window position may be negative.
    // This causes later problem with cropping shot image.
    // So, we ensure that Window position is valid.
    JavaInfoUtils.executeScript(m_window, "object.setPosition(10, 10);");
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    if (setSizeInts(width, height)) {
      return;
    }
    if (setSizeStrings(width, height)) {
      return;
    }
    boolean wh = false;
    wh |= setWidthInt(width);
    wh |= setWidthString(width);
    wh |= setHeightInt(height);
    wh |= setHeightString(height);
    if (wh) {
      return;
    }
    super.setSize(width, height);
  }

  private boolean setSizeInts(int width, int height) throws Exception {
    MethodInvocation invocation = m_window.getMethodInvocation("setSize(int,int)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression((Expression) invocation.arguments().get(0), Integer.toString(width));
      editor.replaceExpression((Expression) invocation.arguments().get(1), Integer.toString(height));
      return true;
    }
    // not found
    return false;
  }

  private boolean setSizeStrings(int width, int height) throws Exception {
    MethodInvocation invocation =
        m_window.getMethodInvocation("setSize(java.lang.String,java.lang.String)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression(
          (Expression) invocation.arguments().get(0),
          "\"" + Integer.toString(width) + "px\"");
      editor.replaceExpression(
          (Expression) invocation.arguments().get(1),
          "\"" + Integer.toString(height) + "px\"");
      return true;
    }
    // not found
    return false;
  }

  private boolean setWidthInt(int width) throws Exception {
    MethodInvocation invocation = m_window.getMethodInvocation("setWidth(int)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression((Expression) invocation.arguments().get(0), Integer.toString(width));
      return true;
    }
    // not found
    return false;
  }

  private boolean setWidthString(int width) throws Exception {
    MethodInvocation invocation = m_window.getMethodInvocation("setWidth(java.lang.String)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression(
          (Expression) invocation.arguments().get(0),
          "\"" + Integer.toString(width) + "px\"");
      return true;
    }
    // not found
    return false;
  }

  private boolean setHeightInt(int height) throws Exception {
    MethodInvocation invocation = m_window.getMethodInvocation("setHeight(int)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression((Expression) invocation.arguments().get(0), Integer.toString(height));
      return true;
    }
    // not found
    return false;
  }

  private boolean setHeightString(int height) throws Exception {
    MethodInvocation invocation = m_window.getMethodInvocation("setHeight(java.lang.String)");
    if (invocation != null) {
      AstEditor editor = m_window.getEditor();
      editor.replaceExpression(
          (Expression) invocation.arguments().get(0),
          "\"" + Integer.toString(height) + "px\"");
      return true;
    }
    // not found
    return false;
  }
}
