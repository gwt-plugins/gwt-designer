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

import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.layout.SectionStackSection</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public class SectionStackSectionInfo extends JsObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SectionStackSectionInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the parent {@link SectionStackInfo}.
   */
  public SectionStackInfo getSectionStack() {
    return (SectionStackInfo) getParent();
  }

  /**
   * @return the children {@link CanvasInfo}'s placed on this section.
   */
  public List<CanvasInfo> getCanvases() {
    return getChildren(CanvasInfo.class);
  }

  @Override
  public boolean isCreated() {
    return super.isCreated() && getSectionStack().isCreated();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    SectionStackInfo sectionStackInfo = getSectionStack();
    int index = sectionStackInfo.getSections().indexOf(this);
    if (index != -1
        && (!sectionStackInfo.isSectionVisible(index) || !sectionStackInfo.isSectionExpanded(index))) {
      // do not fetch bounds for children, set to zero
      for (CanvasInfo canvasInfo : getCanvases()) {
        canvasInfo.setModelBounds(new Rectangle(0, 0, 0, 0));
      }
    } else {
      super.refresh_fetch();
    }
  }
}
