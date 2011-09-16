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

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.FlowLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link LayoutContainerInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutContainerTest extends GxtModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty container should have some reasonable size (because of message).
   */
  public void test_empty() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      LayoutContainer inner = new LayoutContainer();",
            "      add(inner);",
            "    }",
            "  }",
            "}");
    container.refresh();
    //
    LayoutContainerInfo inner = getJavaInfoByName("inner");
    {
      Rectangle bounds = inner.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  /**
   * By default {@link DefaultLayoutInfo} is used.
   */
  public void test_implicitLayout_DefaultLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    assertThat(container.getLayout()).isInstanceOf(DefaultLayoutInfo.class);
  }

  /**
   * Test that <code>FlowLayout</code> is detected and its model created.
   */
  public void test_implicitLayout_FlowLayout() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyContainer.java",
        getTestSource(
            "public class MyContainer extends LayoutContainer {",
            "  public MyContainer() {",
            "    setLayout(new FlowLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyContainer {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.client.MyContainer} {this} {}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {implicit-layout} {}");
    // check layout
    assertTrue(container.hasLayout());
    LayoutInfo layout = container.getLayout();
    assertThat(layout).isInstanceOf(FlowLayoutInfo.class);
    // simulate LayoutInfo absence
    {
      container.removeChild(layout);
      try {
        container.getLayout();
        fail();
      } catch (IllegalStateException e) {
      }
    }
  }

  /**
   * If container is placeholder, then it has no layout.
   */
  public void test_implicitLayout_ifPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyContainer.java",
        getTestSource(
            "public class MyContainer extends LayoutContainer {",
            "  public MyContainer() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MyContainer myContainer = new MyContainer();",
        "      add(myContainer);",
        "    }",
        "  }",
        "}");
    // no layout
    LayoutContainerInfo myContainer = getJavaInfoByName("myContainer");
    assertFalse(myContainer.hasLayout());
  }

  public void test_parse_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout(5));",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}");
    assertThat(container.getLayout()).isInstanceOf(FlowLayoutInfo.class);
  }

  /**
   * Test that {@link LayoutContainerInfo#getLayout()} throws exception if no layout expected.
   */
  public void test_getLayout_whenNoLayoutExpected() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyContainer.java",
        getTestSource(
            "public class MyContainer extends LayoutContainer {",
            "  public MyContainer() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyContainer.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.has'>no</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyContainer {",
            "  public Test() {",
            "  }",
            "}");
    // can not use getLayout()
    assertFalse(container.hasLayout());
    try {
      container.getLayout();
      fail();
    } catch (DesignerException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout_1() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // initial state
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    assertThat(container.getLayout()).isInstanceOf(DefaultLayoutInfo.class);
    // set FlowLayout
    {
      LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FlowLayout");
      container.setLayout(layout);
      assertEditor(
          "// filler filler filler",
          "public class Test extends LayoutContainer {",
          "  public Test() {",
          "    setLayout(new FlowLayout(5));",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout(5))/}",
          "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}");
      assertSame(layout, container.getLayout());
    }
    // set RowLayout
    {
      LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.RowLayout");
      container.setLayout(layout);
      assertEditor(
          "// filler filler filler",
          "public class Test extends LayoutContainer {",
          "  public Test() {",
          "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}",
          "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}");
      assertSame(layout, container.getLayout());
    }
  }

  /**
   * Test for {@link LayoutContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout_2() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout(5));",
            "  }",
            "}");
    // initial state
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}");
    assertThat(container.getLayout()).isInstanceOf(FlowLayoutInfo.class);
    // set RowLayout
    {
      LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.RowLayout");
      container.setLayout(layout);
      assertEditor(
          "public class Test extends LayoutContainer {",
          "  public Test() {",
          "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}",
          "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}");
      assertSame(layout, container.getLayout());
    }
  }

  /**
   * Test for {@link LayoutContainerInfo#setLayout(LayoutInfo)} and {@link ComponentOrder}.
   */
  public void test_setLayout_order() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    add(new Button());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(new Button())/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {empty} {/add(new Button())/}");
    // set RowLayout
    LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.RowLayout");
    assertSame(ComponentOrderFirst.INSTANCE, layout.getDescription().getOrder());
    container.setLayout(layout);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
        "    add(new Button());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(new Button())/ /setLayout(new RowLayout(Orientation.HORIZONTAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {empty} {/add(new Button())/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test copy/paste {@link LayoutContainerInfo} with implicit layout.
   */
  public void test_clipboard_implicitLayout() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyContainer.java",
        getTestSource(
            "public class MyContainer extends LayoutContainer {",
            "  public MyContainer() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      MyContainer inner = new MyContainer();",
            "      add(inner);",
            "    }",
            "  }",
            "}");
    container.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(inner)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: test.client.MyContainer} {local-unique: inner} {/new MyContainer()/ /add(inner)/}",
        "    {implicit-layout: com.extjs.gxt.ui.client.widget.layout.RowLayout} {implicit-layout} {}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    // prepare memento
    JavaInfoMemento memento;
    {
      LayoutContainerInfo inner = (LayoutContainerInfo) container.getWidgets().get(0);
      memento = JavaInfoMemento.createMemento(inner);
    }
    // create new Widget
    WidgetInfo newWidget = (WidgetInfo) memento.create(container);
    container.getLayout().command_CREATE(newWidget, null);
    memento.apply();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      MyContainer inner = new MyContainer();",
        "      add(inner);",
        "    }",
        "    {",
        "      MyContainer myContainer = new MyContainer();",
        "      add(myContainer);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(inner)/ /add(myContainer)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: test.client.MyContainer} {local-unique: inner} {/new MyContainer()/ /add(inner)/}",
        "    {implicit-layout: com.extjs.gxt.ui.client.widget.layout.RowLayout} {implicit-layout} {}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "  {new: test.client.MyContainer} {local-unique: myContainer} {/new MyContainer()/ /add(myContainer)/}",
        "    {implicit-layout: com.extjs.gxt.ui.client.widget.layout.RowLayout} {implicit-layout} {}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
  }

  /**
   * Test copy/paste {@link LayoutContainerInfo} applies {@link LayoutInfo}.
   */
  public void test_clipboard_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      LayoutContainer inner = new LayoutContainer();",
            "      add(inner);",
            "      inner.setLayout(new RowLayout(Orientation.VERTICAL));",
            "    }",
            "  }",
            "}");
    container.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(inner)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: inner} {/new LayoutContainer()/ /add(inner)/ /inner.setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/inner.setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    // prepare memento
    JavaInfoMemento memento;
    {
      LayoutContainerInfo inner = (LayoutContainerInfo) container.getWidgets().get(0);
      memento = JavaInfoMemento.createMemento(inner);
    }
    // create new Widget
    WidgetInfo newWidget = (WidgetInfo) memento.create(container);
    container.getLayout().command_CREATE(newWidget, null);
    memento.apply();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      LayoutContainer inner = new LayoutContainer();",
        "      add(inner);",
        "      inner.setLayout(new RowLayout(Orientation.VERTICAL));",
        "    }",
        "    {",
        "      LayoutContainer layoutContainer = new LayoutContainer();",
        "      layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));",
        "      add(layoutContainer);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(inner)/ /add(layoutContainer)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: inner} {/new LayoutContainer()/ /add(inner)/ /inner.setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/inner.setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: layoutContainer} {/new LayoutContainer()/ /add(layoutContainer)/ /layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL))/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There was problem with "afterCreate" notifications and using "onRender". We did not call
   * "onRender" during refresh (only during parsing), so this caused exceptions because of no
   * objects for models.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43278
   */
  public void test_Composite_inWidgetComposite() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "import com.google.gwt.user.client.ui.Composite;",
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initWidget(new Button());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "  protected void onRender(com.google.gwt.user.client.Element parent, int index) {",
            "    super.onRender(parent, index);",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      WidgetComponent compositeComponent = new WidgetComponent(composite);",
            "      add(compositeComponent);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(compositeComponent)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.WidgetComponent} {local-unique: compositeComponent} {/new WidgetComponent(composite)/ /add(compositeComponent)/}",
        "    {new: test.client.MyComposite} {local-unique: composite} {/new MyComposite()/ /new WidgetComponent(composite)/}");
    //
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * Some (stupid) users try to use <code>LayoutContainer</code> in combination with
   * <code>RootPanel</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47794
   */
  public void test_bugWithRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    frame.refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Set Layout" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link LayoutContainerInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_1() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    container.refresh();
    assertTrue(container.hasLayout());
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager;
    {
      IMenuManager menuManager = getContextMenu(container);
      layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNotNull(layoutManager);
    }
    // check for existing actions
    assertNotNull(findChildAction(layoutManager, "AbsoluteLayout"));
    assertNotNull(findChildAction(layoutManager, "RowLayout, vertical"));
    assertNotNull(findChildAction(layoutManager, "FillLayout, horizontal"));
    assertNotNull(findChildAction(layoutManager, "ColumnLayout"));
    assertNotNull(findChildAction(layoutManager, "BorderLayout"));
    assertNotNull(findChildAction(layoutManager, "CenterLayout"));
    assertNotNull(findChildAction(layoutManager, "AnchorLayout"));
    assertNotNull(findChildAction(layoutManager, "FormLayout"));
    assertNotNull(findChildAction(layoutManager, "FitLayout"));
    assertNotNull(findChildAction(layoutManager, "AccordionLayout"));
    assertNotNull(findChildAction(layoutManager, "CardLayout"));
    assertNotNull(findChildAction(layoutManager, "TableLayout"));
    assertNotNull(findChildAction(layoutManager, "TableRowLayout"));
    assertNotNull(findChildAction(layoutManager, "HBoxLayout"));
    assertNotNull(findChildAction(layoutManager, "VBoxLayout"));
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "RowLayout, horizontal");
      action.run();
      assertEditor(
          "public class Test extends LayoutContainer {",
          "  public Test() {",
          "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
          "  }",
          "}");
    }
  }

  /**
   * No "Set Layout" sub-menu if <code>LayoutContainer</code> has no layout.
   */
  public void test_setLayoutMenu_2() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo("public class Test extends VerticalPanel {", "  public Test() {", "  }", "}");
    container.refresh();
    // no layout
    assertFalse(container.hasLayout());
    // ...so, no "Set layout" menu
    {
      IMenuManager menuManager = getContextMenu(container);
      IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNull(layoutManager);
    }
  }
}