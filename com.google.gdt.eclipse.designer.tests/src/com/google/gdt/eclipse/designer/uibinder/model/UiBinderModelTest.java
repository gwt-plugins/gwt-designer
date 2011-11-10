/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.uibinder.model;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderParser;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.tests.designer.XML.model.AbstractXmlModelTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Abstract super class for GWT UiBinder tests.
 * 
 * @author scheglov_ke
 */
public abstract class UiBinderModelTest extends AbstractXmlModelTest {
  protected static final ToolkitDescription TOOLKIT = GwtToolkitDescription.INSTANCE;
  private GwtState m_lastState;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureTestPreferences(TOOLKIT);
    // by default use shared GWTState
    UiBinderContext.setUseSharedGWTState(true);
  }

  @Override
  protected void tearDown() throws Exception {
    configureDefaultPreferences(TOOLKIT);
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

  /**
   * Configures project and GWT module for given version.
   */
  protected final void configureForGWT_version(String location) throws Exception {
    do_projectDispose();
    do_projectCreate();
    GTestUtils.configure(location, m_testProject);
    IFile moduleFile = GTestUtils.createModule(m_testProject, "test.Module");
    ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
    configureModule(moduleDescription);
  }

  @Override
  protected void disposeLastModel() throws Exception {
    super.disposeLastModel();
    // dispose GWTState
    if (m_lastState != null && !m_lastState.isShared()) {
      m_lastState.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures toolkit with preferences suitable for test.
   */
  protected void configureTestPreferences(ToolkitDescription toolkit) {
  }

  /**
   * Configures toolkit with default preferences.
   */
  protected void configureDefaultPreferences(ToolkitDescription toolkit) {
    NamesManager.setNameDescriptions(toolkit, ImmutableList.<ComponentNameDescription>of());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getJavaSourceToAssert() {
    try {
      IType type = m_javaProject.findType("test.client.Test");
      return type.getCompilationUnit().getSource();
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  protected String[] getJavaSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.*;",
            "import com.google.gwt.event.dom.client.*;",
            "import com.google.gwt.dom.client.Style.Unit;",
            "import com.google.gwt.user.client.ui.*;",
            "import com.google.gwt.uibinder.client.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing and source
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link XmlObjectInfo} for parsed UiBinder source, in "src/test/Text.ui.xml" file.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected final <T extends XmlObjectInfo> T parse(String... lines) throws Exception {
    String source = getTestSource(lines);
    return (T) _parse("src/test/client/Test.ui.xml", source);
  }

  /**
   * Parses <code>ui.xml</code> file with given path and content.
   */
  protected final XmlObjectInfo _parse(String path, String content) throws Exception {
    IFile file = setFileContent(path, content);
    IDocument document = new Document(content);
    // prepare UiBinderContext
    UiBinderContext context = new UiBinderContext(file, document);
    m_lastContext = context;
    // parse
    try {
      UiBinderParser parser = new UiBinderParser(context);
      m_lastObject = parser.parse();
      m_lastLoader = m_lastContext.getClassLoader();
    } finally {
      m_lastState = context.getState();
    }
    // done
    return m_lastObject;
  }

  @Override
  protected String getTestSource_namespaces() {
    return " xmlns:wbp='http://www.google.com/gwt/gdt/uibinder'"
        + " xmlns:t='urn:import:test.client'"
        + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
        + " xmlns:g='urn:import:com.google.gwt.user.client.ui'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} for GWT <code>Button</code>, with text.
   */
  protected static WidgetInfo createButtonWithText() throws Exception {
    return createObject("com.google.gwt.user.client.ui.Button");
  }

  /**
   * @return the {@link WidgetInfo} for GWT <code>Button</code>, without text.
   */
  public static WidgetInfo createButton() throws Exception {
    return createObject("com.google.gwt.user.client.ui.Button", "empty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic containers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link XmlObjectInfo} has canvas/tree {@link FlowContainer} for
   * <code>Widget</code> .
   */
  protected static void assertHasWidgetFlowContainer(XmlObjectInfo panel, boolean forCanvas)
      throws Exception {
    assertHasWidgetFlowContainer(panel, forCanvas, true);
  }

  /**
   * Checks if given {@link XmlObjectInfo} has canvas/tree {@link FlowContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetFlowContainer(XmlObjectInfo panel,
      boolean forCanvas,
      boolean expected) throws Exception {
    FlowContainerFactory factory = new FlowContainerFactory(panel, forCanvas);
    List<FlowContainer> flowContainers = factory.get();
    // check each factory
    WidgetInfo button = createButton();
    for (FlowContainer flowContainer : flowContainers) {
      boolean valid = flowContainer.validateComponent(button);
      assertThat(valid).isEqualTo(expected);
    }
  }

  /**
   * Asserts that given {@link XmlObjectInfo} has canvas/tree {@link SimpleContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetSimpleContainer(XmlObjectInfo container, boolean forCanvas)
      throws Exception {
    assertHasWidgetSimpleContainer(container, forCanvas, true);
  }

  /**
   * Checks if given {@link XmlObjectInfo} has canvas/tree {@link SimpleContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetSimpleContainer(XmlObjectInfo container,
      boolean forCanvas,
      boolean expected) throws Exception {
    SimpleContainerFactory factory = new SimpleContainerFactory(container, forCanvas);
    List<SimpleContainer> simpleContainers = factory.get();
    // check each factory
    WidgetInfo button = createButton();
    for (SimpleContainer simpleContainer : simpleContainers) {
      boolean valid = simpleContainer.validateComponent(button);
      assertThat(valid).isEqualTo(expected);
    }
  }
}