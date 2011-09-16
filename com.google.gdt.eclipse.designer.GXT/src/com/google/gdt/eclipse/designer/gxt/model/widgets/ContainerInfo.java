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

import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>Container</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class ContainerInfo extends BoxComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContainerInfo(AstEditor editor,
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
   * @return the {@link WidgetInfo} children.
   */
  public final List<WidgetInfo> getWidgets() {
    return getChildren(WidgetInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    if (!isPlaceholder()) {
      ReflectionUtils.invokeMethod(getObject(), "layout(boolean)", true);
    }
    super.refresh_afterCreate();
  }
}
