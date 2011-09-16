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
package com.google.gdt.eclipse.designer.smart.wizards;

import com.google.gdt.eclipse.designer.smart.Activator;
import com.google.gdt.eclipse.designer.wizards.ui.GwtWizardPage;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.wizard.WizardPage;

import java.io.InputStream;

/**
 * {@link WizardPage} for SmartGWT.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.wizard
 */
public abstract class SmartGwtWizardPage extends GwtWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Template
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final InputStream getTemplate2(String path) {
    try {
      return Activator.getFile("templates/visual/" + path);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }
}
