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

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;

/**
 * Test for <code>com.google.gwt.user.client.ui.DisclosurePanel</code> widget in GEF.
 * 
 * @author scheglov_ke
 */
public class DisclosurePanelGefTest extends UiBinderGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    DisclosurePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DisclosurePanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel>",
        "    <g:Button width='5cm' height='4cm'/>",
        "  </g:DisclosurePanel>",
        "</ui:UiBinder>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_directEdit() throws Exception {
    DisclosurePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DisclosurePanel/>",
            "</ui:UiBinder>");
    //
    canvas.performDirectEdit(panel, "My header");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel>",
        "    <g:header>My header</g:header>",
        "  </g:DisclosurePanel>",
        "</ui:UiBinder>");
  }

  public void test_flipOpen() throws Exception {
    DisclosurePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DisclosurePanel/>",
            "</ui:UiBinder>");
    // false -> true
    canvas.doubleClick(panel);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel open='true'/>",
        "</ui:UiBinder>");
    // true -> false
    canvas.doubleClick(panel);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DisclosurePanel/>",
        "</ui:UiBinder>");
  }
}