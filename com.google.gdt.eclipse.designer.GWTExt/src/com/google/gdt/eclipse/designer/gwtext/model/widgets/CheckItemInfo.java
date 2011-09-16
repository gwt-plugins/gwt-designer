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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>CheckItem</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model
 */
public class CheckItemInfo extends BaseItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CheckItemInfo(AstEditor editor,
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
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    ObjectInfo parent = getParent();
    if (parent instanceof CycleButtonInfo) {
      Rectangle parentBounds = ((CycleButtonInfo) parent).getModelBounds();
      setModelBounds(new Rectangle(0, parentBounds.height, parentBounds.width, 0));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isChecked() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "isChecked()");
  }

  public boolean isCheckedProperty() throws Exception {
    Property property = getPropertyByTitle("checked");
    return (Boolean) property.getValue();
  }

  public void setChecked(boolean checked) throws Exception {
    ReflectionUtils.invokeMethod(getObject(), "setChecked(boolean)", checked);
  }

  public void setCheckedProperty(boolean checked) throws Exception {
    getPropertyByTitle("checked").setValue(checked);
  }
}
