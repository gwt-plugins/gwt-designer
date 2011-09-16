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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>Panel</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model
 */
public class PanelInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    removeHtmlProperty_whenAddChild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Combination of <code>Panel</code> with "html" property and <code>TreePanel</code> child is
   * buggy.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44153
   */
  private void removeHtmlProperty_whenAddChild() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (parent == PanelInfo.this) {
          Property property = getPropertyByTitle("html");
          if (property.isModified()) {
            property.setValue(Property.UNKNOWN_VALUE);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    {
      Rectangle panelBounds = getAbsoluteBounds(getObject());
      Object body = ReflectionUtils.invokeMethod(getObject(), "getBody()");
      Rectangle bodyBounds = getAbsoluteBounds(body);
      setClientAreaInsets(new Insets(bodyBounds.y - panelBounds.y,
          bodyBounds.x - panelBounds.x,
          panelBounds.bottom() - bodyBounds.bottom(),
          panelBounds.right() - bodyBounds.right()));
    }
  }

  private static Rectangle getAbsoluteBounds(Object boxObject) throws Exception {
    Object box = ReflectionUtils.invokeMethod(boxObject, "getBox()");
    int x = (Integer) ReflectionUtils.invokeMethod(box, "getX()");
    int y = (Integer) ReflectionUtils.invokeMethod(box, "getY()");
    int width = (Integer) ReflectionUtils.invokeMethod(box, "getWidth()");
    int height = (Integer) ReflectionUtils.invokeMethod(box, "getHeight()");
    return new Rectangle(x, y, width, height);
  }
}
