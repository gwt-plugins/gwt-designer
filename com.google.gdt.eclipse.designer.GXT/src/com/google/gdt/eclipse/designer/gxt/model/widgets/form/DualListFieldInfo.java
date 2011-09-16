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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.form.DualListField</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class DualListFieldInfo extends MultiFieldInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DualListFieldInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // ensure ListStore for children
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        String thisSource = TemplateUtils.getExpression(DualListFieldInfo.this);
        String storeSource = "setStore(new com.extjs.gxt.ui.client.store.ListStore())";
        addExpressionStatement(thisSource + ".getToList()." + storeSource);
        addExpressionStatement(thisSource + ".getFromList()." + storeSource);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // ensure ListStore for children
    JavaInfoUtils.executeScript(this, CodeUtils.getSource(
        "import com.extjs.gxt.ui.client.store.ListStore;",
        "object.getFromList().setStore(new ListStore());",
        "object.getToList().setStore(new ListStore());",
        ""));
  }
}
