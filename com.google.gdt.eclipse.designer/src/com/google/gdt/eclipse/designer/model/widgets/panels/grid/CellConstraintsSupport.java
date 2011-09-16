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
package com.google.gdt.eclipse.designer.model.widgets.panels.grid;

import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo.CellInvocationsVisitor;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Container with information about single {@link WidgetInfo} int {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class CellConstraintsSupport {
  private final HTMLTableInfo m_panel;
  private final WidgetInfo m_component;
  private int m_column;
  private int m_row;
  private int m_colSpan;
  private int m_rowSpan;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  CellConstraintsSupport(HTMLTableInfo panel, WidgetInfo component) throws Exception {
    m_panel = panel;
    m_component = component;
    // set defaults
    {
      m_column = -1;
      m_row = -1;
      m_colSpan = 1;
      m_rowSpan = 1;
    }
    // try to initialize
    if (m_component.getObject() != null) {
      Rectangle cells = m_panel.getGridInfo().getComponentCells(m_component);
      if (cells != null) {
        m_column = cells.x;
        m_row = cells.y;
        m_colSpan = cells.width;
        m_rowSpan = cells.height;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_column + " " + m_row + " " + m_colSpan + " " + m_rowSpan;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // X 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the column.
   */
  public int getX() {
    return m_column;
  }

  /**
   * Sets the column.
   */
  public void setX(int x) throws Exception {
    m_column = x;
  }

  /**
   * Updates column.
   */
  public void updateX(int delta) throws Exception {
    setX(getX() + delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the colSpan.
   */
  public int getWidth() {
    return m_colSpan;
  }

  /**
   * Sets the colSpan.
   */
  public void setWidth(int width) throws Exception {
    m_colSpan = width;
  }

  /**
   * Updates colSpan.
   */
  public void updateWidth(int delta) throws Exception {
    setWidth(getWidth() + delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Y 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the row.
   */
  public int getY() {
    return m_row;
  }

  /**
   * Sets the row.
   */
  public void setY(int y) throws Exception {
    m_row = y;
  }

  /**
   * Updates row.
   */
  public void updateY(int delta) throws Exception {
    setY(getY() + delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the rowSpan.
   */
  public int getHeight() {
    return m_rowSpan;
  }

  /**
   * Sets the rowSpan.
   */
  public void setHeight(int height) throws Exception {
    m_rowSpan = height;
  }

  /**
   * Updates rowSpan.
   */
  public void updateHeight(int delta) throws Exception {
    setHeight(getHeight() + delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets integer argument of {@link WidgetInfo} association with {@link HTMLTableInfo}.
   */
  void setAssociationArgumentInt(String tagName, int y) throws Exception {
    if (m_component.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association =
          (InvocationChildAssociation) m_component.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      for (ParameterDescription parameter : association.getDescription().getParameters()) {
        if (parameter.getTags().containsKey(tagName)) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          Expression argument = arguments.get(parameter.getIndex());
          m_panel.setIntExpression(argument, y);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String SET_ALIGNMENT = "setAlignment(int,int,"
      + "com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant,"
      + "com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant)";
  private static final String SET_HOR_ALIGNMENT = "setHorizontalAlignment(int,int,"
      + "com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)";
  private static final String SET_VER_ALIGNMENT = "setVerticalAlignment(int,int,"
      + "com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant)";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Horizontal alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the horizontal alignment of {@link WidgetInfo} in cell.
   */
  public ColumnInfo.Alignment getHorizontalAlignment() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<ColumnInfo.Alignment>() {
      public ColumnInfo.Alignment runObject() throws Exception {
        return getHorizontalAlignment0();
      }
    }, ColumnInfo.Alignment.UNKNOWN);
  }

  /**
   * Implementation of {@link #getHorizontalAlignment()}.
   */
  private ColumnInfo.Alignment getHorizontalAlignment0() throws Exception {
    // may be fill
    if (hasWidthFill()) {
      return ColumnInfo.Alignment.FILL;
    }
    // analyze setAlignment() invocations
    final ColumnInfo.Alignment[] result = new ColumnInfo.Alignment[]{ColumnInfo.Alignment.UNKNOWN};
    m_panel.visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        if (row == m_row) {
          int column = m_panel.getStatus().getColumnOfCell(row, cell);
          if (column == m_column) {
            if (methodDescription.getSignature().equals(SET_HOR_ALIGNMENT)) {
              updateAlignment(invocation, 2);
            }
            if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
              updateAlignment(invocation, 2);
            }
          }
        }
      }

      private void updateAlignment(MethodInvocation invocation, int index) throws Exception {
        Expression alignmentArgument = DomGenerics.arguments(invocation).get(index);
        Object alignmentObject = JavaInfoEvaluationHelper.getValue(alignmentArgument);
        String alignmentString = (String) invokeMethod(alignmentObject, "getTextAlignString()");
        for (ColumnInfo.Alignment alignment : ColumnInfo.Alignment.values()) {
          if (ObjectUtils.equals(alignment.getAlignmentString(), alignmentString)) {
            result[0] = alignment;
          }
        }
      }
    });
    return result[0];
  }

  /**
   * Sets horizontal alignment of {@link WidgetInfo} in cell.
   */
  public void setHorizontalAlignment(ColumnInfo.Alignment alignment) throws Exception {
    // remove existing alignment invocations
    m_panel.visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        if (row == m_row) {
          int column = m_panel.getStatus().getColumnOfCell(row, cell);
          if (column == m_column) {
            AstEditor editor = m_panel.getEditor();
            if (methodDescription.getSignature().equals(SET_HOR_ALIGNMENT)) {
              editor.removeEnclosingStatement(invocation);
            } else if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
              editor.replaceInvocationName(invocation, "setVerticalAlignment");
              editor.removeInvocationArgument(invocation, 2);
              editor.replaceInvocationBinding(invocation);
            }
          }
        }
      }
    });
    // remove fill
    if (hasWidthFill()) {
      m_component.getSizeSupport().setSize(IUIObjectSizeSupport.NO_SIZE, null);
    }
    // may be set fill
    if (alignment == ColumnInfo.Alignment.FILL) {
      m_component.getSizeSupport().setSize("100%", null);
      return;
    }
    // set alignment for cell
    String alignmentSource = alignment.getAlignmentField();
    if (alignmentSource != null) {
      alignmentSource = "com.google.gwt.user.client.ui.HasHorizontalAlignment." + alignmentSource;
      {
        StatementTarget target = JavaInfoUtils.getTarget(m_panel);
        int cell = m_panel.getStatus().getCellOfColumn(m_row, m_column);
        String arguments = m_row + ", " + cell + ", " + alignmentSource;
        m_panel.getCellFormatter().addMethodInvocation(target, SET_HOR_ALIGNMENT, arguments);
      }
    }
  }

  /**
   * @return <code>true</code> if widget fill cell horizontally.
   */
  private boolean hasWidthFill() throws Exception {
    Property widthProperty = PropertyUtils.getByPath(m_component, "Size/width");
    Object width = widthProperty.getValue();
    return "100%".equals(width);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Vertical alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the vertical alignment of {@link WidgetInfo} in cell.
   */
  public RowInfo.Alignment getVerticalAlignment() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<RowInfo.Alignment>() {
      public RowInfo.Alignment runObject() throws Exception {
        return getVerticalAlignment0();
      }
    }, RowInfo.Alignment.UNKNOWN);
  }

  /**
   * Implementation of {@link #getVerticalAlignment()}.
   */
  private RowInfo.Alignment getVerticalAlignment0() throws Exception {
    // may be fill
    if (hasHeightFill()) {
      return RowInfo.Alignment.FILL;
    }
    // analyze setAlignment() invocations
    final RowInfo.Alignment[] result = new RowInfo.Alignment[]{RowInfo.Alignment.UNKNOWN};
    m_panel.visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        if (row == m_row) {
          int column = m_panel.getStatus().getColumnOfCell(row, cell);
          if (column == m_column) {
            if (methodDescription.getSignature().equals(SET_VER_ALIGNMENT)) {
              updateAlignment(invocation, 2);
            }
            if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
              updateAlignment(invocation, 3);
            }
          }
        }
      }

      private void updateAlignment(MethodInvocation invocation, int index) throws Exception {
        Expression alignmentArgument = DomGenerics.arguments(invocation).get(index);
        Object alignmentObject = JavaInfoEvaluationHelper.getValue(alignmentArgument);
        String alignmentString = (String) invokeMethod(alignmentObject, "getVerticalAlignString()");
        for (RowInfo.Alignment alignment : RowInfo.Alignment.values()) {
          if (ObjectUtils.equals(alignment.getAlignmentString(), alignmentString)) {
            result[0] = alignment;
          }
        }
      }
    });
    return result[0];
  }

  /**
   * Sets vertical alignment of {@link WidgetInfo} in cell.
   */
  public void setVerticalAlignment(RowInfo.Alignment alignment) throws Exception {
    // remove existing alignment invocations
    m_panel.visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        if (row == m_row) {
          int column = m_panel.getStatus().getColumnOfCell(row, cell);
          if (column == m_column) {
            AstEditor editor = m_panel.getEditor();
            if (methodDescription.getSignature().equals(SET_VER_ALIGNMENT)) {
              editor.removeEnclosingStatement(invocation);
            } else if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
              editor.removeInvocationArgument(invocation, 3);
              editor.replaceInvocationName(invocation, "setHorizontalAlignment");
              editor.replaceInvocationBinding(invocation);
            }
          }
        }
      }
    });
    // remove fill
    if (hasHeightFill()) {
      m_component.getSizeSupport().setSize(null, IUIObjectSizeSupport.NO_SIZE);
    }
    // may be set fill
    if (alignment == RowInfo.Alignment.FILL) {
      m_component.getSizeSupport().setSize(null, "100%");
      return;
    }
    // set alignment for cell
    String alignmentSource = alignment.getAlignmentField();
    if (alignmentSource != null) {
      alignmentSource = "com.google.gwt.user.client.ui.HasVerticalAlignment." + alignmentSource;
      {
        StatementTarget target = JavaInfoUtils.getTarget(m_panel);
        int cell = m_panel.getStatus().getCellOfColumn(m_row, m_column);
        String arguments = m_row + ", " + cell + ", " + alignmentSource;
        m_panel.getCellFormatter().addMethodInvocation(target, SET_VER_ALIGNMENT, arguments);
      }
    }
  }

  /**
   * @return <code>true</code> if widget fill cell vertically.
   */
  private boolean hasHeightFill() throws Exception {
    Property heightProperty = PropertyUtils.getByPath(m_component, "Size/height");
    Object height = heightProperty.getValue();
    return "100%".equals(height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds items to the context {@link IMenuManager}.
   */
  public void addContextMenu(IMenuManager manager) throws Exception {
    // horizontal
    {
      IMenuManager manager2 = new MenuManager("Horizontal alignment");
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillHorizontalAlignmentMenu(manager2);
    }
    // vertical
    {
      IMenuManager manager2 = new MenuManager("Vertical alignment");
      manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
      fillVerticalAlignmentMenu(manager2);
    }
  }

  /**
   * Adds the horizontal alignment {@link Action}'s.
   */
  public void fillHorizontalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetHorizontalAlignmentAction("&Default\tD", ColumnInfo.Alignment.UNKNOWN));
    manager.add(new SetHorizontalAlignmentAction("&Left\tL", ColumnInfo.Alignment.LEFT));
    manager.add(new SetHorizontalAlignmentAction("&Center\tC", ColumnInfo.Alignment.CENTER));
    manager.add(new SetHorizontalAlignmentAction("&Fill\tF", ColumnInfo.Alignment.FILL));
    manager.add(new SetHorizontalAlignmentAction("&Right\tR", ColumnInfo.Alignment.RIGHT));
  }

  /**
   * Adds the vertical alignment {@link Action}'s.
   */
  public void fillVerticalAlignmentMenu(IMenuManager manager) {
    manager.add(new SetVerticalAlignmentAction("&Default\tShift+D", RowInfo.Alignment.UNKNOWN));
    manager.add(new SetVerticalAlignmentAction("&Top\tT", RowInfo.Alignment.TOP));
    manager.add(new SetVerticalAlignmentAction("&Middle\tM", RowInfo.Alignment.MIDDLE));
    manager.add(new SetVerticalAlignmentAction("&Fill\tShift+F", RowInfo.Alignment.FILL));
    manager.add(new SetVerticalAlignmentAction("&Bottom\tB", RowInfo.Alignment.BOTTOM));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetHorizontalAlignmentAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for modifying horizontal alignment.
   */
  private class SetHorizontalAlignmentAction extends ObjectInfoAction {
    private final ColumnInfo.Alignment m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SetHorizontalAlignmentAction(String text, ColumnInfo.Alignment alignment) {
      super(m_panel, text, AS_RADIO_BUTTON);
      m_alignment = alignment;
      setImageDescriptor(alignment.getMenuImage());
      setChecked(getHorizontalAlignment() == m_alignment);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      setHorizontalAlignment(m_alignment);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // SetVerticalAlignmentAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for modifying vertical alignment.
   */
  private class SetVerticalAlignmentAction extends ObjectInfoAction {
    private final RowInfo.Alignment m_alignment;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SetVerticalAlignmentAction(String text, RowInfo.Alignment alignment) {
      super(m_panel, text, AS_RADIO_BUTTON);
      m_alignment = alignment;
      setImageDescriptor(alignment.getMenuImage());
      setChecked(getVerticalAlignment() == m_alignment);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      setVerticalAlignment(m_alignment);
    }
  }
}
