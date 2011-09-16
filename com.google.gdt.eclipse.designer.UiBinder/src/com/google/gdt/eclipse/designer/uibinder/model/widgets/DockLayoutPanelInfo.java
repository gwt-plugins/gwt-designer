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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.panels.IDockLayoutPanelInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Model for <code>com.google.gwt.user.client.ui.DockLayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class DockLayoutPanelInfo extends ComplexPanelInfo
    implements
      IDockLayoutPanelInfo<WidgetInfo> {
  private final Property m_unitProperty = new UnitAttributeProperty(this) {
    @Override
    protected void setValueEx(Object value) throws Exception {
      value = toUnit(value);
      scaleSize_whenChangeUnit(value);
      super.setValueEx(value);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DockLayoutPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    decorateWidgetText();
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

  private void scaleSize_whenChangeUnit(Object value) throws Exception {
    if (XmlObjectMemento.isApplying(this)) {
      return;
    }
    if (value != Property.UNKNOWN_VALUE) {
      double factorH = getFactor(value, false);
      double factorV = getFactor(value, true);
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
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    addUnitProperty(properties);
    return properties;
  }

  protected void addUnitProperty(List<Property> properties) {
    properties.add(m_unitProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edge
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getEdge(WidgetInfo widget) {
    DocumentElement dockElement = widget.getElement().getParent();
    return dockElement.getTagLocal().toUpperCase(Locale.ENGLISH);
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

  public void setEdge(WidgetInfo widget, String newEdge) throws Exception {
    String oldEdge = getEdge(widget);
    DocumentElement dockElement = widget.getElement().getParent();
    // replace tag
    dockElement.setTag(dockElement.getTagNS() + newEdge.toLowerCase());
    // to CENTER
    if ("CENTER".equals(newEdge) && !"CENTER".equals(oldEdge)) {
      dockElement.setAttribute("size", null);
    }
    // from CENTER
    if ("CENTER".equals(oldEdge) && !"CENTER".equals(newEdge)) {
      setReasonableSize(widget);
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
   * @return the current size of {@link WidgetInfo}, in units.
   */
  private Double getSize(WidgetInfo widget) {
    DocumentElement dockElement = widget.getElement().getParent();
    Object value = getContext().getAttributeValue(dockElement, "size");
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return null;
  }

  public void setSize(WidgetInfo widget, double size) throws Exception {
    DocumentElement dockElement = widget.getElement().getParent();
    String sizeString = SIZE_FORMAT.format(size);
    dockElement.setAttribute("size", sizeString);
  }

  public void setReasonableSize(WidgetInfo widget) throws Exception {
    String edge = getEdge(widget);
    if (!"CENTER".equals(edge)) {
      double size = getReasonableSize(widget);
      setSize(widget, size);
    }
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
    final Double size = getSize(widget);
    commands.add(new PanelClipboardCommand<DockLayoutPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(DockLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.setEdge(widget, edge);
        if (size != null) {
          panel.setSize(widget, size);
        }
      }
    });
  }
}
