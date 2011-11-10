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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiChildSupport.Position;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

/**
 * Test for {@link UiChildSupport} in GEF.
 * 
 * @author scheglov_ke
 */
public class UiChildGefTest extends UiBinderGefTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setUp() throws Exception {
    UiBinderContext.disposeSharedGWTState();
  }

  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    UiChildTest.prepareMyContainer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    WidgetInfo container =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button' text='Existing'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    Position position = UiChildTest.getPosition(container, "topbutton");
    WidgetInfo button = getObjectByName("button");
    // try Label, no command
    {
      loadCreationTool("com.google.gwt.user.client.ui.Label");
      tree.moveOn(position);
      tree.assertCommandNull();
    }
    // create Button
    loadButton();
    tree.moveOn(position).assertCommandNotNull();
    tree.moveBefore(button).assertCommandNotNull();
    tree.click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button' text='Existing'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  public void test_CREATE_full() throws Exception {
    WidgetInfo container =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button text='Button 1'/>",
            "    </t:topbutton>",
            "    <t:topbutton>",
            "      <g:Button text='Button 2'/>",
            "    </t:topbutton>",
            "    <t:topbutton>",
            "      <g:Button text='Button 3'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    Position position = UiChildTest.getPosition(container, "topbutton");
    // create Button, but container already full
    loadButton();
    tree.moveOn(position).assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    WidgetInfo container =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button' text='Button'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    Position position = UiChildTest.getPosition(container, "topbutton");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveOn(position).assertCommandNotNull();
    tree.click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button' text='Button'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button text='Button'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  public void test_PASTE_full() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button' text='Button 1'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button text='Button 2'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button text='Button 3'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveBefore(button).assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_1' text='Button 1'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_2' text='Button 2'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_3' text='Button 3'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    WidgetInfo button_3 = getObjectByName("button_3");
    WidgetInfo button_1 = getObjectByName("button_1");
    //
    tree.startDrag(button_3).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_3' text='Button 3'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_1' text='Button 1'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_2' text='Button 2'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  public void test_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='Button'/>",
        "    <t:MyContainer wbp:name='container'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    WidgetInfo container = getObjectByName("container");
    Position position = UiChildTest.getPosition(container, "topbutton");
    //
    tree.startDrag(button).dragOn(position).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyContainer wbp:name='container'>",
        "      <t:topbutton>",
        "        <g:Button wbp:name='button' text='Button'/>",
        "      </t:topbutton>",
        "    </t:MyContainer>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    UiBinderContext.disposeSharedGWTState();
    super.test_tearDown();
  }
}