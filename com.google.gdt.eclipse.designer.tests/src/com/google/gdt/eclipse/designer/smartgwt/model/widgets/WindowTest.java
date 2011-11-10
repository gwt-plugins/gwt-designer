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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.smart.model.WindowInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>com.smartgwt.client.widgets.Window</code>.
 * 
 * @author sablin_aa
 */
public class WindowTest extends SmartGwtModelTest {
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
  public void test_flowContainer_CREATE() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Window {",
            "  public Test() {",
            "  }",
            "}");
    window.refresh();
    // prepare FlowContainer
    FlowContainer flowContainer;
    {
      List<FlowContainer> flowContainers = new FlowContainerFactory(window, true).get();
      assertThat(flowContainers).hasSize(1);
      flowContainer = flowContainers.get(0);
      assertTrue(!flowContainer.isHorizontal());
    }
    // do create
    WidgetInfo newButton = createButton();
    assertTrue(flowContainer.validateComponent(newButton));
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Window {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      addItem(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was problem that <code>Window.setAutoCenter(true)</code> caused wrong absolute bounds, so
   * exception during preparing screen shot.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47230
   */
  public void test_ignore_setAutoCenter() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Window {",
            "  public Test() {",
            "    setAutoSize(true);",
            "    setAutoCenter(true);",
            "  }",
            "}");
    window.refresh();
    assertNoErrors(window);
  }
}