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

/**
 * Test for size management in {@link PanelInfo}.
 * 
 * @author scheglov_ke
 */
public class SizeManagementTest extends UiBinderModelTest {
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
  // Remove size on MOVE out
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_onChildOut_removeSize__never() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildOut.removeSize'>never</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <t:MyPanel wbp:name='panel'>",
            "      <g:Button wbp:name='button' width='50%' height='100'/>",
            "    </t:MyPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // move "button"
    flowContainer_MOVE(flowPanel, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button' width='50%' height='100'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_onChildOut_removeSize__always() throws Exception {
    prepare_onChildOut_removeSize__always();
    // parse
    ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <t:MyPanel wbp:name='panel'>",
            "      <g:Button wbp:name='button' width='50%' height='100'/>",
            "    </t:MyPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // move "button"
    flowContainer_MOVE(flowPanel, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * We should not do anything when delete {@link WidgetInfo}, this can cause exception.
   */
  public void test_onChildOut_removeSize__always__whenDelete() throws Exception {
    prepare_onChildOut_removeSize__always();
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button' width='50%' height='100'/>",
        "    </t:MyPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // delete "button"
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  private void prepare_onChildOut_removeSize__always() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildOut.removeSize'>always</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set size on CREATE/ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use empty string for size, so don't set size.
   */
  public void test_onChildAdd_setSize__CREATE_noSize() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>null</parameter>",
            "    <parameter name='onChildAdd.setHeight'>null</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyPanel wbp:name='panel'/>",
            "</ui:UiBinder>");
    refresh();
    // create new Button
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(panel, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyPanel wbp:name='panel'>",
        "    <g:Button/>",
        "  </t:MyPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Sets some not constant size on create.
   */
  public void test_onChildAdd_setSize__CREATE_withSize() throws Exception {
    prepare_onChildAdd_setSize();
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyPanel wbp:name='panel'/>",
            "</ui:UiBinder>");
    refresh();
    // create new Button
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(panel, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyPanel wbp:name='panel'>",
        "    <g:Button width='100%' height='75'/>",
        "  </t:MyPanel>",
        "</ui:UiBinder>");
  }

  /**
   * During move size is ignored.
   */
  public void test_onChildAdd_setSize__MOVE() throws Exception {
    prepare_onChildAdd_setSize();
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyPanel wbp:name='panel'>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </t:MyPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // move "button_2"
    flowContainer_MOVE(panel, button_2, button_1);
    panel.command_MOVE2(button_2, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyPanel wbp:name='panel'>",
        "    <g:Button wbp:name='button_2'/>",
        "    <g:Button wbp:name='button_1'/>",
        "  </t:MyPanel>",
        "</ui:UiBinder>");
  }

  public void test_onChildAdd_setSize__ADD() throws Exception {
    prepare_onChildAdd_setSize();
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    ComplexPanelInfo panel = getObjectByName("panel");
    // move "button"
    flowContainer_MOVE(panel, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button' width='100%' height='75'/>",
        "    </t:MyPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  private void prepare_onChildAdd_setSize() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>100%</parameter>",
            "    <parameter name='onChildAdd.setHeight'>75</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyPanel() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
  }
}