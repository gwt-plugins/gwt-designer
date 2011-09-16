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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.beans.Beans;

/**
 * Test for visual inheritance supported by {@link GWTHierarchyProvider}.
 * 
 * @author scheglov_ke
 */
public class VisualInheritanceTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    do_projectDispose();
  }

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
   * Test that binary execution flow activated from <code>Widget.onLoad()</code> is supported.
   */
  public void test_binaryExecutionFlow() throws Exception {
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public abstract class MyComposite extends Composite {",
            "  private FlowPanel m_flowPanel = new FlowPanel();",
            "  public MyComposite() {",
            "    initWidget(m_flowPanel);",
            "  }",
            "  protected void onLoad() {",
            "    m_flowPanel.add(createMyWidget());",
            "  }",
            "  protected abstract Widget createMyWidget();",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends MyComposite {",
            "  public Test() {",
            "  }",
            "  protected Widget createMyWidget() {",
            "    Button button = new Button();",
            "    return button;",
            "  }",
            "}");
    composite.refresh();
    assertHierarchy(
        "{this: test.client.MyComposite} {this} {}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /button/}");
  }

  /**
   * When we add <code>Element</code> of <code>Widget</code> into <code>Panel</code> parent/child
   * link should be established.
   */
  public void test_binaryBinding_byElement() throws Exception {
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends FlowPanel {",
            "  public MyPanel() {",
            "  }",
            "  public void addWidget(Widget widget) {",
            "    DOM.appendChild(getElement(), widget.getElement());",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addWidget'>",
            "      <parameter type='com.google.gwt.user.client.ui.Widget'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel myPanel = new MyPanel();",
            "    rootPanel.add(myPanel);",
            "    //",
            "    Button button = new Button();",
            "    myPanel.addWidget(button);",
            "  }",
            "}");
    frame.refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(myPanel)/}",
        "  {new: test.client.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /rootPanel.add(myPanel)/ /myPanel.addWidget(button)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /myPanel.addWidget(button)/}");
  }

  /**
   * When we add <code>Widget</code> into <code>Panel</code> using method that is not described as
   * association, than parent/child link is still established, but using {@link UnknownAssociation}.
   */
  public void test_binaryBinding_byWidget() throws Exception {
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends FlowPanel {",
            "  public MyPanel() {",
            "  }",
            "  public void addWidget(Widget widget) {",
            "    add(widget);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addWidget'>",
            "      <parameter type='com.google.gwt.user.client.ui.Widget'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel myPanel = new MyPanel();",
            "    rootPanel.add(myPanel);",
            "    //",
            "    Button button = new Button();",
            "    myPanel.addWidget(button);",
            "  }",
            "}");
    frame.refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(myPanel)/}",
        "  {new: test.client.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /rootPanel.add(myPanel)/ /myPanel.addWidget(button)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /myPanel.addWidget(button)/}");
  }

  public void test_logicalHierarchy() throws Exception {
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends FlowPanel {",
            "  private final FlowPanel m_container;",
            "  private final Button m_button;",
            "  public MyPanel() {",
            "    m_container = new FlowPanel();",
            "    add(m_container);",
            "    //",
            "    m_button = new Button();",
            "    m_container.add(m_button);",
            "  }",
            "  public FlowPanel getContainer() {",
            "    return m_container;",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel myPanel = new MyPanel();",
            "    rootPanel.add(myPanel);",
            "  }",
            "}");
    frame.refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(myPanel)/}",
        "  {new: test.client.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /rootPanel.add(myPanel)/}",
        "    {method: public com.google.gwt.user.client.ui.FlowPanel test.client.MyPanel.getContainer()} {property} {}",
        "      {method: public com.google.gwt.user.client.ui.Button test.client.MyPanel.getButton()} {property} {}");
  }

  public void test_logicalHierarchy_forComposite() throws Exception {
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Composite {",
            "  private final FlowPanel m_container;",
            "  private final Button m_button;",
            "  public MyPanel() {",
            "    m_container = new FlowPanel();",
            "    setWidget(m_container);",
            "    //",
            "    m_button = new Button();",
            "    m_container.add(m_button);",
            "  }",
            "  public FlowPanel getContainer() {",
            "    return m_container;",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel myPanel = new MyPanel();",
            "    rootPanel.add(myPanel);",
            "  }",
            "}");
    frame.refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(myPanel)/}",
        "  {new: test.client.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /rootPanel.add(myPanel)/}",
        "    {method: public com.google.gwt.user.client.ui.FlowPanel test.client.MyPanel.getContainer()} {property} {}",
        "      {method: public com.google.gwt.user.client.ui.Button test.client.MyPanel.getButton()} {property} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isDesignTime()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * GWT has {@link Beans#isDesignTime()}.
   */
  public void test_isDesignTime() throws Exception {
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "import java.beans.Beans;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    Button button = new Button();",
        "    button.setEnabled(!Beans.isDesignTime());",
        "    add(button);",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // isDesignTime() == true
    assertEquals(false, ReflectionUtils.invokeMethod(button.getObject(), "isEnabled()"));
  }
}