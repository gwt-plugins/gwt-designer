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
package com.google.gdt.eclipse.designer.gwtext.model.layout.assistant;

import com.google.gdt.eclipse.designer.gwtext.model.layout.BorderLayoutDataInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.List;

/**
 * Assistant for GWT-Ext {@link BorderLayoutDataInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public final class BorderLayoutDataAssistantPage extends LayoutDataAssistantPage {
  public BorderLayoutDataAssistantPage(Composite parent, List<ObjectInfo> selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // position
    GridDataFactory.create(
        addStaticFieldsProperty(
            this,
            "region",
            "Region",
            "com.gwtext.client.core.RegionPosition",
            new String[]{"NORTH", "SOUTH", "EAST", "WEST", "CENTER"})).fillH();
    // options
    {
      Group optionsGroup =
          addBooleanProperties(this, "Options", new String[][]{
              new String[]{"animFloat", "animFloat"},
              new String[]{"autoHide", "autoHide"},
              new String[]{"collapseModeMini", "collapseModeMini"},
              new String[]{"floatable", "floatable"},
              new String[]{"split", "split"},
              new String[]{"useSplitTips", "useSplitTips"}});
      GridDataFactory.create(optionsGroup).fillH();
    }
    // margins
    {
      Group marginsGroup =
          addIntegerProperties(this, "Margins", new String[][]{
              new String[]{"margins:top", "top"},
              new String[]{"margins:left", "left"},
              new String[]{"margins:right", "right"},
              new String[]{"margins:bottom", "bottom"}});
      GridDataFactory.create(marginsGroup).fillH();
    }
    // margins-c
    {
      Group marginsGroup =
          addIntegerProperties(this, "Margins-c", new String[][]{
              new String[]{"margins-c:top", "top"},
              new String[]{"margins-c:left", "left"},
              new String[]{"margins-c:right", "right"},
              new String[]{"margins-c:bottom", "bottom"}});
      GridDataFactory.create(marginsGroup).fillH();
    }
    // others 1
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addIntegerProperty(composite, "maxSize");
      addIntegerProperty(composite, "minSize");
    }
    // others 2
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addIntegerProperty(composite, "minWidth");
      addIntegerProperty(composite, "minHeight");
    }
    // tips
    addStringProperty(this, "collapsibleSplitTip");
    addStringProperty(this, "splitTip");
  }
}