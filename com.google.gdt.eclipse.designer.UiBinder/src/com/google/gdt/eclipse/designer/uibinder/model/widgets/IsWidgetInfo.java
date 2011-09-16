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

import com.google.gdt.eclipse.designer.uibinder.model.widgets.IsWidgetWrappedInfo.IsWidgetWrappedCreationSupport;

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.IWrapperInfo;
import org.eclipse.wb.internal.core.xml.model.UseModelIfNotAlready;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;

/**
 * Model for <code>com.google.gwt.user.client.ui.IsWidget</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
@UseModelIfNotAlready(WidgetInfo.class)
public final class IsWidgetInfo extends XmlObjectInfo implements IWrapperInfo {
  private final IsWidgetWrappedInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IsWidgetInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    // prepare CreationSupport
    DocumentElement element = creationSupport.getElement();
    if (element == null) {
      setCreationSupport(new ElementCreationSupport());
      creationSupport = new CreationSupport() {
        @Override
        public void addElement(DocumentElement parent, int index) throws Exception {
          getCreationSupport().addElement(parent, index);
          DocumentElement newElement = getCreationSupport().getElement();
          setCreationSupport(new ElementCreationSupport(newElement));
          m_widget.setCreationSupport(new IsWidgetWrappedCreationSupport(newElement));
        }
      };
    } else {
      setCreationSupport(new ElementCreationSupport(element));
      creationSupport = new IsWidgetWrappedCreationSupport(element);
    }
    // create Widget
    {
      Class<?> widgetClass =
          context.getClassLoader().loadClass("com.google.gwt.user.client.ui.Widget");
      ComponentDescription widgetDescription =
          ComponentDescriptionHelper.getDescription(context, widgetClass);
      m_widget = new IsWidgetWrappedInfo(context, widgetDescription, creationSupport, this);
    }
    // bind IsWidget to Widget
    m_widget.addChild(this);
    addBroadcastListener(new XmlObjectSetObjectAfter() {
      public void invoke(XmlObjectInfo target, Object o) throws Exception {
        if (target == IsWidgetInfo.this) {
          Object widget = ReflectionUtils.invokeMethod(o, "asWidget()");
          m_widget.setObject(widget);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "IsWidget";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWrapperInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo getWrapped() throws Exception {
    return m_widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new XmlObjectPresentation(this) {
      @Override
      public boolean isVisible() throws Exception {
        return false;
      }
    };
  }
}
