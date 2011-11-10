/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.smart.model.form;

import com.google.gdt.eclipse.designer.smart.model.ArrayChildrenContainerUtils;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.data.DataSourceInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.IObjectPropertyProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.form.DynamicForm</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class DynamicFormInfo extends CanvasInfo implements IObjectPropertyProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DynamicFormInfo(AstEditor editor,
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
   * @return the list of children {@link FormItemInfo}.
   */
  public List<FormItemInfo> getItems() {
    return getChildren(FormItemInfo.class);
  }

  /**
   * @return the {@link AbstractArrayObjectInfo} for "setFields" invocation.
   */
  public AbstractArrayObjectInfo getItemsArrayInfo() throws Exception {
    return ArrayChildrenContainerUtils.getMethodParameterArrayInfo(
        this,
        "setFields",
        "com.smartgwt.client.widgets.form.fields.FormItem");
  }

  /**
   * @return <code>true</code> if this {@link DynamicFormInfo} has absolute item layout.
   */
  public final boolean isAbsoluteItemLayout() {
    Object itemLayoutValue;
    try {
      itemLayoutValue = getPropertyByTitle("itemLayout").getValue();
    } catch (Exception e) {
      throw ReflectionUtils.propagate(e);
    }
    if (itemLayoutValue != null && itemLayoutValue != Property.UNKNOWN_VALUE) {
      Object value = ReflectionUtils.invokeMethodEx(itemLayoutValue, "getValue()");
      return "absolute".equalsIgnoreCase((String) value);
    }
    return false;
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
  // Commands: <code>com.smartgwt.client.widgets.form.fields.FormItem</code>
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(FormItemInfo newItem, FormItemInfo referenceItem) throws Exception {
    AbstractArrayObjectInfo arrayInfo = getItemsArrayInfo();
    arrayInfo.command_CREATE(newItem, referenceItem);
  }

  public void command_MOVE(FormItemInfo moveItem, FormItemInfo referenceItem) throws Exception {
    AbstractArrayObjectInfo arrayInfo = getItemsArrayInfo();
    arrayInfo.command_MOVE(moveItem, referenceItem);
  }

  public void command_BOUNDS(FormItemInfo item, Point location, Dimension size) throws Exception {
    Assert.isTrue(getChildren().contains(item), "%s is not child of %s.", item, this);
    if (size != null) {
      command_BOUNDS_setSize(item, size);
    }
    if (location != null) {
      command_BOUNDS_setLocation(item, location);
    }
  }

  /**
   * Update/add location invocations.
   */
  private void command_BOUNDS_setLocation(FormItemInfo item, Point location) throws Exception {
    Assert.isNotNull(location);
    // top
    command_BOUNDS_setValue(item, "setTop", location.y);
    // left
    command_BOUNDS_setValue(item, "setLeft", location.x);
  }

  /**
   * Update/add size invocations.
   */
  private void command_BOUNDS_setSize(FormItemInfo item, Dimension size) throws Exception {
    Assert.isNotNull(size);
    // height
    item.removeMethodInvocations("setHeight(java.lang.String)");
    command_BOUNDS_setValue(item, "setHeight", size.height);
    // width
    item.removeMethodInvocations("setWidth(java.lang.String)");
    command_BOUNDS_setValue(item, "setWidth", size.width);
  }

  /**
   * Update/add invocation for specified method-setter with value.
   */
  private void command_BOUNDS_setValue(FormItemInfo item, String methodName, int value)
      throws Exception {
    String wString = getRectString(value);
    MethodInvocation invocation = item.getMethodInvocation(methodName + "(int)");
    if (invocation != null) {
      item.getEditor().replaceInvocationArgument(invocation, 0, wString);
    } else {
      item.addMethodInvocation(methodName + "(int)", wString);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: <code>com.smartgwt.client.widgets.Canvas</code>
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(CanvasInfo newCanvas, FormItemInfo referenceItem) throws Exception {
    FormItemInfo newItem = createCanvasItem(referenceItem);
    AssociationObject associationObject = createAssociationObject();
    StatementTarget statementTarget = createStatementTarget(newItem);
    JavaInfoUtils.addTarget(newCanvas, associationObject, newItem, statementTarget);
  }

  public void command_MOVE(CanvasInfo moveCanvas, FormItemInfo referenceItem) throws Exception {
    ObjectInfo parent = moveCanvas.getParent();
    if (parent instanceof FormItemInfo) {
      FormItemInfo moveItem = (FormItemInfo) parent;
      command_MOVE(moveItem, referenceItem);
      return;
    }
    FormItemInfo newItem = createCanvasItem(referenceItem);
    AssociationObject associationObject = createAssociationObject();
    StatementTarget statementTarget = createStatementTarget(newItem);
    JavaInfoUtils.moveTarget(moveCanvas, associationObject, newItem, null, statementTarget);
  }

  /**
   * @return {@link FormItemInfo} wrapper by
   *         <code>com.smartgwt.client.widgets.form.fields.CanvasItem</code>
   */
  private FormItemInfo createCanvasItem(FormItemInfo referenceItem) throws Exception {
    FormItemInfo newItem =
        (FormItemInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.smartgwt.client.widgets.form.fields.CanvasItem",
            new ConstructorCreationSupport());
    command_CREATE(newItem, referenceItem);
    {
      // materialize
      Statement targetStatement =
          AstNodeUtils.getEnclosingStatement(newItem.getCreationSupport().getNode());
      StatementTarget statementTarget = new StatementTarget(targetStatement, true);
      newItem.getVariableSupport().ensureInstanceReadyAt(statementTarget);
    }
    return newItem;
  }

  /**
   * @return the {@link AssociationObject} for standard association with <code>Canvas</code>.
   */
  private static AssociationObject createAssociationObject() {
    return AssociationObjects.invocationChild("%parent%.setCanvas(%child%)", false);
  }

  /**
   * @return the {@link StatementTarget} for association invocation.
   */
  private static StatementTarget createStatementTarget(FormItemInfo itemInfo) {
    Statement targetStatement =
        AstNodeUtils.getEnclosingStatement(itemInfo.getCreationSupport().getNode());
    return new StatementTarget(targetStatement, false);
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
