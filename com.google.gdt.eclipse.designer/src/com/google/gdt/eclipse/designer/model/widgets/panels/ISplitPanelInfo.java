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
 * Interface model for GWT <code>SplitPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public interface ISplitPanelInfo<T extends IWidgetInfo> extends IPanelInfo<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the empty region, or <code>null</code> if all regions are already filled.
   */
  String getEmptyRegion();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link IWidgetInfo} into given region.
   */
  void command_CREATE(T widget, String region) throws Exception;

  /**
   * Moves child of this {@link ISplitPanelInfo} before other child.
   */
  void command_MOVE(T widget, T next) throws Exception;

  /**
   * Moves existing {@link IWidgetInfo} into given region.
   */
  void command_MOVE(T widget, String region) throws Exception;
}
