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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.smartgwt.client.widgets.BaseWidget</code>.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class BaseWidgetInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BaseWidgetInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getElement() throws Exception {
    Object element = null;
    if (!isPlaceholder()) {
      element = ReflectionUtils.invokeMethodEx(getObject(), "getDOM()");
    }
    if (element == null) {
      element = super.getElement();
    }
    return element;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // remember Object
    Object object = getObject();
    // dispose children
    super.refresh_dispose();
    // destroy object
    destroyObject(object);
  }

  @Override
  protected void disposeRoot() throws Exception {
    // super.disposeRoot(); do not dispose Root, wait for destroy Object
  }

  protected void destroyObject(Object object) throws Exception {
    if (!isPlaceholder()) {
      SmartClientUtils.destroyCanvas(object);
    }
  }
}
