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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

/**
 * Test for <code>com.extjs.gxt.ui.client.widget.form.RadioGroup</code>.
 * 
 * @author scheglov_ke
 */
public class RadioGroupTest extends GxtModelTest {
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
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      RadioGroup group = new RadioGroup();",
        "      {",
        "        Radio radio = new Radio();",
        "        group.add(radio);",
        "      }",
        "      add(group);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(group)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.RadioGroup} {local-unique: group} {/new RadioGroup()/ /group.add(radio)/ /add(group)/}",
        "    {new: com.extjs.gxt.ui.client.widget.form.Radio} {local-unique: radio} {/new Radio()/ /group.add(radio)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}");
  }

  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      RadioGroup group = new RadioGroup();",
            "      add(group);",
            "    }",
            "  }",
            "}");
    container.refresh();
    //
    MultiFieldInfo group = getJavaInfoByName("group");
    FlowContainer flowContainer = new FlowContainerFactory(group, true).get().get(0);
    // don't accept generic Component
    {
      ComponentInfo newComponent = createJavaInfo("com.extjs.gxt.ui.client.widget.button.Button");
      assertFalse(flowContainer.validateComponent(newComponent));
    }
    // accept Radio
    ComponentInfo newComponent = createJavaInfo("com.extjs.gxt.ui.client.widget.form.Radio");
    assertTrue(flowContainer.validateComponent(newComponent));
    flowContainer.command_CREATE(newComponent, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      RadioGroup group = new RadioGroup();",
        "      {",
        "        Radio radio = new Radio();",
        "        group.add(radio);",
        "        radio.setBoxLabel('New Radio');",
        "        radio.setHideLabel(true);",
        "      }",
        "      add(group);",
        "    }",
        "  }",
        "}");
  }
}