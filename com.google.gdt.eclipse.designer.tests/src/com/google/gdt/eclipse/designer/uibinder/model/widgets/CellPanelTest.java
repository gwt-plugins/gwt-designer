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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>com.google.gwt.user.client.ui.CellPanel</code> widget and its subclasses.
 * 
 * @author scheglov_ke
 */
public class CellPanelTest extends UiBinderModelTest {
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
  // Cell property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_propertyCell_existingElement() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:HorizontalPanel>",
        "    <g:Cell>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Cell>",
        "  </g:HorizontalPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // get "Cell" property
    {
      Property cellProperty = button.getPropertyByTitle("Cell");
      assertNotNull(cellProperty);
      assertTrue(cellProperty.getCategory().isSystem());
    }
    // "width"
    {
      Property property = PropertyUtils.getByPath(button, "Cell/width");
      // no value initially
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
      // set value
      property.setValue("100px");
      assertXML(
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:HorizontalPanel>",
          "    <g:Cell width='100px'>",
          "      <g:Button wbp:name='button'/>",
          "    </g:Cell>",
          "  </g:HorizontalPanel>",
          "</ui:UiBinder>");
      // has value
      assertTrue(property.isModified());
      assertEquals("100px", property.getValue());
    }
  }

  public void test_propertyCell_existingElement_horizontalAlignment() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:HorizontalPanel>",
        "    <g:Cell>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Cell>",
        "  </g:HorizontalPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // "horizontalAlignment"
    {
      Property property = PropertyUtils.getByPath(button, "Cell/horizontalAlignment");
      // no value initially
      assertFalse(property.isModified());
      assertEquals("ALIGN_LEFT", getPropertyText(property));
      // check items
      {
        addComboPropertyItems(property);
        List<String> items = getComboPropertyItems();
        assertThat(items).containsExactly("ALIGN_LEFT", "ALIGN_CENTER", "ALIGN_RIGHT");
      }
      // set value
      setComboPropertyValue(property, 1);
      assertXML(
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:HorizontalPanel>",
          "    <g:Cell horizontalAlignment='ALIGN_CENTER'>",
          "      <g:Button wbp:name='button'/>",
          "    </g:Cell>",
          "  </g:HorizontalPanel>",
          "</ui:UiBinder>");
      // has value
      assertTrue(property.isModified());
      assertEquals("ALIGN_CENTER", getPropertyText(property));
    }
  }

  public void test_propertyCell_noElement() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:HorizontalPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:HorizontalPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // "width"
    {
      Property property = PropertyUtils.getByPath(button, "Cell/width");
      // no value
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
      // set value, materialize "Cell"
      property.setValue("100px");
      assertXML(
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:HorizontalPanel>",
          "    <g:Cell width='100px'>",
          "      <g:Button wbp:name='button'/>",
          "    </g:Cell>",
          "  </g:HorizontalPanel>",
          "</ui:UiBinder>");
      // has value
      assertTrue(property.isModified());
      assertEquals("100px", property.getValue());
    }
  }

  /**
   * We should remove enclosing "Cell" element if it has no attributes.
   */
  public void test_propertyCell_removeElement() throws Exception {
    XmlObjectInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:HorizontalPanel>",
            "    <g:Cell width='100px'>",
            "      <g:Button/>",
            "    </g:Cell>",
            "  </g:HorizontalPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = (WidgetInfo) panel.getChildren().get(0);
    // "width"
    {
      Property property = PropertyUtils.getByPath(button, "Cell/width");
      // remove value
      property.setValue(Property.UNKNOWN_VALUE);
      assertXML(
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:HorizontalPanel>",
          "    <g:Button/>",
          "  </g:HorizontalPanel>",
          "</ui:UiBinder>");
      // no value
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_HorizontalPanel_isFlowContainer() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:HorizontalPanel/>",
            "</ui:UiBinder>");
    WidgetInfo newButton = createButton();
    // canvas
    {
      FlowContainer flowContainer = getFlowContainer(panel, true);
      flowContainer.validateComponent(newButton);
      assertTrue(flowContainer.isHorizontal());
    }
    // tree
    {
      FlowContainer flowContainer = getFlowContainer(panel, false);
      flowContainer.validateComponent(newButton);
    }
  }

  public void test_VerticalPanel_isFlowContainer() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:VerticalPanel/>",
            "</ui:UiBinder>");
    WidgetInfo newButton = createButton();
    // canvas
    {
      FlowContainer flowContainer = getFlowContainer(panel, true);
      flowContainer.validateComponent(newButton);
      assertFalse(flowContainer.isHorizontal());
    }
    // tree
    {
      FlowContainer flowContainer = getFlowContainer(panel, false);
      flowContainer.validateComponent(newButton);
    }
  }

  private static FlowContainer getFlowContainer(ComplexPanelInfo panel, boolean forCanvas) {
    return new FlowContainerFactory(panel, forCanvas).get().get(0);
  }
}