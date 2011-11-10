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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>com.google.gwt.user.client.ui.DisclosurePanel</code> widget.
 * 
 * @author scheglov_ke
 */
public class DisclosurePanelTest extends UiBinderModelTest {
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
   * Even empty <code>DisclosurePanel</code> should have reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DisclosurePanel wbp:name='panel' open='true'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    DisclosurePanelInfo panel = getObjectByName("panel");
    //
    assertNull(panel.getWidget());
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThan(100);
      assertThat(bounds.height).isGreaterThan(50);
    }
  }

  /**
   * Test for "Header" property.
   */
  public void test_headerProperty() throws Exception {
    DisclosurePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DisclosurePanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    Property property = panel.getPropertyByTitle("Header");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    // no value
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // new value
    property.setValue("New header");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel>",
        "    <g:header>New header</g:header>",
        "  </g:DisclosurePanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("New header", property.getValue());
    // update
    property.setValue("Updated header");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel>",
        "    <g:header>Updated header</g:header>",
        "  </g:DisclosurePanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("Updated header", property.getValue());
    // remove
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel/>",
        "</ui:UiBinder>");
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
  }

  public void test_CREATE_this() throws Exception {
    ComplexPanelInfo rootPanel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    DisclosurePanelInfo panel = createObject("com.google.gwt.user.client.ui.DisclosurePanel");
    flowContainer_CREATE(rootPanel, panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DisclosurePanel open='true'>",
        "      <g:header>New DisclosurePanel</g:header>",
        "    </g:DisclosurePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_simpleContainer() throws Exception {
    DisclosurePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DisclosurePanel/>",
            "</ui:UiBinder>");
    refresh();
    // no widget
    assertSame(null, panel.getWidget());
    // add widget
    WidgetInfo newButton = createButton();
    simpleContainer_CREATE(panel, newButton);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel>",
        "    <g:Button width='5cm' height='4cm'/>",
        "  </g:DisclosurePanel>",
        "</ui:UiBinder>");
    assertSame(newButton, panel.getWidget());
  }

  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:DisclosurePanel wbp:name='panel'>",
            "      <g:header>My header</g:header>",
            "      <g:Button width='3cm' height='10mm'/>",
            "    </g:DisclosurePanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    // do copy/paste
    {
      WidgetInfo panel = getObjectByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(flowPanel, copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DisclosurePanel wbp:name='panel'>",
        "      <g:header>My header</g:header>",
        "      <g:Button width='3cm' height='10mm'/>",
        "    </g:DisclosurePanel>",
        "    <g:DisclosurePanel>",
        "      <g:header>My header</g:header>",
        "      <g:Button width='3cm' height='10mm'/>",
        "    </g:DisclosurePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}