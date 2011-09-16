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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>menu.TextItem</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public class BaseItemInfo extends ComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BaseItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    fetchElement_duringParse();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    fetchElement();
  }

  private void fetchElement_duringParse() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        fetchElement();
      }
    });
  }

  /**
   * <code>TextItem</code> and <code>Separator</code> have no element initially, so we need this
   * workaround.
   */
  private void fetchElement() throws Exception {
    ObjectInfo parent = getParent();
    if (parent instanceof MenuInfo
        && ReflectionUtils.getFieldObject(getObject(), "element") == null) {
      MenuInfo parentMenu = (MenuInfo) parent;
      int index = parentMenu.getChildrenJava().indexOf(this);
      Object[] items =
          (Object[]) ReflectionUtils.invokeMethod(parentMenu.getObject(), "getItems()");
      Object element = ReflectionUtils.invokeMethod(items[index], "getElement()");
      ReflectionUtils.invokeMethod(
          getObject(),
          "setElement(com.google.gwt.user.client.Element)",
          element);
    }
  }
}
