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

import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AbsoluteLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AbsoluteLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsoluteLayoutTest extends GwtExtModelTest {
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
  public void test_parse() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new AbsoluteLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.AbsoluteLayout} {empty} {/setLayout(new AbsoluteLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.AbsoluteLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AbsoluteLayoutDataInfo layoutData = AbsoluteLayoutInfo.getAbsoluteData(label);
    assertNotNull(layoutData);
  }

  public void test_BOUNDS_location() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    //
    layout.command_BOUNDS(label, new Point(100, 50), null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AbsoluteLayoutData(100, 50));",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_size() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    //
    layout.command_BOUNDS(label, null, new Dimension(100, 40));
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label);",
        "      label.setSize('100px', '40px');",
        "    }",
        "  }",
        "}");
  }

  public void test_setAbsoluteLayout() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
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
    panel.refresh();
    // set AbsoluteLayout
    {
      LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.AbsoluteLayout");
      panel.setLayout(layout);
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Label label_1 = new Label();",
        "      add(label_1, new AbsoluteLayoutData(1, 1));",
        "      label_1.setSize('448px', '149px');",
        "    }",
        "    {",
        "      Label label_2 = new Label();",
        "      add(label_2, new AbsoluteLayoutData(1, 150));",
        "      label_2.setSize('448px', '149px');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteAbsoluteLayout() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Label label_1 = new Label();",
            "      add(label_1, new AbsoluteLayoutData(0, 0));",
            "      label_1.setSize('448px', '149px');",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2, new AbsoluteLayoutData(0, 149));",
            "      label_2.setSize('448px', '149px');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // set RowLayout
    {
      LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.RowLayout");
      panel.setLayout(layout);
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      com.google.gwt.user.client.ui.Button button = new com.google.gwt.user.client.ui.Button();",
            "      add(button, new AbsoluteLayoutData(100, 50));",
            "    }",
            "    {",
            "      com.google.gwt.user.client.ui.Label label = new com.google.gwt.user.client.ui.Label();",
            "      add(label, new AbsoluteLayoutData(100, 200));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    WidgetInfo label = panel.getChildrenWidgets().get(1);
    // Bounds
    layout.command_BOUNDS(label, new Point(5, 5), null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      com.google.gwt.user.client.ui.Label label = new com.google.gwt.user.client.ui.Label();",
        "      add(label, new AbsoluteLayoutData(5, 5));",
        "    }",
        "    {",
        "      com.google.gwt.user.client.ui.Button button = new com.google.gwt.user.client.ui.Button();",
        "      add(button, new AbsoluteLayoutData(100, 50));",
        "    }",
        "  }",
        "}");
  }
}