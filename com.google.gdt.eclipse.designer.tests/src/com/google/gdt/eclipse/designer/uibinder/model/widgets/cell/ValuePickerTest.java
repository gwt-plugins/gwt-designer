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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.cell;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link com.google.gwt.user.client.ui.ValuePicker}.
 * 
 * @author scheglov_ke
 */
public class ValuePickerTest extends UiBinderModelTest {
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
  public void test_parseProvided() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.user.cellview.client.*;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField(provided=true) ValuePicker valuePicker;",
            "  public Test() {",
            "    CellList cellList = new CellList(new TextCell());",
            "    valuePicker = new ValuePicker(cellList);",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='0'>",
        "      <g:ValuePicker wbp:name='valuePicker' ui:field='valuePicker'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    ValuePickerInfo valuePicker = getObjectByName("valuePicker");
    // we have actual object
    Object valuePickerObject = valuePicker.getObject();
    assertEquals(
        "com.google.gwt.user.client.ui.ValuePicker",
        valuePickerObject.getClass().getName());
    // has reasonable size (we fill it with items)
    {
      Rectangle bounds = valuePicker.getBounds();
      assertThat(bounds.width).isGreaterThan(100).isLessThan(200);
      assertThat(bounds.height).isGreaterThan(80);
    }
  }
}