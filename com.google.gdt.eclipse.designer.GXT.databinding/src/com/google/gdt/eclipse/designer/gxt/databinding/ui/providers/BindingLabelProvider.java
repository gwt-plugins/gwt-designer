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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.providers;

import com.google.gdt.eclipse.designer.gxt.databinding.Activator;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingsInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FormBindingInfo;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author lobas_av
 * 
 */
public class BindingLabelProvider extends LabelProvider implements ITableLabelProvider {
  private static final Image FORM_BINDING_IMAGE = Activator.getImage("FormPanel.png");
  private static final Image FORM_FIELD_BINDING_IMAGE = Activator.getImage("FormPanelElement.png");
  private static final Image FORM_AUTO_FIELD_BINDING_IMAGE =
      Activator.getImage("FormPanelAutoElement.png");
  private static final Image BINDINGS_IMAGE = Activator.getImage("Bindings.png");
  private static final Image FIELD_BINDING_IMAGE = Activator.getImage("Field.png");
  public static final BindingLabelProvider INSTANCE = new BindingLabelProvider();

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITableLabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getColumnText(final Object element, final int column) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return getBindingColumnText((BindingInfo) element, column);
      }
    }, "<exception, see log>");
  }

  private static String getBindingColumnText(BindingInfo binding, int column) throws Exception {
    switch (column) {
      case 1 :
        // target
        return binding.getTargetPresentationText();
      case 2 :
        // model
        return binding.getModelPresentationText();
      case 3 :
        // binding
        return binding.getVariableIdentifier();
      default :
        return null;
    }
  }

  public Image getColumnImage(Object element, int column) {
    if (column == 0) {
      if (element instanceof FormBindingInfo) {
        return FORM_BINDING_IMAGE;
      }
      if (element instanceof BindingsInfo) {
        return BINDINGS_IMAGE;
      }
      if (element instanceof FieldBindingInfo) {
        FieldBindingInfo binding = (FieldBindingInfo) element;
        if (binding.getParentBinding() == null) {
          return FIELD_BINDING_IMAGE;
        }
        return binding.isAutobind() ? FORM_AUTO_FIELD_BINDING_IMAGE : FORM_FIELD_BINDING_IMAGE;
      }
    }
    return null;
  }
}