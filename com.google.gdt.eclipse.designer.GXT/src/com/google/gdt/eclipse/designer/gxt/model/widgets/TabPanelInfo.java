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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.TabPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class TabPanelInfo extends ContainerInfo {
  private final StackContainerSupport<TabItemInfo> m_stackContainer =
      new StackContainerSupport<TabItemInfo>(this) {
        @Override
        protected List<TabItemInfo> getChildren() {
          return getItems();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelInfo(AstEditor editor,
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
   * @return all {@link TabItemInfo} children.
   */
  public List<TabItemInfo> getItems() {
    return getChildren(TabItemInfo.class);
  }

  /**
   * @return the selected {@link TabItemInfo}.
   */
  public TabItemInfo getSelectedItem() {
    return m_stackContainer.getActive();
  }

  /**
   * @return the {@link Header} objects for each {@link TabItemInfo}.
   */
  public List<Header> getHeaders() {
    return m_headers;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    // render all items
    if (!isPlaceholder()) {
      JavaInfoUtils.executeScript(this, CodeUtils.getSource(
          "// filler filler filler",
          "for (item : object.getItems()) {",
          "  object.setSelection(item);",
          "}"));
    }
    // continue
    super.refresh_afterCreate();
    // select item
    if (!isPlaceholder()) {
      TabItemInfo selectedItem = getSelectedItem();
      if (selectedItem != null) {
        ReflectionUtils.invokeMethod(
            getObject(),
            "setSelection(com.extjs.gxt.ui.client.widget.TabItem)",
            selectedItem.getObject());
      }
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    m_headers.clear();
    for (TabItemInfo item : getItems()) {
      Object headerObject = ReflectionUtils.invokeMethod(item.getObject(), "getHeader()");
      Object headerElement = ReflectionUtils.invokeMethod(headerObject, "getElement()");
      Rectangle headerBounds = getState().getAbsoluteBounds(headerElement);
      headerBounds.translate(getAbsoluteBounds().getLocation().getNegated());
      m_headers.add(new Header(item, headerBounds));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Headers
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Header> m_headers = Lists.newArrayList();

  public final class Header extends AbstractWidgetHandle<TabItemInfo> {
    private final TabItemInfo m_item;
    private final Rectangle m_bounds;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Header(TabItemInfo item, Rectangle bounds) {
      super(item);
      m_item = item;
      m_bounds = bounds;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Rectangle getBounds() {
      return m_bounds;
    }

    @Override
    public void show() {
      m_stackContainer.setActive(m_item);
    }
  }
}
