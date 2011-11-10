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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.CheckItemInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.CycleButtonInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link CycleButtonInfo}.
 * 
 * @author sablin_aa
 */
public class CycleButtonTest extends GwtExtModelTest {
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
  public void test_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    {
      WidgetInfo button = createWidget("com.gwtext.client.widgets.CycleButton");
      assertThat(button).isNotNull();
      assertThat(button.getImage()).isNotNull();
    }
  }

  // XXX
  public void test_properties_waitForRendered() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem checkItem = new CheckItem('check item', true);",
            "        button.addItem(checkItem);",
            "      }",
            "      rootPanel.add(button, 5, 5);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    assertEquals(true, ReflectionUtils.invokeMethod(button.getObject(), "isRendered()"));
    // prepare item
    CheckItemInfo item;
    {
      List<CheckItemInfo> items = button.getCheckItems();
      assertThat(items).hasSize(1);
      item = items.get(0);
    }
    // item should be rendered, default values for properties fetched
    assertEquals(true, ReflectionUtils.invokeMethod(item.getObject(), "isRendered()"));
    {
      Property property = item.getPropertyByTitle("disabled");
      assertNotNull(property);
      assertFalse(property.isModified());
      assertEquals(false, property.getValue());
    }
  }

  public void test_invalidCheckingState_noChecked() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem checkItem1 = new CheckItem('check 1', false);",
            "        button.addItem(checkItem1);",
            "      }",
            "      {",
            "        CheckItem checkItem2 = new CheckItem('check 2', false);",
            "        button.addItem(checkItem2);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    List<CheckItemInfo> items = button.getCheckItems();
    assertThat(items).hasSize(2);
    {
      CheckItemInfo item_1 = items.get(0);
      assertThat(item_1.getPropertyByTitle("checked").getValue()).isEqualTo(false);
      assertThat(item_1.isChecked()).isTrue();
    }
    {
      CheckItemInfo item_2 = items.get(1);
      assertThat(item_2.getPropertyByTitle("checked").getValue()).isEqualTo(false);
      assertThat(item_2.isChecked()).isFalse();
    }
  }

  public void test_invalidCheckingState_allChecked() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem checkItem1 = new CheckItem('check 1', true);",
            "        button.addItem(checkItem1);",
            "      }",
            "      {",
            "        CheckItem checkItem2 = new CheckItem('check 2', true);",
            "        button.addItem(checkItem2);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    List<CheckItemInfo> items = button.getCheckItems();
    assertThat(items).hasSize(2);
    {
      CheckItemInfo item_1 = items.get(0);
      assertThat(item_1.getPropertyByTitle("checked").getValue()).isEqualTo(true);
      assertThat(item_1.isChecked()).isTrue();
    }
    {
      CheckItemInfo item_2 = items.get(1);
      assertThat(item_2.getPropertyByTitle("checked").getValue()).isEqualTo(true);
      assertThat(item_2.isChecked()).isFalse();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for creating {@link CycleButtonInfo}.
   */
  public void test_create() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create CycleButton
    CycleButtonInfo button =
        (CycleButtonInfo) createWidget("com.gwtext.client.widgets.CycleButton");
    assertThat(button.getCheckItems()).isEmpty();
    // add CycleButton on RootPanel
    frame.command_CREATE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton cycleButton = new CycleButton();",
        "      cycleButton.setShowText(true);",
        "      {",
        "        CheckItem checkItem = new CheckItem('Default', true);",
        "        cycleButton.addItem(checkItem);",
        "      }",
        "      rootPanel.add(cycleButton);",
        "    }",
        "  }",
        "}");
    // CycleButton already contains one CheckItem
    assertThat(button.getCheckItems()).hasSize(1);
  }

  public void test_remove_item() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem checkItem1 = new CheckItem('check 1', true);",
            "        button.addItem(checkItem1);",
            "      }",
            "      {",
            "        CheckItem checkItem2 = new CheckItem('check 2', false);",
            "        button.addItem(checkItem2);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    List<CheckItemInfo> items = button.getCheckItems();
    assertThat(items).hasSize(2);
    // remove all items
    CheckItemInfo item_1 = items.get(0);
    CheckItemInfo item_2 = items.get(1);
    // delete "item_1", so "item_2" should be checked
    item_1.delete();
    assertThat(button.getCheckItems()).hasSize(1);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem checkItem2 = new CheckItem('check 2', true);",
        "        button.addItem(checkItem2);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    // delete "item_2", default item should be added
    item_2.delete();
    assertThat(button.getCheckItems()).hasSize(1);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem checkItem = new CheckItem('Default', true);",
        "        button.addItem(checkItem);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_asLast() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem checkItem = new CheckItem('check 1', false);",
            "        button.addItem(checkItem);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    assertThat(button.getCheckItems()).hasSize(1);
    // add new item
    CheckItemInfo newItem =
        (CheckItemInfo) createWidget("com.gwtext.client.widgets.menu.CheckItem");
    do_CREATE(button, newItem, null);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem checkItem = new CheckItem('check 1', true);",
        "        button.addItem(checkItem);",
        "      }",
        "      {",
        "        CheckItem checkItem = new CheckItem('New check item', false);",
        "        button.addItem(checkItem);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeExisting() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem item_1 = new CheckItem('check 1', false);",
            "        button.addItem(item_1);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    assertThat(button.getCheckItems()).hasSize(1);
    CheckItemInfo existingItem = button.getCheckItems().get(0);
    // add new item
    CheckItemInfo newItem =
        (CheckItemInfo) createWidget("com.gwtext.client.widgets.menu.CheckItem");
    do_CREATE(button, newItem, existingItem);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem checkItem = new CheckItem('New check item', true);",
        "        button.addItem(checkItem);",
        "      }",
        "      {",
        "        CheckItem item_1 = new CheckItem('check 1', false);",
        "        button.addItem(item_1);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_severalChecked() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem item = new CheckItem('check 1', true);",
            "        button.addItem(item);",
            "      }",
            "      {",
            "        CheckItem item = new CheckItem('check 2', true);",
            "        button.addItem(item);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    assertThat(button.getCheckItems()).hasSize(2);
    // add new item
    CheckItemInfo newItem =
        (CheckItemInfo) createWidget("com.gwtext.client.widgets.menu.CheckItem");
    do_CREATE(button, newItem, null);
    assertThat(button.getCheckItems()).hasSize(3);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem item = new CheckItem('check 1', true);",
        "        button.addItem(item);",
        "      }",
        "      {",
        "        CheckItem item = new CheckItem('check 2', false);",
        "        button.addItem(item);",
        "      }",
        "      {",
        "        CheckItem checkItem = new CheckItem('New check item', false);",
        "        button.addItem(checkItem);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.CycleButton;",
            "import com.gwtext.client.widgets.menu.CheckItem;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CycleButton button = new CycleButton();",
            "      {",
            "        CheckItem item = new CheckItem('check 1', true);",
            "        button.addItem(item);",
            "      }",
            "      {",
            "        CheckItem item = new CheckItem('check 2', false);",
            "        button.addItem(item);",
            "      }",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    CycleButtonInfo button = frame.getChildren(CycleButtonInfo.class).get(0);
    List<CheckItemInfo> items = button.getCheckItems();
    // add new item
    do_MOVE(button, items.get(0), null);
    assertEditor(
        "import com.gwtext.client.widgets.CycleButton;",
        "import com.gwtext.client.widgets.menu.CheckItem;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CycleButton button = new CycleButton();",
        "      {",
        "        CheckItem item = new CheckItem('check 2', false);",
        "        button.addItem(item);",
        "      }",
        "      {",
        "        CheckItem item = new CheckItem('check 1', true);",
        "        button.addItem(item);",
        "      }",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void do_CREATE(CycleButtonInfo button,
      CheckItemInfo newItem,
      CheckItemInfo nextItem) throws Exception {
    getFlowContainer(button).command_CREATE(newItem, nextItem);
  }

  private static void do_MOVE(CycleButtonInfo button, CheckItemInfo item, CheckItemInfo nextItem)
      throws Exception {
    getFlowContainer(button).command_MOVE(item, nextItem);
  }

  private static FlowContainer getFlowContainer(CycleButtonInfo button) {
    return new FlowContainerFactory(button, false).get().get(0);
  }
}