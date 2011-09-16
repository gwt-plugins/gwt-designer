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

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;

/**
 * Test for {@link CenterLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class CenterLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set CenterLayout
    CenterLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.CenterLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CenterLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new CenterLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.CenterLayout} {empty} {/setLayout(new CenterLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_command_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new CenterLayout());",
            "  }",
            "}");
    container.refresh();
    CenterLayoutInfo layout = (CenterLayoutInfo) container.getLayout();
    //
    ComponentInfo newButton = createButton();
    layout.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CenterLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new CenterLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.CenterLayout} {empty} {/setLayout(new CenterLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}");
  }

  /**
   * Test for simple container support.
   */
  public void test_simpleContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new CenterLayout());",
            "  }",
            "}");
    container.refresh();
    CenterLayoutInfo layout = (CenterLayoutInfo) container.getLayout();
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    // empty initially
    assertTrue(simpleContainer.isEmpty());
    // add new Button
    ComponentInfo newButton = createButton();
    assertTrue(simpleContainer.validateComponent(newButton));
    simpleContainer.command_CREATE(newButton);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CenterLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // has child
    assertFalse(simpleContainer.isEmpty());
  }
}