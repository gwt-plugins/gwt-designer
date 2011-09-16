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
package com.google.gdt.eclipse.designer.nls;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.ui.common.AbstractFieldsSourceNewComposite;

import org.eclipse.swt.widgets.Composite;

/**
 * Composite for creating new source.
 * 
 * @author scheglov_ke
 * @coverage gwt.nls
 */
public final class GwtSourceNewComposite extends AbstractFieldsSourceNewComposite {
  private final ClassSelectionGroup m_classSelectionGroup;
  private final FieldNameGroup m_fieldNameGroup;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
    // create controls
    {
      m_classSelectionGroup =
          new ClassSelectionGroup(this,
              "Constants class (will be created if does not exist)",
              "*Constants<");
      m_fieldNameGroup = new FieldNameGroup(this, "Field for Constants instance");
    }
    // initialize controls
    m_classSelectionGroup.initialize("AppConstants");
    m_fieldNameGroup.initialize("CONSTANTS");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return "GWT messages class";
  }

  @Override
  public String getSample() {
    return "private static final MyConstants CONSTANTS = (MyConstants) GWT.create(MyConstants.class);\n"
        + "...\n"
        + "button.setText( CONSTANTS.buttonText() );";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void validateAll() {
    m_classSelectionGroup.validate();
    m_fieldNameGroup.validate();
    super.validateAll();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IEditableSource createEditableSource(Object o) throws Exception {
    SourceParameters parameters = (SourceParameters) o;
    String className = parameters.m_constant.m_fullClassName;
    String fieldName = parameters.m_fieldName;
    // create editable source
    IEditableSource editableSource;
    {
      // check, may be we already have such Constants class
      if (parameters.m_constant.m_exists) {
        GwtSource source = new GwtSource(m_root, className, fieldName);
        editableSource = source.getEditable();
      } else {
        editableSource = createEmptyEditable(className);
      }
    }
    // configure editable source and return
    editableSource.setKeyGeneratorStrategy(GwtSource.GWT_KEY_GENERATOR);
    return editableSource;
  }

  @Override
  public Object createParametersObject() throws Exception {
    SourceParameters parameters = new SourceParameters();
    parameters.m_constant = m_classSelectionGroup.getParameters();
    parameters.m_fieldName = m_fieldNameGroup.getName();
    return parameters;
  }
}
