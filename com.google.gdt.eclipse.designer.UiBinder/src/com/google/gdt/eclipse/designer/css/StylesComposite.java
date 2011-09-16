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
package com.google.gdt.eclipse.designer.css;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link Composite} for CSS styles management.
 * 
 * @author scheglov_ke
 * @coverage gwt.css
 */
public class StylesComposite extends Composite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylesComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    SashForm sashForm = new SashForm(this, SWT.VERTICAL);
    // create pages
    {
      new StyleListPage(sashForm, SWT.NONE);
      // properties
      /*{
        PageSiteComposite siteComposite = new PageSiteComposite(sashForm, SWT.BORDER);
        siteComposite.setTitleText("Properties");
        siteComposite.setTitleImage(DesignerPlugin.getImage("structure/properties_view.gif"));
        ComponentsPropertiesPage m_propertiesPage = new ComponentsPropertiesPage();
        siteComposite.setPage(m_propertiesPage);
      }
      sashForm.setWeights(new int[]{2, 1});*/
      sashForm.setWeights(new int[]{1});
    }
  }
}
