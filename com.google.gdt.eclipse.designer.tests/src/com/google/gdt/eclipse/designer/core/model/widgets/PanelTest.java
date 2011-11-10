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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.panels.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link PanelInfo}.
 * 
 * @author scheglov_ke
 */
public class PanelTest extends GwtModelTest {
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
   * Test for {@link PanelInfo#shouldDrawDotsBorder()}.<br>
   * No border, so show dots.
   */
  public void test_shouldDrawDotsBorder_showDots() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    //
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    assertTrue(panel.shouldDrawDotsBorder());
  }

  /**
   * Test for {@link PanelInfo#shouldDrawDotsBorder()}.<br>
   * Has CSS border, so don't show dots.
   */
  public void test_shouldDrawDotsBorder_hasBorder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/public/Module.css",
        getSourceDQ(".my-Panel {", "  border: 1px solid red;", "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "    panel.setStyleName('my-Panel');",
            "  }",
            "}");
    frame.refresh();
    //
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    assertFalse(panel.shouldDrawDotsBorder());
  }

  public void test_extendPanel() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // bounds
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.width).isEqualTo(450);
    assertThat(bounds.height).isEqualTo(300);
  }

  /**
   * We use this test to ensure that MVEL works correctly with multiple types and
   * <code>setSize()</code>.
   * <p>
   * http://jira.codehaus.org/browse/MVEL-151
   */
  public void test_extendPanel_MVEL151() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // bounds
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.width).isEqualTo(450);
    assertThat(bounds.height).isEqualTo(300);
  }

  /**
   * If it happens that Panel is not connected to RootPanel or Composite, would be good still render
   * Panel. So, we need to have top bounds script.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41093
   */
  public void test_renderPanel() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    VerticalPanel panel = new VerticalPanel();",
            "    panel.add(new Button());",
            "  }",
            "}");
    panel.refresh();
    //
    Rectangle bounds = panel.getBounds();
    assertEquals(450, bounds.width);
    assertEquals(300, bounds.height);
  }

  /**
   * We should not intercept method <code>HasWidgets.iterator()</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?45034
   */
  public void test_dontIntercept_iterator() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "  }",
            "  public java.util.Iterator iterator() {",
            "    return this.getChildren().iterator();",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }
}