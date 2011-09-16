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

import com.google.gdt.eclipse.designer.smart.model.data.DataSourceInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.IObjectPropertyProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.grid.ColumnTree</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class ColumnTreeInfo extends CanvasInfo implements IObjectPropertyProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnTreeInfo(AstEditor editor,
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
   * @return the list of children {@link ListGridFieldInfo}.
   */
  public List<? extends ListGridFieldInfo> getFields() {
    return getChildren(ListGridFieldInfo.class);
  }

  /**
   * @return the {@link AbstractArrayObjectInfo} for "setFields" invocation.
   */
  public AbstractArrayObjectInfo getFieldsArrayInfo() throws Exception {
    return ArrayChildrenContainerUtils.getMethodParameterArrayInfo(
        this,
        "setFields",
        "com.smartgwt.client.widgets.grid.ListGridField");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // tweaks for correct render
    if (!isPlaceholder()) {
      ReflectionUtils.invokeMethod(object, "setAutoFetchData(java.lang.Boolean)", false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(ListGridFieldInfo newField, ListGridFieldInfo referenceField)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getFieldsArrayInfo();
    arrayInfo.command_CREATE(newField, referenceField);
  }

  public void command_MOVE(ListGridFieldInfo moveField, ListGridFieldInfo referenceField)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getFieldsArrayInfo();
    arrayInfo.command_MOVE(moveField, referenceField);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObjectPropertyProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StatementTarget getObjectPropertyStatementTarget(GenericProperty property,
      JavaInfo componentValue) throws Exception {
    if ("dataSource".equals(property.getTitle()) && componentValue instanceof DataSourceInfo) {
      DataSourceInfo dataSource = (DataSourceInfo) componentValue;
      return dataSource.calculateStatementTarget(this);
    }
    return null;
  }
}
