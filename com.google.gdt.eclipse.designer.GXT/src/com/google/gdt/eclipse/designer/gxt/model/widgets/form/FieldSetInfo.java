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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.gdt.eclipse.designer.gxt.model.widgets.GxtUtils;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.form.FieldSet</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class FieldSetInfo extends LayoutContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldSetInfo(AstEditor editor,
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
  protected void fetchClientAreaInsets() throws Exception {
    Object panelEl = JavaInfoUtils.executeScript(this, "object.el()");
    Object bodyEl = ReflectionUtils.getFieldObject(getObject(), "body");
    Rectangle panelBounds = GxtUtils.getAbsoluteBounds(panelEl);
    Rectangle bodyBounds = GxtUtils.getAbsoluteBounds(bodyEl);
    {
      int top = bodyBounds.y - panelBounds.y;
      int left = bodyBounds.x - panelBounds.x;
      int bottom = panelBounds.bottom() - bodyBounds.bottom();
      int right = panelBounds.right() - bodyBounds.right();
      /*if (bottom < 0) {
      	bottom *= -1;
      }*/
      setClientAreaInsets(new Insets(top, left, bottom, right));
    }
  }
}
