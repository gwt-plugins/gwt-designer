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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.MultiFieldPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Tests for {@link MultiFieldPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class MultiFieldPanelTest extends GwtExtModelTest {
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
    MultiFieldPanelInfo panel =
        parseJavaInfo(
            "public class Test extends MultiFieldPanel {",
            "  public Test() {",
            "    {",
            "      TextField textField_1 = new TextField();",
            "      addToRow(textField_1, 100);",
            "    }",
            "    {",
            "      TextField textField_2 = new TextField();",
            "      addToRow(textField_2, new ColumnLayoutData(0.5));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField_1, 100)/ /addToRow(textField_2, new ColumnLayoutData(0.5))/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField_1} {/new TextField()/ /addToRow(textField_1, 100)/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField_2} {/new TextField()/ /addToRow(textField_2, new ColumnLayoutData(0.5))/}",
        "    {new: com.gwtext.client.widgets.layout.ColumnLayoutData} {empty} {/addToRow(textField_2, new ColumnLayoutData(0.5))/}");
  }

  public void test_propertyWidth() throws Exception {
    MultiFieldPanelInfo panel =
        parseJavaInfo(
            "public class Test extends MultiFieldPanel {",
            "  public Test() {",
            "    {",
            "      TextField textField = new TextField();",
            "      addToRow(textField, 100);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo field = panel.getChildrenWidgets().get(0);
    Property property = field.getPropertyByTitle("Width");
    assertNotNull(property);
    // "int" value
    assertEquals("100", property.getValue());
    // set "50%"
    property.setValue("50%");
    assertEditor(
        "public class Test extends MultiFieldPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      addToRow(textField, new ColumnLayoutData(0.5));",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField, new ColumnLayoutData(0.5))/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField} {/new TextField()/ /addToRow(textField, new ColumnLayoutData(0.5))/}",
        "    {new: com.gwtext.client.widgets.layout.ColumnLayoutData} {empty} {/addToRow(textField, new ColumnLayoutData(0.5))/}");
    assertEquals("50%", property.getValue());
    // set "60%"
    property.setValue("60%");
    assertEditor(
        "public class Test extends MultiFieldPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      addToRow(textField, new ColumnLayoutData(0.6));",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField, new ColumnLayoutData(0.6))/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField} {/new TextField()/ /addToRow(textField, new ColumnLayoutData(0.6))/}",
        "    {new: com.gwtext.client.widgets.layout.ColumnLayoutData} {empty} {/addToRow(textField, new ColumnLayoutData(0.6))/}");
    assertEquals("60%", property.getValue());
    // set "200"
    property.setValue("200");
    assertEditor(
        "public class Test extends MultiFieldPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      addToRow(textField, 200);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField, 200)/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField} {/new TextField()/ /addToRow(textField, 200)/}");
    assertEquals("200", property.getValue());
    // set "300"
    property.setValue("300");
    assertEditor(
        "public class Test extends MultiFieldPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      addToRow(textField, 300);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField, 300)/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField} {/new TextField()/ /addToRow(textField, 300)/}");
    assertEquals("300", property.getValue());
    // set default
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends MultiFieldPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      addToRow(textField, 100);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.form.MultiFieldPanel} {this} {/addToRow(textField, 100)/}",
        "  {new: com.gwtext.client.widgets.form.TextField} {local-unique: textField} {/new TextField()/ /addToRow(textField, 100)/}");
    assertEquals("100", property.getValue());
  }
}