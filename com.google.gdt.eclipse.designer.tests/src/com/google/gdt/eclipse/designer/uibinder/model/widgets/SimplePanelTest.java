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
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link SimplePanelInfo}.
 * 
 * @author scheglov_ke
 */
public class SimplePanelTest extends UiBinderModelTest {
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
   * Even empty <code>SimplePanel</code> should have reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SimplePanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    SimplePanelInfo panel = getObjectByName("panel");
    //
    assertNull(panel.getWidget());
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    SimplePanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:SimplePanel/>",
            "</ui:UiBinder>");
    refresh();
    SimpleContainer simpleContainer = getSimpleContainer(panel);
    // no child Widget initially
    assertTrue(simpleContainer.isEmpty());
    // do CREATE
    WidgetInfo newButton = createButton();
    simpleContainer.command_CREATE(newButton);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:SimplePanel>",
        "    <g:Button width='100%' height='100%'/>",
        "  </g:SimplePanel>",
        "</ui:UiBinder>");
    assertSame(newButton, panel.getWidget());
    assertFalse(simpleContainer.isEmpty());
  }

  public void test_ADD() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SimplePanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    SimplePanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    SimpleContainer simpleContainer = getSimpleContainer(panel);
    // do ADD
    simpleContainer.command_ADD(button);
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SimplePanel wbp:name='panel'>",
        "      <g:Button wbp:name='button' width='100%' height='100%'/>",
        "    </g:SimplePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * When move {@link WidgetInfo} from {@link SimplePanelInfo} we should remove size specification.
   */
  public void test_MOVE_out() throws Exception {
    ComplexPanelInfo frame =
        parse(
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:SimplePanel wbp:name='panel'>",
            "      <g:Button wbp:name='button' width='100%' height='100%'/>",
            "    </g:SimplePanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // do MOVE
    flowContainer_MOVE(frame, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SimplePanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo frame =
        parse(
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:SimplePanel wbp:name='panel'>",
            "      <g:Button width='100%' height='100%'/>",
            "    </g:SimplePanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    // do copy/paste
    {
      SimplePanelInfo panel = getObjectByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:SimplePanel wbp:name='panel'>",
        "      <g:Button width='100%' height='100%'/>",
        "    </g:SimplePanel>",
        "    <g:SimplePanel>",
        "      <g:Button width='100%' height='100%'/>",
        "    </g:SimplePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other SimpleConatiner-s
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that known GWT panels use <code>"simpleContainer"</code> parameter, so all of them will
   * have appropriate {@link LayoutEditPolicy}.
   */
  public void test_simpleContainers() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    refresh();
    // SimplePanel+
    {
      check_is_simpleContainer("com.google.gwt.user.client.ui.SimplePanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DecoratorPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.FocusPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.FormPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.PopupPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DecoratedPopupPanel");
      check_is_simpleContainer("com.google.gwt.user.client.ui.DialogBox");
      check_is_simpleContainer("com.google.gwt.user.client.ui.ScrollPanel");
    }
    // CaptionPanel
    check_is_simpleContainer("com.google.gwt.user.client.ui.CaptionPanel");
  }

  /**
   * Checks that GWT component has <code>"simpleContainer"</code> parameter.
   */
  private void check_is_simpleContainer(String className) throws Exception {
    XmlObjectInfo model = createObject(className);
    assertHasWidgetSimpleContainer(model, true);
    assertHasWidgetSimpleContainer(model, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return some {@link SimpleContainer} for given {@link XmlObjectInfo}.
   */
  private static SimpleContainer getSimpleContainer(XmlObjectInfo container) {
    List<SimpleContainer> containers = new SimpleContainerFactory(container, false).get();
    assertThat(containers).isNotEmpty();
    return containers.get(0);
  }
}