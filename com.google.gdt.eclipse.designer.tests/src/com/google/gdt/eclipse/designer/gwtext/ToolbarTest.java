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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ToolbarInfo;

/**
 * Tests for {@link ToolbarInfo}.
 * 
 * @author scheglov_ke
 */
public class ToolbarTest extends GwtExtModelTest {
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
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      toolbar.addButton(new ToolbarButton('A'));",
            "      toolbar.addButton(new ToolbarMenuButton('B'));",
            "      toolbar.addItem(new ToolbarTextItem('C'));",
            "      toolbar.addField(new TextField('D'));",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(new ToolbarButton('A'))/ /toolbar.addButton(new ToolbarMenuButton('B'))/ /toolbar.addItem(new ToolbarTextItem('C'))/ /toolbar.addField(new TextField('D'))/ /add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarButton} {empty} {/toolbar.addButton(new ToolbarButton('A'))/}",
        "    {new: com.gwtext.client.widgets.ToolbarMenuButton} {empty} {/toolbar.addButton(new ToolbarMenuButton('B'))/}",
        "    {new: com.gwtext.client.widgets.ToolbarTextItem} {empty} {/toolbar.addItem(new ToolbarTextItem('C'))/}",
        "    {new: com.gwtext.client.widgets.form.TextField} {empty} {/toolbar.addField(new TextField('D'))/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    //
    ToolbarInfo toolbar = (ToolbarInfo) panel.getChildrenWidgets().get(0);
    assertFalse(toolbar.shouldDrawDotsBorder());
  }
}