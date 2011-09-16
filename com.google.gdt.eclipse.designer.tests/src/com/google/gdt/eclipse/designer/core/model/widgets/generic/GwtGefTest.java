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
package com.google.gdt.eclipse.designer.core.model.widgets.generic;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Abstract test for GWT in editor.
 * 
 * @author scheglov_ke
 */
public class GwtGefTest extends DesignerEditorTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureDefaults(com.google.gdt.eclipse.designer.ToolkitProvider.DESCRIPTION);
    // ensure GWT project
    if (m_testProject == null) {
      do_projectCreate();
      configureProject();
      waitForAutoBuild();
    }
    // by default use shared GWTState
    ParseFactory.setUseSharedGWTState(true);
  }

  protected void configureProject() throws Exception {
    GTestUtils.configure(getGWTLocation_forProject(), m_testProject);
    IFile moduleFile = GTestUtils.createModule(m_testProject, "test.Module");
    ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
    configureModule(moduleDescription);
  }

  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
  }

  /**
   * @return the location of GWT to use for this test, can be changed to test something with
   *         non-default GWT version.
   */
  protected String getGWTLocation_forProject() {
    return GTestUtils.getLocation();
  }

  /**
   * Disables using shared GWTState and {@link ClassLoader}, for example because we test custom
   * widgets, so standard shared {@link ClassLoader} (with only standard GWT modules) is not enough
   * for us.
   */
  protected static void dontUseSharedGWTState() {
    ParseFactory.setUseSharedGWTState(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final RootPanelInfo openFrame(String... lines) throws Exception {
    return openJavaInfo(lines);
  }

  protected final CompositeInfo openComposite(String... lines) throws Exception {
    return openJavaInfo(lines);
  }

  @SuppressWarnings("unchecked")
  protected final <T extends JavaInfo> T openJavaInfo(String... lines) throws Exception {
    String source = getTestSource(lines);
    ICompilationUnit unit = createModelCompilationUnit("test.client", "Test.java", source);
    openDesign(unit);
    return (T) m_contentJavaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} with empty <code>Button</code>.
   */
  public final WidgetInfo loadButton() throws Exception {
    return loadCreationTool("com.google.gwt.user.client.ui.Button", "empty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that active {@link AstEditor} has expected GWT source.
   */
  public final void assertEditor(String... lines) {
    assertEditor(getTestSource(lines), m_lastEditor);
  }

  /**
   * @return the source for GWT.
   */
  protected final String getTestSource(String... lines) {
    lines = getDoubleQuotes(lines);
    lines = getTestSource_decorate(lines);
    return getSource(lines);
  }

  /**
   * "Decorates" given lines of source, usually adds required imports.
   */
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.*;",
            "import com.google.gwt.dom.client.Style.Unit;",
            "import com.google.gwt.user.client.*;",
            "import com.google.gwt.user.client.ui.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget for GEF
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void prepareBox() throws Exception {
    prepareBox(100, 50);
  }

  protected void prepareBox(int width, int height) throws Exception {
    setFileContentSrc(
        "test/client/Box.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class Box extends Button {",
            "  public Box() {",
            "    setSize('" + width + "px', '" + height + "px');",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Box.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.width'/>",
            "    <parameter name='liveComponent.forcedSize.height'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  protected WidgetInfo loadCreationBox() throws Exception {
    return loadCreationTool("test.client.Box");
  }
}
