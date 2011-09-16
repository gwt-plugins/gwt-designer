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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtGefTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

import org.eclipse.jface.action.IAction;

/**
 * Tests for {@link AbsoluteLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class AbsoluteLayoutGefTest extends GxtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_initialized = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (!m_initialized) {
      ParseFactory.disposeSharedGWTState();
      prepareBox();
      forgetCreatedResources();
      m_initialized = true;
    }
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "  }",
            "}");
    // create Box
    loadCreationBox();
    // use canvas
    canvas.sideMode().create(100, 50);
    canvas.target(container).in(30, 40).move();
    canvas.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new AbsoluteData(30, 40));",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_PASTE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Box boxA = new Box();",
            "      add(boxA, new AbsoluteData(5, 5));",
            "    }",
            "  }",
            "}");
    // copy "boxA"
    {
      // select "boxA"
      ComponentInfo boxA = getJavaInfoByName("boxA");
      canvas.select(boxA);
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
    // move
    canvas.sideMode().create(100, 50);
    canvas.target(container).inX(50).inY(100).move();
    canvas.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box boxA = new Box();",
        "      add(boxA, new AbsoluteData(5, 5));",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box, new AbsoluteData(50, 100));",
        "      box.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Box box = new Box();",
            "      add(box, new AbsoluteData(30, 40));",
            "    }",
            "  }",
            "}");
    ComponentInfo box = getJavaInfoByName("box");
    // move
    canvas.sideMode().beginMove(box);
    canvas.target(container).inX(50).inY(80).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new AbsoluteData(50, 80));",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_ADD() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      LayoutContainer inner = new LayoutContainer();",
            "      inner.setLayout(new AbsoluteLayout());",
            "      {",
            "        Box box = new Box();",
            "        inner.add(box, new AbsoluteData(5, 5));",
            "      }",
            "      add(inner, new AbsoluteData(20, 100));",
            "      inner.setSize(200, 150);",
            "    }",
            "  }",
            "}");
    ComponentInfo box = getJavaInfoByName("box");
    // move
    canvas.sideMode().beginMove(box);
    canvas.target(container).in(50, 20).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      LayoutContainer inner = new LayoutContainer();",
        "      inner.setLayout(new AbsoluteLayout());",
        "      add(inner, new AbsoluteData(20, 100));",
        "      inner.setSize(200, 150);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box, new AbsoluteData(50, 20));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "  }",
            "}");
    // create Box
    ComponentInfo newBox = loadCreationBox();
    // use tree
    tree.moveOn(container);
    tree.assertFeedback_on(container);
    tree.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newBox);
  }

  public void test_tree_PASTE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Box boxA = new Box();",
            "      add(boxA);",
            "    }",
            "  }",
            "}");
    // copy "boxA"
    {
      // select "boxA"
      ComponentInfo boxA = getJavaInfoByName("boxA");
      canvas.select(boxA);
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
    // use tree
    tree.moveOn(container);
    tree.assertFeedback_on(container);
    tree.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box boxA = new Box();",
        "      add(boxA);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box boxA = new Box();",
        "      add(boxA, new AbsoluteData(10, 10));",
        "    }",
        "    {",
        "      Box boxB = new Box();",
        "      add(boxB, new AbsoluteData(10, 100));",
        "    }",
        "  }",
        "}");
    ComponentInfo boxA = getJavaInfoByName("boxA");
    ComponentInfo boxB = getJavaInfoByName("boxB");
    // use tree
    tree.startDrag(boxB);
    tree.dragBefore(boxA);
    tree.assertFeedback_before(boxA);
    tree.endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      Box boxB = new Box();",
        "      add(boxB, new AbsoluteData(10, 100));",
        "    }",
        "    {",
        "      Box boxA = new Box();",
        "      add(boxA, new AbsoluteData(10, 10));",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(boxB);
  }

  public void test_tree_ADD() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      LayoutContainer inner = new LayoutContainer();",
            "      inner.setLayout(new AbsoluteLayout());",
            "      {",
            "        Box box = new Box();",
            "        inner.add(box, new AbsoluteData(5, 5));",
            "      }",
            "      add(inner, new AbsoluteData(20, 100));",
            "      inner.setSize(200, 150);",
            "    }",
            "  }",
            "}");
    ComponentInfo box = getJavaInfoByName("box");
    // use tree
    tree.startDrag(box);
    tree.dragOn(container);
    tree.assertFeedback_on(container);
    tree.endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      LayoutContainer inner = new LayoutContainer();",
        "      inner.setLayout(new AbsoluteLayout());",
        "      add(inner, new AbsoluteData(20, 100));",
        "      inner.setSize(200, 150);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }
}
