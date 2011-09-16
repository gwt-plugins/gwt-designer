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

/**
 * Interface model for GWT <code>com.google.gwt.user.client.ui.DockLayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public interface IDockLayoutPanelInfo<T extends IWidgetInfo> extends IComplexPanelInfo<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Edge
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of edge.
   */
  String getEdge(T widget);

  /**
   * @param edge
   *          the name of edge, should be "WEST", "NORTH", "EAST", "SOUTH" or "CENTER".
   */
  void setEdge(T widget, String edge) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new size of {@link IWidgetInfo}, in units.
   */
  void setSize(T widget, double size) throws Exception;

  /**
   * Sets reasonable size for {@link IWidgetInfo}, which is enough to see widget, but no too big.
   */
  void setReasonableSize(T widget) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unit
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the unit size for given pixel size.
   */
  double getSizeInUnits(int pixels, boolean vertical);

  /**
   * @return the string for given size in current units.
   */
  String getUnitSizeTooltip(double units);
}
