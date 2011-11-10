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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.PaddedPanelInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;

/**
 * Tests for {@link PaddedPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class PaddedPanelTest extends GwtExtModelTest {
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Panel content = new Panel();",
            "      PaddedPanel paddedPanel = new PaddedPanel(content, 10);",
            "      add(paddedPanel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(paddedPanel)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.PaddedPanel} {local-unique: paddedPanel} {/new PaddedPanel(content, 10)/ /add(paddedPanel)/}",
        "    {new: com.gwtext.client.widgets.Panel} {local-unique: content} {/new Panel()/ /new PaddedPanel(content, 10)/}",
        "      {implicit-layout: default} {implicit-layout} {}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    PaddedPanelInfo paddedPanel = (PaddedPanelInfo) panel.getChildrenWidgets().get(0);
    PanelInfo content = paddedPanel.getContent();
    assertFalse(content.canDelete());
    assertTrue(paddedPanel.canDelete());
  }

  public void test_CREATE() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    panel.refresh();
    //
    PaddedPanelInfo paddedPanel =
        (PaddedPanelInfo) createWidget("com.gwtext.client.widgets.PaddedPanel");
    panel.getLayout().command_CREATE(paddedPanel, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      PaddedPanel paddedPanel = new PaddedPanel(new Panel('New Panel'), 10, 10, 10, 10);",
        "      add(paddedPanel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(paddedPanel)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.PaddedPanel} {local-unique: paddedPanel} {/new PaddedPanel(new Panel('New Panel'), 10, 10, 10, 10)/ /add(paddedPanel)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}",
        "    {new: com.gwtext.client.widgets.Panel} {empty} {/new PaddedPanel(new Panel('New Panel'), 10, 10, 10, 10)/}");
    panel.refresh();
    // "paddedPanel" can be deleted
    assertTrue(paddedPanel.canDelete());
    assertFalse(paddedPanel.getContent().canDelete());
    paddedPanel.delete();
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "  }",
        "}");
  }
}