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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.UIObjectSizeSupport;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link UIObjectSizeSupport} for {@link CanvasInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public class CanvasSizeSupport extends UIObjectSizeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasSizeSupport(CanvasInfo canvas) {
    super(canvas);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setSize0_addInvocationsToRemove(List<MethodInvocation> oldInvocations) {
    oldInvocations.addAll(getMethodInvocations("resizeTo(int,int)"));
    super.setSize0_addInvocationsToRemove(oldInvocations);
  }

  @Override
  protected void setSize0_addInvocation(StatementTarget target,
      boolean widthHas,
      boolean heightHas,
      String widthString,
      String heightString) throws Exception {
    // may be setRect(left, top, width, height)
    List<MethodInvocation> invocations = getMethodInvocations("setRect(int,int,int,int)");
    if (!invocations.isEmpty()) {
      MethodInvocation invocation = invocations.get(0);
      AstEditor editor = m_object.getEditor();
      {
        Integer widthInteger = getIntegerValue(widthString);
        Integer heightInteger = getIntegerValue(heightString);
        // replace "int" width/height
        if (widthHas && heightHas) {
          if (widthInteger != null && heightInteger != null) {
            editor.replaceInvocationArgument(invocation, 2, widthInteger.toString());
            editor.replaceInvocationArgument(invocation, 3, heightInteger.toString());
            return;
          }
        }
        // replace "int" width
        if (widthHas && !heightHas) {
          if (widthInteger != null) {
            editor.replaceInvocationArgument(invocation, 2, widthInteger.toString());
            return;
          }
        }
        // replace "int" height
        if (heightHas && !widthHas) {
          if (heightInteger != null) {
            editor.replaceInvocationArgument(invocation, 3, heightInteger.toString());
            return;
          }
        }
      }
      // replace with "moveTo"
      {
        List<Expression> arguments = DomGenerics.arguments(invocation);
        String args =
            editor.getSource(arguments.get(0)) + ", " + editor.getSource(arguments.get(1));
        m_object.addMethodInvocation("moveTo(int,int)", args);
        // remove "setRect"
        editor.removeEnclosingStatement(invocation);
      }
    }
    // continue
    super.setSize0_addInvocation(target, widthHas, heightHas, widthString, heightString);
  }

  /**
   * @return the {@link Integer} value if given size string is integer, else <code>null</code>.
   */
  static Integer getIntegerValue(String s) {
    s = StringUtils.substringBetween(s, "\"");
    s = StringUtils.substringBeforeLast(s, "px");
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException e) {
    }
    return null;
  }
}
