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
package com.google.gdt.eclipse.designer.smart.model.form;

import com.google.gdt.eclipse.designer.smart.model.CanvasAfterAttach;
import com.google.gdt.eclipse.designer.smart.model.JsObjectInfo;
import com.google.gdt.eclipse.designer.smart.model.live.FormItemLiveManager;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.model.util.factory.FactoryActionsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.form.fields.FormItem</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class FormItemInfo extends JsObjectInfo {
  private final FormItemInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    installListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isCreated() {
    return super.isCreated() && getForm().isCreated();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void installListeners() {
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
    // add root attach listener
    addBroadcastListener(new CanvasAfterAttach() {
      public void invoke() throws Exception {
        processObjectReady();
      }
    });
    // add "bounds" property
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo == m_this && javaInfo.getParent() instanceof DynamicFormInfo) {
          FormItemInfo item = (FormItemInfo) javaInfo;
          collectBoundsProperty(item, properties);
        }
      }
    });
    // don't allow spaces in "name" property
    addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (property.getJavaInfo() == m_this
            && property.getTitle().equals("name")
            && value[0] instanceof String) {
          String stringValue = (String) value[0];
          value[0] = stringValue.replace(' ', '_');
        }
      }
    });
    // contribute context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          ExposeComponentSupport.contribute(m_this, manager, "Expose component...");
          MorphingSupport.contribute(
              "com.smartgwt.client.widgets.form.fields.FormItem",
              m_this,
              manager);
          FactoryActionsSupport.contribute(m_this, manager);
          RenameConvertSupport.contribute(objects, manager);
        }
      }
    });
  }

  /**
   * Collect bounds property from simple properties.
   */
  private static void collectBoundsProperty(FormItemInfo item, List<Property> properties) {
    final String key = "FormItem_Info.boundsProperty";
    ComplexProperty boundsProperty = (ComplexProperty) item.getArbitraryValue(key);
    if (boundsProperty == null) {
      boundsProperty = new ComplexProperty("Bounds", null);
      item.putArbitraryValue(key, boundsProperty);
    }
    // show bounds-property for absolute item layout
    boundsProperty.setCategory(item.getForm().isAbsoluteItemLayout()
        ? PropertyCategory.PREFERRED
        : PropertyCategory.ADVANCED);
    // move bounds items properties
    boundsProperty.setProperties(new Property[]{
        extractPropertyByTitle(properties, "left"),
        extractPropertyByTitle(properties, "top"),
        extractPropertyByTitle(properties, "width(int)"),
        extractPropertyByTitle(properties, "width(java.lang.String)"),
        extractPropertyByTitle(properties, "height(int)"),
        extractPropertyByTitle(properties, "height(java.lang.String)")});
    // apply current bounds
    Rectangle modelBounds = item.getModelBounds();
    if (modelBounds != null) {
      boundsProperty.setText("("
          + modelBounds.x
          + ", "
          + modelBounds.y
          + ", "
          + modelBounds.width
          + ", "
          + modelBounds.height
          + ")");
    }
    properties.add(boundsProperty);
  }

  private static Property extractPropertyByTitle(List<Property> properties, String title) {
    for (Property property : properties) {
      if (property.getTitle().equalsIgnoreCase(title)) {
        properties.remove(property);
        property.setCategory(PropertyCategory.NORMAL);
        return property;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image m_liveDefaultImage = new Image(null, 1, 1);
  /**
   * We set this flag during requesting live image because:
   * <ul>
   * <li>Getting live image performs refresh();</li>
   * <li>refresh() may run messages loop;</li>
   * <li>during messages loop user may move mouse again and request live image again;</li>
   * <li>we don't support recursive live image requests.</li>
   * </ul>
   */
  private boolean m_liveInProgress;

  @Override
  protected Image getLiveImage() {
    // live image is supported only for component from palette
    if (getArbitraryValue(FLAG_MANUAL_COMPONENT) != Boolean.TRUE) {
      return null;
    }
    // prevent recursive live image requests
    if (m_liveInProgress) {
      return m_liveDefaultImage;
    }
    // OK, get live image
    m_liveInProgress = true;
    try {
      return getLiveComponentsManager().getImage();
    } finally {
      m_liveInProgress = false;
    }
  }

  /**
   * @return the {@link FormItemLiveManager} instance.
   */
  protected FormItemLiveManager getLiveComponentsManager() {
    return new FormItemLiveManager(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public DynamicFormInfo getForm() {
    return (DynamicFormInfo) getParentJava();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    // set bounds
    setModelBounds(new Rectangle(fetchObjectLocation(), fetchObjectSize()));
    // process children
    super.refresh_fetch();
  }

  /**
   * @return {@link Point} real object location.
   */
  protected Point fetchObjectLocation() throws Exception {
    Object object = getObject();
    int left = (Integer) ReflectionUtils.invokeMethodEx(object, "getLeft()");
    int top = (Integer) ReflectionUtils.invokeMethodEx(object, "getTop()");
    return new Point(left, top);
  }

  /**
   * @return {@link Point} real object size.
   */
  protected Dimension fetchObjectSize() throws Exception {
    Object object = getObject();
    Integer width = (Integer) ReflectionUtils.invokeMethodEx(object, "getVisibleWidth()");
    Integer height = (Integer) ReflectionUtils.invokeMethodEx(object, "getVisibleHeight()");
    return new Dimension(width, height);
  }
}
