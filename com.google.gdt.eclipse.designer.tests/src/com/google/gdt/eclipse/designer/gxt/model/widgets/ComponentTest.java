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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.gxt.ExtGwtTests;
import com.google.gdt.eclipse.designer.gxt.IExceptionConstants;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.ZipFileFactory;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.core.model.property.EventsPropertyTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Test for basic ExtGWT support.
 * 
 * @author scheglov_ke
 */
public class ComponentTest extends GxtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbsolutePanel
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * ExtGWT has problems with <code>AbsolutePanel</code>.
   * <p>
   * http://extjs.com/forum/showthread.php?p=346814
   */
  public void test_AbsolutePanel_parse() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetPosition(button, 100, 50);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(button)/ /rootPanel.setWidgetPosition(button, 100, 50)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button)/ /rootPanel.setWidgetPosition(button, 100, 50)/}");
    frame.refresh();
    ComponentInfo button = (ComponentInfo) frame.getChildrenWidgets().get(0);
    // check that location applied
    Rectangle bounds = button.getModelBounds();
    assertThat(bounds.x).isEqualTo(100);
    assertThat(bounds.y).isEqualTo(50);
  }

  /**
   * ExtGWT has problems with <code>AbsolutePanel</code>.
   * <p>
   * http://extjs.com/forum/showthread.php?p=346814
   */
  public void test_AbsolutePanel_setLocation() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // add new Button
    ComponentInfo newButton = createButton();
    frame.command_CREATE2(newButton, null);
    frame.command_BOUNDS(newButton, new Point(100, 50), new Dimension(200, 30));
    frame.refresh();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetPosition(button, 100, 50);",
        "      button.setSize('200px', '30px');",
        "    }",
        "  }",
        "}");
    // check that location applied
    Rectangle bounds = newButton.getModelBounds();
    assertThat(bounds).isEqualTo(new Rectangle(100, 50, 200, 30));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * ExtGWT uses classes (not interfaces) as listener parameter. It also uses generics.
   */
  public void test_eventListeners_1() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}");
    panel.refresh();
    // check that "selection" listener exists
    WidgetInfo button = panel.getWidgets().get(0);
    EventsPropertyTest.ensureListenerMethod(button, "selection", "componentSelected");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      button.addSelectionListener(new SelectionListener<ButtonEvent>() {",
        "        public void componentSelected(ButtonEvent ce) {",
        "        }",
        "      });",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * ExtGWT uses classes (not interfaces) as listener parameter. It also uses generics.
   */
  public void test_eventListeners_2() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}");
    panel.refresh();
    // check that "widget" listener exists
    WidgetInfo button = panel.getWidgets().get(0);
    assertNotNull(EventsPropertyTest.getEventsListenerMethod(button, "widget", "resized"));
    assertNotNull(EventsPropertyTest.getEventsListenerMethod(button, "widget", "attached"));
    assertNotNull(EventsPropertyTest.getEventsListenerMethod(button, "widget", "detached"));
  }

  /**
   * There was problem with <code>TreePanel</code> without specific type argument.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43808
   */
  public void test_eventListeners_TreePanel() throws Exception {
    parseJavaInfo(
        "import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;",
        "import com.extjs.gxt.ui.client.store.TreeStore;",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      TreePanel treePanel = new TreePanel(new TreeStore());",
        "      add(treePanel);",
        "    }",
        "  }",
        "}");
    // open listener
    WidgetInfo treePanel = getJavaInfoByName("treePanel");
    EventsPropertyTest.ensureListenerMethod(treePanel, "check", "changed");
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;",
        "import com.extjs.gxt.ui.client.store.TreeStore;",
        "import com.extjs.gxt.ui.client.data.ModelData;",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      TreePanel treePanel = new TreePanel(new TreeStore());",
        "      treePanel.addCheckListener(new CheckChangedListener<ModelData>() {",
        "        public void checkChanged(CheckChangedEvent<ModelData> event) {",
        "        }",
        "      });",
        "      add(treePanel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Simple components
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ToggleButton() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      ToggleButton button = new ToggleButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidgets().get(0);
    //
    Property toggleProperty = button.getPropertyByTitle("toggle");
    assertNotNull(toggleProperty);
    assertEquals(false, toggleProperty.getValue());
  }

  /**
   * When <code>Widget</code> is added to GXT <code>Container</code>, it is wrapped into
   * <code>WidgetComponent</code>, in DOM this looks as <code>DIV</code> around main
   * <code>Element</code>. We fetch bounds of <code>Widget</code> from its <code>Element</code>, but
   * if it was wrapped into <code>DIV</code>, then "offsetLeft" of main <code>Element</code> are
   * <code>0, 0</code>. So, we need to use "wrapper" <code>Element</code> or alternative approach
   * for calculating bounds.
   */
  public void test_standardWidgetBounds() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      com.google.gwt.user.client.ui.Button button = new com.google.gwt.user.client.ui.Button();",
            "      add(button, new AbsoluteData(30, 20));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    {
      Rectangle modelBounds = button.getModelBounds();
      assertEquals(30, modelBounds.x);
      assertEquals(20, modelBounds.y);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for http://www.instantiations.com/forum/viewtopic.php?f=11&t=3704 There is static Element
   * in the IconHelper added into 'document.body'. After parsing we clean up DOM but then we should
   * reset 'initialized' field in the IconHelper.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=3704
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43687
   */
  @DisposeProjectAfter
  public void test_IconHelper_createStyle() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler ................................. */",
            ".test {",
            "  background-image: url(/images/32x32/about.png) !important;",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      button.setIcon(IconHelper.create('test'));",
            "    }",
            "  }",
            "}");
    // refresh(), so work on empty RootPanel; then ask icon image
    {
      frame.refresh();
      check_IconHelper_createStyle();
    }
    // refresh() again, so clean RootPanel again; check that icon image still can be asked
    {
      frame.refresh();
      check_IconHelper_createStyle();
    }
  }

  private void check_IconHelper_createStyle() throws Exception {
    Class<?> class_IconHelper = m_lastLoader.loadClass("com.extjs.gxt.ui.client.util.IconHelper");
    Object imagePrototype =
        ReflectionUtils.invokeMethod(class_IconHelper, "createStyle(java.lang.String)", "test");
    Object safeUrl = ReflectionUtils.getFieldObject(imagePrototype, "url");
    String uri = ReflectionUtils.getFieldString(safeUrl, "uri");
    assertThat(uri).contains("/images/32x32/about.png");
  }

  /**
   * GXT 2.1.0 uses <code>TextMetrics</code> class for resizing <code>Button</code>. It adds
   * invisible "div" to "body". However we remove all children of "body". So, we have to clean-up
   * <code>TextMetrics</code>.
   */
  public void test_TextMetrics_cleanup() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button('New Button');",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComponentInfo button = (ComponentInfo) frame.getChildrenWidgets().get(0);
    // check that location applied
    Rectangle bounds = button.getModelBounds();
    assertThat(bounds.width).isGreaterThan(60);
    assertThat(bounds.height).isGreaterThan(20);
  }

  /**
   * Some users try to use old version of GXT, but our descriptions are for GXT 2.0.1 (we are not
   * subscribers, so don't have access to higher versions). We should check version and fail with
   * good message.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43669
   */
  @DisposeProjectAfter
  public void test_oldVersion() throws Exception {
    dontUseSharedGWTState();
    // replace GXT jar, use old version
    {
      IClasspathEntry[] entries = m_javaProject.getRawClasspath();
      for (int i = 0; i < entries.length; i++) {
        IClasspathEntry entry = entries[i];
        String path = entry.getPath().toString();
        if (path.contains("gxt") && path.endsWith(".jar")) {
          entries = (IClasspathEntry[]) ArrayUtils.remove(entries, i);
          break;
        }
      }
      m_javaProject.setRawClasspath(entries, null);
      waitForAutoBuild();
      // add gxt.jar for version 1.2.4
      m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION_OLD + "/gxt.jar");
    }
    // try to parse, failure expected
    try {
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      Button button = new Button();",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(de.getCode(), IExceptionConstants.INCORRECT_VERSION);
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  /**
   * Some users want to use GXT resources from single jar, instead of copying them in each project.
   * <p>
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=6093
   */
  @DisposeProjectAfter
  public void test_resourcesInJar() throws Exception {
    dontUseSharedGWTState();
    // prepare GXT resources jar
    String resourcesLocation;
    {
      String wsLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
      resourcesLocation = wsLocation + "/gxt-resources.jar";
      OutputStream zipOutputStream = new FileOutputStream(resourcesLocation);
      ZipFileFactory zipFileFactory = new ZipFileFactory(zipOutputStream);
      zipFileFactory.add("my/public", new File(m_project.getLocation().toPortableString()
          + "/war/ExtGWT"));
      zipFileFactory.add(
          "my/Resources.gwt.xml",
          getSourceDQ(
              "<!-- filler filler filler filler filler -->",
              "<!-- filler filler filler filler filler -->",
              "<module>",
              "  <stylesheet src='ExtGWT/css/gxt-all.css'/>",
              "</module>"));
      zipFileFactory.close();
    }
    // delete resources
    {
      IFolder folder = getFolder("war/ExtGWT");
      assertTrue(folder.exists());
      folder.delete(true, null);
    }
    // remove GXT resources reference from HTML file
    {
      IFile htmlFile = getFile("war/Module.html");
      String html = getFileContent(htmlFile);
      html =
          StringUtils.replace(
              html,
              "<link type=\"text/css\" rel=\"stylesheet\" href=\"ExtGWT/css/gxt-all.css\">",
              "");
      assertThat(html).excludes("gxt-all.css");
      assertThat(html).excludes("gxt-");
      setFileContent(htmlFile, html);
    }
    // add GXT resources jar
    m_testProject.addExternalJar(resourcesLocation);
    waitForAutoBuild();
    // add my.Resources module
    {
      ModuleDescription module = Utils.getModule(m_javaProject, "test.Module");
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          moduleElement.addInheritsElement("my.Resources");
        }
      });
    }
    // GXT resources are not in project, they are in jar, but this is OK
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Some users try to use GXT without copying required resources. We should check this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44864
   */
  @DisposeProjectAfter
  public void test_noResources() throws Exception {
    dontUseSharedGWTState();
    // delete resources
    {
      IFolder folder = getFolder("war/ExtGWT");
      assertTrue(folder.exists());
      folder.delete(true, null);
    }
    // try to parse, failure expected
    try {
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      Button button = new Button();",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(de.getCode(), IExceptionConstants.NO_RESOURCES);
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  /**
   * We should check for GXT resources only if module imports GXT module.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=4786
   */
  @DisposeProjectAfter
  public void test_hasGXTjar_noResources_butNotGXTModule() throws Exception {
    do_projectDispose();
    // create standard GWT module
    {
      do_projectCreate();
      GTestUtils.configure(getGWTLocation_forProject(), m_testProject);
      GTestUtils.createModule(m_testProject, "test.Module");
    }
    // add GXT jar, but don't import module
    m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION + "/" + ExtGwtTests.GXT_FILE);
    // parse, no exception, even without GXT resources
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }
}