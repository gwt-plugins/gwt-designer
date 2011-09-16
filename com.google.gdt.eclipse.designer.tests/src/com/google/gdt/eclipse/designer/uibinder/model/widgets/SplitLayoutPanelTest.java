/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;

/**
 * Test for <code>com.google.gwt.user.client.ui.SplitLayoutPanel</code> in UiBinder.
 * 
 * @author scheglov_ke
 */
public class SplitLayoutPanelTest extends UiBinderModelTest {
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
   * Even empty <code>SplitLayoutPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SplitLayoutPanel wbp:name='panel' width='150' height='100'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:SplitLayoutPanel wbp:name='panel' width='150' height='100'>");
    refresh();
    SplitLayoutPanelInfo panel = getObjectByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 150, 100), panel.getBounds());
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  public void test_parse_this() throws Exception {
    SplitLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:SplitLayoutPanel>",
            "    <g:west size='150'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:SplitLayoutPanel>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:SplitLayoutPanel>",
        "  <g:Button wbp:name='button'>");
    refresh();
    // "panel" bounds
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    // "button" bounds
    {
      WidgetInfo button = getObjectByName("button");
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(0, 0, 150, 300), bounds);
    }
  }

  public void test_CREATE_it() throws Exception {
    ComplexPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    SplitLayoutPanelInfo panel = createObject("com.google.gwt.user.client.ui.SplitLayoutPanel");
    frame.command_CREATE2(panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SplitLayoutPanel width='150px' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_propertyUnit() throws Exception {
    SplitLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:SplitLayoutPanel/>",
            "</ui:UiBinder>");
    refresh();
    // Unit property
    Property unitProperty = panel.getPropertyByTitle("Unit");
    assertNull(unitProperty);
  }
}