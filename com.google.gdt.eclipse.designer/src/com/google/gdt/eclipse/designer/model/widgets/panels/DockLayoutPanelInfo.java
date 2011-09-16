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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.DockLayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class DockLayoutPanelInfo extends ComplexPanelInfo
    implements
      IDockLayoutPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DockLayoutPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    decorateWidgetText();
    scaleSize_whenChangeUnit();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Decorate Widget's text with edge.
   */
  private void decorateWidgetText() {
    addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == DockLayoutPanelInfo.this) {
          String edgeName = getEdge((WidgetInfo) object);
          text[0] = edgeName + " - " + text[0];
        }
      }
    });
  }

  private void scaleSize_whenChangeUnit() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void setPropertyExpression(GenericPropertyImpl property,
          String[] source,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        if (property.getJavaInfo() == DockLayoutPanelInfo.this
            && property.getTitle().equalsIgnoreCase("unit")
            && value[0] != Property.UNKNOWN_VALUE) {
          double factorH = getFactor(value[0], false);
          double factorV = getFactor(value[0], true);
          for (WidgetInfo widget : getChildrenWidgets()) {
            Double oldSize = getSize(widget);
            if (oldSize != null) {
              boolean isHorizontal = isHorizontalEdge(widget);
              boolean isVertical = isVerticalEdge(widget);
              if (isHorizontal) {
                double newSize = oldSize * factorH;
                setSize(widget, newSize);
              }
              if (isVertical) {
                double newSize = oldSize * factorV;
                setSize(widget, newSize);
              }
            }
          }
        }
      }

      private double getFactor(Object newUnit, boolean vertical) throws Exception {
        Object oldUnit = getCurrentUnit();
        double oldUnitSize = getUnitSize(oldUnit, vertical);
        double newUnitSize = getUnitSize(newUnit, vertical);
        return oldUnitSize / newUnitSize;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new RootLayoutPanelTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edge
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getEdge(WidgetInfo widget) {
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      String methodName = association.getInvocation().getName().getIdentifier();
      String edge = StringUtils.removeStart(methodName, "add");
      if (edge.length() == 0) {
        return "CENTER";
      }
      return edge.toUpperCase(Locale.ENGLISH);
    }
    return "UNKNOWN";
  }

  /**
   * @return <code>true</code> if given {@link WidgetInfo} is on horizontal edge (west or east).
   */
  public boolean isHorizontalEdge(WidgetInfo widget) {
    String edge = getEdge(widget);
    return edge.equals("WEST") || edge.equals("EAST");
  }

  /**
   * @return <code>true</code> if given {@link WidgetInfo} is on vertical edge (north or south).
   */
  public boolean isVerticalEdge(WidgetInfo widget) {
    String edge = getEdge(widget);
    return edge.equals("NORTH") || edge.equals("SOUTH");
  }

  public void setEdge(WidgetInfo widget, String edge) throws Exception {
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      // add/remove "size" argument
      String oldMethodName = invocation.getName().getIdentifier();
      if (!edge.equals("CENTER") && oldMethodName.equals("add")) {
        getEditor().addInvocationArgument(invocation, 1, "1.0");
        setReasonableSize(widget);
      } else if (edge.equals("CENTER") && !oldMethodName.equals("add")) {
        getEditor().removeInvocationArgument(invocation, 1);
      }
      // prepare new method name
      String newMethodName;
      if (edge.equals("CENTER")) {
        newMethodName = "add";
      } else {
        newMethodName = "add" + StringUtils.capitalize(edge.toLowerCase());
      }
      // replace method name
      getEditor().replaceInvocationName(invocation, newMethodName);
      // ensure correct order
      ensureWidgetBeforeCenter(widget);
    }
  }

  private void ensureWidgetBeforeCenter(WidgetInfo widget) throws Exception {
    List<WidgetInfo> childrenWidgets = getChildrenWidgets();
    // prepare CENTER widget
    WidgetInfo centerWidget = null;
    for (WidgetInfo childWidget : childrenWidgets) {
      String childEdge = getEdge(childWidget);
      if ("CENTER".equals(childEdge)) {
        centerWidget = childWidget;
      }
    }
    // move current widget before CENTER
    if (centerWidget != null && centerWidget != widget) {
      int widgetIndex = childrenWidgets.indexOf(widget);
      int centerIndex = childrenWidgets.indexOf(centerWidget);
      if (widgetIndex > centerIndex) {
        command_MOVE2(widget, centerWidget);
      }
    }
  }

  /**
   * This method is used by description based "Edge" property.
   */
  void setEdge(final WidgetInfo widget, final Object edge) throws Exception {
    if (edge instanceof String) {
      ExecutionUtils.run(this, new RunnableEx() {
        public void run() throws Exception {
          setEdge(widget, (String) edge);
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.0",
      new DecimalFormatSymbols(Locale.ENGLISH));

  /**
   * @return the current size of {@link WidgetInfo}, in units. May be <code>null</code>.
   */
  private Double getSize(WidgetInfo widget) {
    Expression sizeExpression = getSizeExpression(widget);
    if (sizeExpression != null) {
      return ((Number) JavaInfoEvaluationHelper.getValue(sizeExpression)).doubleValue();
    }
    return null;
  }

  public void setSize(WidgetInfo widget, double size) throws Exception {
    Expression sizeExpression = getSizeExpression(widget);
    if (sizeExpression != null) {
      String newSizeSource = SIZE_FORMAT.format(size);
      getEditor().replaceExpression(sizeExpression, newSizeSource);
    }
  }

  /**
   * @return the {@link Expression} of size argument of association. May be <code>null</code>.
   */
  private Expression getSizeExpression(WidgetInfo widget) {
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      List<Expression> arguments = DomGenerics.arguments(invocation);
      if (arguments.size() == 2) {
        return arguments.get(1);
      }
    }
    return null;
  }

  public void setReasonableSize(WidgetInfo widget) throws Exception {
    double size = getReasonableSize(widget);
    setSize(widget, size);
  }

  /**
   * @return the size in units to use as reasonable size.
   */
  private double getReasonableSize(WidgetInfo widget) {
    String unit = getCurrentUnit().toString();
    // special cases
    if (unit.equals("CM")) {
      return 1.0;
    }
    if (unit.equals("MM")) {
      return 10.0;
    }
    if (unit.equals("IN")) {
      return 1.0;
    }
    // generic case, use fixed pixels
    boolean horizontal = isHorizontalEdge(widget);
    return getSizeInUnits(100, horizontal);
  }

  public double getSizeInUnits(int pixels, boolean vertical) {
    Object unit = getCurrentUnit();
    return pixels / getUnitSize(unit, vertical);
  }

  public String getUnitSizeTooltip(double units) {
    return SIZE_FORMAT.format(units) + getCurrentUnit().toString().toLowerCase();
  }

  /**
   * @return the current "Unit", used in this panel.
   */
  private Object getCurrentUnit() {
    return ReflectionUtils.getFieldObject(getObject(), "unit");
  }

  /**
   * @return the size of "Unit" in pixels.
   */
  private double getUnitSize(Object unit, boolean vertical) {
    Object layout = ReflectionUtils.getFieldObject(getObject(), "layout");
    return (Double) ReflectionUtils.invokeMethodEx(
        layout,
        "getUnitSize(com.google.gwt.dom.client.Style.Unit,boolean)",
        unit,
        vertical);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final String edge = getEdge(widget);
    final double size = getSize(widget);
    commands.add(new PanelClipboardCommand<DockLayoutPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(DockLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.setEdge(widget, edge);
        panel.setSize(widget, size);
      }
    });
  }
}
