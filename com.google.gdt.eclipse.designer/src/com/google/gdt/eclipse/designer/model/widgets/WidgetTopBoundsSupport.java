/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.hosted.IBrowserShell;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TopBoundsSupport} for any {@link WidgetInfo} except
 * <code>RootPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.top
 */
public class WidgetTopBoundsSupport extends TopBoundsSupport {
  private final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetTopBoundsSupport(WidgetInfo widget) {
    super(widget);
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This constant is using to expand size of {@link IBrowserShell} relative to size of widget. We
   * need to do this because (for unknown reason) in other case GWT DialogBox and PopupPanel do
   * wrapping after changing {@link IBrowserShell} size, so change size of image to take.
   */
  private static final int SIZE_EXPAND = 50;

  @Override
  public void apply() throws Exception {
    m_widget.getBroadcast(WidgetAttachAfterConstructor.class).invoke();
    // remove borders
    dontUseBorderForRootPanel();
    // ensure attached
    invokeAttachScript();
    // apply size to get actual widget size
    Dimension resourceSize = getResourceSize();
    doApply(resourceSize);
    // get the real "size" to include full widget
    Dimension size = getExpandedSize();
    // correct size to fit into top-level root panel
    Dimension correctedSize = new Dimension(resourceSize);
    correctedSize.width -= size.width - resourceSize.width;
    correctedSize.height -= size.height - resourceSize.height;
    // apply corrected size to the widget
    if (correctedSize.width > 0 && correctedSize.height > 0) {
      doApply(correctedSize);
    }
    afterApply();
    // set Shell size
    IBrowserShell shell = m_widget.getState().getShell();
    shell.prepare();
    org.eclipse.swt.graphics.Rectangle shellBounds =
        shell.computeTrim(0, 0, size.width, size.height);
    shell.setSize(shellBounds.width + SIZE_EXPAND, shellBounds.height + SIZE_EXPAND);
  }

  private void doApply(Dimension resourceSize) throws Exception {
    if (!isSizeAlreadySet()) {
      applySizeUsingScript(resourceSize);
    }
  }

  protected void afterApply() throws Exception {
  }

  protected boolean isSizeAlreadySet() throws Exception {
    boolean hasSize =
        hasMethodInvocations(new String[]{"setSize(int,int)", "setWidth(int)", "setHeight(int)"});
    // process "xxx%" size specification
    if (!hasSize) {
      {
        MethodInvocation invocation =
            m_component.getMethodInvocation("setSize(java.lang.String,java.lang.String)");
        if (invocation != null) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          String width = (String) JavaInfoEvaluationHelper.getValue(arguments.get(0));
          String height = (String) JavaInfoEvaluationHelper.getValue(arguments.get(1));
          if (width.endsWith("%") || height.endsWith("%")) {
            // size is not set at actual value
            return false;
          }
          hasSize = true;
        }
      }
      {
        MethodInvocation invocation = m_component.getMethodInvocation("setWidth(java.lang.String)");
        if (invocation != null) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          String width = (String) JavaInfoEvaluationHelper.getValue(arguments.get(0));
          if (width.endsWith("%")) {
            // size is not set at actual value
            return false;
          }
          hasSize = true;
        }
      }
      {
        MethodInvocation invocation =
            m_component.getMethodInvocation("setHeight(java.lang.String)");
        if (invocation != null) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          String height = (String) JavaInfoEvaluationHelper.getValue(arguments.get(0));
          if (height.endsWith("%")) {
            // size is not set at actual value
            return false;
          }
          hasSize = true;
        }
      }
    }
    return hasSize;
  }

  protected Dimension getExpandedSize() throws Exception {
    return m_widget.getState().getAbsoluteBounds(m_widget.getElement()).getSize();
  }

  private void dontUseBorderForRootPanel() throws Exception {
    m_widget.getUIObjectUtils().executeScript(
        "DOM.setStyleAttribute(rootPanel.getElement(), 'border', '0');");
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    setSizeUsingScript(width, height);
    setResourceSize(width, height);
    {
      setSizeInt("setSize(int,int)", 0, width);
      setSizeString("setSize(java.lang.String,java.lang.String)", 0, width);
      setSizeInt("setWidth(int)", 0, width);
      setSizeString("setWidth(java.lang.String)", 0, width);
    }
    {
      setSizeInt("setSize(int,int)", 1, height);
      setSizeString("setSize(java.lang.String,java.lang.String)", 1, height);
      setSizeInt("setHeight(int)", 0, height);
      setSizeString("setHeight(java.lang.String)", 0, height);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void applySizeUsingScript(Dimension size) throws Exception {
    invokeSizeScript("applyTopBoundsScript", size);
  }

  private void setSizeUsingScript(int width, int height) throws Exception {
    invokeSizeScript("setTopBoundsScript", new Dimension(width, height));
  }

  private void invokeSizeScript(String scriptName, Dimension size) throws Exception {
    String script = JavaInfoUtils.getParameter(m_widget, scriptName);
    if (script != null) {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("model", m_widget);
      variables.put("widget", m_widget.getObject());
      variables.put("size", size);
      m_widget.getUIObjectUtils().executeScript(script, variables);
    }
  }

  private void invokeAttachScript() throws Exception {
    String script = JavaInfoUtils.getParameter(m_widget, "attachTopBoundsScript");
    if (script != null) {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("model", m_widget);
      variables.put("widget", m_widget.getObject());
      m_widget.getUIObjectUtils().executeScript(script, variables);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Apply size in source
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final boolean setSizeInt(String signature, int index, int value) throws Exception {
    String expression = Integer.toString(value);
    return setSizeExpression(signature, index, expression);
  }

  protected final boolean setSizeString(String signature, int index, int value) throws Exception {
    String expression = "\"" + Integer.toString(value) + "px\"";
    return setSizeExpression(signature, index, expression);
  }

  private boolean setSizeExpression(String signature, int index, String expression)
      throws Exception {
    MethodInvocation invocation = m_widget.getMethodInvocation(signature);
    if (invocation != null) {
      AstEditor editor = m_widget.getEditor();
      editor.replaceInvocationArgument(invocation, index, expression);
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    m_widget.getState().showShell();
    return true;
  }
}
