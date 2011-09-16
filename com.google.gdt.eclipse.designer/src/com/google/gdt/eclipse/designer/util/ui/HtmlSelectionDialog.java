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
package com.google.gdt.eclipse.designer.util.ui;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Dialog for selecting HTML file from public folders of modules.
 * 
 * @author scheglov_ke
 * @author lobas_av
 * @coverage gwt.util.ui
 */
public final class HtmlSelectionDialog extends ResourceSelectionDialog {
  private static final String[] EXTENSIONS = {"html", "shtml", "htm"};
  private static final Color RED = new Color(null, 255, 0, 0);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HtmlSelectionDialog(Shell parentShell,
      IResourcesProvider provider,
      ModuleDescription moduleDescription,
      String title) throws Exception {
    super(parentShell, provider, moduleDescription, title);
    addFilter("HTML files", "*.html");
    addFilter("SSIs HTML files", "*.shtml");
    addFilter("HTM files", "*.htm");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void previewFile(Composite parent, ResourceFile file) {
    if (checkExtension(file.getExtension())) {
      // create text control
      GridLayoutFactory.create(parent);
      StyledText htmlTextControl =
          new StyledText(parent, SWT.BORDER
              | SWT.READ_ONLY
              | SWT.MULTI
              | SWT.H_SCROLL
              | SWT.V_SCROLL);
      GridDataFactory.create(htmlTextControl).fill().grab();
      parent.layout();
      // set content
      try {
        InputStream stream = getResourceAsStream(file);
        try {
          htmlTextControl.setText(IOUtils.toString(stream));
        } finally {
          IOUtils.closeQuietly(stream);
        }
      } catch (Throwable e) {
        htmlTextControl.setText(e.getMessage());
        htmlTextControl.setForeground(RED);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean checkExtension(String extension) {
    for (String ext : EXTENSIONS) {
      if (ext.equalsIgnoreCase(extension)) {
        return true;
      }
    }
    return false;
  }
}