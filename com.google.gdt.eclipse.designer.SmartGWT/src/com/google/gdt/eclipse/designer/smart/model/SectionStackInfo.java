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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.layout.SectionStack</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public class SectionStackInfo extends CanvasInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SectionStackInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the children {@link SectionStackSectionInfo}.
   */
  public List<SectionStackSectionInfo> getSections() {
    return getChildren(SectionStackSectionInfo.class);
  }

  /**
   * @return true if section by index is visible
   */
  public boolean isSectionVisible(int index) {
    return (Boolean) ReflectionUtils.invokeMethodEx(getObject(), "sectionIsVisible(int)", index);
  }

  /**
   * @return true if section by index is expanded
   */
  public boolean isSectionExpanded(int index) {
    return (Boolean) ReflectionUtils.invokeMethodEx(getObject(), "sectionIsExpanded(int)", index);
  }

  /**
   * @return reverseOrder flag
   */
  public boolean isReverseOrder() {
    Boolean value = (Boolean) ReflectionUtils.invokeMethodEx(getObject(), "getReverseOrder()");
    return value == null ? false : value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // calculate section bounds
    refresh_bounds_sections();
  }

  /**
   * Calculate {@link SectionStackSectionInfo}'s bounds as summary of bounds his children canvases.
   */
  private void refresh_bounds_sections() throws Exception {
    boolean reverseOrder = isReverseOrder();
    Rectangle bounds = getModelBounds();
    int top = reverseOrder ? bounds.height : 0;
    int width = bounds.width;
    int sectionHeaderHeight =
        (Integer) ReflectionUtils.invokeMethod(getObject(), "getHeaderHeight()");
    List<SectionStackSectionInfo> sectionInfos = getSections();
    for (int i = 0; i < sectionInfos.size(); i++) {
      SectionStackSectionInfo sectionInfo = sectionInfos.get(i);
      Rectangle sectionBounds;
      if (isSectionVisible(i)) {
        // add header height
        boolean showHeader = (Boolean) sectionInfo.getPropertyByTitle("showHeader").getValue();
        int height = showHeader ? sectionHeaderHeight : 0;
        if (isSectionExpanded(i)) {
          // add Canvases heights
          for (CanvasInfo canvasInfo : sectionInfo.getCanvases()) {
            int canvasHeight = canvasInfo.getModelBounds().height;
            height += canvasHeight;
            // correct children Canvases bounds
            canvasInfo.setModelBounds(translateCanvas2Section(canvasInfo, new Point(0, -top)));
          }
          if (reverseOrder) {
            // reverse tweak children Canvases bounds
            for (CanvasInfo canvasInfo : sectionInfo.getCanvases()) {
              canvasInfo.setModelBounds(canvasInfo.getModelBounds().getTranslated(0, height));
            }
          }
        }
        sectionBounds =
            reverseOrder ? new Rectangle(0, top - height, width, height) : new Rectangle(0,
                top,
                width,
                height);
      } else {
        sectionBounds = new Rectangle(0, 0, 0, 0);
      }
      sectionInfo.setModelBounds(sectionBounds);
      top += reverseOrder ? -sectionBounds.height : sectionBounds.height;
    }
  }

  /**
   * @return the child {@link CanvasInfo} bounds translated to {@link SectionStackSectionInfo} area.
   */
  protected Rectangle translateCanvas2Section(CanvasInfo child, Point shift) {
    Point parentLocation = getModelBounds().getLocation();
    Point translatedLocation = parentLocation.getNegated().getTranslated(shift);
    if (!isRoot()) {
      UIObjectInfo root = (UIObjectInfo) getRootJava();
      translatedLocation.translate(root.getModelBounds().getLocation().getNegated());
    }
    Rectangle childBounds = child.getModelBounds();
    return childBounds.getTranslated(translatedLocation);
  }
}
