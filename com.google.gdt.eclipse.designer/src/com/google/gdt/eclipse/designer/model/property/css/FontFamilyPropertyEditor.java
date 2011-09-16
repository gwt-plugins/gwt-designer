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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.css.dialogs.style.FontListDialog;
import org.eclipse.wb.internal.css.semantics.FontProperty;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;

/**
 * {@link PropertyEditor} for family property in {@link FontProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class FontFamilyPropertyEditor extends AbstractTextPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new FontFamilyPropertyEditor();

  FontFamilyPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      openDialog(property);
    }
  };

  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text for current color value.
   */
  @Override
  public String getText(Property property) throws Exception {
    return (String) property.getValue();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_isInKeyDown;

  @Override
  public void keyDown(PropertyTable propertyTable, Property property, KeyEvent event)
      throws Exception {
    try {
      m_isInKeyDown = true;
      super.keyDown(propertyTable, property, event);
    } finally {
      m_isInKeyDown = false;
    }
  }

  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard (Space, Enter) - open dialog
    if (location == null && !m_isInKeyDown) {
      openDialog(property);
      return false;
    }
    // activate as text editor
    return super.activate(propertyTable, property, location);
  }

  @Override
  protected String getEditorText(Property property) throws Exception {
    return getText(property);
  }

  @Override
  protected boolean setEditorText(Property property, String text) throws Exception {
    property.setValue(text);
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private final FontListDialog m_fontListDialog = new FontListDialog(DesignerPlugin.getShell());

  /**
   * Opens editing dialog.
   */
  private void openDialog(Property property) throws Exception {
    // set initial color
    {
      Object value = property.getValue();
      if (value instanceof String) {
        m_fontListDialog.setFontsString((String) value);
      }
    }
    // open dialog
    if (m_fontListDialog.open() == Window.OK) {
      String fontsString = m_fontListDialog.getFontsString();
      property.setValue(fontsString);
    }
  }
}
