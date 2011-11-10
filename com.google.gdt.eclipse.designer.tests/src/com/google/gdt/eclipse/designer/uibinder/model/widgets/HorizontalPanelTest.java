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

/**
 * Test for <code>com.google.gwt.user.client.ui.HorizontalPanel</code> widget.
 * 
 * @author scheglov_ke
 */
public class HorizontalPanelTest extends UiBinderModelTest {
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
  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:HorizontalPanel/>",
            "</ui:UiBinder>");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  public void test_clipboard() throws Exception {
    final ComplexPanelInfo rootPanel =
        parse(
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:HorizontalPanel wbp:name='panel'>",
            "      <g:Button text='A'/>",
            "      <g:Button text='B'/>",
            "    </g:HorizontalPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      ComplexPanelInfo panel = getObjectByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(rootPanel, copy, null);
        }
      });
    }
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:HorizontalPanel wbp:name='panel'>",
        "      <g:Button text='A'/>",
        "      <g:Button text='B'/>",
        "    </g:HorizontalPanel>",
        "    <g:HorizontalPanel>",
        "      <g:Button text='A'/>",
        "      <g:Button text='B'/>",
        "    </g:HorizontalPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}