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

import com.google.gdt.eclipse.designer.gxt.model.layout.RowLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Assistant for GXT <code>RowLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class RowLayoutAssistantPage extends AbstractExtGwtAssistantPage {
  public RowLayoutAssistantPage(Composite parent, RowLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // orientation
    addEnumProperty(this, "orientation", "Orientation", "com.extjs.gxt.ui.client.Style$Orientation");
    // options
    {
      Group optionsGroup =
          addBooleanProperties(this, "Options", new String[][]{
              new String[]{"adjastForScroll", "adjastForScroll"},
              new String[]{"firesEvents", "firesEvents"},
              new String[]{"renderHidden", "renderHidden"}});
      GridDataFactory.create(optionsGroup).fill();
    }
  }
}