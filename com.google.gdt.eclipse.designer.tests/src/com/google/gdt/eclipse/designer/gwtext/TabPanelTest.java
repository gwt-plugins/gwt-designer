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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.TabPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import java.util.List;

/**
 * Tests for {@link TabPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class TabPanelTest extends GwtExtModelTest {
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
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Panel panel = new Panel('A');",
            "      add(panel);",
            "    }",
            "    {",
            "      Panel panel = new Panel('B');",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.TabPanel} {this} {/add(panel)/ /add(panel)/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel('A')/ /add(panel)/}",
        "    {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel('B')/ /add(panel)/}",
        "    {implicit-layout: default} {implicit-layout} {}");
  }

  /**
   * <code>Toolbar</code> with <code>TextField</code> on second page causes problem.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42660
   */
  public void test_invisibleToolbar() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Panel panel = new Panel('A');",
            "      add(panel);",
            "    }",
            "    {",
            "      Panel panel = new Panel('B');",
            "      add(panel);",
            "      {",
            "        Toolbar toolbar = new Toolbar();",
            "        toolbar.addField(new TextField());",
            "        add(toolbar);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Ensure visible
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ensureVisible() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Panel panel_1 = new Panel('A');",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel('B');",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<WidgetInfo> panels = panel.getChildrenWidgets();
    // initially "panel_1" is expanded
    assertActiveIndex(panel, 0);
    // notify about "panel_2"
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "panel_2" is expanded
      assertActiveIndex(panel, 1);
    }
    // second notification about "panel_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertFalse(shouldRefresh);
    }
  }

  private static void assertActiveIndex(TabPanelInfo tabPanel, int expected) throws Exception {
    int actual = tabPanel.getActiveIndex();
    assertEquals(expected, actual);
  }
}