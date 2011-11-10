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
package com.google.gdt.eclipse.designer.core.model.widgets.generic;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;

import org.eclipse.jface.action.IAction;

/**
 * Tests for "simple container" support.
 * 
 * @author scheglov_ke
 */
public class SimpleContainerTest extends GwtGefTest {
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
   * Test that known GWT panels use <code>"simpleContainer"</code> parameter, so all of them will
   * have appropriate {@link LayoutEditPolicy}.
   */
  public void test_simpleContainer() throws Exception {
    openFrame(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    // SimplePanel+
    {
      check_is_simpleContainer("com.google.gwt.user.client.ui.SimplePanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DecoratorPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.FocusPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.FormPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.PopupPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DecoratedPopupPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DialogBox");
      check_is_simpleContainer("com.google.gwt.user.client.ui.ScrollPanel");
    }
    // CaptionPanel
    check_is_simpleContainer("com.google.gwt.user.client.ui.CaptionPanel");
  }

  /**
   * Checks that GWT component has <code>"simpleContainer"</code> parameter.
   */
  private void check_is_simpleContainer(String className) throws Exception {
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, className);
    assertEquals("true", description.getParameter("simpleContainer"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_onEmpty() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    // begin creating Button
    JavaInfo newButton = loadCreationTool("com.google.gwt.user.client.ui.Button");
    // move on "panel": feedback appears, command not null
    canvas.moveTo(panel, 0, 0);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button button = new Button('New button');",
        "        panel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_CREATE_onFilled() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "      {",
            "        Button button = new Button();",
            "        panel.setWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    // begin creating Button
    loadCreationTool("com.google.gwt.user.client.ui.Button");
    // move on "panel": has feedback appears, no command
    canvas.moveTo(panel, 0, 0);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE_onEmpty() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      panel.setPixelSize(200, 200);",
            "      rootPanel.add(panel, 10, 10);",
            "    }",
            "    {",
            "      Button rootButton = new Button('A');",
            "      rootPanel.add(rootButton, 10, 250);",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo rootButton = frame.getChildrenWidgets().get(1);
    // copy "rootButton"
    {
      // select "rootButton"
      canvas.select(rootButton);
      // do copy
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
    }
    // move on "panel": has feedback, has command
    canvas.moveTo(panel);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      panel.setPixelSize(200, 200);",
        "      rootPanel.add(panel, 10, 10);",
        "      {",
        "        Button button = new Button('A');",
        "        panel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      Button rootButton = new Button('A');",
        "      rootPanel.add(rootButton, 10, 250);",
        "    }",
        "  }",
        "}");
    // EditPart for "newButton" exists and selected
    {
      WidgetInfo newButton = panel.getChildren(WidgetInfo.class).get(0);
      canvas.assertPrimarySelected(newButton);
    }
  }

  public void test_PASTE_onFilled() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "      {",
            "        Button button = new Button();",
            "        panel.setWidget(button);",
            "      }",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildren(WidgetInfo.class).get(0);
    // copy "button"
    {
      // select "button"
      canvas.select(button);
      // do copy
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
    }
    // move on "panel": has feedback, no command
    canvas.moveTo(panel);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD_onEmpty() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "    }",
            "    {",
            "      Button button = new Button('A');",
            "      rootPanel.add(button, 10, 250);",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo button = frame.getChildrenWidgets().get(1);
    // drag "button"
    canvas.beginDrag(button, 10, 10).dragTo(panel);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // done drag, so finish ADD
    canvas.endDrag();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button button = new Button('A');",
        "        panel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(button);
  }

  public void test_ADD_moreThanOne() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "    }",
            "    {",
            "      Button button_A = new Button('A');",
            "      rootPanel.add(button_A, 10, 250);",
            "    }",
            "    {",
            "      Button button_B = new Button('B');",
            "      rootPanel.add(button_B, 100, 250);",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo button_A = frame.getChildrenWidgets().get(1);
    WidgetInfo button_B = frame.getChildrenWidgets().get(2);
    // try to drag "button_A" and "button_B": more than one component, so ADD is not possible
    canvas.select(button_A, button_B);
    canvas.beginDrag(button_A, 10, 10).dragTo(panel);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNull();
  }
}
