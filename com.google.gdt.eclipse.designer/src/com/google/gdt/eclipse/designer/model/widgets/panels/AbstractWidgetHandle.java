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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;

/**
 * Several panels in GWT use objects related with widgets, for example tabs in {@link TabPanelInfo}
 * or title objects in {@link StackPanelInfo}. So, we need some abstract object with common
 * functionality.
 * 
 * NB: we can not store bounds here, because GEF for same model (equal) will use old
 * {@link EditPart} with old model, that has old bounds.
 * 
 * @author scheglov_ke
 */
public abstract class AbstractWidgetHandle<T extends IWidgetInfo> {
  protected final T m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractWidgetHandle(T widget) {
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IWidgetInfo} that has this header.
   */
  public final T getWidget() {
    return m_widget;
  }

  /**
   * @return the bounds of this header relative to the container.
   */
  public abstract Rectangle getBounds();

  /**
   * Ensures that this {@link IWidgetInfo} is displayed.
   */
  public abstract void show();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final int hashCode() {
    return m_widget.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof AbstractWidgetHandle) {
      AbstractWidgetHandle<?> object = (AbstractWidgetHandle<?>) obj;
      return object.m_widget == m_widget;
    }
    return false;
  }
}
