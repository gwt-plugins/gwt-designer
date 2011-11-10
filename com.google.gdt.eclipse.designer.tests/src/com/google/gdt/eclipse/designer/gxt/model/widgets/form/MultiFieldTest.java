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
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link MultiFieldInfo}.
 * 
 * @author scheglov_ke
 */
public class MultiFieldTest extends GxtModelTest {
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
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      {",
            "        LabelField labelField = new LabelField();",
            "        multiField.add(labelField);",
            "      }",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(multiField)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.MultiField} {local-unique: multiField} {/new MultiField()/ /multiField.add(labelField)/ /add(multiField)/}",
        "    {new: com.extjs.gxt.ui.client.widget.form.LabelField} {local-unique: labelField} {/new LabelField()/ /multiField.add(labelField)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}");
    // 
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * Empty <code>MultiField</code> should have some reasonable size.
   */
  public void test_empty() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MultiFieldInfo multiField = (MultiFieldInfo) container.getWidgets().get(0);
    Rectangle bounds = multiField.getBounds();
    assertThat(bounds.height).isGreaterThan(10);
  }

  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MultiFieldInfo multiField = (MultiFieldInfo) container.getWidgets().get(0);
    FlowContainer flowContainer = new FlowContainerFactory(multiField, false).get().get(0);
    // add new LabelField
    FieldInfo newField = createJavaInfo("com.extjs.gxt.ui.client.widget.form.LabelField", "empty");
    assertTrue(flowContainer.validateComponent(newField));
    flowContainer.command_CREATE(newField, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      MultiField multiField = new MultiField();",
        "      {",
        "        LabelField labelField = new LabelField();",
        "        multiField.add(labelField);",
        "      }",
        "      add(multiField);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(multiField)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.MultiField} {local-unique: multiField} {/new MultiField()/ /add(multiField)/ /multiField.add(labelField)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}",
        "    {new: com.extjs.gxt.ui.client.widget.form.LabelField empty} {local-unique: labelField} {/new LabelField()/ /multiField.add(labelField)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isHorizontal()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MultiFieldInfo#isHorizontal()}.
   */
  public void test_isHorizontal_default() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MultiFieldInfo multiField = (MultiFieldInfo) container.getWidgets().get(0);
    assertTrue(multiField.isHorizontal());
  }

  /**
   * Test for {@link MultiFieldInfo#isHorizontal()}.
   */
  public void test_isHorizontal_true() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      multiField.setOrientation(Orientation.HORIZONTAL);",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MultiFieldInfo multiField = (MultiFieldInfo) container.getWidgets().get(0);
    assertTrue(multiField.isHorizontal());
  }

  /**
   * Test for {@link MultiFieldInfo#isHorizontal()}.
   */
  public void test_isHorizontal_false() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      MultiField multiField = new MultiField();",
            "      multiField.setOrientation(Orientation.VERTICAL);",
            "      add(multiField);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MultiFieldInfo multiField = (MultiFieldInfo) container.getWidgets().get(0);
    assertFalse(multiField.isHorizontal());
  }
}