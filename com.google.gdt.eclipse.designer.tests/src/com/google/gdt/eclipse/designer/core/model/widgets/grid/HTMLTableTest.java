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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.GridInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link HTMLTableInfo} and {@link GridInfo}.
 * 
 * @author scheglov_ke
 */
public class HTMLTableTest extends GwtModelTest {
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
   * Even empty <code>Grid</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(85);
    assertThat(panel.getBounds().height).isGreaterThan(20);
    // "formatters" are not visible
    assertThat(panel.getPresentation().getChildrenTree()).isEmpty();
    assertThat(panel.getPresentation().getChildrenGraphical()).isEmpty();
  }

  /**
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43883
   */
  public void test_addMethodShouldNotBeUsed() throws Exception {
    try {
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    Grid grid = new Grid();",
          "    rootPanel.add(grid);",
          "    // Panel.add(Widget) is not implemented",
          "    grid.add(new Button());",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.PANEL_ADD_INVOCATION, de.getCode());
      assertTrue(DesignerExceptionUtils.isFatal(e));
      assertTrue(DesignerExceptionUtils.isWarning(e));
      assertThat(de.getSourcePosition()).isPositive();
    }
  }

  /**
   * If <code>HTMLTable</code> was not created and replaced with placeholder, we should not call its
   * methods and fail because of this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43997
   */
  public void test_ifExceptionInCreation() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Grid {",
            "  public MyPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: test.client.MyPanel} {local-unique: panel} {/new MyPanel()/ /rootPanel.add(panel)/}");
    frame.refresh();
    //
    HTMLTableInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.isPlaceholder());
  }
}