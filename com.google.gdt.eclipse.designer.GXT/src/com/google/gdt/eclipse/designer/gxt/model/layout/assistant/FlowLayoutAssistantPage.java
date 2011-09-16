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
package com.google.gdt.eclipse.designer.gxt.model.layout.assistant;

import com.google.gdt.eclipse.designer.gxt.model.layout.FlowLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Assistant for GXT <code>FlowLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class FlowLayoutAssistantPage extends AbstractExtGwtAssistantPage {
  public FlowLayoutAssistantPage(Composite parent, FlowLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // options
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      {
        Group optionsGroup =
            addBooleanProperties(composite, "Options", new String[][]{
                new String[]{"removePositioning", "removePositioning"},
                new String[]{"firesEvents", "firesEvents"},
                new String[]{"renderHidden", "renderHidden"}});
        GridDataFactory.create(optionsGroup).spanH(2).fill();
      }
      addIntegerProperty(composite, "margin", "Margin");
    }
    // margins
    {
      Group group = addMarginProperty(this, "margins", "Margins for sides");
      GridDataFactory.create(group).fillV();
    }
  }
}