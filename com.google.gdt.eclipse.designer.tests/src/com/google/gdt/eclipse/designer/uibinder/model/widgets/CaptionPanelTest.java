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

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>com.google.gwt.user.client.ui.CaptionPanel</code> widget.
 * 
 * @author scheglov_ke
 */
public class CaptionPanelTest extends UiBinderModelTest {
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
  public void test_parseThis() throws Exception {
    CaptionPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:CaptionPanel captionText='My panel'/>",
            "</ui:UiBinder>");
    refresh();
    //
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.width).isEqualTo(450);
    assertThat(bounds.height).isEqualTo(300);
  }

  public void test_parseChild() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:CaptionPanel wbp:name='panel' captionText='My panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    CaptionPanelInfo panel = getObjectByName("panel");
    // bounds
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.width).isEqualTo(450);
    assertThat(bounds.height).isGreaterThan(40);
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
    CaptionPanelInfo panel = createObject("com.google.gwt.user.client.ui.CaptionPanel");
    flowContainer_CREATE(rootPanel, panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:CaptionPanel captionText='New panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_simpleContainer() throws Exception {
    CaptionPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:CaptionPanel/>",
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
        "  <g:CaptionPanel>",
        "    <g:Button width='5cm' height='3cm'/>",
        "  </g:CaptionPanel>",
        "</ui:UiBinder>");
    assertSame(newButton, panel.getWidget());
  }
}