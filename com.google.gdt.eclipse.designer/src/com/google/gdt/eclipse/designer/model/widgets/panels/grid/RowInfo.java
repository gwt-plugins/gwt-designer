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
package com.google.gdt.eclipse.designer.model.widgets.panels.grid;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;

import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Information about single row in {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class RowInfo extends DimensionInfo {
  public enum Alignment {
    UNKNOWN {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("v/alignment/unknown.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("v/menu/unknown.gif");
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
    TOP {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("v/alignment/top.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("v/menu/top.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_TOP";
      }

      @Override
      public String getAlignmentString() {
        return "top";
      }
    },
    MIDDLE {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("v/alignment/middle.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("v/menu/middle.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_MIDDLE";
      }

      @Override
      public String getAlignmentString() {
        return "middle";
      }
    },
    FILL {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("v/alignment/fill.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("v/menu/fill.gif");
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
    BOTTOM {
      @Override
      public Image getSmallImage() {
        return HTMLTableInfo.getImage("v/alignment/bottom.gif");
      }

      @Override
      public ImageDescriptor getMenuImage() {
        return HTMLTableInfo.getImageDescriptor("v/menu/bottom.gif");
      }

      @Override
      public String getAlignmentField() {
        return "ALIGN_BOTTOM";
      }

      @Override
      public String getAlignmentString() {
        return "bottom";
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
  public RowInfo(HTMLTableInfo panel) {
    super(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_panel.getRows().indexOf(this);
  }

  @Override
  public boolean isLast() {
    return GenericsUtils.getLast(m_panel.getRows()) == this;
  }

  @Override
  public boolean isEmpty() {
    return m_panel.isEmptyRow(getIndex());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String SET_VER_ALIGNMENT = "setVerticalAlign(int,"
      + "com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant)";

  /**
   * @return the common vertical alignment of {@link WidgetInfo}'s in this column. There are no
   *         separate way to ask/set horizontal alignment for column of <code>HTMLTable</code>.
   */
  public Alignment getAlignment() {
    int index = getIndex();
    List<MethodInvocation> invocations =
        m_panel.getRowFormatter().getMethodInvocations(SET_VER_ALIGNMENT);
    for (MethodInvocation invocation : invocations) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      Expression rowExpression = arguments.get(0);
      Integer rowValue = (Integer) JavaInfoEvaluationHelper.getValue(rowExpression);
      if (rowValue != null && rowValue == index) {
        Expression alignmentArgument = arguments.get(1);
        return getAlignment(alignmentArgument);
      }
    }
    return Alignment.UNKNOWN;
  }

  /**
   * @return the {@link Alignment} for given GWT alignment expression.
   */
  private static Alignment getAlignment(final Expression alignmentArgument) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Alignment>() {
      public Alignment runObject() throws Exception {
        return getAlignment0(alignmentArgument);
      }
    });
  }

  /**
   * @return the {@link Alignment} for given GWT alignment expression.
   */
  private static Alignment getAlignment0(Expression alignmentArgument) throws Exception {
    Object alignmentObject = JavaInfoEvaluationHelper.getValue(alignmentArgument);
    String alignmentString = (String) invokeMethod(alignmentObject, "getVerticalAlignString()");
    Alignment alignment = null;
    for (Alignment _alignment : Alignment.values()) {
      if (ObjectUtils.equals(_alignment.getAlignmentString(), alignmentString)) {
        alignment = _alignment;
      }
    }
    return alignment;
  }

  /**
   * Sets horizontal alignment of {@link WidgetInfo}'s for all cells in this column. There are no
   * separate way to ask/set horizontal alignment for column of <code>HTMLTable</code>, so we have
   * to set horizontal alignment for all cells.
   */
  public void setAlignment(Alignment alignment) throws Exception {
    String alignmentSource = alignment.getAlignmentField();
    if (alignmentSource != null) {
      alignmentSource = "com.google.gwt.user.client.ui.HasVerticalAlignment." + alignmentSource;
      MethodInvocation invocation =
          m_panel.getRowFormatter().getMethodInvocation(SET_VER_ALIGNMENT);
      if (invocation == null) {
        StatementTarget target = JavaInfoUtils.getTarget(m_panel);
        String arguments = getIndex() + ", " + alignmentSource;
        m_panel.getRowFormatter().addMethodInvocation(target, SET_VER_ALIGNMENT, arguments);
      } else {
        Expression alignmentExpression = DomGenerics.arguments(invocation).get(1);
        m_panel.getEditor().replaceExpression(alignmentExpression, alignmentSource);
      }
    } else {
      m_panel.getRowFormatter().removeMethodInvocations(SET_VER_ALIGNMENT);
    }
  }
}
