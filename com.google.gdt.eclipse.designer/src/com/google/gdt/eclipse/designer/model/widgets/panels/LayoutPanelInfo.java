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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDeactivePropertyEditor;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.DoublePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.LayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class LayoutPanelInfo extends ComplexPanelInfo implements ILayoutPanelInfo<WidgetInfo> {
  private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0",
      new DecimalFormatSymbols(Locale.ENGLISH));
  private final LayoutPanelAlignmentSupport<WidgetInfo> m_alignmentSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    m_alignmentSupport = new LayoutPanelAlignmentSupport<WidgetInfo>(this);
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == LayoutPanelInfo.this && child instanceof WidgetInfo) {
          WidgetInfo widget = (WidgetInfo) child;
          for (MethodInvocation invocation : getMethodInvocations()) {
            if (isLocationInvocation(invocation, widget)) {
              getEditor().removeEnclosingStatement(invocation);
            }
          }
        }
      }

      private boolean isLocationInvocation(MethodInvocation invocation, WidgetInfo widget) {
        String signature = AstNodeUtils.getMethodSignature(invocation);
        if (signature.startsWith("setWidget")
            && signature.endsWith("(com.google.gwt.user.client.ui.Widget,"
                + "double,com.google.gwt.dom.client.Style.Unit,"
                + "double,com.google.gwt.dom.client.Style.Unit)")) {
          Expression widgetExpression = DomGenerics.arguments(invocation).get(0);
          return widget.isRepresentedBy(widgetExpression);
        }
        return false;
      }
    });
    addLocationProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the provider for managing "anchor".
   */
  public LayoutPanelAlignmentSupport<WidgetInfo> getAlignmentSupport() {
    return m_alignmentSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addLocationProperties() {
    addBroadcastListener(new ObjectInfoAllProperties() {
      public void invoke(ObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == LayoutPanelInfo.this) {
          WidgetInfo widget = (WidgetInfo) object;
          addLocationProperties(properties, widget, true, "Anchor H");
          addLocationProperties(properties, widget, false, "Anchor V");
        }
      }
    });
  }

  private void addLocationProperties(List<Property> properties,
      WidgetInfo widget,
      boolean horizontal,
      String title) {
    MethodInvocation invocation = getLocationInvocation(widget, horizontal);
    if (invocation == null) {
      return;
    }
    String signature = AstNodeUtils.getMethodSignature(invocation);
    //
    ComplexProperty complexProperty;
    {
      @SuppressWarnings("unchecked")
      Map<String, ComplexProperty> complexProperties =
          (Map<String, ComplexProperty>) widget.getArbitraryValue(this);
      if (complexProperties == null) {
        complexProperties = Maps.newTreeMap();
        widget.putArbitraryValue(this, complexProperties);
      }
      complexProperty = complexProperties.get(title);
      if (complexProperty == null) {
        complexProperty = new ComplexProperty(title, "<properties>");
        complexProperty.setCategory(PropertyCategory.system(10));
        complexProperties.put(signature, complexProperty);
      }
      properties.add(complexProperty);
    }
    // update sub-properties
    String[] propertyTitles = getLocationPropertyTitles(signature);
    String title_1 = propertyTitles[0];
    String title_3 = propertyTitles[1];
    Property property_1 = new LocationValue_Property(title_1, invocation, 1);
    Property property_1u = new LocationUnit_Property(title_1 + " unit", invocation, 2, horizontal);
    Property property_3 = new LocationValue_Property(title_3, invocation, 3);
    Property property_3u = new LocationUnit_Property(title_3 + " unit", invocation, 4, horizontal);
    Property[] subProperties = new Property[]{property_1, property_1u, property_3, property_3u};
    complexProperty.setProperties(subProperties);
  }

  /**
   * Converts "setWidgetLeftRight()" into <code>["left", "right"]</code>.
   */
  private static String[] getLocationPropertyTitles(String signature) {
    String name = StringUtils.substringBefore(signature, "(");
    String elementsName = StringUtils.remove(name, "setWidget");
    String[] titles = StringUtils.splitByCharacterTypeCamelCase(elementsName);
    Assert.isTrue(titles.length == 2, signature);
    titles[0] = titles[0].toLowerCase();
    titles[1] = titles[1].toLowerCase();
    return titles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property: value
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class LocationValue_Property extends Property {
    private final String m_title;
    private final int m_index;
    private final MethodInvocation m_invocation;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LocationValue_Property(String title, MethodInvocation invocation, int index) {
      super(DoublePropertyEditor.INSTANCE);
      m_title = title;
      m_invocation = invocation;
      m_index = index;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return m_title;
    }

    @Override
    public boolean isModified() throws Exception {
      return true;
    }

    @Override
    public Object getValue() throws Exception {
      Expression expression = DomGenerics.arguments(m_invocation).get(m_index);
      return JavaInfoEvaluationHelper.getValue(expression);
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof Double) {
        final double pixels = ((Double) value).doubleValue();
        ExecutionUtils.run(LayoutPanelInfo.this, new RunnableEx() {
          public void run() throws Exception {
            setInvocationArgument(m_invocation, m_index, pixels);
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property: unit
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String[] UNIT_NAMES =
      {"PX", "PCT", "EM", "EX", "PT", "PC", "IN", "CM", "MM"};
  private static final PropertyEditor UNIT_PROPERTY_EDITOR =
      new StringComboPropertyEditor(UNIT_NAMES);

  private final class LocationUnit_Property extends Property {
    private final String m_title;
    private final int m_index;
    private final MethodInvocation m_invocation;
    private final boolean m_horizontal;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LocationUnit_Property(String title,
        MethodInvocation invocation,
        int index,
        boolean horizontal) {
      super(UNIT_PROPERTY_EDITOR);
      m_title = title;
      m_invocation = invocation;
      m_index = index;
      m_horizontal = horizontal;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return m_title;
    }

    @Override
    public boolean isModified() throws Exception {
      return true;
    }

    @Override
    public Object getValue() throws Exception {
      return getUnit(m_invocation, m_index).toString();
    }

    @Override
    public void setValue(final Object value) throws Exception {
      if (value instanceof String) {
        ExecutionUtils.run(LayoutPanelInfo.this, new RunnableEx() {
          public void run() throws Exception {
            changeInvocationUnit(m_invocation, m_index, value, m_horizontal);
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location hint in units for given location in pixels.
   */
  public String getLocationHint(final WidgetInfo widget, final int x, final int y) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return getLocationHint(x, widget, true) + " x " + getLocationHint(y, widget, false);
      }
    }, x + " x " + y);
  }

  private String getLocationHint(int pixels, WidgetInfo widget, boolean horizontal)
      throws Exception {
    MethodInvocation invocation = getLocationInvocation(widget, horizontal);
    // may be trailing
    if (isLocationTrailing(invocation)) {
      if (horizontal) {
        pixels = getBounds().width - (widget.getBounds().width + pixels);
      } else {
        pixels = getBounds().height - (widget.getBounds().height + pixels);
      }
    }
    // convert pixels to units
    Object unit = getLocationUnit(invocation);
    double units = pixels / getUnitSize(unit, !horizontal);
    return SIZE_FORMAT.format(units) + unit.toString().toLowerCase();
  }

  /**
   * @return <code>true</code> if {@link WidgetInfo} is attached to trailing size of panel.
   */
  public boolean getLocationHint_isTrailing(WidgetInfo widget, boolean horizontal) {
    MethodInvocation invocation = getLocationInvocation(widget, horizontal);
    return isLocationTrailing(invocation);
  }

  /**
   * @return <code>true</code> if {@link MethodInvocation} is attachment to trailing size of panel.
   */
  private boolean isLocationTrailing(MethodInvocation invocation) {
    if (invocation != null) {
      String name = invocation.getName().getIdentifier();
      return "setWidgetRightWidth".equals(name) || "setWidgetBottomHeight".equals(name);
    }
    return false;
  }

  private Object getLocationUnit(MethodInvocation invocation) throws Exception {
    if (invocation != null) {
      return getUnit(invocation, 2);
    }
    return getUnitByName("PX");
  }

  MethodInvocation getLocationInvocation(WidgetInfo widget, boolean horizontal) {
    MethodInvocation invocation;
    if (horizontal) {
      if ((invocation = getWidgetInvocation(widget, "setWidgetLeftWidth")) != null) {
        return invocation;
      }
      if ((invocation = getWidgetInvocation(widget, "setWidgetRightWidth")) != null) {
        return invocation;
      }
      if ((invocation = getWidgetInvocation(widget, "setWidgetLeftRight")) != null) {
        return invocation;
      }
    } else {
      if ((invocation = getWidgetInvocation(widget, "setWidgetTopHeight")) != null) {
        return invocation;
      }
      if ((invocation = getWidgetInvocation(widget, "setWidgetBottomHeight")) != null) {
        return invocation;
      }
      if ((invocation = getWidgetInvocation(widget, "setWidgetTopBottom")) != null) {
        return invocation;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds: location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_LOCATION(WidgetInfo widget, Point location) throws Exception {
    command_LOCATION_Y(widget, location.y);
    command_LOCATION_X(widget, location.x);
  }

  private void command_LOCATION_X(WidgetInfo widget, int x) throws Exception {
    // LeftWidth
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetLeftWidth");
      if (invocation != null) {
        setInvocationArgument(invocation, 1, x, true);
        return;
      }
    }
    // RightWidth
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetRightWidth");
      if (invocation != null) {
        int right = getBounds().width - x - widget.getBounds().width;
        setInvocationArgument(invocation, 1, right, true);
        return;
      }
    }
    // LeftRight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetLeftRight");
      if (invocation != null) {
        int right = getBounds().width - x - widget.getBounds().width;
        setInvocationArgument(invocation, 1, x, true);
        setInvocationArgument(invocation, 3, right, true);
        return;
      }
    }
    // new, use LeftWidth
    {
      int defaultWidth = getDefaultSize(widget).width;
      String source =
          TemplateUtils.format(
              "{0}.setWidgetLeftWidth({1}, {2}, {3}, {4}, {3})",
              this,
              widget,
              SIZE_FORMAT.format(x),
              "com.google.gwt.dom.client.Style.Unit.PX",
              SIZE_FORMAT.format(defaultWidth));
      StatementTarget target = getNewConstraintsTarget(widget);
      Expression expression = widget.addExpressionStatement(target, source);
      addRelatedNodes(expression);
    }
  }

  private void command_LOCATION_Y(WidgetInfo widget, int y) throws Exception {
    // TopHeight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetTopHeight");
      if (invocation != null) {
        setInvocationArgument(invocation, 1, y, false);
        return;
      }
    }
    // BottomHeight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetBottomHeight");
      if (invocation != null) {
        int bottom = getBounds().height - y - widget.getBounds().height;
        setInvocationArgument(invocation, 1, bottom, false);
        return;
      }
    }
    // TopBottom
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetTopBottom");
      if (invocation != null) {
        int bottom = getBounds().height - y - widget.getBounds().height;
        setInvocationArgument(invocation, 1, y, false);
        setInvocationArgument(invocation, 3, bottom, false);
        return;
      }
    }
    // new, use TopHeight
    {
      int defaultHeight = getDefaultSize(widget).height;
      String source =
          TemplateUtils.format(
              "{0}.setWidgetTopHeight({1}, {2}, {3}, {4}, {3})",
              this,
              widget,
              SIZE_FORMAT.format(y),
              "com.google.gwt.dom.client.Style.Unit.PX",
              SIZE_FORMAT.format(defaultHeight));
      StatementTarget target = getNewConstraintsTarget(widget);
      Expression expression = widget.addExpressionStatement(target, source);
      addRelatedNodes(expression);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds: size
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_SIZE(WidgetInfo widget,
      Dimension size,
      ResizeDirection hDirection,
      ResizeDirection vDirection) throws Exception {
    command_SIZE_Y(widget, size.height, vDirection);
    command_SIZE_X(widget, size.width, hDirection);
  }

  private void command_SIZE_X(WidgetInfo widget, int width, ResizeDirection direction)
      throws Exception {
    Rectangle bounds = widget.getBounds();
    // LeftWidth
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetLeftWidth");
      if (invocation != null) {
        if (direction == ResizeDirection.LEADING) {
          int oldLeft = bounds.left();
          int deltaWidth = width - bounds.width;
          int left = oldLeft - deltaWidth;
          setInvocationArgument(invocation, 1, left, true);
        }
        setInvocationArgument(invocation, 3, width, true);
        return;
      }
    }
    // RightWidth
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetRightWidth");
      if (invocation != null) {
        if (direction == ResizeDirection.TRAILING) {
          int oldRight = getBounds().width - bounds.right();
          int deltaWidth = width - bounds.width;
          int right = oldRight - deltaWidth;
          setInvocationArgument(invocation, 1, right, true);
        }
        setInvocationArgument(invocation, 3, width, true);
        return;
      }
    }
    // LeftRight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetLeftRight");
      if (invocation != null) {
        if (direction == ResizeDirection.LEADING) {
          int oldLeft = bounds.left();
          int deltaWidth = width - bounds.width;
          int left = oldLeft - deltaWidth;
          setInvocationArgument(invocation, 1, left, true);
        }
        if (direction == ResizeDirection.TRAILING) {
          int oldRight = getBounds().width - bounds.right();
          int deltaWidth = width - bounds.width;
          int right = oldRight - deltaWidth;
          setInvocationArgument(invocation, 3, right, true);
        }
        return;
      }
    }
    // new, use LeftWidth
    if (direction == ResizeDirection.TRAILING) {
      String source =
          TemplateUtils.format(
              "{0}.setWidgetLeftWidth({1}, {2}, {3}, {4}, {3})",
              this,
              widget,
              SIZE_FORMAT.format(0.0),
              "com.google.gwt.dom.client.Style.Unit.PX",
              SIZE_FORMAT.format(width));
      StatementTarget target = getNewConstraintsTarget(widget);
      Expression expression = widget.addExpressionStatement(target, source);
      addRelatedNodes(expression);
    }
  }

  private void command_SIZE_Y(WidgetInfo widget, int height, ResizeDirection direction)
      throws Exception {
    Rectangle bounds = widget.getBounds();
    // TopHeight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetTopHeight");
      if (invocation != null) {
        if (direction == ResizeDirection.LEADING) {
          int oldTop = bounds.top();
          int deltaHeight = height - bounds.height;
          int top = oldTop - deltaHeight;
          setInvocationArgument(invocation, 1, top, false);
        }
        setInvocationArgument(invocation, 3, height, false);
        return;
      }
    }
    // BottomHeight
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetBottomHeight");
      if (invocation != null) {
        if (direction == ResizeDirection.TRAILING) {
          int oldBottom = getBounds().height - bounds.bottom();
          int deltaHeight = height - bounds.height;
          int bottom = oldBottom - deltaHeight;
          setInvocationArgument(invocation, 1, bottom, false);
        }
        setInvocationArgument(invocation, 3, height, false);
        return;
      }
    }
    // TopBottom
    {
      MethodInvocation invocation = getWidgetInvocation(widget, "setWidgetTopBottom");
      if (invocation != null) {
        if (direction == ResizeDirection.LEADING) {
          int oldTop = bounds.top();
          int deltaHeight = height - bounds.height;
          int top = oldTop - deltaHeight;
          setInvocationArgument(invocation, 1, top, false);
        }
        if (direction == ResizeDirection.TRAILING) {
          int oldBottom = getBounds().height - bounds.bottom();
          int deltaHeight = height - bounds.height;
          int bottom = oldBottom - deltaHeight;
          setInvocationArgument(invocation, 3, bottom, false);
        }
        return;
      }
    }
    // new, use TopHeight
    if (direction == ResizeDirection.TRAILING) {
      String source =
          TemplateUtils.format(
              "{0}.setWidgetTopHeight({1}, {2}, {3}, {4}, {3})",
              this,
              widget,
              SIZE_FORMAT.format(0.0),
              "com.google.gwt.dom.client.Style.Unit.PX",
              SIZE_FORMAT.format(height));
      StatementTarget target = getNewConstraintsTarget(widget);
      Expression expression = widget.addExpressionStatement(target, source);
      addRelatedNodes(expression);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Anchor} type for given {@link WidgetInfo}.
   */
  public Anchor getAnchor(WidgetInfo widget, boolean horizontal) {
    if (horizontal) {
      if (getWidgetInvocation(widget, "setWidgetLeftWidth") != null) {
        return Anchor.LEADING;
      }
      if (getWidgetInvocation(widget, "setWidgetRightWidth") != null) {
        return Anchor.TRAILING;
      }
      if (getWidgetInvocation(widget, "setWidgetLeftRight") != null) {
        return Anchor.BOTH;
      }
    } else {
      if (getWidgetInvocation(widget, "setWidgetTopHeight") != null) {
        return Anchor.LEADING;
      }
      if (getWidgetInvocation(widget, "setWidgetBottomHeight") != null) {
        return Anchor.TRAILING;
      }
      if (getWidgetInvocation(widget, "setWidgetTopBottom") != null) {
        return Anchor.BOTH;
      }
    }
    return Anchor.NONE;
  }

  /**
   * Sets anchor for given {@link WidgetInfo}.
   */
  public void command_ANCHOR(WidgetInfo widget, boolean horizontal, Anchor anchor) throws Exception {
    if (horizontal) {
      command_ANCHOR_horizontal(widget, anchor);
    } else {
      command_ANCHOR_vertical(widget, anchor);
    }
    getBroadcast(ObjectInfoDeactivePropertyEditor.class).invoke();
  }

  private void command_ANCHOR_horizontal(WidgetInfo widget, Anchor anchor) throws Exception {
    MethodInvocation invocation;
    Rectangle bounds = widget.getBounds();
    AstEditor editor = getEditor();
    if ((invocation = getWidgetInvocation(widget, "setWidgetLeftWidth")) != null) {
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.TRAILING) {
        int right = getBounds().width - bounds.right();
        editor.replaceInvocationName(invocation, "setWidgetRightWidth");
        setInvocationArgument(invocation, 1, right, true);
      }
      if (anchor == Anchor.BOTH) {
        int right = getBounds().width - bounds.right();
        editor.replaceInvocationName(invocation, "setWidgetLeftRight");
        setInvocationArgument(invocation, 3, right, true);
      }
      return;
    }
    if ((invocation = getWidgetInvocation(widget, "setWidgetRightWidth")) != null) {
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.LEADING) {
        int left = bounds.left();
        editor.replaceInvocationName(invocation, "setWidgetLeftWidth");
        setInvocationArgument(invocation, 1, left, true);
      }
      if (anchor == Anchor.BOTH) {
        editor.replaceInvocationName(invocation, "setWidgetLeftRight");
        {
          Expression rightExpression = DomGenerics.arguments(invocation).get(1);
          editor.replaceInvocationArgument(invocation, 3, editor.getSource(rightExpression));
        }
        setInvocationArgument(invocation, 1, bounds.left(), true);
      }
      return;
    }
    if ((invocation = getWidgetInvocation(widget, "setWidgetLeftRight")) != null) {
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.LEADING) {
        editor.replaceInvocationName(invocation, "setWidgetLeftWidth");
        // use same unit for left/width
        Object unit = getUnit(invocation, 2);
        setInvocationUnit(invocation, 4, unit);
        // set width
        setInvocationArgument(invocation, 3, bounds.width, true);
      }
      if (anchor == Anchor.TRAILING) {
        editor.replaceInvocationName(invocation, "setWidgetRightWidth");
        // use same unit for right/width
        Object unit = getUnit(invocation, 4);
        setInvocationUnit(invocation, 2, unit);
        // set right/width
        {
          Expression rightExpression = DomGenerics.arguments(invocation).get(3);
          editor.replaceInvocationArgument(invocation, 1, editor.getSource(rightExpression));
        }
        // set width
        setInvocationArgument(invocation, 3, bounds.width, true);
      }
      return;
    }
    // no anchor yet, generate LEADING and convert
    if (anchor != Anchor.NONE) {
      command_LOCATION_X(widget, bounds.left());
      command_SIZE_X(widget, bounds.width, ResizeDirection.TRAILING);
      if (anchor == Anchor.TRAILING) {
        command_ANCHOR_horizontal(widget, anchor);
      }
      if (anchor == Anchor.BOTH) {
        command_ANCHOR_horizontal(widget, anchor);
      }
    }
  }

  private void command_ANCHOR_vertical(WidgetInfo widget, Anchor anchor) throws Exception {
    MethodInvocation invocation;
    Rectangle bounds = widget.getBounds();
    AstEditor editor = getEditor();
    if ((invocation = getWidgetInvocation(widget, "setWidgetTopHeight")) != null) {
      int bottom = getBounds().height - bounds.bottom();
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.TRAILING) {
        editor.replaceInvocationName(invocation, "setWidgetBottomHeight");
        setInvocationArgument(invocation, 1, bottom, true);
      }
      if (anchor == Anchor.BOTH) {
        editor.replaceInvocationName(invocation, "setWidgetTopBottom");
        setInvocationArgument(invocation, 3, bottom, true);
      }
      return;
    }
    if ((invocation = getWidgetInvocation(widget, "setWidgetBottomHeight")) != null) {
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.LEADING) {
        editor.replaceInvocationName(invocation, "setWidgetTopHeight");
        setInvocationArgument(invocation, 1, bounds.top(), true);
      }
      if (anchor == Anchor.BOTH) {
        editor.replaceInvocationName(invocation, "setWidgetTopBottom");
        {
          Expression rightExpression = DomGenerics.arguments(invocation).get(1);
          editor.replaceInvocationArgument(invocation, 3, editor.getSource(rightExpression));
        }
        setInvocationArgument(invocation, 1, bounds.top(), true);
      }
      return;
    }
    if ((invocation = getWidgetInvocation(widget, "setWidgetTopBottom")) != null) {
      if (anchor == Anchor.NONE) {
        editor.removeEnclosingStatement(invocation);
      }
      if (anchor == Anchor.LEADING) {
        editor.replaceInvocationName(invocation, "setWidgetTopHeight");
        // use same unit for top/height
        Object unit = getUnit(invocation, 2);
        setInvocationUnit(invocation, 4, unit);
        // set height
        setInvocationArgument(invocation, 3, bounds.height, true);
      }
      if (anchor == Anchor.TRAILING) {
        editor.replaceInvocationName(invocation, "setWidgetBottomHeight");
        // use same unit for bottom/height
        Object unit = getUnit(invocation, 4);
        setInvocationUnit(invocation, 2, unit);
        // set bottom/height
        {
          Expression bottomExpression = DomGenerics.arguments(invocation).get(3);
          editor.replaceInvocationArgument(invocation, 1, editor.getSource(bottomExpression));
        }
        // set height
        setInvocationArgument(invocation, 3, bounds.height, true);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static StatementTarget getNewConstraintsTarget(WidgetInfo widget) {
    Statement associationStatement = widget.getAssociation().getStatement();
    return new StatementTarget(associationStatement, false);
  }

  private MethodInvocation getWidgetInvocation(WidgetInfo widget, String name) {
    List<MethodInvocation> invocations =
        getMethodInvocations(name
            + "(com.google.gwt.user.client.ui.Widget,"
            + "double,com.google.gwt.dom.client.Style.Unit,"
            + "double,com.google.gwt.dom.client.Style.Unit)");
    for (MethodInvocation invocation : invocations) {
      Expression widgetExpression = DomGenerics.arguments(invocation).get(0);
      if (widget.isRepresentedBy(widgetExpression)) {
        return invocation;
      }
    }
    return null;
  }

  /**
   * @return the <code>Unit</code> value of {@link MethodInvocation} argument.
   */
  private Object getUnit(MethodInvocation invocation, int index) throws Exception {
    Expression argument = DomGenerics.arguments(invocation).get(index);
    Object value = JavaInfoEvaluationHelper.getValue(argument);
    if (value != null) {
      return value;
    }
    return getUnitByName("PX");
  }

  /**
   * @return the <code>double</code> value of {@link MethodInvocation} argument.
   */
  private double getValue(MethodInvocation invocation, int index) throws Exception {
    Expression argument = DomGenerics.arguments(invocation).get(index);
    Object value = JavaInfoEvaluationHelper.getValue(argument);
    if (value instanceof Double) {
      return (Double) value;
    }
    return 0.0;
  }

  /**
   * @return the <code>Unit</code> object by its name.
   */
  private Object getUnitByName(String name) throws ClassNotFoundException {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> classUnit = classLoader.loadClass("com.google.gwt.dom.client.Style$Unit");
    return ReflectionUtils.getFieldObject(classUnit, name);
  }

  /**
   * @return the size of "Unit" in pixels.
   */
  private double getUnitSize(Object unit, boolean horizontal) {
    Object layout = ReflectionUtils.getFieldObject(getObject(), "layout");
    return (Double) ReflectionUtils.invokeMethodEx(
        layout,
        "getUnitSize(com.google.gwt.dom.client.Style.Unit,boolean)",
        unit,
        !horizontal);
  }

  /**
   * Sets {@link MethodInvocation} argument is appropriate units, for given value in pixels.
   */
  private void setInvocationArgument(MethodInvocation invocation,
      int index,
      int pixels,
      boolean horizontal) throws Exception {
    Object unit = getUnit(invocation, index + 1);
    double units = pixels / getUnitSize(unit, horizontal);
    setInvocationArgument(invocation, index, units);
  }

  /**
   * Sets {@link MethodInvocation} argument is units.
   */
  private void setInvocationArgument(MethodInvocation invocation, int index, double units)
      throws Exception {
    getEditor().replaceInvocationArgument(invocation, index, SIZE_FORMAT.format(units));
  }

  /**
   * Sets {@link MethodInvocation} "unit" argument, with converting value.
   */
  private void changeInvocationUnit(MethodInvocation invocation,
      int index,
      Object newUnit,
      boolean horizontal) throws Exception {
    if (newUnit instanceof String) {
      newUnit = getUnitByName((String) newUnit);
    }
    // prepare value in pixels
    double pixels;
    {
      double oldValue = getValue(invocation, index - 1);
      Object oldUnit = getUnit(invocation, index);
      pixels = oldValue * getUnitSize(oldUnit, horizontal);
    }
    // convert value
    double newUnitSize = getUnitSize(newUnit, horizontal);
    if (newUnitSize > 0.0) {
      double newValue = pixels / newUnitSize;
      setInvocationArgument(invocation, index - 1, newValue);
      setInvocationUnit(invocation, index, newUnit);
    }
  }

  /**
   * Sets {@link MethodInvocation} "unit" argument, without converting value.
   */
  private void setInvocationUnit(MethodInvocation invocation, int index, Object unit)
      throws Exception {
    Expression expression =
        getEditor().replaceInvocationArgument(
            invocation,
            index,
            "com.google.gwt.dom.client.Style.Unit." + unit.toString());
    JavaInfoEvaluationHelper.setValue(expression, unit);
  }

  /**
   * @return the default size for new {@link WidgetInfo}.
   */
  private static Dimension getDefaultSize(WidgetInfo widget) {
    return widget.getBounds().getSize();
  }
}
