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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.BoxComponentInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Test for {@link BoxComponentInfo}.
 * 
 * @author scheglov_ke
 */
public class BoxComponentTest extends GwtExtModelTest {
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
   * During parsing <code>boxComponent.setEl(widget.getElement())</code> builds parent/child
   * association.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42699
   */
  public void test_parse() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.HTML;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      BoxComponent boxComponent = new BoxComponent();",
            "      boxComponent.setEl(new HTML().getElement());",
            "      rootPanel.add(boxComponent);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(boxComponent)/}",
        "  {new: com.gwtext.client.widgets.BoxComponent} {local-unique: boxComponent} {/new BoxComponent()/ /boxComponent.setEl(new HTML().getElement())/ /rootPanel.add(boxComponent)/}",
        "    {new: com.google.gwt.user.client.ui.HTML} {empty} {/new HTML().getElement()/}");
    //
    frame.refresh();
    assertNoErrors(frame);
  }

  /**
   * When drop new <code>BoxComponent</code>, we should use <code>setEl</code> to set some element.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42699
   */
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.HTML;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // do create
    BoxComponentInfo newComponent = createJavaInfo("com.gwtext.client.widgets.BoxComponent");
    frame.command_CREATE2(newComponent, null);
    assertEditor(
        "import com.google.gwt.user.client.ui.HTML;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      BoxComponent boxComponent = new BoxComponent();",
        "      boxComponent.setEl(new HTML('New BoxComponent', true).getElement());",
        "      rootPanel.add(boxComponent);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(boxComponent)/}",
        "  {new: com.gwtext.client.widgets.BoxComponent} {local-unique: boxComponent} {/new BoxComponent()/ /rootPanel.add(boxComponent)/ /boxComponent.setEl(new HTML('New BoxComponent', true).getElement())/}",
        "    {new: com.google.gwt.user.client.ui.HTML} {empty} {/new HTML('New BoxComponent', true).getElement()/}");
    // refresh
    frame.refresh();
    assertNoErrors(frame);
  }
}