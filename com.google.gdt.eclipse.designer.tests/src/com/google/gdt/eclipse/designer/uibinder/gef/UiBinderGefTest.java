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
package com.google.gdt.eclipse.designer.uibinder.gef;

import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderEditor;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XML.editor.AbstractXmlGefTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;

/**
 * Abstract super class for UiBinder GEF tests.
 * 
 * @author scheglov_ke
 */
public abstract class UiBinderGefTest extends AbstractXmlGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureForTestPreferences(GwtToolkitDescription.INSTANCE.getPreferences());
    // by default use shared GWTState
    UiBinderContext.setUseSharedGWTState(true);
  }

  @Override
  protected void tearDown() throws Exception {
    configureDefaultPreferences(GwtToolkitDescription.INSTANCE.getPreferences());
    super.tearDown();
    GwtModelTest.assertGWTStateDisposed();
  }

  /**
   * Configures created project.
   */
  @Override
  protected void configureNewProject() throws Exception {
    GTestUtils.configure(getGWTLocation_forProject(), m_testProject);
    IFile moduleFile = GTestUtils.createModule(m_testProject, "test.Module");
    ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
    configureModule(moduleDescription);
    waitForAutoBuild();
  }

  /**
   * Configures test module, for example for using some specific library.
   */
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    forgetCreatedResources();
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
    UiBinderContext.setUseSharedGWTState(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures test values for toolkit preferences.
   */
  protected void configureForTestPreferences(IPreferenceStore preferences) {
    // direct edit
    preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, false);
  }

  /**
   * Configures default values for toolkit preferences.
   */
  protected void configureDefaultPreferences(IPreferenceStore preferences) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open "Design" and fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens {@link AbstractXmlEditor} with given XML source.
   */
  @SuppressWarnings("unchecked")
  protected <T extends XmlObjectInfo> T openEditor(String... lines) throws Exception {
    IFile file = setFileContentSrc("test/client/Test.ui.xml", getTestSource(lines));
    openDesign(file);
    return (T) m_contentObject;
  }

  @Override
  protected final String getEditorID() {
    return UiBinderEditor.ID;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getJavaSourceToAssert() {
    return getFileContentSrc("test/client/Test.java");
  }

  @Override
  protected String[] getJavaSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.*;",
            "import com.google.gwt.dom.client.Style.Unit;",
            "import com.google.gwt.user.client.*;",
            "import com.google.gwt.user.client.ui.*;",
            "import com.google.gwt.uibinder.client.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    String newLine = "\n\t";
    return " xmlns:wbp='http://www.google.com/gwt/gdt/uibinder'"
        + newLine
        + " xmlns:t='urn:import:test.client'"
        + newLine
        + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
        + newLine
        + " xmlns:g='urn:import:com.google.gwt.user.client.ui'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} with {@link Button} without text.
   */
  protected final WidgetInfo loadButton() throws Exception {
    return loadCreationTool("com.google.gwt.user.client.ui.Button", "empty");
  }

  /**
   * Loads {@link CreationTool} with {@link Button} with text.
   */
  protected final WidgetInfo loadButtonWithText() throws Exception {
    return loadCreationTool("com.google.gwt.user.client.ui.Button");
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
        getJavaSource(
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