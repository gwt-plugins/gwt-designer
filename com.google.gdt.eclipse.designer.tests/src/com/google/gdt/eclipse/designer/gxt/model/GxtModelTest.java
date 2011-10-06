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
package com.google.gdt.eclipse.designer.gxt.model;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.gxt.ExtGwtTests;
import com.google.gdt.eclipse.designer.gxt.actions.ConfigureExtGwtOperation;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ButtonInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Test for Ext-GWT widgets.
 * 
 * @author scheglov_ke
 */
public abstract class GxtModelTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean GXT_STARTED = false;

  @Override
  protected void setUp() throws Exception {
    ensureStartedGXT();
    super.setUp();
  }

  private static void ensureStartedGXT() {
    if (!GXT_STARTED) {
      ParseFactory.disposeSharedGWTState();
      GXT_STARTED = true;
    }
  }

  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    new ConfigureExtGwtOperation(moduleDescription, ExtGwtTests.GXT_LOCATION).run(null);
    // don't use "strict" mode
    {
      String html = getFileContent("war/Module.html");
      html = StringUtils.remove(html, "<!doctype html>");
      setFileContent("war/Module.html", html);
    }
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
            "import com.extjs.gxt.ui.client.*;",
            "import com.extjs.gxt.ui.client.Style.*;",
            "import com.extjs.gxt.ui.client.util.*;",
            "import com.extjs.gxt.ui.client.widget.*;",
            "import com.extjs.gxt.ui.client.widget.button.*;",
            "import com.extjs.gxt.ui.client.widget.layout.*;",
            "import com.extjs.gxt.ui.client.widget.form.*;",
            "import com.extjs.gxt.ui.client.widget.menu.*;",
            "import com.extjs.gxt.ui.client.widget.toolbar.*;",
            "import com.extjs.gxt.ui.client.widget.custom.*;",
            "import com.extjs.gxt.ui.client.event.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Models
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ButtonInfo} for <code>com.extjs.gxt.ui.client.widget.button.Button</code>.
   */
  public static ButtonInfo createButton() throws Exception {
    return createJavaInfo("com.extjs.gxt.ui.client.widget.button.Button", "empty");
  }
}