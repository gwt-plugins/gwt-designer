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

import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Rectangle;

import java.util.List;

/**
 * Implementation of {@link TopBoundsSupport} for {@link RootPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.top
 */
public class RootPanelTopBoundsSupport extends TopBoundsSupport {
  private final RootPanelInfo m_rootPanel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootPanelTopBoundsSupport(RootPanelInfo rootPanel) {
    super(rootPanel);
    m_rootPanel = rootPanel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int SIZE_EXPAND = 50;

  @Override
  public void apply() throws Exception {
    GwtState state = m_rootPanel.getState();
    Dimension size = getSize();
    // prepare Shell
    IBrowserShell shell = state.getShell();
    shell.prepare();
    // set Shell size
    {
      Rectangle shellBounds = shell.computeTrim(0, 0, size.width, size.height);
      if (state.isStrictMode() && !state.isBrowserWebKit()) {
        shellBounds.width += SIZE_EXPAND;
        shellBounds.height += SIZE_EXPAND;
      }
      shell.setSize(shellBounds.width, shellBounds.height);
    }
    // in "strict" mode RootPanel (i.e. "body") has size required by widgets, so force size
    if (state.isStrictMode()) {
      Insets margins = state.getMargins(m_rootPanel.getElement());
      Insets borders = state.getBorders(m_rootPanel.getElement());
      int width = size.width - margins.getWidth() - borders.getWidth();
      int height = size.height - margins.getHeight() - borders.getHeight();
      ReflectionUtils.invokeMethod(m_rootPanel.getObject(), "setPixelSize(int,int)", width, height);
    }
  }

  private Dimension getSize() throws Exception {
    // check for forced size
    {
      MethodInvocation invocation = m_rootPanel.getMethodInvocation("setPixelSize(int,int)");
      if (invocation != null) {
        List<Expression> arguments = DomGenerics.arguments(invocation);
        int width = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(0));
        int height = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(1));
        return new Dimension(width, height);
      }
    }
    // use size from resource properties
    return getResourceSize();
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // remember size in resource properties
    setResourceSize(width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    m_rootPanel.getState().showShell();
    return true;
  }
}
