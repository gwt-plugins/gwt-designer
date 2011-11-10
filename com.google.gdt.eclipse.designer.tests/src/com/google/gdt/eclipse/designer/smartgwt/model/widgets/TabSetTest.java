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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.TabInfo;
import com.google.gdt.eclipse.designer.smart.model.TabSetInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabSetInfo}.
 * 
 * @author sablin_aa
 */
public class TabSetTest extends SmartGwtModelTest {
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
  public void test_parse() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    tabSet.setTabs(new Tab('Tab_2'));",
            "    tabSet.draw();",
            "  }",
            "}"});
    tabSet.refresh();
    check_parse(tabSet);
  }

  public void test_parse_this() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test extends TabSet {",
            "  public Test() {",
            "    addTab(new Tab('Tab_1'));",
            "    setTabs(new Tab('Tab_2'));",
            "  }",
            "}"});
    //tabSet.getTopBoundsSupport().setSize(270, 240);
    tabSet.refresh();
    check_parse(tabSet);
  }

  private void check_parse(TabSetInfo tabSet) {
    assertThat(tabSet.getWidgets()).isEmpty();
    assertThat(tabSet.getTabs().size()).isEqualTo(2);
    // check bounds
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    Rectangle tabSetBounds = tabSet.getModelBounds();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(insets.left, barThickness + insets.right, tabSetBounds.width
            - insets.getWidth(), tabSetBounds.height - barThickness - insets.getHeight()));
    //
    AbstractArrayObjectInfo objectTabsInfo =
        tabSet.getChildren(AbstractArrayObjectInfo.class).get(0);
    assertThat(objectTabsInfo.getItems().size()).isEqualTo(1);
    assertThat(objectTabsInfo.getChildren(TabInfo.class).size()).isEqualTo(0);
  }

  /**
   * {@link TabInfo} added before {@link TabSetInfo} association.
   */
  public void test_tab_ADD() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo rootPanel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    TabSet tabSet = new TabSet();",
            "    rootPanel.add(tabSet, 10, 10);",
            "    tabSet.setSize('250px', '200px');",
            "  }",
            "}");
    rootPanel.refresh();
    //
    TabSetInfo tabSet = (TabSetInfo) rootPanel.getChildrenWidgets().get(0);
    assertThat(tabSet.getChildrenJava()).isEmpty();
    // create new Tab
    TabInfo newTab = createJavaInfo("com.smartgwt.client.widgets.tab.Tab");
    {
      FlowContainer flowContainer = new FlowContainerFactory(tabSet, false).get().get(0);
      assertTrue(flowContainer.validateComponent(newTab));
      flowContainer.command_CREATE(newTab, null);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    TabSet tabSet = new TabSet();",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      tabSet.addTab(tab);",
        "    }",
        "    rootPanel.add(tabSet, 10, 10);",
        "    tabSet.setSize('250px', '200px');",
        "  }",
        "}");
    // place Button on Tab
    CanvasInfo newButton = createJavaInfo("com.smartgwt.client.widgets.Button");
    {
      SimpleContainer simpleContainer = new SimpleContainerFactory(newTab, false).get().get(1);
      assertTrue(simpleContainer.validateComponent(newButton));
      simpleContainer.command_CREATE(newButton);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    TabSet tabSet = new TabSet();",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      {",
        "        Button button = new Button('New Button');",
        "        tab.setPane(button);",
        "      }",
        "      tabSet.addTab(tab);",
        "    }",
        "    rootPanel.add(tabSet, 10, 10);",
        "    tabSet.setSize('250px', '200px');",
        "  }",
        "}");
  }

  /**
   * Add {@link CanvasInfo}.
   */
  public void test_canvas_ADD() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo rootPanel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    TabSet tabSet = new TabSet();",
            "    rootPanel.add(tabSet, 10, 10);",
            "    tabSet.setSize('250px', '200px');",
            "  }",
            "}");
    rootPanel.refresh();
    //
    TabSetInfo tabSet = (TabSetInfo) rootPanel.getChildrenWidgets().get(0);
    assertThat(tabSet.getChildrenJava()).isEmpty();
    // create new Tab
    CanvasInfo newCanvas = createJavaInfo("com.smartgwt.client.widgets.Button");
    {
      FlowContainer flowContainer = new FlowContainerFactory(tabSet, false).get().get(1);
      assertTrue(flowContainer.validateComponent(newCanvas));
      flowContainer.command_CREATE(newCanvas, null);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    TabSet tabSet = new TabSet();",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      {",
        "        Button button = new Button('New Button');",
        "        tab.setPane(button);",
        "      }",
        "      tabSet.addTab(tab);",
        "    }",
        "    rootPanel.add(tabSet, 10, 10);",
        "    tabSet.setSize('250px', '200px');",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    {",
            "      Tab tab = new Tab('newTab');",
            "      Button button = new Button('New Button');",
            "      tab.setPane(button);",
            "      tabSet.addTab(tab);",
            "    }",
            "    canvas.addChild(tabSet);",
            "    com.smartgwt.client.widgets.Label label = new com.smartgwt.client.widgets.Label();",
            "    canvas.addChild(label);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    List<CanvasInfo> children = canvas.getChildren(CanvasInfo.class);
    //
    TabSetInfo tabSet = (TabSetInfo) children.get(0);
    assertThat(tabSet.getTabs().size()).isEqualTo(1);
    CanvasInfo label = children.get(1);
    // add label to TabSet
    tabSet.command_MOVE(label, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    TabSet tabSet = new TabSet();",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      Button button = new Button('New Button');",
        "      tab.setPane(button);",
        "      tabSet.addTab(tab);",
        "    }",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      com.smartgwt.client.widgets.Label label = new com.smartgwt.client.widgets.Label();",
        "      tab.setPane(label);",
        "      tabSet.addTab(tab);",
        "    }",
        "    canvas.addChild(tabSet);",
        "    canvas.draw();",
        "  }",
        "}");
    // move label to first position
    tabSet.command_MOVE(label, tabSet.getTabs().get(0));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    TabSet tabSet = new TabSet();",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      com.smartgwt.client.widgets.Label label = new com.smartgwt.client.widgets.Label();",
        "      tab.setPane(label);",
        "      tabSet.addTab(tab);",
        "    }",
        "    {",
        "      Tab tab = new Tab('newTab');",
        "      Button button = new Button('New Button');",
        "      tab.setPane(button);",
        "      tabSet.addTab(tab);",
        "    }",
        "    canvas.addChild(tabSet);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test {@link Tab_Info} bounds.
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tabBounds_top() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    canvas.addChild(tabSet);",
            "    tabSet.moveTo(30, 20);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    TabSetInfo tabSet = canvas.getChildren(TabSetInfo.class).get(0);
    // check bounds
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    Rectangle tabSetBounds = tabSet.getModelBounds();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(insets.left, barThickness + insets.right, tabSetBounds.width
            - insets.getWidth(), tabSetBounds.height - barThickness - insets.getHeight()));
  }

  public void test_tabBounds_left() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.setTabBarPosition(Side.LEFT);",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    canvas.addChild(tabSet);",
            "    tabSet.moveTo(30, 20);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    TabSetInfo tabSet = canvas.getChildren(TabSetInfo.class).get(0);
    // check bounds
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    Rectangle tabSetBounds = tabSet.getModelBounds();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(barThickness + insets.left, insets.right, tabSetBounds.width
            - barThickness
            - insets.getWidth(), tabSetBounds.height - insets.getHeight()));
  }

  public void test_tabBounds_bottom() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.setTabBarPosition(Side.BOTTOM);",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    canvas.addChild(tabSet);",
            "    tabSet.moveTo(30, 20);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    TabSetInfo tabSet = canvas.getChildren(TabSetInfo.class).get(0);
    // check bounds
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    Rectangle tabSetBounds = tabSet.getModelBounds();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(insets.left,
            insets.right,
            tabSetBounds.width - insets.getWidth(),
            tabSetBounds.height - barThickness - insets.getHeight()));
  }

  public void test_tabBounds_right() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.setTabBarPosition(Side.RIGHT);",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    canvas.addChild(tabSet);",
            "    tabSet.moveTo(30, 20);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    TabSetInfo tabSet = canvas.getChildren(TabSetInfo.class).get(0);
    // check bounds
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    Rectangle tabSetBounds = tabSet.getModelBounds();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(insets.left, insets.right, tabSetBounds.width
            - barThickness
            - insets.getWidth(), tabSetBounds.height - insets.getHeight()));
  }

  public void test_tabBounds_button() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    TabSet tabSet = new TabSet();",
            "    Tab tab = new Tab('Tab');",
            "    tab.setPane(new Button('Button'));",
            "    tabSet.addTab(tab);",
            "    canvas.addChild(tabSet);",
            "    tabSet.moveTo(30, 20);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    TabSetInfo tabSet = canvas.getChildren(TabSetInfo.class).get(0);
    // check bounds
    Rectangle tabSetBounds = tabSet.getModelBounds();
    Insets insets = tabSet.getTabInsets();
    int barThickness = tabSet.getTabBarThickness();
    TabInfo selectedTab = tabSet.getSelectedTab();
    assertThat(selectedTab.getModelBounds()).isEqualTo(
        new Rectangle(insets.left, barThickness + insets.right, tabSetBounds.width
            - insets.getWidth(),//Canvas_Test.BUTTON_WIDTH,
            tabSetBounds.height - barThickness - insets.getHeight()//Canvas_Test.BUTTON_HEIGHT
        ));
  }
}