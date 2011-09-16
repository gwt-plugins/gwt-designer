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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.gdt.eclipse.designer.gef.part.panels.AbstractWidgetHandleEditPart;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.DirectTextEditPolicy;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.StringUtils;

/**
 * {@link EditPolicy} direct editing of {@link Property} is {@link WidgetInfo} in
 * {@link AbstractWidgetHandle}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class AbstractWidgetHandleDirectTextEditPolicy extends DirectTextEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link WidgetInfo} of given {@link AbstractWidgetHandle} has {@link Property} with given
   * title, installs {@link AbstractWidgetHandleDirectTextEditPolicy}.
   */
  public static void install(final AbstractWidgetHandleEditPart editPart, final String propertyTitle) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        AbstractWidgetHandle<?> handle = (AbstractWidgetHandle<?>) editPart.getModel();
        IWidgetInfo widget = handle.getWidget();
        Property property = widget.getPropertyByTitle(propertyTitle);
        if (property != null) {
          editPart.installEditPolicy(new AbstractWidgetHandleDirectTextEditPolicy(property));
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Property m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbstractWidgetHandleDirectTextEditPolicy(Property property) {
    m_property = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DirectTextEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return (String) m_property.getValue();
      }
    }, StringUtils.EMPTY);
  }

  @Override
  protected void setText(final String text) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_property.setValue(text);
      }
    });
  }

  @Override
  protected Point getTextWidgetLocation(Rectangle hostBounds, Dimension textSize) {
    int x = hostBounds.getCenter().x - textSize.width / 2;
    int y = hostBounds.getCenter().y - textSize.height / 2;
    return new Point(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed() && keyRequest.getCharacter() == ' ') {
        beginEdit();
      }
    }
    super.performRequest(request);
  }
}
