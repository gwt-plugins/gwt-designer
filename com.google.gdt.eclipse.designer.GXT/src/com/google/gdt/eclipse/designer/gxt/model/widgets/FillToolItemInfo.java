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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.toolbar.FillToolItem</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class FillToolItemInfo extends ComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FillToolItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_finish() throws Exception {
    super.refresh_finish();
    if (getParent() instanceof ToolBarInfo) {
      ToolBarInfo bar = (ToolBarInfo) getParent();
      List<ComponentInfo> items = bar.getItems();
      // prepare next/prev items
      int index = items.indexOf(this);
      ComponentInfo prevItem = GenericsUtils.getPrevOrNull(items, index);
      ComponentInfo nextItem = GenericsUtils.getNextOrNull(items, index);
      // prepare bounds
      int x = prevItem != null ? prevItem.getBounds().right() : 0;
      int right = nextItem != null ? nextItem.getBounds().left() : bar.getBounds().width;
      setBounds(new Rectangle(x, 0, right - x, bar.getBounds().height));
    }
  }
}
