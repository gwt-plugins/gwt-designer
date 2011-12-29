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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelAlignmentSupport;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsoluteLayoutTest extends GxtModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    container.refresh();
    // set AbsoluteLayout
    AbsoluteLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new AbsoluteLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout} {empty} {/setLayout(new AbsoluteLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_setLayout_applyBounds() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200, 100, new Margins(20, 0, 0, 10)));",
            "    }",
            "  }",
            "}");
    container.refresh();
    // set AbsoluteLayout
    AbsoluteLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout");
    container.setLayout(layout);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(10, 20));",
        "      button.setSize('190px', '80px');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button, new AbsoluteData(10, 20))/ /setLayout(new AbsoluteLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout} {empty} {/setLayout(new AbsoluteLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new AbsoluteData(10, 20))/ /button.setSize('190px', '80px')/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.AbsoluteData} {empty} {/add(button, new AbsoluteData(10, 20))/}");
  }

  public void test_deleteLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(10, 20));",
            "      button.setSize('200px', '80px');",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new AbsoluteLayout())/ /add(button, new AbsoluteData(10, 20))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout} {empty} {/setLayout(new AbsoluteLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new AbsoluteData(10, 20))/ /button.setSize('200px', '80px')/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.AbsoluteData} {empty} {/add(button, new AbsoluteData(10, 20))/}");
    container.refresh();
    // delete AbsoluteLayout
    container.getLayout().delete();
    assertEditor(
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
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  /**
   * {@link AbsoluteLayoutInfo} is not flow container.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "  }",
            "}");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    // no "canvas"
    {
      List<FlowContainer> flowContainers = new FlowContainerFactory(layout, true).get();
      assertThat(flowContainers).isEmpty();
    }
    // has "tree"
    {
      List<FlowContainer> flowContainers = new FlowContainerFactory(layout, false).get();
      assertThat(flowContainers).hasSize(1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_BOUNDS
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_BOUNDS_setLocation_materialize() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo button = container.getWidgets().get(0);
    // set location
    layout.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(1, 2));",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setLocation_update() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(10, 20));",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo button = container.getWidgets().get(0);
    // set location
    layout.command_BOUNDS(button, new Point(1, 2), null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(1, 2));",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setLocation_default() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(10, 20));",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo button = container.getWidgets().get(0);
    // set location
    layout.command_BOUNDS(button, new Point(-1, -1), null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setSize() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo button = container.getWidgets().get(0);
    // set size
    layout.command_BOUNDS(button, null, new Dimension(100, 50));
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setSize_removeAnchor() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      AbsoluteData absoluteData = new AbsoluteData(10, 20);",
            "      absoluteData.setAnchorSpec('50% 20%');",
            "      add(button, absoluteData);",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo button = container.getWidgets().get(0);
    // set size
    layout.command_BOUNDS(button, null, new Dimension(100, 50));
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(10, 20));",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_setAnchor_removeSize() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(10, 20));",
            "      button.setSize('200px', '100px');",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    AbsoluteDataInfo absoluteData = AbsoluteLayoutInfo.getAbsoluteData(button);
    // set anchor: width
    absoluteData.setAnchorWidth("50%");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      AbsoluteData absoluteData = new AbsoluteData(10, 20);",
        "      absoluteData.setAnchorSpec('50%');",
        "      add(button, absoluteData);",
        "      button.setHeight('100px');",
        "    }",
        "  }",
        "}");
    // set anchor: height
    absoluteData.setAnchorHeight("20%");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      AbsoluteData absoluteData = new AbsoluteData(10, 20);",
        "      absoluteData.setAnchorSpec('50% 20%');",
        "      add(button, absoluteData);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    WidgetInfo newButton = createButton();
    // add new Button
    layout.command_CREATE(newButton);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // set bounds
    layout.command_BOUNDS(newButton, new Point(10, 20), new Dimension(100, 50));
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(10, 20));",
        "      button.setSize('100px', '50px');",
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
   * Test for {@link AbsoluteLayoutAlignmentSupport}.
   */
  public void test_alignmentActions_LEFT() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, new AbsoluteData(10, 20));",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, new AbsoluteData(30, 100));",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    setupSelectionActions(layout);
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // perform alignment
    alignWidgets("Align left edges", button_1, button_2);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, new AbsoluteData(10, 20));",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, new AbsoluteData(10, 100));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsoluteLayoutAlignmentSupport}.
   */
  public void test_alignmentActions_centerInWindow() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(10, 20));",
            "      button.setSize(100, 30);",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    setupSelectionActions(layout);
    WidgetInfo button = getJavaInfoByName("button");
    // perform alignment
    alignWidgets("Center horizontally in window", button);
    assertEditor(
        "public class Test extends Dialog {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(168, 20));",
        "      button.setSize(100, 30);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Runs alignment action from {@link AbsolutePanelAlignmentSupport}.
   */
  private void alignWidgets(String actionText, WidgetInfo... buttons) throws Exception {
    // prepare selection
    List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>copyOf(buttons);
    // prepare action
    IAction action;
    {
      List<Object> actions = Lists.newArrayList();
      buttons[0].getBroadcastObject().addSelectionActions(selectedObjects, actions);
      action = findAction(actions, actionText);
    }
    // perform alignment
    action.run();
  }

  private void setupSelectionActions(final AbsoluteLayoutInfo layout) {
    layout.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        new AbsoluteLayoutAlignmentSupport(layout).addAlignmentActions(objects, actions);
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
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AbsoluteData(100, 50));",
            "    }",
            "    {",
            "      Text text = new Text();",
            "      add(text, new AbsoluteData(150, 100));",
            "    }",
            "  }",
            "}");
    container.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) container.getLayout();
    ComponentInfo text = container.getChildren(ComponentInfo.class).get(1);
    // Bounds
    layout.command_BOUNDS(text, new Point(5, 5), null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Text text = new Text();",
        "      add(text, new AbsoluteData(5, 5));",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AbsoluteData(100, 50));",
        "    }",
        "  }",
        "}");
  }
}