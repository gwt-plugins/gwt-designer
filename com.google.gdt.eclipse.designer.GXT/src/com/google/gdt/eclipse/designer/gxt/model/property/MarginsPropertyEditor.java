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
package com.google.gdt.eclipse.designer.gxt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.geometry.AbstractGeometryDialog;

import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * Implementation of {@link PropertyEditor} for <code>Margins</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.property
 */
public final class MarginsPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new MarginsPropertyEditor();

  private MarginsPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (Margins.isMargins(value)) {
      Margins margins = new Margins(value);
      return "("
          + margins.top
          + ", "
          + margins.right
          + ", "
          + margins.bottom
          + ", "
          + margins.left
          + ")";
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    // prepare Margins to edit
    Margins margins;
    {
      Object value = property.getValue();
      if (Margins.isMargins(value)) {
        margins = new Margins(value);
      } else {
        margins = new Margins();
      }
    }
    // prepare dialog
    MarginsDialog dialog = new MarginsDialog(property.getTitle(), margins);
    // open dialog
    int result = dialog.open();
    if (result == IDialogConstants.IGNORE_ID) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else if (result == IDialogConstants.OK_ID) {
      property.setValue(margins);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MarginsDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class MarginsDialog extends AbstractGeometryDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MarginsDialog(String title, Object margins) {
      super(title, margins);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor("&Top:", "top");
      createEditor("&Right:", "right");
      createEditor("&Bottom:", "bottom");
      createEditor("&Left:", "left");
    }
  }
}
