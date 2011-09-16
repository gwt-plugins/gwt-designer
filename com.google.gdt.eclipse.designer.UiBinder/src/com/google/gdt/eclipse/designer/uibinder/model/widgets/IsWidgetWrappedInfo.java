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

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import org.eclipse.swt.graphics.Image;

/**
 * <code>com.google.gwt.user.client.ui.Widget</code> wrapped into
 * <code>com.google.gwt.user.client.ui.IsWidget</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class IsWidgetWrappedInfo extends WidgetInfo {
  private final IsWidgetInfo m_wrapper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IsWidgetWrappedInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport,
      IsWidgetInfo wrapper) throws Exception {
    super(context, description, creationSupport);
    m_wrapper = wrapper;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IsWidgetInfo} wrapper.
   */
  public IsWidgetInfo getWrapper() {
    return m_wrapper;
  }

  @Override
  public Property[] getProperties() throws Exception {
    return m_wrapper.getProperties();
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
      public Image getIcon() throws Exception {
        return m_wrapper.getPresentation().getIcon();
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IsWidgetWrappedCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link CreationSupport} for {@link IsWidgetWrappedInfo}. Right now it is used to remove
   * clipboard support, because it is not implemented.
   */
  static class IsWidgetWrappedCreationSupport extends ElementCreationSupport {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public IsWidgetWrappedCreationSupport(DocumentElement element) {
      super(element);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Clipboard
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public IClipboardCreationSupport getClipboard() {
      return null;
    }
  }
}
