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
package com.google.gdt.eclipse.designer.gwtext.model.layout.assistant;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * Assistant for {@link LayoutDataInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
abstract class LayoutDataAssistantPage extends AbstractGwtExtAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataAssistantPage(Composite parent, List<ObjectInfo> objects) {
    super(parent, getLayoutDataList(objects));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link LayoutDataInfo}s for given {@link WidgetInfo}s.
   */
  private static List<LayoutDataInfo> getLayoutDataList(List<ObjectInfo> objects) {
    ArrayList<LayoutDataInfo> layoutDataList = Lists.newArrayList();
    for (ObjectInfo object : objects) {
      if (object instanceof WidgetInfo) {
        WidgetInfo widget = (WidgetInfo) object;
        LayoutDataInfo layoutData = LayoutInfo.getLayoutData(widget);
        if (layoutData != null) {
          layoutDataList.add(layoutData);
        }
      }
    }
    return layoutDataList;
  }
}