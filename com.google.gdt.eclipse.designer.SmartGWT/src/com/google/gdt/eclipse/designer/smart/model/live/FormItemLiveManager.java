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
package com.google.gdt.eclipse.designer.smart.model.live;

import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveCacheEntry;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.live.AbstractLiveManager;
import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * Default live components manager for SmartGWT <code>FormItem</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class FormItemLiveManager extends AbstractLiveManager {
  private final UIObjectUtils m_utils;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormItemLiveManager(FormItemInfo formItem) {
    super(formItem);
    m_utils = formItem.getState().getUIObjectUtils();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractLiveComponentsManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected FormItemInfo createLiveComponent() throws Exception {
    m_utils.setLiveManager(true);
    // prepare empty RootPanel
    RootPanelInfo panel;
    {
      String[] sourceLines =
          new String[]{
              "  com.google.gwt.user.client.ui.RootPanel __wbp_panel = com.google.gwt.user.client.ui.RootPanel.get();",
              "  __wbp_panel.setPixelSize(800, 600);",};
      panel = (RootPanelInfo) parse(sourceLines);
    }
    // prepare Canvas
    final DynamicFormInfo parentCanvas =
        (DynamicFormInfo) JavaInfoUtils.createJavaInfo(
            m_editor,
            "com.smartgwt.client.widgets.form.DynamicForm",
            new ConstructorCreationSupport());
    {
      // drop Canvas on RootPanel
      panel.command_CREATE2(parentCanvas, null);
      panel.command_BOUNDS(parentCanvas, new Point(10, 10), new Dimension(700, 500));
      // broadcast for set widget as "live"
      parentCanvas.addBroadcastListener(new JavaInfoSetObjectAfter() {
        public void invoke(JavaInfo target, Object o) throws Exception {
          if (target == parentCanvas) {
            parentCanvas.removeBroadcastListener(this);
            m_utils.setLiveWidget(o);
          }
        }
      });
    }
    // prepare component
    FormItemInfo formItem = createClone();
    // add component on Canvas
    parentCanvas.command_CREATE(formItem, null);
    // remove "live" mark from RootPanel,
    // because only one instance of RootPanel exists, so we update it also for "main" hierarchy
    panel.getPropertyByTitle("title").setValue(null);
    // ready to get live values
    return formItem;
  }

  @Override
  protected void cleanupLiveComponent(AbstractComponentInfo liveComponentInfo) throws Exception {
    super.cleanupLiveComponent(liveComponentInfo);
    // reset "live" state of UIObjectManipulations
    {
      FormItemInfo formItem = (FormItemInfo) liveComponentInfo;
      DynamicFormInfo form = formItem.getForm();
      if (form != null) {
        m_utils.setLiveWidget(null);
      }
    }
    m_utils.setLiveManager(false);
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo component) {
    FormItemInfo formItem = (FormItemInfo) component;
    // do create cache entry
    GwtLiveCacheEntry entry = new GwtLiveCacheEntry();
    entry.setImage(getWidgetImage(formItem));
    entry.shouldSetSize(false);
    return entry;
  }

  /**
   * @return the {@link Image} of {@link FormItemInfo}.
   */
  private Image getWidgetImage(final FormItemInfo formItem) {
    Rectangle bounds = formItem.getAbsoluteBounds();
    Image componentImage = new Image(null, bounds.width, bounds.height);
    GC gc = new GC(componentImage);
    try {
      RootPanelInfo rootPanel = (RootPanelInfo) formItem.getForm().getRootJava();
      gc.drawImage(
          rootPanel.getImage(),
          bounds.x,
          bounds.y,
          bounds.width,
          bounds.height,
          0,
          0,
          bounds.width,
          bounds.height);
    } finally {
      gc.dispose();
    }
    return componentImage;
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntryEx(Throwable e) {
    GwtLiveCacheEntry cacheEntry = new GwtLiveCacheEntry();
    // set image
    {
      Image image = createImageForException(e);
      cacheEntry.setImage(image);
    }
    // done
    return cacheEntry;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of "live" {@link Image} of component, must be disposed by caller.
   */
  public Image getImage() {
    // get image from memento during paste
    {
      Image image = ComponentInfoMemento.getImage(m_component);
      if (image != null) {
        return image;
      }
    }
    // prepare image
    return getEntry().getImage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LiveComponentsCacheEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  private GwtLiveCacheEntry getEntry() {
    // check if we have LiveComponentsCacheEntry set externally
    {
      GwtLiveCacheEntry entry =
          (GwtLiveCacheEntry) m_component.getArbitraryValue(GwtLiveCacheEntry.class);
      if (entry != null) {
        return entry;
      }
    }
    // use code parsing
    return (GwtLiveCacheEntry) getCachedEntry();
  }
}
