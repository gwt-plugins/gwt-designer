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
package com.google.gdt.eclipse.designer.core.model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.preferences.IPreferenceConstants;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Abstract test for any GWT {@link JavaInfo} model tests.
 * <p>
 * It automatically (re)creates standard <code>test.Module</code> module.
 * 
 * @author scheglov_ke
 */
public abstract class GwtModelTest extends AbstractJavaInfoTest {
  private boolean m_convertSingleQuotesToDouble = true;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int m_testCount = 0;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // ensure GWT project
    if (m_testProject == null) {
      do_projectCreate();
      configureNewProject();
    }
    // by default use shared GWTState
    ParseFactory.setUseSharedGWTState(true);
    // templates
    Activator.getDefault().getPreferenceStore().setValue(
        IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
    Activator.getDefault().getPreferenceStore().setValue(
        IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
  }

  @Override
  protected void configureToolkits() {
    super.configureToolkits();
    configureDefaults(com.google.gdt.eclipse.designer.ToolkitProvider.DESCRIPTION);
  }

  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    assertGWTStateDisposed();
    // dispose shared GWTState every 100 tests
    {
      m_testCount++;
      if (m_testCount % 100 == 0) {
        ParseFactory.disposeSharedGWTState();
      }
    }
    // print memory XXX
    /*{
      System.out.println(getClass().getName());
      System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }*/
    // print memory XXX
    {
      /*ParseFactory.disposeSharedGWTState();
      for (int i = 0; i < 5; i++) {
        try {
          Object[] ignored = new Object[(int) Runtime.getRuntime().maxMemory()];
          ignored[0] = null;
        } catch (Throwable e) {
          // Ignore OME
        }
        System.gc();
        waitEventLoop(1000);
      }*/
      /*ParseFactory.disposeSharedGWTState();
      try {
        Object[] ignored = new Object[(int) Runtime.getRuntime().maxMemory()];
        ignored[0] = null;
      } catch (Throwable e) {
      }
      System.out.println(getClass().getName());
      System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());*/
      // wait
      /*System.out.print("For memory profiler.............");
      waitEventLoop(500);
      System.out.println("done");*/
    }
    // print memory XXX
    /*{
      //int count = 15;
      int count = 2;
      for (int i = 0; i < count; i++) {
        System.gc();
        Thread.sleep(10);
      }
      System.out.println(getClass().getName()
          + "\n\t\t\t"
          + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }*/
    // XXX temporary, for memory profiling
    /*System.out.print("For memory profiler.............");
    while (true) {
      try {
        Object[] ignored = new Object[(int) Runtime.getRuntime().maxMemory()];
        ignored[0] = null;
      } catch (Throwable e) {
        // Ignore OME
      }
      System.gc();
      System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
      waitEventLoop(1000);
    }*/
  }

  /**
   * Configures created project.
   */
  protected void configureNewProject() throws Exception {
    String gwtLocation = getGWTLocation_forProject();
    configureNewProject(gwtLocation);
  }

  /**
   * Configures created project.
   */
  protected void configureNewProject(String gwtLocation) throws Exception {
    GTestUtils.configure(gwtLocation, m_testProject);
    IFile moduleFile = GTestUtils.createModule(m_testProject, "test.Module");
    ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
    configureModule(moduleDescription);
    waitForAutoBuild();
  }

  /**
   * Configures test module, for example for using some specific library.
   */
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

  /**
   * Configures project as Maven-like with GWT module.
   */
  protected final void configureMavenProject() throws Exception {
    // prepare Maven-like project
    getFolder("src").delete(true, null);
    getFolder("src/main/java");
    getFolder("src/main/resources");
    m_testProject.removeSourceFolder("/TestProject/src");
    m_testProject.addSourceFolder("/TestProject/src/main/java");
    m_testProject.addSourceFolder("/TestProject/src/main/resources");
    waitForAutoBuild();
    // create GWT module
    GTestUtils.createModule(m_testProject, "test.Module");
    // move module file into "resources"
    {
      IFile moduleFile = getFile("src/main/java/test/Module.gwt.xml");
      getFolder("src/main/resources/test");
      moduleFile.move(new Path("/TestProject/src/main/resources/test/Module.gwt.xml"), true, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that at most 1 {@link GwtState} instance exists.
   */
  public static void assertGWTStateDisposed() {
    List<GwtState> instances = GwtState.INSTANCES;
    if (instances.size() > 1) {
      instances = Lists.newArrayList(instances);
      // prepare message for fail()
      String msg = "At most 1 instance expected.\n";
      for (GwtState state : instances) {
        msg += "\t" + state.m_testQualifiedName;
        if (state.isShared()) {
          msg += " (shared)" + "\n";
        } else {
          msg += "\n";
        }
      }
      // dispose extra GWTState instances
      for (GwtState state : instances) {
        if (!state.isShared()) {
          state.dispose();
        }
      }
      // fail
      fail(msg);
    } else if (instances.size() == 1) {
      GwtState state = instances.get(0);
      if (!state.isShared()) {
        String msg = "\t" + state.m_testQualifiedName;
        fail("Only GWTState instance should be shared.\n" + msg);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing and source
  //
  ////////////////////////////////////////////////////////////////////////////
  protected Function<String, String> m_assertEditor_expectedSourceProcessor = null;

  public void dontConvertSingleQuotesToDouble() {
    m_convertSingleQuotesToDouble = false;
  }

  /**
   * @return the {@link JavaInfo} for parsed GWT source.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends JavaInfo> T parseJavaInfo(String... lines) throws Exception {
    return (T) parseSource("test.client", "Test.java", getTestSource(lines));
  }

  /**
   * Asserts that active {@link AstEditor} has expected GWT source.
   */
  public final void assertEditor(String... lines) {
    String expectedSource = getTestSource(lines);
    if (m_assertEditor_expectedSourceProcessor != null) {
      expectedSource = m_assertEditor_expectedSourceProcessor.apply(expectedSource);
    }
    assertEditor(expectedSource, m_lastEditor);
  }

  /**
   * @return the source for GWT.
   */
  protected final String getTestSource(String... lines) {
    if (m_convertSingleQuotesToDouble) {
      lines = getDoubleQuotes(lines);
    }
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
            "import com.google.gwt.text.client.*;",
            "import com.google.gwt.text.shared.*;",
            "import com.google.gwt.dom.client.Style.Unit;",
            "import com.google.gwt.user.client.*;",
            "import com.google.gwt.user.client.ui.*;",}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static WidgetInfo createWidget(String qualifiedClassName) throws Exception {
    return createJavaInfo(qualifiedClassName);
  }

  /**
   * @return the new instance of {@link WidgetInfo} for GWT <code>Button</code>, without text.
   */
  public static WidgetInfo createButton() throws Exception {
    return createJavaInfo("com.google.gwt.user.client.ui.Button", "empty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic containers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link JavaInfo} has canvas/tree {@link FlowContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetFlowContainer(JavaInfo panel, boolean forCanvas)
      throws Exception {
    assertHasWidgetFlowContainer(panel, forCanvas, true);
  }

  /**
   * Checks if given {@link JavaInfo} has canvas/tree {@link FlowContainer} for <code>Widget</code>.
   */
  protected static void assertHasWidgetFlowContainer(JavaInfo panel,
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
   * Asserts that given {@link JavaInfo} has canvas/tree {@link SimpleContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetSimpleContainer(JavaInfo container, boolean forCanvas)
      throws Exception {
    assertHasWidgetSimpleContainer(container, forCanvas, true);
  }

  /**
   * Checks if given {@link JavaInfo} has canvas/tree {@link SimpleContainer} for
   * <code>Widget</code>.
   */
  protected static void assertHasWidgetSimpleContainer(JavaInfo container,
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IFile} of standard test module.
   */
  public static IFile getTestModuleFile() throws Exception {
    return getFileSrc("test/Module.gwt.xml");
  }

  /**
   * @return the {@link ModuleDescription} of standard test module.
   */
  public static ModuleDescription getTestModuleDescription() throws Exception {
    IFile moduleFile = getTestModuleFile();
    return Utils.getExactModule(moduleFile);
  }
}