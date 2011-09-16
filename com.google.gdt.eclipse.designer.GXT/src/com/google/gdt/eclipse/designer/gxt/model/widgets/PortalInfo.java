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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.custom.Portal</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class PortalInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PortalInfo(AstEditor editor,
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
   * @return all {@link PortalInfo} children.
   */
  public List<PortletInfo> getPortlets() {
    return getChildren(PortletInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    prepareColumns();
  }

  /**
   * Updates {@link #m_columns}.
   */
  private void prepareColumns() {
    int numColumns = getColumnCount();
    if (m_columns.size() != numColumns) {
      m_columns.clear();
      for (int i = 0; i < numColumns; i++) {
        m_columns.add(new ColumnInfo());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ColumnInfo> m_columns = Lists.newArrayList();

  public final class ColumnInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the host {@link PortalInfo}.
     */
    public PortalInfo getPortal() {
      return PortalInfo.this;
    }

    /**
     * @return the index of this column.
     */
    public int getIndex() {
      return m_columns.indexOf(this);
    }

    /**
     * @return the {@link PortletInfo}-s in this column.
     */
    public List<PortletInfo> getPortlets() {
      int index = getIndex();
      List<PortletInfo> portlets = Lists.newArrayList();
      for (PortletInfo portlet : PortalInfo.this.getPortlets()) {
        if (getColumnIndex(portlet) == index) {
          portlets.add(portlet);
        }
      }
      return portlets;
    }

    /**
     * @return the bounds of this column in {@link PortalInfo}.
     */
    public Rectangle getBounds() {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Rectangle>() {
        public Rectangle runObject() throws Exception {
          return getBoundsEx();
        }
      }, new Rectangle());
    }

    private Rectangle getBoundsEx() throws Exception {
      Object container = getColumnContainer();
      Object containerElement = getUIObjectUtils().getElement(container);
      Rectangle bounds = getState().getAbsoluteBounds(containerElement);
      bounds.translate(getState().getAbsoluteBounds(getElement()).getLocation().getNegated());
      // exclude spacing
      {
        int spacing = ReflectionUtils.getFieldInt(getObject(), "spacing");
        bounds.moveX(spacing);
      }
      // use full height
      bounds.setBottom(PortalInfo.this.getBounds().height);
      // done
      return bounds;
    }

    /**
     * @return the width of column, in pixels (> 1.0) or fraction of rest parent width (<= 1.0).
     */
    public double getWidth() {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Double>() {
        public Double runObject() throws Exception {
          return getWidthEx();
        }
      }, 100.0);
    }

    private double getWidthEx() throws Exception {
      Object container = getColumnContainer();
      Object columnData =
          ScriptUtils.evaluate(
              JavaInfoUtils.getClassLoader(PortalInfo.this),
              "com.extjs.gxt.ui.client.widget.ComponentHelper.getLayoutData(component)",
              "component",
              container);
      return (Double) ReflectionUtils.invokeMethod(columnData, "getWidth()");
    }

    /**
     * Sets the width of column, in pixels (> 1.0) or fraction of rest parent width (<= 1.0).
     */
    public void setWidth(double width) throws Exception {
      int thisIndex = getIndex();
      setColumnWidth_update(thisIndex, width);
    }

    private Object getColumnContainer() {
      int index = getIndex();
      return ReflectionUtils.invokeMethodEx(getObject(), "getItem(int)", index);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Commands
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Creates new {@link PortalInfo} before given one.
     */
    public void command_CREATE(PortletInfo portlet, PortletInfo nextPortlet) throws Exception {
      int index = getIndex();
      nextPortlet = getNextPortlet(nextPortlet, index);
      JavaInfoUtils.add(portlet, getAssociation(index), PortalInfo.this, nextPortlet);
    }

    /**
     * Moves {@link PortalInfo} before given one.
     */
    public void command_MOVE(PortletInfo portlet, PortletInfo nextPortlet) throws Exception {
      int index = getIndex();
      // if different source column, move to this one
      {
        int sourceColumnIndex = getColumnIndex(portlet);
        if (sourceColumnIndex != index) {
          setColumnIndex(portlet, index);
        }
      }
      // do move
      nextPortlet = getNextPortlet(nextPortlet, index);
      JavaInfoUtils.move(portlet, getAssociation(index), PortalInfo.this, nextPortlet);
    }

    private PortletInfo getNextPortlet(PortletInfo nextPortlet, int index) {
      if (nextPortlet == null) {
        nextPortlet = getColumnReference(index);
      }
      return nextPortlet;
    }

    private AssociationObject getAssociation(int index) {
      String associationSource = MessageFormat.format("%parent%.add(%child%, {0})", index);
      return AssociationObjects.invocationChild(associationSource, false);
    }
  }

  /**
   * @return the {@link ColumnInfo} object that can be used to access information about columns.
   */
  public List<ColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * Creates new {@link ColumnInfo} before given one, or last.
   */
  public ColumnInfo command_CREATE(ColumnInfo nextColumn) throws Exception {
    int targetIndex = nextColumn != null ? nextColumn.getIndex() : m_columns.size();
    // move portlets
    for (PortletInfo portlet : getPortlets()) {
      int index = getColumnIndex(portlet);
      if (index >= targetIndex) {
        setColumnIndex(portlet, index + 1);
      }
    }
    // insert width
    setColumnWidth_insert(targetIndex, 100.0);
    // update "numColumns"
    PropertyUtils.getByPath(this, "Constructor/numColumns").setValue(m_columns.size() + 1);
    // add column
    ColumnInfo column = new ColumnInfo();
    m_columns.add(targetIndex, column);
    return column;
  }

  /**
   * Deletes given {@link ColumnInfo}, with all its {@link PortletInfo} children.
   */
  public void command_DELETE(ColumnInfo column) throws Exception {
    int targetIndex = column.getIndex();
    // prepare portlet indexes
    Map<PortletInfo, Integer> portletToIndex = Maps.newHashMap();
    for (PortletInfo portlet : getPortlets()) {
      int index = getColumnIndex(portlet);
      portletToIndex.put(portlet, index);
    }
    // move portlets
    for (PortletInfo portlet : getPortlets()) {
      int index = portletToIndex.get(portlet);
      if (index == targetIndex) {
        portlet.delete();
      }
      if (index > targetIndex) {
        setColumnIndex(portlet, index - 1);
      }
    }
    // insert width
    setColumnWidth_delete(targetIndex);
    // update "numColumns"
    PropertyUtils.getByPath(this, "Constructor/numColumns").setValue(m_columns.size() - 1);
    // remove column
    m_columns.remove(targetIndex);
  }

  /**
   * Moves given {@link ColumnInfo} before given one, or last.
   */
  public void command_MOVE(ColumnInfo column, ColumnInfo nextColumn) throws Exception {
    int sourceIndex = column.getIndex();
    int targetIndex = nextColumn != null ? nextColumn.getIndex() : m_columns.size();
    PortletInfo reference = getColumnReference(targetIndex - 1);
    if (sourceIndex < targetIndex) {
      targetIndex--;
    }
    // move portlets
    for (PortletInfo portlet : getPortlets()) {
      int index = getColumnIndex(portlet);
      if (index == sourceIndex) {
        setColumnIndex(portlet, targetIndex);
        JavaInfoUtils.move(portlet, null, this, reference);
      }
      if (sourceIndex < targetIndex) {
        if (index > sourceIndex && index <= targetIndex) {
          setColumnIndex(portlet, index - 1);
        }
      }
      if (sourceIndex > targetIndex) {
        if (index >= targetIndex && index < sourceIndex) {
          setColumnIndex(portlet, index + 1);
        }
      }
    }
    // move width in setColumnWidth() invocations
    setColumnWidth_move(sourceIndex, targetIndex);
    // move column
    m_columns.remove(column);
    m_columns.add(targetIndex, column);
  }

  /**
   * @return the {@link PortletInfo} to put last {@link PortalInfo} into target column.
   */
  private PortletInfo getColumnReference(int targetColumn) {
    for (int i = targetColumn + 1; i < m_columns.size(); i++) {
      ColumnInfo column = m_columns.get(i);
      List<PortletInfo> portlets = column.getPortlets();
      if (!portlets.isEmpty()) {
        return portlets.get(0);
      }
    }
    return null;
  }

  private int getColumnCount() {
    return ReflectionUtils.getFieldInt(getObject(), "numColumns");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column width
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets width for column.
   */
  private void setColumnWidth_update(int index, double width) throws Exception {
    String signature = "setColumnWidth(int,double)";
    String widthSource = DoubleConverter.INSTANCE.toJavaSource(null, width);
    //
    for (ValueObject_setColumnWidth object : setColumnWidth_getInvocationObjects()) {
      if (object.m_index == index) {
        getEditor().replaceExpression(object.m_valueExpression, widthSource);
        return;
      }
    }
    //
    MethodInvocation invocation = addMethodInvocation(signature, index + ", " + widthSource);
    JavaInfoEvaluationHelper.setValue(DomGenerics.arguments(invocation).get(0), index);
    setColumnWidth_sort();
  }

  /**
   * Inserts new width at target index, updates indexes after.
   */
  private void setColumnWidth_insert(int target, double width) throws Exception {
    // update indexes
    for (ValueObject_setColumnWidth object : setColumnWidth_getInvocationObjects()) {
      int index = object.m_index;
      if (index >= target) {
        setColumnWidth_setIndex(object, index + 1);
      }
    }
    // set width for new column
    setColumnWidth_update(target, width);
  }

  /**
   * Deletes width at target index, updates indexes after.
   */
  private void setColumnWidth_delete(int target) throws Exception {
    for (ValueObject_setColumnWidth object : setColumnWidth_getInvocationObjects()) {
      int index = object.m_index;
      if (index == target) {
        getEditor().removeEnclosingStatement(object.m_invocation);
      }
      if (index > target) {
        setColumnWidth_setIndex(object, index - 1);
      }
    }
  }

  /**
   * Moves width from source to target index, updates indexes between.
   */
  private void setColumnWidth_move(int source, int target) throws Exception {
    // update indexes
    for (ValueObject_setColumnWidth object : setColumnWidth_getInvocationObjects()) {
      int index = object.m_index;
      if (index == source) {
        setColumnWidth_setIndex(object, target);
      }
      if (source < target) {
        if (index > source && index <= target) {
          setColumnWidth_setIndex(object, index - 1);
        }
      }
      if (source > target) {
        if (index >= target && index < source) {
          setColumnWidth_setIndex(object, index + 1);
        }
      }
    }
    // sort by index
    setColumnWidth_sort();
  }

  private void setColumnWidth_setIndex(ValueObject_setColumnWidth object, int index)
      throws Exception {
    Expression e = getEditor().replaceExpression(object.m_indexExpression, "" + index);
    JavaInfoEvaluationHelper.setValue(e, index);
  }

  /**
   * Sorts invocations by index.
   */
  private void setColumnWidth_sort() throws Exception {
    String signature = "setColumnWidth(int,double)";
    List<ValueObject_setColumnWidth> objects = setColumnWidth_getInvocationObjects();
    // sort invocation List
    Collections.sort(objects, new Comparator<ValueObject_setColumnWidth>() {
      public int compare(ValueObject_setColumnWidth o1, ValueObject_setColumnWidth o2) {
        return o1.m_index - o2.m_index;
      }
    });
    // move invocations
    StatementTarget target = getMethodInvocationTarget(signature);
    for (ValueObject_setColumnWidth object : objects) {
      MethodInvocation invocation = object.m_invocation;
      Statement statement = AstNodeUtils.getEnclosingStatement(invocation);
      getEditor().moveStatement(statement, target);
      target = new StatementTarget(statement, false);
    }
  }

  /**
   * @return prepared information about each invocation.
   */
  private List<ValueObject_setColumnWidth> setColumnWidth_getInvocationObjects() throws Exception {
    String signature = "setColumnWidth(int,double)";
    List<ValueObject_setColumnWidth> objects = Lists.newArrayList();
    for (MethodInvocation invocation : getMethodInvocations(signature)) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      Expression indexExpression = arguments.get(0);
      Expression valueExpression = arguments.get(1);
      Object indexValue = JavaInfoEvaluationHelper.getValue(indexExpression);
      if (indexValue instanceof Integer) {
        int index = ((Integer) indexValue).intValue();
        objects.add(new ValueObject_setColumnWidth(index,
            invocation,
            indexExpression,
            valueExpression));
      }
    }
    return objects;
  }

  private static final class ValueObject_setColumnWidth {
    private final int m_index;
    private final MethodInvocation m_invocation;
    private final Expression m_indexExpression;
    private final Expression m_valueExpression;

    public ValueObject_setColumnWidth(int index,
        MethodInvocation invocation,
        Expression indexExpression,
        Expression valueExpression) {
      m_index = index;
      m_invocation = invocation;
      m_indexExpression = indexExpression;
      m_valueExpression = valueExpression;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Portlet column index
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of column to which given {@link PortletInfo} belongs, may be <code>-1</code>
   *         .
   */
  private static int getColumnIndex(PortletInfo portlet) {
    Expression columnExpression = getColumnIndexExpression(portlet);
    if (columnExpression != null) {
      Object columnValue = JavaInfoEvaluationHelper.getValue(columnExpression);
      if (columnValue instanceof Integer) {
        return ((Integer) columnValue).intValue();
      }
    }
    return -1;
  }

  /**
   * Sets the index of column to which given {@link PortletInfo} belongs.
   */
  private static void setColumnIndex(PortletInfo portlet, int column) throws Exception {
    Expression columnExpression = getColumnIndexExpression(portlet);
    if (columnExpression != null) {
      Expression e = portlet.getEditor().replaceExpression(columnExpression, "" + column);
      JavaInfoEvaluationHelper.setValue(e, column);
    }
  }

  /**
   * @return the {@link Expression} of column index to which given {@link PortletInfo} belongs, may
   *         be <code>null</code>.
   */
  private static Expression getColumnIndexExpression(PortletInfo portlet) {
    if (portlet.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association =
          (InvocationChildAssociation) portlet.getAssociation();
      if (association.getDescription().getSignature().equals(
          "add(com.extjs.gxt.ui.client.widget.custom.Portlet,int)")) {
        MethodInvocation invocation = association.getInvocation();
        return DomGenerics.arguments(invocation).get(1);
      }
    }
    return null;
  }
}
