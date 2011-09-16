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
package com.google.gdt.eclipse.designer.model.property;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;
import com.google.gdt.eclipse.designer.util.ui.ImageSelectionDialog;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link PropertyEditor} for selecting URL of image in public resources.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public final class ImageUrlPropertyEditor extends TextDialogPropertyEditor {
  private Shell m_parentShell;
  private GwtState m_state;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ImageUrlPropertyEditor();

  private ImageUrlPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageUrlPropertyEditor(Shell parentShell, GwtState state) {
    m_parentShell = parentShell;
    m_state = state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GwtState state = getGWTState(property);
    ImageSelectionDialog dialog =
        new ImageSelectionDialog(getParentShell(),
            state.getResourcesProvider(),
            state.getModuleDescription(),
            property.getTitle());
    if (dialog.open() == Window.OK) {
      String resourcePath = dialog.getSelectedResourcePath();
      property.setValue(resourcePath);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Shell} to use as parent for {@link ImageSelectionDialog}, externally
   *         specified or just active {@link Shell}.
   */
  private Shell getParentShell() {
    return m_parentShell != null ? m_parentShell : Display.getCurrent().getActiveShell();
  }

  /**
   * @return the {@link GwtState} to use with given {@link GenericProperty}, or may be externally
   *         specified (if we want to use this {@link ImageUrlPropertyEditor} not with
   *         {@link GenericProperty}).
   */
  private GwtState getGWTState(Property property) {
    if (m_state != null) {
      return m_state;
    } else {
      ObjectInfo model = property.getAdapter(ObjectInfo.class);
      return ((IGwtStateProvider) model).getState();
    }
  }
}
