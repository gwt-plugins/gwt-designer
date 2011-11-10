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

import com.google.gdt.eclipse.designer.gwtext.GwtExtGefTest;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import java.text.MessageFormat;

/**
 * Tests for "flow container" layouts of GWT-Ext.
 * 
 * @author scheglov_ke
 */
public class FlowLayoutsGefTest extends GwtExtGefTest {
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
   * Test that known GWT panels use <code>"flowContainer"</code> parameter, so all of them will have
   * appropriate {@link LayoutEditPolicy}.
   */
  public void test_flowContainer() throws Exception {
    openPanel(
        "// filler filler filler",
        "public class Test extends Panel {",
        "  public Test() {",
        "  }",
        "}");
    //
    check_is_flowContainer("com.gwtext.client.widgets.layout.ColumnLayout");
    check_is_flowContainer("com.gwtext.client.widgets.layout.RowLayout");
    check_is_flowContainer("com.gwtext.client.widgets.layout.HorizontalLayout");
    check_is_flowContainer("com.gwtext.client.widgets.layout.VerticalLayout");
  }

  /**
   * Checks that GWT-Ext component has <code>"flowContainer"</code> parameter.
   */
  private void check_is_flowContainer(String className) throws Exception {
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, className);
    assertEquals("true", description.getParameter("flowContainer"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_DefaultLayout() throws Exception {
    check_SomeLayout0("setDisabled(false);", true);
  }

  public void test_ColumnLayout() throws Exception {
    check_SomeLayout("new ColumnLayout()", true);
  }

  public void test_RowLayout() throws Exception {
    check_SomeLayout("new RowLayout()", false);
  }

  public void test_HorizontalLayout() throws Exception {
    check_SomeLayout("new HorizontalLayout(10)", true);
  }

  public void test_VerticalLayout() throws Exception {
    check_SomeLayout("new VerticalLayout()", false);
  }

  private void check_SomeLayout(String layoutSource, boolean horizontal) throws Exception {
    String setLayoutSource = MessageFormat.format("setLayout({0});", layoutSource);
    check_SomeLayout0(setLayoutSource, horizontal);
  }

  private void check_SomeLayout0(String setLayoutSource, boolean horizontal) throws Exception {
    PanelInfo panel =
        openPanel("public class Test extends Panel {", "  public Test() {", "    "
            + setLayoutSource, "  }", "}");
    // CREATE "label_1"
    GraphicalEditPart labelPart_1;
    {
      JavaInfo label_1 = loadCreationTool("com.gwtext.client.widgets.form.Label");
      canvas.moveTo(panel, 0, 0);
      canvas.assertEmptyFlowContainerFeedback(panel, horizontal);
      // click, so finish creation
      canvas.click();
      canvas.assertNoFeedbacks();
      label_1.getPropertyByTitle("text").setValue("111111111");
      label_1.getVariableSupport().setName("label_1");
      //
      labelPart_1 = canvas.getEditPart(label_1);
      isExistingAndSelectedPart(labelPart_1);
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    " + setLayoutSource,
        "    {",
        "      Label label_1 = new Label('111111111');",
        "      add(label_1);",
        "    }",
        "  }",
        "}");
    // CREATE "label_2" before "label_1"
    GraphicalEditPart labelPart_2;
    {
      JavaInfo label_2 = loadCreationTool("com.gwtext.client.widgets.form.Label");
      canvas.moveTo(labelPart_1);
      assertBeforeFeedback(labelPart_1, horizontal);
      // click, so finish creation
      canvas.click();
      canvas.assertNoFeedbacks();
      label_2.getPropertyByTitle("text").setValue("222222222");
      label_2.getVariableSupport().setName("label_2");
      //
      labelPart_2 = canvas.getEditPart(label_2);
      isExistingAndSelectedPart(labelPart_2);
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    " + setLayoutSource,
        "    {",
        "      Label label_2 = new Label('222222222');",
        "      add(label_2);",
        "    }",
        "    {",
        "      Label label_1 = new Label('111111111');",
        "      add(label_1);",
        "    }",
        "  }",
        "}");
    // MOVE "label_1" before "label_2"
    {
      // drag "button"
      canvas.beginDrag(labelPart_1, 5, 5).dragTo(labelPart_2);
      assertBeforeFeedback(labelPart_2, horizontal);
      canvas.assertCommandNotNull();
      // done drag, so finish MOVE
      canvas.endDrag();
      canvas.assertNoFeedbacks();
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    " + setLayoutSource,
        "    {",
        "      Label label_1 = new Label('111111111');",
        "      add(label_1);",
        "    }",
        "    {",
        "      Label label_2 = new Label('222222222');",
        "      add(label_2);",
        "    }",
        "  }",
        "}");
  }

  private void assertBeforeFeedback(GraphicalEditPart targetPart, boolean horizontal) {
    if (horizontal) {
      canvas.assertFeedbacks(GraphicalRobot.getLinePredicate(targetPart, IPositionConstants.LEFT));
    } else {
      canvas.assertFeedbacks(GraphicalRobot.getLinePredicate(targetPart, IPositionConstants.TOP));
    }
  }

  private static void isExistingAndSelectedPart(GraphicalEditPart newButtonPart) {
    assertNotNull(newButtonPart);
    assertEquals(EditPart.SELECTED_PRIMARY, newButtonPart.getSelected());
  }
}
