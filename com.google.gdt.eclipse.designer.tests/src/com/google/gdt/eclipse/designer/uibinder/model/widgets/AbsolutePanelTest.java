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

import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Test for {@link AbsolutePanelInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsolutePanelTest extends UiBinderModelTest {
  private static final IPreferenceStore preferences =
      GwtToolkitDescription.INSTANCE.getPreferences();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, false);
  }

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
  public void test_flowContainer_inTree() throws Exception {
    AbsolutePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel/>",
            "</ui:UiBinder>");
    assertHasWidgetFlowContainer(panel, false);
    //
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(panel, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='0'>",
        "      <g:Button/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BOUNDS
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLocation() throws Exception {
    AbsolutePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='1' top='2'>",
            "      <g:Button wbp:name='button' width='100px' height='50px'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.command_BOUNDS(button, new Point(10, 20), null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='10' top='20'>",
        "      <g:Button wbp:name='button' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_setSize() throws Exception {
    AbsolutePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='1' top='2'>",
            "      <g:Button wbp:name='button' width='100px' height='50px'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.command_BOUNDS(button, null, new Dimension(101, 51));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='1' top='2'>",
        "      <g:Button wbp:name='button' width='101px' height='51px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_setLocation_setSize() throws Exception {
    AbsolutePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='1' top='2'>",
            "      <g:Button wbp:name='button' width='100px' height='50px'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.command_BOUNDS(button, new Point(10, 20), new Dimension(101, 51));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='10' top='20'>",
        "      <g:Button wbp:name='button' width='101px' height='51px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test applying creation flow order.
   */
  public void test_setLocation_creationFlow() throws Exception {
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, true);
    AbsolutePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='50' top='100'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:at>",
            "    <g:at left='100' top='150'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_2 = getObjectByName("button_2");
    // Bounds
    panel.command_BOUNDS(button_2, new Point(5, 5), null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='5' top='5'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:at>",
        "    <g:at left='50' top='100'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for <code>Order</code> actions.
   */
  public void test_contextMenu_order() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='0'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:at>",
        "    <g:at left='0' top='1'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:at>",
        "    <g:at left='0' top='2'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button_3 = getObjectByName("button_3");
    // prepare action
    IAction action;
    {
      IMenuManager allManager = getContextMenu(button_3);
      IMenuManager orderManager = findChildMenuManager(allManager, "Order");
      action = findChildAction(orderManager, "Bring to Front");
      assertNotNull(action);
    }
    // run action
    action.run();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='2'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:at>",
        "    <g:at left='0' top='0'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:at>",
        "    <g:at left='0' top='1'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for <code>Autosize widget</code> action.
   */
  public void test_contextMenu_autoSize() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='1' top='2'>",
        "      <g:Button wbp:name='button' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // prepare action
    IAction autoSizeAction;
    {
      IMenuManager manager = getContextMenu(button);
      autoSizeAction = findChildAction(manager, "Autosize widget");
      assertNotNull(autoSizeAction);
    }
    // perform auto-size
    autoSizeAction.run();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='1' top='2'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:AbsolutePanel wbp:name='panel'>",
            "      <g:at left='10' top='20'>",
            "        <g:Button width='5cm' height='1in'/>",
            "      </g:at>",
            "    </g:AbsolutePanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    // do copy/paste
    {
      AbsolutePanelInfo panel = getObjectByName("panel");
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
        "    <g:AbsolutePanel wbp:name='panel'>",
        "      <g:at left='10' top='20'>",
        "        <g:Button width='5cm' height='1in'/>",
        "      </g:at>",
        "    </g:AbsolutePanel>",
        "    <g:AbsolutePanel>",
        "      <g:at left='10' top='20'>",
        "        <g:Button height='1in' width='5cm'/>",
        "      </g:at>",
        "    </g:AbsolutePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}