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
package com.google.gdt.eclipse.designer.wizards.model.common;

import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract composite for {@link DialogField} based editing.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class AbstractGwtComposite extends Composite {
  protected final IMessageContainer m_messageContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGwtComposite(Composite parent, int style, IMessageContainer messageContainer) {
    super(parent, style);
    m_messageContainer = messageContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      validateAll();
    }
  };

  /**
   * Validate all and disable/enable "Ok" button.
   */
  public final void validateAll() {
    String errorMessage = validate();
    m_messageContainer.setErrorMessage(errorMessage);
  }

  /**
   * Validate fields and returns error message or <code>null</code>.
   */
  protected String validate() {
    return null;
  }
}
