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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelAlignmentSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.List;

/**
 * Test for {@link AbsolutePanelInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsolutePanelTest extends GwtModelTest {
  private static final IPreferenceStore preferences =
      GwtToolkitDescription.INSTANCE.getPreferences();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, false);
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
  // setLocation()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLocation_addIntInt() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button, 10, 20);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    frame.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 1, 2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Case when <code>add()</code> without location is used and no <code>setWidgetPosition()</code>.
   * Invocation of <code>add()</code> should be updated to use location.
   */
  public void test_setLocation_add() throws Exception {
    RootPanelInfo frame =
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
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(button)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button)/}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    frame.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 1, 2);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(button, 1, 2)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button, 1, 2)/}");
  }

  /**
   * Case when some custom method is used for association (or exposed component) and no
   * <code>setWidgetPosition()</code>. Invocation of <code>setWidgetPosition()</code> should be
   * added.
   */
  public void test_setLocation_customAdd() throws Exception {
    dontUseSharedGWTState();
    createModelCompilationUnit(
        "test.client",
        "MyAbsolutePanel.java",
        getTestSource(
            "public class MyAbsolutePanel extends AbsolutePanel {",
            "  public void myAdd(Widget widget) {",
            "    add(widget);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyAbsolutePanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='myAdd'>",
            "      <parameter type='com.google.gwt.user.client.ui.Widget' child='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MyAbsolutePanel myPanel = new MyAbsolutePanel();",
        "    rootPanel.add(myPanel);",
        "    {",
        "      Button button = new Button();",
        "      myPanel.myAdd(button);",
        "    }",
        "  }",
        "}");
    refresh();
    AbsolutePanelInfo myPanel = getJavaInfoByName("myPanel");
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    myPanel.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MyAbsolutePanel myPanel = new MyAbsolutePanel();",
        "    rootPanel.add(myPanel);",
        "    {",
        "      Button button = new Button();",
        "      myPanel.myAdd(button);",
        "      myPanel.setWidgetPosition(button, 1, 2);",
        "    }",
        "  }",
        "}");
  }

  public void test_setLocation_setWidgetPosition() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetPosition(button, 10, 20);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    frame.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetPosition(button, 1, 2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * ExtGWT is not compatible with <code>add(widget,x,y)</code>, so we have to use always
   * <code>setWidgetPosition</code>.
   * <p>
   * http://extjs.com/forum/showthread.php?p=346814
   */
  public void test_setLocation_setWidgetPosition_force() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='GWT.AbsolutePanel: force setWidgetPosition'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    frame.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyButton button = new MyButton();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetPosition(button, 1, 2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We update all places with location.
   */
  public void test_setLocation_addPlusSet() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button, 10, 20);",
            "      rootPanel.setWidgetPosition(button, 10, 20);",
            "      rootPanel.setWidgetPosition(button, 30, 40);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new location
    frame.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 1, 2);",
        "      rootPanel.setWidgetPosition(button, 1, 2);",
        "      rootPanel.setWidgetPosition(button, 1, 2);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>UIObject.setSize(String,String)</code> is used.
   */
  public void test_setSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      button.setSize('10px', '20px');",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    frame.command_BOUNDS(button, null, new Dimension(100, 50));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for <code>Autosize widget</code> action.
   */
  public void test_contextMenu_autoSize() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // prepare action
    IAction autoSizeAction;
    {
      IMenuManager manager = getContextMenu(button);
      autoSizeAction = findChildAction(manager, "Autosize widget");
      assertNotNull(autoSizeAction);
    }
    // perform auto-size
    autoSizeAction.run();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbsolutePanelInfo#command_CREATE(WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    refresh();
    // do CREATE
    WidgetInfo button = createButton();
    frame.command_CREATE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsolutePanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button_1 = new Button();",
            "      rootPanel.add(button_1, 10, 20);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2, 10, 200);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // do ADD
    frame.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button_2 = new Button();",
        "      rootPanel.add(button_2, 10, 200);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      rootPanel.add(button_1, 10, 20);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsolutePanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_ADD() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 10, 20);",
        "    }",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    AbsolutePanelInfo panel = getJavaInfoByName("panel");
    // do ADD
    panel.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was problem that sequence: reparent, move does not moves add() association.
   */
  public void test_ADD_associationProblem() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      AbsolutePanel panel = new AbsolutePanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    AbsolutePanelInfo panel = getJavaInfoByName("panel");
    // do ADD
    frame.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/ /rootPanel.add(button)/}",
        "  {new: com.google.gwt.user.client.ui.AbsolutePanel} {local-unique: panel} {/new AbsolutePanel()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button)/}");
    // set location
    frame.command_BOUNDS(button, new Point(50, 100), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 50, 100);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/ /rootPanel.add(button, 50, 100)/}",
        "  {new: com.google.gwt.user.client.ui.AbsolutePanel} {local-unique: panel} {/new AbsolutePanel()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button, 50, 100)/}");
    // move "button" before "panel"
    frame.command_MOVE2(button, panel);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button, 50, 100);",
        "    }",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/ /rootPanel.add(button, 50, 100)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button, 50, 100)/}",
        "  {new: com.google.gwt.user.client.ui.AbsolutePanel} {local-unique: panel} {/new AbsolutePanel()/ /rootPanel.add(panel)/}");
  }

  /**
   * Test for copy/paste {@link AbsolutePanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      AbsolutePanel panel = new AbsolutePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button, 50, 100);",
            "        button.setSize('5cm', '1in');",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    // do copy/paste
    {
      AbsolutePanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, 50, 100);",
        "        button.setSize('5cm', '1in');",
        "      }",
        "    }",
        "    {",
        "      AbsolutePanel absolutePanel = new AbsolutePanel();",
        "      rootPanel.add(absolutePanel);",
        "      {",
        "        Button button = new Button();",
        "        absolutePanel.add(button, 50, 100);",
        "        button.setSize('5cm', '1in');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbsolutePanelAlignmentSupport}.
   */
  public void test_alignmentActions_LEFT() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button_1 = new Button();",
            "      rootPanel.add(button_1, 10, 20);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2, 30, 100);",
            "    }",
            "  }",
            "}");
    setupSelectionActions(frame);
    refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // perform alignment
    alignWidgets("Align left edges", frame, button_1, button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button_1 = new Button();",
        "      rootPanel.add(button_1, 10, 20);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      rootPanel.add(button_2, 10, 100);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsolutePanelAlignmentSupport}.
   */
  public void test_alignmentActions_crossPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      AbsolutePanel innerPanel = new AbsolutePanel();",
            "      rootPanel.add(innerPanel, 10, 10);",
            "      {",
            "        Button button_1 = new Button();",
            "        innerPanel.add(button_1, 20, 30);",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2, 0, 100);",
            "    }",
            "  }",
            "}");
    refresh();
    AbsolutePanelInfo innerPanel = getJavaInfoByName("innerPanel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // perform alignment
    setupSelectionActions(frame);
    setupSelectionActions(innerPanel);
    alignWidgets("Align left edges", frame, button_1, button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel innerPanel = new AbsolutePanel();",
        "      rootPanel.add(innerPanel, 10, 10);",
        "      {",
        "        Button button_1 = new Button();",
        "        innerPanel.add(button_1, 20, 30);",
        "      }",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      rootPanel.add(button_2, 30, 100);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsolutePanelAlignmentSupport}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45507
   */
  public void test_alignmentActions_oneWidget_onSimplePanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button_1 = new Button();",
            "      rootPanel.add(button_1, 20, 100);",
            "    }",
            "    {",
            "      SimplePanel innerPanel = new SimplePanel();",
            "      rootPanel.add(innerPanel, 10, 10);",
            "      {",
            "        Button button_2 = new Button();",
            "        innerPanel.setWidget(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    setupSelectionActions(frame);
    refresh();
    //
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // perform alignment
    // "button_2" on SimplePanel is ignored
    alignWidgets("Align left edges", frame, button_1, button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button_1 = new Button();",
        "      rootPanel.add(button_1, 20, 100);",
        "    }",
        "    {",
        "      SimplePanel innerPanel = new SimplePanel();",
        "      rootPanel.add(innerPanel, 10, 10);",
        "      {",
        "        Button button_2 = new Button();",
        "        innerPanel.setWidget(button_2);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Runs alignment action from {@link AbsolutePanelAlignmentSupport}.
   */
  private void alignWidgets(String actionText,
      RootPanelInfo frame,
      WidgetInfo button_1,
      WidgetInfo button_2) throws Exception {
    // prepare selection
    List<ObjectInfo> selectedObjects;
    {
      selectedObjects = Lists.newArrayList();
      selectedObjects.add(button_1);
      selectedObjects.add(button_2);
    }
    // prepare action
    IAction action;
    {
      List<Object> actions = Lists.newArrayList();
      frame.getBroadcastObject().addSelectionActions(selectedObjects, actions);
      action = findAction(actions, actionText);
    }
    // perform alignment
    action.run();
  }

  private void setupSelectionActions(final AbsolutePanelInfo panel) {
    panel.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        AbsolutePanelAlignmentSupport.create(panel).addAlignmentActions(objects, actions);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation flow
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test applying creation flow order.
   */
  public void test_BOUNDS_CreationFlow() throws Exception {
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, true);
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, 50, 100);",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, 100, 150);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    AbsolutePanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // Bounds
    panel.command_BOUNDS(button_2, new Point(5, 5), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, 5, 5);",
        "      }",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, 50, 100);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}