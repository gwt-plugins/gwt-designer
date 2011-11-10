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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelAlignmentSupport;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDeactivePropertyEditor;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.DoublePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.LayoutPanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
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
  public LayoutPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    m_alignmentSupport = new LayoutPanelAlignmentSupport<WidgetInfo>(this);
    contributeWidgetContextMenu();
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
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new RootLayoutPanelTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeWidgetContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == LayoutPanelInfo.this) {
          WidgetInfo component = (WidgetInfo) object;
          contributeWidgetContextMenu(manager, component);
        }
      }
    });
  }

  /**
   * Contributes {@link Action}'s into {@link WidgetInfo} context menu.
   */
  private void contributeWidgetContextMenu(IMenuManager manager, final WidgetInfo widget) {
    // order
    {
      List<WidgetInfo> widgets = getChildrenWidgets();
      new OrderingSupport(widgets, widget).contributeActions(manager);
    }
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
      String locationTitle) {
    Location location = getLocation(widget, horizontal);
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
      complexProperty = complexProperties.get(locationTitle);
      if (complexProperty == null) {
        complexProperty = new ComplexProperty(locationTitle, "<properties>");
        complexProperty.setCategory(PropertyCategory.system(10));
        complexProperties.put(locationTitle, complexProperty);
      }
      properties.add(complexProperty);
    }
    // prepare sub-properties
    List<Property> subProperties = Lists.newArrayList();
    if (location.leading != null) {
      String title = location.leading.attribute;
      subProperties.add(new LocationValue_Property(title, location.leading));
      subProperties.add(new LocationUnit_Property(title + " unit", location.leading));
    }
    if (location.trailing != null) {
      String title = location.trailing.attribute;
      subProperties.add(new LocationValue_Property(title, location.trailing));
      subProperties.add(new LocationUnit_Property(title + " unit", location.trailing));
    }
    if (location.size != null) {
      String title = location.size.attribute;
      subProperties.add(new LocationValue_Property(title, location.size));
      subProperties.add(new LocationUnit_Property(title + " unit", location.size));
    }
    complexProperty.setProperties(subProperties);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property: value
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class LocationValue_Property extends Property {
    private final String m_title;
    private final DocumentElement m_layer;
    private final LocationValue m_value;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LocationValue_Property(String title, LocationValue value) {
      super(DoublePropertyEditor.INSTANCE);
      m_title = title;
      m_layer = value.layer;
      m_value = value;
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
      return m_value.value;
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof Double) {
        final double doubleValue = ((Double) value).doubleValue();
        ExecutionUtils.run(LayoutPanelInfo.this, new RunnableEx() {
          public void run() throws Exception {
            setUnits(m_layer, m_value.attribute, m_value.unit, doubleValue);
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
    private final LocationValue m_value;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LocationUnit_Property(String title, LocationValue value) {
      super(UNIT_PROPERTY_EDITOR);
      m_title = title;
      m_value = value;
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
      return m_value.unit.toString();
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String) {
        final Object newUnit = getUnitByName(((String) value));
        ExecutionUtils.run(LayoutPanelInfo.this, new RunnableEx() {
          public void run() throws Exception {
            setUnit(m_value, newUnit);
          }
        });
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class LocationValue {
    DocumentElement layer;
    String attribute;
    boolean horizontal;
    double value;
    Object unit;
  }
  private static class Location {
    DocumentElement layer;
    LocationValue leading;
    LocationValue trailing;
    LocationValue size;
  }

  private Location getLocation(WidgetInfo widget, boolean horizontal) {
    UiBinderContext context = widget.getContext();
    DocumentElement layerElement = widget.getElement().getParent();
    Location location = new Location();
    location.layer = layerElement;
    if (horizontal) {
      location.leading = getLocationValue(context, layerElement, "left");
      location.trailing = getLocationValue(context, layerElement, "right");
      location.size = getLocationValue(context, layerElement, "width");
    } else {
      location.leading = getLocationValue(context, layerElement, "top");
      location.trailing = getLocationValue(context, layerElement, "bottom");
      location.size = getLocationValue(context, layerElement, "height");
    }
    return location;
  }

  private static LocationValue getLocationValue(UiBinderContext context,
      DocumentElement layerElement,
      String attribute) {
    Object value = context.getAttributeValue(layerElement, attribute);
    if (value instanceof List<?>) {
      LocationValue locationValue = new LocationValue();
      locationValue.layer = layerElement;
      locationValue.attribute = attribute;
      locationValue.value = ((Number) ((List<?>) value).get(0)).doubleValue();
      locationValue.unit = ((List<?>) value).get(1);
      return locationValue;
    }
    return null;
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
    Location location = getLocation(widget, horizontal);
    // may be trailing
    if (isLocationTrailing(location)) {
      if (horizontal) {
        pixels = getBounds().width - (widget.getBounds().width + pixels);
      } else {
        pixels = getBounds().height - (widget.getBounds().height + pixels);
      }
    }
    // convert pixels to units
    Object unit = getLocationUnit(location);
    double units = pixels / getUnitSize(unit, !horizontal);
    return SIZE_FORMAT.format(units) + unit.toString().toLowerCase();
  }

  /**
   * @return <code>true</code> if {@link WidgetInfo} is attached to trailing size of panel.
   */
  public boolean getLocationHint_isTrailing(WidgetInfo widget, boolean horizontal) {
    Location location = getLocation(widget, horizontal);
    return isLocationTrailing(location);
  }

  /**
   * @return <code>true</code> if {@link Location} is attachment to trailing side of panel.
   */
  private boolean isLocationTrailing(Location invocation) {
    return invocation.leading == null && invocation.trailing != null;
  }

  private Object getLocationUnit(Location invocation) throws Exception {
    if (invocation.leading != null) {
      return invocation.leading.unit;
    }
    if (invocation.trailing != null) {
      return invocation.trailing.unit;
    }
    return getUnitByName("PX");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds: location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_LOCATION(WidgetInfo widget, Point location) throws Exception {
    command_LOCATION_X(widget, location.x);
    command_LOCATION_Y(widget, location.y);
    ExecutionUtils.refresh(this);
  }

  private void command_LOCATION_X(WidgetInfo widget, int x) throws Exception {
    Location location = getLocation(widget, true);
    DocumentElement layer = location.layer;
    // LeftWidth
    if (location.leading != null && location.size != null) {
      setPixels(location.leading, x);
      return;
    }
    // RightWidth
    if (location.trailing != null && location.size != null) {
      int right = getBounds().width - x - widget.getBounds().width;
      setPixels(location.trailing, right);
      return;
    }
    // LeftRight
    if (location.leading != null && location.trailing != null) {
      int right = getBounds().width - x - widget.getBounds().width;
      setPixels(location.leading, x);
      setPixels(location.trailing, right);
      return;
    }
    // new, use LeftWidth
    {
      int defaultWidth = getDefaultSize(widget).width;
      setPixels(layer, "left", x);
      setPixels(layer, "width", defaultWidth);
    }
  }

  private void command_LOCATION_Y(WidgetInfo widget, int y) throws Exception {
    Location location = getLocation(widget, false);
    DocumentElement layer = location.layer;
    // TopHeight
    if (location.leading != null && location.size != null) {
      setPixels(location.leading, y);
      return;
    }
    // BottomHeight
    if (location.trailing != null && location.size != null) {
      int bottom = getBounds().height - y - widget.getBounds().height;
      setPixels(location.trailing, bottom);
      return;
    }
    // TopBottom
    if (location.leading != null && location.trailing != null) {
      int bottom = getBounds().height - y - widget.getBounds().height;
      setPixels(location.leading, y);
      setPixels(location.trailing, bottom);
      return;
    }
    // new, use TopHeight
    {
      int defaultHeight = getDefaultSize(widget).height;
      setPixels(layer, "top", y);
      setPixels(layer, "height", defaultHeight);
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
    command_SIZE_X(widget, size.width, hDirection);
    command_SIZE_Y(widget, size.height, vDirection);
    ExecutionUtils.refresh(this);
  }

  private void command_SIZE_X(WidgetInfo widget, int width, ResizeDirection direction)
      throws Exception {
    Location location = getLocation(widget, true);
    DocumentElement layer = location.layer;
    Rectangle bounds = widget.getBounds();
    // LeftWidth
    if (location.leading != null && location.size != null) {
      if (direction == ResizeDirection.LEADING) {
        int oldLeft = bounds.left();
        int deltaWidth = width - bounds.width;
        int left = oldLeft - deltaWidth;
        setPixels(location.leading, left);
      }
      setPixels(location.size, width);
      return;
    }
    // RightWidth
    if (location.trailing != null && location.size != null) {
      if (direction == ResizeDirection.TRAILING) {
        int oldRight = getBounds().width - bounds.right();
        int deltaWidth = width - bounds.width;
        int right = oldRight - deltaWidth;
        setPixels(location.trailing, right);
      }
      setPixels(location.size, width);
      return;
    }
    // LeftRight
    if (location.leading != null && location.trailing != null) {
      if (direction == ResizeDirection.LEADING) {
        int oldLeft = bounds.left();
        int deltaWidth = width - bounds.width;
        int left = oldLeft - deltaWidth;
        setPixels(location.leading, left);
      }
      if (direction == ResizeDirection.TRAILING) {
        int oldRight = getBounds().width - bounds.right();
        int deltaWidth = width - bounds.width;
        int right = oldRight - deltaWidth;
        setPixels(location.trailing, right);
      }
      return;
    }
    // new, use LeftWidth
    if (direction == ResizeDirection.TRAILING) {
      layer.setAttribute("left", "0px");
      layer.setAttribute("width", width + "px");
    }
  }

  private void command_SIZE_Y(WidgetInfo widget, int height, ResizeDirection direction)
      throws Exception {
    Location location = getLocation(widget, false);
    DocumentElement layer = location.layer;
    Rectangle bounds = widget.getBounds();
    // TopHeight
    if (location.leading != null && location.size != null) {
      if (direction == ResizeDirection.LEADING) {
        int oldTop = bounds.top();
        int deltaHeight = height - bounds.height;
        int top = oldTop - deltaHeight;
        setPixels(location.leading, top);
      }
      setPixels(location.size, height);
      return;
    }
    // BottomHeight
    if (location.trailing != null && location.size != null) {
      if (direction == ResizeDirection.TRAILING) {
        int oldBottom = getBounds().height - bounds.bottom();
        int deltaHeight = height - bounds.height;
        int bottom = oldBottom - deltaHeight;
        setPixels(location.trailing, bottom);
      }
      setPixels(location.size, height);
      return;
    }
    // TopBottom
    if (location.leading != null && location.trailing != null) {
      if (direction == ResizeDirection.LEADING) {
        int oldTop = bounds.top();
        int deltaHeight = height - bounds.height;
        int top = oldTop - deltaHeight;
        setPixels(location.leading, top);
      }
      if (direction == ResizeDirection.TRAILING) {
        int oldBottom = getBounds().height - bounds.bottom();
        int deltaHeight = height - bounds.height;
        int bottom = oldBottom - deltaHeight;
        setPixels(location.trailing, bottom);
      }
      return;
    }
    // new, use TopHeight
    if (direction == ResizeDirection.TRAILING) {
      layer.setAttribute("top", "0px");
      layer.setAttribute("height", height + "px");
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
    Location location = getLocation(widget, horizontal);
    if (location.leading != null && location.size != null) {
      return Anchor.LEADING;
    }
    if (location.trailing != null && location.size != null) {
      return Anchor.TRAILING;
    }
    if (location.leading != null && location.trailing != null) {
      return Anchor.BOTH;
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
    ExecutionUtils.refresh(this);
  }

  private void command_ANCHOR_horizontal(WidgetInfo widget, Anchor anchor) throws Exception {
    Location location = getLocation(widget, true);
    DocumentElement layer = location.layer;
    Rectangle bounds = widget.getBounds();
    if (location.leading != null && location.size != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("left", null);
        layer.setAttribute("width", null);
      }
      if (anchor == Anchor.TRAILING) {
        int right = getBounds().width - bounds.right();
        layer.setAttribute("left", null);
        setPixels(location.leading, "right", right);
      }
      if (anchor == Anchor.BOTH) {
        int right = getBounds().width - bounds.right();
        layer.setAttribute("width", null);
        setPixels(location.leading, "right", right);
      }
      return;
    }
    if (location.trailing != null && location.size != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("right", null);
        layer.setAttribute("width", null);
      }
      if (anchor == Anchor.LEADING) {
        int left = bounds.left();
        layer.setAttribute("right", null);
        setPixels(location.trailing, "left", left);
      }
      if (anchor == Anchor.BOTH) {
        int left = bounds.left();
        layer.setAttribute("width", null);
        setPixels(location.trailing, "left", left);
      }
      return;
    }
    if (location.leading != null && location.trailing != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("left", null);
        layer.setAttribute("right", null);
      }
      if (anchor == Anchor.LEADING) {
        layer.setAttribute("right", null);
        setPixels(location.leading, "width", bounds.width);
      }
      if (anchor == Anchor.TRAILING) {
        layer.setAttribute("left", null);
        setPixels(location.trailing, "width", bounds.width);
      }
      return;
    }
    // no anchor yet
    if (anchor != Anchor.NONE) {
      if (anchor == Anchor.LEADING) {
        layer.setAttribute("left", bounds.left() + "px");
        layer.setAttribute("width", bounds.width + "px");
      }
      if (anchor == Anchor.TRAILING) {
        layer.setAttribute("right", getBounds().width - bounds.right() + "px");
        layer.setAttribute("width", bounds.width + "px");
      }
      if (anchor == Anchor.BOTH) {
        layer.setAttribute("left", bounds.left() + "px");
        layer.setAttribute("right", getBounds().width - bounds.right() + "px");
      }
    }
  }

  private void command_ANCHOR_vertical(WidgetInfo widget, Anchor anchor) throws Exception {
    Location location = getLocation(widget, false);
    DocumentElement layer = location.layer;
    Rectangle bounds = widget.getBounds();
    if (location.leading != null && location.size != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("top", null);
        layer.setAttribute("height", null);
      }
      if (anchor == Anchor.TRAILING) {
        int bottom = getBounds().height - bounds.bottom();
        layer.setAttribute("top", null);
        setPixels(location.leading, "bottom", bottom);
      }
      if (anchor == Anchor.BOTH) {
        int bottom = getBounds().height - bounds.bottom();
        layer.setAttribute("height", null);
        setPixels(location.leading, "bottom", bottom);
      }
      return;
    }
    if (location.trailing != null && location.size != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("bottom", null);
        layer.setAttribute("height", null);
      }
      if (anchor == Anchor.LEADING) {
        int top = bounds.top();
        layer.setAttribute("bottom", null);
        setPixels(location.trailing, "top", top);
      }
      if (anchor == Anchor.BOTH) {
        int top = bounds.top();
        layer.setAttribute("height", null);
        setPixels(location.trailing, "top", top);
      }
      return;
    }
    if (location.leading != null && location.trailing != null) {
      if (anchor == Anchor.NONE) {
        layer.setAttribute("top", null);
        layer.setAttribute("bottom", null);
      }
      if (anchor == Anchor.LEADING) {
        layer.setAttribute("bottom", null);
        setPixels(location.leading, "height", bounds.height);
      }
      if (anchor == Anchor.TRAILING) {
        layer.setAttribute("top", null);
        setPixels(location.trailing, "height", bounds.height);
      }
      return;
    }
    // no anchor yet
    if (anchor != Anchor.NONE) {
      if (anchor == Anchor.LEADING) {
        layer.setAttribute("top", bounds.top() + "px");
        layer.setAttribute("height", bounds.height + "px");
      }
      if (anchor == Anchor.TRAILING) {
        layer.setAttribute("bottom", getBounds().height - bounds.bottom() + "px");
        layer.setAttribute("height", bounds.height + "px");
      }
      if (anchor == Anchor.BOTH) {
        layer.setAttribute("top", bounds.top() + "px");
        layer.setAttribute("bottom", getBounds().height - bounds.bottom() + "px");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>Unit</code> object by its name.
   */
  private Object getUnitByName(String name) throws ClassNotFoundException {
    ClassLoader classLoader = getContext().getClassLoader();
    Class<?> classUnit = classLoader.loadClass("com.google.gwt.dom.client.Style$Unit");
    return ReflectionUtils.getFieldObject(classUnit, name);
  }

  /**
   * @return the size of "unit" in pixels.
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
   * Sets attribute value in pixels.
   */
  private void setPixels(DocumentElement layer, String attribute, int pixels) throws Exception {
    Object unit = getUnitByName("PX");
    setUnits(layer, attribute, unit, pixels);
  }

  /**
   * Sets {@link LocationValue} in pixels, converts into appropriate unit.
   */
  private void setPixels(LocationValue value, int pixels) throws Exception {
    setPixels(value, value.attribute, pixels);
  }

  /**
   * Sets attribute value in pixels, converts into appropriate unit from given {@link LocationValue}
   * .
   */
  private void setPixels(LocationValue value, String attribute, int pixels) throws Exception {
    Object unit = value.unit;
    double units = pixels / getUnitSize(unit, value.horizontal);
    setUnits(value.layer, attribute, unit, units);
  }

  /**
   * Sets value of attribute in units.
   */
  private void setUnits(DocumentElement layer, String attribute, Object unit, double units) {
    String unitName = unit.toString().toLowerCase();
    if ("px".equals(unitName)) {
      layer.setAttribute(attribute, (int) units + "px");
    } else {
      layer.setAttribute(attribute, SIZE_FORMAT.format(units) + unitName);
    }
    {
      UiBinderContext context = getContext();
      context.setAttributeValue(layer, attribute, ImmutableList.of(units, unit));
    }
  }

  /**
   * Changes {@link Location} "unit", with converting value.
   */
  private void setUnit(LocationValue value, Object newUnit) throws Exception {
    boolean horizontal = value.horizontal;
    // prepare value in pixels
    double pixels;
    {
      double oldValue = value.value;
      Object oldUnit = value.unit;
      pixels = oldValue * getUnitSize(oldUnit, horizontal);
    }
    // convert value
    double newUnitSize = getUnitSize(newUnit, horizontal);
    double newValue = pixels / newUnitSize;
    setUnits(value.layer, value.attribute, newUnit, newValue);
  }

  /**
   * @return the default size for new {@link WidgetInfo}.
   */
  private static Dimension getDefaultSize(WidgetInfo widget) {
    return widget.getBounds().getSize();
  }
}
