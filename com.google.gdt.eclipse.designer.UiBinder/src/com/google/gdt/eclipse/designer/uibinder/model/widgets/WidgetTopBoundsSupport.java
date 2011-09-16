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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.hosted.IBrowserShell;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.Map;

/**
 * Implementation of {@link TopBoundsSupport} for any {@link WidgetInfo} except
 * <code>RootPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
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
    dontUseBorderForRootPanel();
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
    applySizeUsingScript(resourceSize);
  }

  protected void afterApply() throws Exception {
  }

  protected Dimension getExpandedSize() throws Exception {
    return m_widget.getState().getAbsoluteBounds(m_widget.getDOMElement()).getSize();
  }

  private void dontUseBorderForRootPanel() throws Exception {
    m_widget.getUIObjectUtils().executeScript(
        "DOM.setStyleAttribute(rootPanel.getElement(), 'border', '0');");
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    setSizeUsingScript(width, height);
    setResourceSize(width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void applySizeUsingScript(Dimension size) throws Exception {
    invokeSizeScript("applyTopBoundsScript", true, size);
  }

  private void setSizeUsingScript(int width, int height) throws Exception {
    invokeSizeScript("setTopBoundsScript", false, new Dimension(width, height));
  }

  private void invokeSizeScript(String scriptName, boolean required, Dimension size)
      throws Exception {
    String script = XmlObjectUtils.getParameter(m_widget, scriptName);
    if (script != null) {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("model", m_widget);
      variables.put("widget", m_widget.getObject());
      variables.put("size", size);
      m_widget.getUIObjectUtils().executeScript(script, variables);
    }
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
