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
package com.google.gdt.eclipse.designer.smartgwt.model;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.smart.actions.ConfigureSmartGwtOperation;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smartgwt.SmartGwtTests;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

/**
 * Superclass for SmartGWT model tests.
 * 
 * @author scheglov_ke
 */
public abstract class SmartGwtModelTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean SMART_GWT_STARTED = false;

  @Override
  protected void setUp() throws Exception {
    ensureStartedSmartGWT();
    super.setUp();
  }

  private static void ensureStartedSmartGWT() throws Exception {
    if (!SMART_GWT_STARTED) {
      ParseFactory.disposeSharedGWTState();
      SMART_GWT_STARTED = true;
    }
  }

  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    m_testProject.addExternalJar(SmartGwtTests.LOCATION + "/smartgwt.jar");
    m_testProject.addExternalJar(SmartGwtTests.LOCATION + "/smartgwt-skins.jar");
    new ConfigureSmartGwtOperation(moduleDescription, SmartGwtTests.LOCATION).run(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.EntryPoint;",
            "import com.google.gwt.user.client.ui.RootPanel;",
            "import com.smartgwt.client.widgets.*;",
            "import com.smartgwt.client.widgets.calendar.*;",
            "import com.smartgwt.client.widgets.grid.*;",
            "import com.smartgwt.client.widgets.layout.*;",
            "import com.smartgwt.client.widgets.menu.*;",
            "import com.smartgwt.client.widgets.tile.*;",
            "import com.smartgwt.client.widgets.toolbar.*;",
            "import com.smartgwt.client.widgets.tree.*;",
            "import com.smartgwt.client.widgets.viewer.*;",
            "import com.smartgwt.client.widgets.tab.*;",
            "import com.smartgwt.client.widgets.events.*;",
            "import com.smartgwt.client.widgets.form.*;",
            "import com.smartgwt.client.widgets.form.fields.*;",
            "import com.smartgwt.client.data.*;",
            "import com.smartgwt.client.data.fields.*;",
            "import com.smartgwt.client.types.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Models
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} for <code>com.smartgwt.client.widgets.Button</code>.
   */
  public static CanvasInfo createButton() throws Exception {
    return createJavaInfo("com.smartgwt.client.widgets.Button", "empty");
  }
}