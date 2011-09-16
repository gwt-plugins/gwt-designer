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

import com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import org.apache.commons.lang.ArrayUtils;

/**
 * {@link PropertyEditor} for selecting {@link ImageBundlePrototypeDescription}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public final class ImagePrototypePropertyEditor extends TextDialogPropertyEditor {
  private final Shell m_parentShell;
  private final JavaInfo m_rootJavaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImagePrototypePropertyEditor(Shell parentShell, JavaInfo rootJavaInfo) {
    m_parentShell = parentShell;
    m_rootJavaInfo = rootJavaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    ImageBundlePrototypeDescription prototype =
        (ImageBundlePrototypeDescription) property.getValue();
    if (prototype != null) {
      return prototype.getMethod().getName()
          + "() from "
          + prototype.getBundle().getDescription().getComponentClass().getName();
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
    ElementTreeSelectionDialog selectionDialog;
    {
      selectionDialog = new ElementTreeSelectionDialog(m_parentShell, new LabelProvider() {
        @Override
        public Image getImage(Object element) {
          if (element instanceof ImageBundleInfo) {
            return ObjectsLabelProvider.INSTANCE.getImage(element);
          }
          if (element instanceof ImageBundlePrototypeDescription) {
            ImageBundlePrototypeDescription prototype = (ImageBundlePrototypeDescription) element;
            return prototype.getIcon();
          }
          return null;
        }

        @Override
        public String getText(Object element) {
          if (element instanceof ImageBundleInfo) {
            return ObjectsLabelProvider.INSTANCE.getText(element);
          }
          if (element instanceof ImageBundlePrototypeDescription) {
            ImageBundlePrototypeDescription prototype = (ImageBundlePrototypeDescription) element;
            return prototype.getMethod().getName() + "()";
          }
          return null;
        }
      }, new ITreeContentProvider() {
        public Object[] getElements(Object inputElement) {
          return ImageBundleContainerInfo.getBundles((JavaInfo) inputElement).toArray();
        }

        public Object[] getChildren(Object parentElement) {
          if (parentElement instanceof ImageBundleInfo) {
            return ((ImageBundleInfo) parentElement).getPrototypes().toArray();
          }
          return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        public Object getParent(Object element) {
          if (element instanceof ImageBundlePrototypeDescription) {
            return ((ImageBundlePrototypeDescription) element).getBundle();
          }
          return null;
        }

        public boolean hasChildren(Object element) {
          return getChildren(element).length != 0;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
      }) {
        @Override
        public void create() {
          super.create();
          getTreeViewer().expandAll();
        }

        @Override
        protected Control createDialogArea(Composite parent) {
          return super.createDialogArea(parent);
        }
      };
      // validator
      selectionDialog.setValidator(new ISelectionStatusValidator() {
        public IStatus validate(Object[] selection) {
          if (selection.length == 1 && selection[0] instanceof ImageBundlePrototypeDescription) {
            return StatusUtils.OK_STATUS;
          } else {
            return StatusUtils.ERROR_STATUS;
          }
        }
      });
      // configure
      selectionDialog.setAllowMultiple(false);
      selectionDialog.setTitle(property.getTitle());
      selectionDialog.setMessage("Select prototype:");
      // set input
      selectionDialog.setInput(m_rootJavaInfo);
      // set initial selection
      selectionDialog.setInitialSelection(property.getValue());
    }
    // open dialog
    if (selectionDialog.open() == Window.OK) {
      ImageBundlePrototypeDescription prototype =
          (ImageBundlePrototypeDescription) selectionDialog.getFirstResult();
      property.setValue(prototype);
    }
  }
}
