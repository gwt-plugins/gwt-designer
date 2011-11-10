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
import com.google.gdt.eclipse.designer.model.widgets.panels.LazyPanelInfo;

/**
 * Test for {@link LazyPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class LazyPanelTest extends GwtModelTest {
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
    LazyPanelInfo panel =
        parseJavaInfo(
            "public class Test extends LazyPanel {",
            "  protected Widget createWidget() {",
            "    return new Button();",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.LazyPanel} {this} {}",
        "  {new: com.google.gwt.user.client.ui.Button} {empty} {/new Button()/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_setSize_forContent() throws Exception {
    LazyPanelInfo panel =
        parseJavaInfo(
            "public class Test extends LazyPanel {",
            "  public Test() {",
            "  }",
            "  protected Widget createWidget() {",
            "    return new Button();",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidget();
    //
    button.getSizeSupport().setSize("200px", "100px");
    assertEditor(
        "public class Test extends LazyPanel {",
        "  public Test() {",
        "  }",
        "  protected Widget createWidget() {",
        "    Button button = new Button();",
        "    button.setSize('200px', '100px');",
        "    return button;",
        "  }",
        "}");
  }
}