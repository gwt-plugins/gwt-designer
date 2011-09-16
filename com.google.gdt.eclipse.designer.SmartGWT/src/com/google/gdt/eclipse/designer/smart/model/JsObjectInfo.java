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

import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for <code>com.smartgwt.client.core.JsObject</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public abstract class JsObjectInfo extends AbstractComponentInfo implements IGwtStateProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JsObjectInfo(AstEditor editor,
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
   * @return <code>true</code> if object created in JS.
   */
  public boolean isCreated() {
    return !isPlaceholder() && SmartClientUtils.isJsObjectCreated(getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGWTStateProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtState getState() {
    return (GwtState) getEditor().getGlobalValue(UIObjectInfo.STATE_KEY);
  }
}
