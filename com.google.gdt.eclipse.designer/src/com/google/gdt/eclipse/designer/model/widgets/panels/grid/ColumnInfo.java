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

import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo.CellInvocationsVisitor;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;

import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ObjectUtils;

import java.util.Set;

/**
 * Information about single column in {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class ColumnInfo extends DimensionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public enum Alignment {
    UNKNOWN {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("h/alignment/unknown.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("h/menu/unknown.gif");
      }

      @Override
      public String getAlignmentField() {
        return null;
      }

      @Override
      public String getAlignmentString() {
        return null;
      }
    },
    LEFT {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("h/alignment/left.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("h/menu/left.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_LEFT";
      }

      @Override
      public String getAlignmentString() {
        return "left";
      }
    },
    CENTER {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("h/alignment/center.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("h/menu/center.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_CENTER";
      }

      @Override
      public String getAlignmentString() {
        return "center";
      }
    },
    FILL {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("h/alignment/fill.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("h/menu/fill.gif");
      }

      @Override
      public String getAlignmentField() {
        return "N/A";
      }

      @Override
      public String getAlignmentString() {
        return "N/A";
      }
    },
    RIGHT {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("h/alignment/right.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("h/menu/right.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_RIGHT";
      }

      @Override
      public String getAlignmentString() {
        return "right";
      }
    };
    /**
     * @return the small image (9x5) to display current alignment to user.
     */
    public abstract Image getSmallImage();

    /**
     * @return the big image (16x16) to display for user in menu.
     */
    public abstract ImageDescriptor getMenuImage();

    /**
     * @return the name of field in <code>HasHorizontalAlignment</code> type.
     */
    public abstract String getAlignmentField();

    /**
     * @return the value of <code>HorizontalAlignmentConstant.getTextAlignString()</code>.
     */
    public abstract String getAlignmentString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnInfo(HTMLTableInfo panel) {
    super(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_panel.getColumns().indexOf(this);
  }

  @Override
  public boolean isLast() {
    return GenericsUtils.getLast(m_panel.getColumns()) == this;
  }

  @Override
  public boolean isEmpty() {
    return m_panel.isEmptyColumn(getIndex());
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

  /**
   * @return the common horizontal alignment of {@link WidgetInfo}'s in this column. There are no
   *         separate way to ask/set horizontal alignment for column of <code>HTMLTable</code>.
   */
  public Alignment getAlignment() {
    final Set<Alignment> alignments = Sets.newHashSet();
    visitColumnInvocations(new ColumnInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          int row,
          int column) throws Exception {
        if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
          addAlignment(invocation, 2);
        }
        if (methodDescription.getSignature().equals(SET_HOR_ALIGNMENT)) {
          addAlignment(invocation, 2);
        }
      }

      private void addAlignment(MethodInvocation invocation, int alignmentArgumentIndex)
          throws Exception {
        Expression alignmentArgument =
            DomGenerics.arguments(invocation).get(alignmentArgumentIndex);
        Object alignmentObject = JavaInfoEvaluationHelper.getValue(alignmentArgument);
        String alignmentString = (String) invokeMethod(alignmentObject, "getTextAlignString()");
        for (Alignment alignment : Alignment.values()) {
          if (ObjectUtils.equals(alignment.getAlignmentString(), alignmentString)) {
            alignments.add(alignment);
          }
        }
      }
    });
    if (alignments.size() == 1) {
      return alignments.iterator().next();
    } else {
      return Alignment.UNKNOWN;
    }
  }

  /**
   * Sets horizontal alignment of {@link WidgetInfo}'s for all cells in this column. There are no
   * separate way to ask/set horizontal alignment for column of <code>HTMLTable</code>, so we have
   * to set horizontal alignment for all cells.
   */
  public void setAlignment(Alignment alignment) throws Exception {
    // remove existing alignment invocations
    visitColumnInvocations(new ColumnInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          int row,
          int column) throws Exception {
        AstEditor editor = m_panel.getEditor();
        if (methodDescription.getSignature().equals(SET_HOR_ALIGNMENT)) {
          editor.removeEnclosingStatement(invocation);
        } else if (methodDescription.getSignature().equals(SET_ALIGNMENT)) {
          editor.replaceInvocationName(invocation, "setVerticalAlignment");
          editor.removeInvocationArgument(invocation, 2);
          editor.replaceInvocationBinding(invocation);
        }
      }
    });
    // add invocations for each cell in column
    String alignmentSource = alignment.getAlignmentField();
    if (alignmentSource != null) {
      alignmentSource = "com.google.gwt.user.client.ui.HasHorizontalAlignment." + alignmentSource;
      HTMLTableStatus status = m_panel.getStatus();
      int column = getIndex();
      for (int row = 0; row < status.getRowCount();) {
        int cell = status.getCellOfColumn(row, column);
        // set alignment for cell
        {
          StatementTarget target = JavaInfoUtils.getTarget(m_panel);
          String arguments = row + ", " + cell + ", " + alignmentSource;
          m_panel.getCellFormatter().addMethodInvocation(target, SET_HOR_ALIGNMENT, arguments);
        }
        // if cell is row spanned, skip these rows
        {
          int rowSpan = status.getRowSpan(row, cell);
          row += rowSpan;
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits <code>setX(row, cell, value)</code> invocations.
   */
  private interface ColumnInvocationsVisitor {
    void visit(MethodDescription methodDescription, MethodInvocation invocation, int row, int column)
        throws Exception;
  }

  /**
   * Visits all {@link MethodInvocation}'s for this column.
   */
  private void visitColumnInvocations(final ColumnInvocationsVisitor visitor) {
    final int index = getIndex();
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        m_panel.visitCellInvocations(new CellInvocationsVisitor() {
          public void visit(MethodDescription methodDescription,
              MethodInvocation invocation,
              Expression rowArgument,
              Expression cellArgument,
              int row,
              int cell) throws Exception {
            int column = m_panel.getStatus().getColumnOfCell(row, cell);
            if (column == index) {
              visitor.visit(methodDescription, invocation, row, index);
            }
          }
        });
      }
    });
  }
}
