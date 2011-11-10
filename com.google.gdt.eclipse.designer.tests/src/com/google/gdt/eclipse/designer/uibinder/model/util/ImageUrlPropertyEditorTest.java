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

import com.google.gdt.eclipse.designer.model.property.ImageUrlPropertyEditor;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.tests.designer.tests.common.PropertyNoValue;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Test for {@link ImageUrlPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ImageUrlPropertyEditorTest extends UiBinderModelTest {
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
  public void test_getText_noValue() throws Exception {
    Property property = new PropertyNoValue(ImageUrlPropertyEditor.INSTANCE);
    assertEquals(null, getPropertyText(property));
  }

  public void test_getText_hasValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Image wbp:name='image' url='1.png'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo image = getObjectByName("image");
    Property property = image.getPropertyByTitle("url");
    //
    assertEquals("1.png", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openDialog() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Image wbp:name='image'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo image = getObjectByName("image");
    //
    final Property property = image.getPropertyByTitle("url");
    animateDialog(property);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Image wbp:name='image' url='2.png'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_openDialog_withShell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Image wbp:name='image'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo image = getObjectByName("image");
    //
    final Object[] result = new Object[1];
    PropertyEditor propertyEditor =
        new ImageUrlPropertyEditor(DesignerPlugin.getShell(), image.getState());
    final Property property = new PropertyNoValue(propertyEditor) {
      @Override
      public String getTitle() {
        return "url";
      }

      @Override
      public void setValue(Object value) throws Exception {
        result[0] = value;
      }
    };
    animateDialog(property);
    assertEquals("2.png", result[0]);
  }

  private static void animateDialog(final Property property) throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("url");
        {
          Text text = context.findFirstWidget(Text.class);
          text.setText("2.png");
        }
        {
          TreeItem item = context.getTreeItem("2.png");
          UiContext.setSelection(item);
        }
        context.clickButton("OK");
      }
    });
  }
}