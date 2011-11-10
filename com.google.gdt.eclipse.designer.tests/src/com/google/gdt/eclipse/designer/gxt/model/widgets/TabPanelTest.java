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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.TabPanelInfo.Header;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class TabPanelTest extends GxtModelTest {
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
   * Even empty TabPanel should have reasonable size.
   */
  public void test_parseEmpty() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      TabPanel panel = new TabPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    container.refresh();
    TabPanelInfo panel = (TabPanelInfo) container.getWidgets().get(0);
    // 
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.height).isGreaterThan(40);
  }

  public void test_parse() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      TabItem item_1 = new TabItem();",
            "      add(item_1);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem();",
            "      add(item_2);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.TabPanel} {this} {/add(item_1)/ /add(item_2)/}",
        "  {new: com.extjs.gxt.ui.client.widget.TabItem} {local-unique: item_1} {/new TabItem()/ /add(item_1)/}",
        "    {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.TabItem} {local-unique: item_2} {/new TabItem()/ /add(item_2)/}",
        "    {implicit-layout: default} {implicit-layout} {}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * In <code>TabPanel</code> was not created and replaced with placeholder, we should not call its
   * methods and fail because of this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44903
   */
  public void test_exceptionPanelCreation() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends TabPanel {",
            "  public MyPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MyPanel panel = new MyPanel();",
        "    add(panel);",
        "    {",
        "      TabItem item = new TabItem('foo');",
        "      panel.add(item);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyPanel} {local-unique: panel} {/new MyPanel()/ /add(panel)/ /panel.add(item)/}",
        "    {new: com.extjs.gxt.ui.client.widget.TabItem} {local-unique: item} {/new TabItem('foo')/ /panel.add(item)/}",
        "      {implicit-layout: default} {implicit-layout} {}");
    refresh();
    //
    TabPanelInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.isPlaceholder());
  }

  /**
   * In <code>TabItem</code> was not created and replaced with placeholder, this should not cause
   * problems.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?47032
   */
  public void test_exceptionItemCreation() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyItem.java",
        getTestSource(
            "public class MyItem extends TabItem {",
            "  public MyItem() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      MyItem item = new MyItem();",
        "      panel.add(item);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.TabPanel} {local-unique: panel} {/new TabPanel()/ /add(panel)/ /panel.add(item)/}",
        "    {new: test.client.MyItem} {local-unique: item} {/new MyItem()/ /panel.add(item)/}");
    refresh();
    //
    TabItemInfo item = getJavaInfoByName("item");
    assertTrue(item.isPlaceholder());
    assertInstanceOf("com.extjs.gxt.ui.client.widget.TabItem", item.getObject());
  }

  /**
   * We should render all items, because when we fetch information for not rendered components, it
   * may fail.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43509
   */
  public void test_forceRenderForAllItems() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      TabItem item_1 = new TabItem();",
            "      {",
            "        Button button_1 = new Button();",
            "        item_1.add(button_1);",
            "      }",
            "      add(item_1);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem();",
            "      {",
            "        Button button_2 = new Button();",
            "        item_2.add(button_2);",
            "      }",
            "      add(item_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(button_1.isRendered());
    assertTrue(button_2.isRendered());
  }

  /**
   * We should render all items before visiting items, because sometimes item expects that it
   * already has <code>Element</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44414
   */
  public void test_renderAllItems_beforeVisitingChildren() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      TabItem item_1 = new TabItem();",
            "      add(item_1);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem();",
            "      item_2.setLayout(new TableLayout(1));;",
            "      add(item_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test for {@link TabPanelInfo#getHeaders()}.
   */
  public void test_getHeaders() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      TabItem item_0 = new TabItem();",
            "      add(item_0);",
            "    }",
            "    {",
            "      TabItem item_1 = new TabItem();",
            "      add(item_1);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<TabItemInfo> items = panel.getItems();
    List<Header> headers = panel.getHeaders();
    //
    assertThat(items).hasSize(2);
    assertThat(headers).hasSize(2);
    {
      Header header = headers.get(0);
      assertSame(items.get(0), header.getWidget());
      assertEquals(new Rectangle(1, 2, 22, 8), header.getBounds());
    }
    {
      Header header = headers.get(1);
      assertSame(items.get(1), header.getWidget());
      assertEquals(new Rectangle(23, 2, 22, 8), header.getBounds());
    }
    // initially "item_0" selected
    assertSame(items.get(0), panel.getSelectedItem());
    // use "header_1" to select "item_1"
    headers.get(1).show();
    assertSame(items.get(1), panel.getSelectedItem());
  }

  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    FlowContainer flowContainer = new FlowContainerFactory(panel, false).get().get(0);
    // add new TabItem
    TabItemInfo newItem = createJavaInfo("com.extjs.gxt.ui.client.widget.TabItem");
    assertTrue(flowContainer.validateComponent(newItem));
    flowContainer.command_CREATE(newItem, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      TabItem tabItem = new TabItem('New TabItem');",
        "      add(tabItem);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage selected
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageSelected() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      TabItem item_1 = new TabItem();",
            "      add(item_1);",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem();",
            "      add(item_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<TabItemInfo> items = panel.getItems();
    // initially "item_1" is selected
    assertActiveIndex(panel, 0);
    // notify about "item_2"
    {
      boolean shouldRefresh = notifySelecting(items.get(1));
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "item_2" is expanded
      assertActiveIndex(panel, 1);
    }
    // second notification about "item_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(items.get(1));
      assertFalse(shouldRefresh);
    }
  }

  private static void assertActiveIndex(TabPanelInfo panel, int expectedIndex) throws Exception {
    TabItemInfo actual = panel.getSelectedItem();
    TabItemInfo expected = panel.getItems().get(expectedIndex);
    assertEquals(expected, actual);
  }
}