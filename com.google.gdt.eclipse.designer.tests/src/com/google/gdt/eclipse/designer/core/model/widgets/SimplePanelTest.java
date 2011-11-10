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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.SimplePanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link SimplePanelInfo}.
 * 
 * @author scheglov_ke
 */
public class SimplePanelTest extends GwtModelTest {
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
   * Even empty <code>SimplePanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    SimplePanel panel = new SimplePanel();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
    refresh();
    SimplePanelInfo panel = getJavaInfoByName("panel");
    //
    assertNull(panel.getWidget());
    assertThat(panel.getBounds().width).isGreaterThan(150);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link SimplePanelInfo#command_CREATE(WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    SimplePanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends SimplePanel {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    SimpleContainer simpleContainer = getSimpleContainer(panel);
    // no child Widget initially
    assertTrue(simpleContainer.isEmpty());
    // do CREATE
    WidgetInfo newButton = createButton();
    simpleContainer.command_CREATE(newButton);
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends SimplePanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      setWidget(button);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
    assertSame(newButton, panel.getWidget());
    assertFalse(simpleContainer.isEmpty());
  }

  /**
   * Test for {@link SimplePanelInfo#command_ADD(WidgetInfo)}.
   */
  public void test_ADD() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    SimplePanelInfo panel = getJavaInfoByName("panel");
    SimpleContainer simpleContainer = getSimpleContainer(panel);
    // do ADD
    simpleContainer.command_ADD(button);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * When move {@link WidgetInfo} from {@link SimplePanelInfo} we should remove size specification.
   */
  public void test_moveOut() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // do ADD
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button = getJavaInfoByName("button");
        frame.command_MOVE2(button, null);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      SimplePanel panel = new SimplePanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button('Some button');",
            "        panel.setWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // do copy/paste
    {
      SimplePanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      SimplePanel panel = new SimplePanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button('Some button');",
        "        panel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      SimplePanel simplePanel = new SimplePanel();",
        "      rootPanel.add(simplePanel);",
        "      {",
        "        Button button = new Button('Some button');",
        "        simplePanel.setWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return some {@link SimpleContainer} for given {@link JavaInfo}.
   */
  private static SimpleContainer getSimpleContainer(JavaInfo container) {
    List<SimpleContainer> containers = new SimpleContainerFactory(container, false).get();
    assertThat(containers).isNotEmpty();
    return containers.get(0);
  }
}