/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.core.model.widgets.generic;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;

import org.eclipse.jface.action.IAction;

/**
 * Tests for "flow container" support.
 * 
 * @author scheglov_ke
 */
public class FlowContainerTest extends GwtGefTest {
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
    openFrame(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    //
    check_is_flowContainer("com.google.gwt.user.client.ui.FlowPanel", true);
    check_is_flowContainer("com.google.gwt.user.client.ui.HorizontalPanel", true);
    check_is_flowContainer("com.google.gwt.user.client.ui.VerticalPanel", false);
    check_is_flowContainer("com.google.gwt.user.client.ui.DeckPanel", true);
  }

  /**
   * Checks that GWT component has <code>"flowContainer"</code> parameter.
   */
  private void check_is_flowContainer(String className, boolean horizontal) throws Exception {
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, className);
    assertEquals("true", description.getParameter("flowContainer"));
    assertEquals(
        horizontal,
        Boolean.parseBoolean(description.getParameter("flowContainer.horizontal")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlowPanel panel = new FlowPanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "      {",
            "        Button existingButton = new Button();",
            "        panel.add(existingButton);",
            "      }",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo existingButton = panel.getChildren(WidgetInfo.class).get(0);
    // begin creating Button
    JavaInfo newButton = loadCreationTool("com.google.gwt.user.client.ui.Button");
    // move on "panel": feedback appears, command not null
    canvas.moveTo(existingButton, 0, 0);
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FlowPanel panel = new FlowPanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button button = new Button('New button');",
        "        panel.add(button);",
        "      }",
        "      {",
        "        Button existingButton = new Button();",
        "        panel.add(existingButton);",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(newButton);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    RootPanelInfo frame =
        openFrame(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlowPanel panel = new FlowPanel();",
            "      rootPanel.add(panel, 10, 10);",
            "      panel.setPixelSize(200, 200);",
            "      {",
            "        Button existingButton = new Button();",
            "        panel.add(existingButton);",
            "      }",
            "    }",
            "    {",
            "      Button rootButton = new Button('A');",
            "      rootPanel.add(rootButton, 10, 250);",
            "    }",
            "  }",
            "}");
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    WidgetInfo existingButton = panel.getChildren(WidgetInfo.class).get(0);
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
    // move on "panel": feedback appears, command not null
    canvas.moveTo(existingButton, 0, 0);
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FlowPanel panel = new FlowPanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button button = new Button('A');",
        "        panel.add(button);",
        "      }",
        "      {",
        "        Button existingButton = new Button();",
        "        panel.add(existingButton);",
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    openFrame(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FlowPanel panel = new FlowPanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button existingButton = new Button('Existing Button');",
        "        panel.add(existingButton);",
        "      }",
        "    }",
        "    {",
        "      Button button = new Button('A');",
        "      rootPanel.add(button, 10, 250);",
        "    }",
        "  }",
        "}");
    WidgetInfo panel = getJavaInfoByName("panel");
    WidgetInfo existingButton = getJavaInfoByName("existingButton");
    WidgetInfo button = getJavaInfoByName("button");
    // drag "button"
    canvas.beginDrag(button);
    canvas.target(panel).in(5, 5).drag();
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
    canvas.assertCommandNotNull();
    // done drag, so finish ADD
    canvas.endDrag();
    canvas.assertNoFeedbacks();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FlowPanel panel = new FlowPanel();",
        "      rootPanel.add(panel, 10, 10);",
        "      panel.setPixelSize(200, 200);",
        "      {",
        "        Button button = new Button('A');",
        "        panel.add(button);",
        "      }",
        "      {",
        "        Button existingButton = new Button('Existing Button');",
        "        panel.add(existingButton);",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(button);
  }
}
