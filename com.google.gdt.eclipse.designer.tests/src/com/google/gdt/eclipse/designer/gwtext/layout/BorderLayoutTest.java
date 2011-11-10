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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.BorderLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.BorderLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Tests for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class BorderLayoutTest extends GwtExtModelTest {
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new BorderLayoutData(RegionPosition.CENTER));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new BorderLayout())/ /add(label, new BorderLayoutData(RegionPosition.CENTER))/}",
        "  {new: com.gwtext.client.widgets.layout.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label, new BorderLayoutData(RegionPosition.CENTER))/}",
        "    {new: com.gwtext.client.widgets.layout.BorderLayoutData} {empty} {/add(label, new BorderLayoutData(RegionPosition.CENTER))/}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    BorderLayoutDataInfo layoutData = BorderLayoutInfo.getBorderData(label);
    assertNotNull(layoutData);
    // check position
    assertEquals("center", layoutData.getPosition());
  }

  /**
   * We should be able to parse <code>BorderLayout</code> without center widget. We create
   * artificial <code>Panel</code> at center.
   */
  public void test_parseEmpty() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new BorderLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.BorderLayout} {empty} {/setLayout(new BorderLayout())/}");
    // at refresh() we also should add fix
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Empty <code>Panel</code> with <code>BorderLayout</code> added to <code>RootPanel</code>.
   */
  public void test_parseEmpty2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Panel panel = new Panel();",
            "    panel.setLayout(new BorderLayout());",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel()/ /panel.setLayout(new BorderLayout())/ /rootPanel.add(panel)/}",
        "    {new: com.gwtext.client.widgets.layout.BorderLayout} {empty} {/panel.setLayout(new BorderLayout())/}");
    // at refresh() we also should add fix
    frame.refresh();
    assertNoErrors(frame);
  }

  public void test_setLayout() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    {",
            "      Label label_1 = new Label();",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2);",
            "    }",
            "  }",
            "}");
    {
      LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.BorderLayout");
      panel.setLayout(layout);
    }
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(label_1, new BorderLayoutData(RegionPosition.CENTER))/ /add(label_2, new BorderLayoutData(RegionPosition.CENTER))/ /setLayout(new BorderLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label_1} {/new Label()/ /add(label_1, new BorderLayoutData(RegionPosition.CENTER))/}",
        "    {new: com.gwtext.client.widgets.layout.BorderLayoutData} {empty} {/add(label_1, new BorderLayoutData(RegionPosition.CENTER))/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label_2} {/new Label()/ /add(label_2, new BorderLayoutData(RegionPosition.CENTER))/}",
        "    {new: com.gwtext.client.widgets.layout.BorderLayoutData} {empty} {/add(label_2, new BorderLayoutData(RegionPosition.CENTER))/}");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      Label label_1 = new Label();",
        "      add(label_1, new BorderLayoutData(RegionPosition.CENTER));",
        "    }",
        "    {",
        "      Label label_2 = new Label();",
        "      add(label_2, new BorderLayoutData(RegionPosition.CENTER));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setPosition(String)}.
   */
  public void test_setPosition() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new BorderLayoutData(RegionPosition.CENTER));",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2, new BorderLayoutData(RegionPosition.NORTH));",
            "    }",
            "  }",
            "}");
    WidgetInfo label_2 = panel.getChildrenWidgets().get(1);
    BorderLayoutDataInfo layoutData = BorderLayoutInfo.getBorderData(label_2);
    assertNotNull(layoutData);
    // check position
    assertEquals("north", layoutData.getPosition());
    layoutData.setPosition("west");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new BorderLayoutData(RegionPosition.CENTER));",
        "    }",
        "    {",
        "      Label label_2 = new Label();",
        "      add(label_2, new BorderLayoutData(RegionPosition.WEST));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link BorderLayoutInfo#getWidget(String)}.
   */
  public void test_getWidget() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new BorderLayoutData(RegionPosition.CENTER));",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2, new BorderLayoutData(RegionPosition.NORTH));",
            "    }",
            "  }",
            "}");
    BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
    WidgetInfo label_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo label_2 = panel.getChildrenWidgets().get(1);
    //
    assertSame(label_2, layout.getWidget("north"));
    assertSame(null, layout.getWidget("south"));
    assertSame(null, layout.getWidget("west"));
    assertSame(null, layout.getWidget("east"));
    assertSame(label_1, layout.getWidget("center"));
  }
}