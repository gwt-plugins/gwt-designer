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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for {@link AnchorLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AnchorLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set AnchorLayout
    AnchorLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.AnchorLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new AnchorLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AnchorLayout} {empty} {/setLayout(new AnchorLayout())/}");
    assertSame(layout, container.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AnchorDataInfo#getAnchor()} and {@link AnchorDataInfo#setAnchor(String)}.
   */
  public void test_anchor() throws Exception {
    AnchorDataInfo anchorData = parse_anchorSpec("50% 100");
    // current value
    assertEquals("50% 100", anchorData.getAnchor());
    // set new value
    anchorData.setAnchor("100 30%");
    assert_anchorSpec("100 30%");
    assertEquals("100 30%", anchorData.getAnchor());
    // set default value
    anchorData.getPropertyByTitle("anchorSpec").setValue(Property.UNKNOWN_VALUE);
    assert_anchorSource("");
  }

  /**
   * Test for {@link AnchorDataInfo#getAnchorWidth()} and
   * {@link AnchorDataInfo#setAnchorWidth(Object)}.
   */
  public void test_anchorWidth_null() throws Exception {
    Property property = parse_anchorSpec_getPropertyWidth("null");
    assertEquals(null, property.getValue());
  }

  /**
   * Test for {@link AnchorDataInfo#getAnchorWidth()} and
   * {@link AnchorDataInfo#setAnchorWidth(Object)}.
   */
  public void test_anchorWidth_noHeight() throws Exception {
    Property property = parse_anchorSpec_getPropertyWidth("50%");
    // current value
    assertEquals("50%", property.getValue());
    // set new value
    property.setValue("70%");
    assert_anchorSpec("70%");
    assertEquals("70%", property.getValue());
    // set new value
    property.setValue("-50");
    assert_anchorSpec("-50");
    assertEquals("-50", property.getValue());
    // set default value
    property.setValue(Property.UNKNOWN_VALUE);
    assert_anchorSource("");
  }

  /**
   * Test for {@link AnchorDataInfo#getAnchorWidth()} and
   * {@link AnchorDataInfo#setAnchorWidth(Object)}.
   */
  public void test_anchorWidth_withHeight() throws Exception {
    Property property = parse_anchorSpec_getPropertyWidth("50% 100");
    // current value
    assertEquals("50%", property.getValue());
    // set new value
    property.setValue("70%");
    assert_anchorSpec("70% 100");
    assertEquals("70%", property.getValue());
    // set new value
    property.setValue("-50");
    assert_anchorSpec("-50 100");
    assertEquals("-50", property.getValue());
    // set default value
    property.setValue(Property.UNKNOWN_VALUE);
    assert_anchorSpec("0% 100");
  }

  /**
   * Test for {@link AnchorDataInfo#getAnchorHeight()} and
   * {@link AnchorDataInfo#setAnchorHeight(Object)}.
   */
  public void test_anchorHeight_null() throws Exception {
    Property property = parse_anchorSpec_getPropertyHeight("null");
    assertEquals(null, property.getValue());
    // set new value
    property.setValue("70%");
    assert_anchorSpec("0% 70%");
    assertEquals("70%", property.getValue());
  }

  /**
   * Test for {@link AnchorDataInfo#getAnchorHeight()} and
   * {@link AnchorDataInfo#setAnchorHeight(Object)}.
   */
  public void test_anchorHeight() throws Exception {
    Property property = parse_anchorSpec_getPropertyHeight("100 50%");
    assertEquals("50%", property.getValue());
    // set new value
    property.setValue("70%");
    assert_anchorSpec("100 70%");
    assertEquals("70%", property.getValue());
    // set new value
    property.setValue("-50");
    assert_anchorSpec("100 -50");
    assertEquals("-50", property.getValue());
    // set default value
    property.setValue(Property.UNKNOWN_VALUE);
    assert_anchorSpec("100");
  }

  /**
   * Parses <code>AnchorData</code> with given "spec".
   */
  private Property parse_anchorSpec_getPropertyWidth(String anchorSpec) throws Exception {
    AnchorDataInfo anchorData = parse_anchorSpec(anchorSpec);
    return anchorData.getPropertyByTitle("anchorWidth");
  }

  /**
   * Parses <code>AnchorData</code> with given "spec".
   */
  private Property parse_anchorSpec_getPropertyHeight(String anchorSpec) throws Exception {
    AnchorDataInfo anchorData = parse_anchorSpec(anchorSpec);
    return anchorData.getPropertyByTitle("anchorHeight");
  }

  /**
   * Parses <code>AnchorData</code> with given "spec".
   */
  private AnchorDataInfo parse_anchorSpec(String anchorSpec) throws Exception {
    if (!"null".equals(anchorSpec)) {
      anchorSpec = "'" + anchorSpec + "'";
    }
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new AnchorData(" + anchorSpec + "));",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
    WidgetInfo button = container.getWidgets().get(0);
    AnchorDataInfo anchorData = AnchorLayoutInfo.getAnchorData(button);
    return anchorData;
  }

  /**
   * Asserts that <code>AnchorData</code> has given "spec".
   */
  private void assert_anchorSpec(String expectedSpec) throws Exception {
    String expectedAnchorSource = ", new AnchorData('" + expectedSpec + "')";
    assert_anchorSource(expectedAnchorSource);
  }

  /**
   * Asserts that <code>AnchorData</code> has given source.
   */
  private void assert_anchorSource(String expectedAnchorSource) throws Exception {
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button" + expectedAnchorSource + ");",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AnchorLayout());",
            "  }",
            "}");
    container.refresh();
    AnchorLayoutInfo layout = (AnchorLayoutInfo) container.getLayout();
    FlowContainer flowContainer = new FlowContainerFactory(layout, false).get().get(0);
    // add new Button
    ComponentInfo newButton = createButton();
    assertTrue(flowContainer.validateComponent(newButton));
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AnchorLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new AnchorData('100%'));",
        "    }",
        "  }",
        "}");
  }
}