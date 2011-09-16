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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>Component</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class ComponentInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentInfo(AstEditor editor,
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
   * @return <code>true</code> if this <code>Component</code> is rendered, so has real
   *         <code>Element</code>.
   */
  public final boolean isRendered() {
    return (Boolean) ReflectionUtils.invokeMethodEx(getObject(), "isRendered()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    fixGWTBugs(object);
    super.setObject(object);
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    fixGWTBugs(getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fixes for GXT specific problems
  //
  ////////////////////////////////////////////////////////////////////////////
  private void fixGWTBugs(Object object) throws Exception {
    if (object != null) {
      // http://www.extjs.com/forum/showthread.php?p=396823
      if (ReflectionUtils.isSuccessorOf(object, "com.extjs.gxt.ui.client.widget.ContentPanel")) {
        fixGWTBugs_ContentPanel_component(object, "getTopComponent()");
        fixGWTBugs_ContentPanel_component(object, "getBottomComponent()");
      }
      // visit children
      if (ReflectionUtils.isSuccessorOf(object, "com.extjs.gxt.ui.client.widget.Composite")) {
        Object component = ReflectionUtils.invokeMethod(object, "getComponent()");
        fixGWTBugs(component);
      }
      if (ReflectionUtils.isSuccessorOf(object, "com.extjs.gxt.ui.client.widget.Container")) {
        List<?> items = (List<?>) ReflectionUtils.invokeMethod(object, "getItems()");
        for (Object item : items) {
          fixGWTBugs(item);
        }
      }
    }
  }

  /**
   * Ensure that "parent" is set for top/bottom child of <code>ContentPanel</code>.
   * <p>
   * http://www.extjs.com/forum/showthread.php?p=396823
   */
  private void fixGWTBugs_ContentPanel_component(Object object, String methodName) throws Exception {
    Object topComponent = ReflectionUtils.invokeMethod(object, methodName);
    if (topComponent != null) {
      ReflectionUtils.setField(topComponent, "parent", object);
    }
  }
}
