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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AnchorLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AnchorLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Tests for {@link AnchorLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AnchorLayoutTest extends GwtExtModelTest {
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
  public void test_0() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertNoErrors(panel);
    assertInstanceOf(AnchorLayoutInfo.class, panel.getLayout());
    //
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    assertNotNull(anchorData);
    assertSame(Property.UNKNOWN_VALUE, anchorData.getAnchor());
    // AnchorLayoutData_Info always has "anchor" property
    Property property = anchorData.getPropertyByTitle("anchor");
    assertNotNull(property);
    assertFalse(property.isModified());
    // set value
    property.setValue("-50 30%");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('-50 30%'));",
        "    }",
        "  }",
        "}");
    assertTrue(property.isModified());
    assertEquals("-50 30%", property.getValue());
    assertEquals("-50 30%", anchorData.getAnchor());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setAnchorWidth()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorWidth(String)}.
   */
  public void test_setAnchorWidth_fromUnknown() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorWidth("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('-100'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorWidth(String)}.
   */
  public void test_setAnchorWidth_fromWidthOnly() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData('-50'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorWidth("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('-100'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorWidth(String)}.
   */
  public void test_setAnchorWidth_fromHeightOnly() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData(' 30%'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorWidth("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('-100 30%'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorWidth(String)}.
   */
  public void test_setAnchorWidth_fromWidthAndHeight() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData('-50 30%'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorWidth("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('-100 30%'));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setAnchorWidth()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorHeight(String)}.
   */
  public void test_setAnchorHeight_fromUnknown() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorHeight("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData(' -100'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorHeight(String)}.
   */
  public void test_setAnchorHeight_fromHeightOnly() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData(' -50'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorHeight("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData(' -100'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorHeight(String)}.
   */
  public void test_setAnchorHeight_fromWidthOnly() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData('50%'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorHeight("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('50% -100'));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AnchorLayoutDataInfo#setAnchorHeight(String)}.
   */
  public void test_setAnchorHeight_fromWidthAndHeight() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new AnchorLayoutData('50% -50'));",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    AnchorLayoutDataInfo anchorData = AnchorLayoutInfo.getAnchorData(label);
    // set value
    anchorData.setAnchorHeight("-100");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new AnchorLayoutData('50% -100'));",
        "    }",
        "  }",
        "}");
  }
}