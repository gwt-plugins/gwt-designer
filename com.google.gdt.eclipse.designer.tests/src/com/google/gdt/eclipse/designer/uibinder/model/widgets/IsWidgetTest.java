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
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link IsWidgetInfo} and {@link IsWidgetWrappedInfo}.
 * 
 * @author scheglov_ke
 */
public class IsWidgetTest extends UiBinderModelTest {
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
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    prepareMyWidget();
  }

  static void prepareMyWidget() throws Exception {
    UiBinderContext.disposeSharedGWTState();
    setFileContentSrc(
        "test/client/MyWidget.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.ui.*;",
            "public class MyWidget implements IsWidget {",
            "  private Button button = new Button('MyWidget');",
            "  public Widget asWidget() {",
            "    return button;",
            "  }",
            "  public void setText(String text) {",
            "    button.setHTML(text);",
            "  }",
            "}"));
    forgetCreatedResources();
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyWidget wbp:name='widget'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <t:MyWidget wbp:name='widget'>",
        "    IsWidget");
    refresh();
    IsWidgetWrappedInfo widget = getObjectByName("widget");
    IsWidgetInfo wrapper = widget.getWrapper();
    // IsWidget model is not visible
    assertVisible(wrapper, false);
    // wrapped Widget uses MyWidget icon
    assertSame(wrapper.getPresentation().getIcon(), widget.getPresentation().getIcon());
    // only MyWidget properties, not Button properties
    {
      Property[] properties = widget.getProperties();
      String[] titles = PropertyUtils.getTitles(properties);
      assertThat(titles).excludes("enabled");
      assertThat(titles).contains("Class", "text");
    }
    // no clipboard
    assertFalse(XmlObjectMemento.hasMemento(widget));
    // delete
    widget.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
  }

  public void test_CREATE() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    {
      IsWidgetInfo myWidget = createObject("test.client.MyWidget");
      WidgetInfo widget = myWidget.getWrapped();
      flowContainer_CREATE(panel, widget, null);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyWidget/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_MOVE() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <t:MyWidget wbp:name='widget'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo widget = getObjectByName("widget");
    // do move
    flowContainer_MOVE(panel, widget, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyWidget wbp:name='widget'/>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_ADD() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyWidget wbp:name='widget'/>",
        "    <g:FlowPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo panel = getObjectByName("panel");
    WidgetInfo widget = getObjectByName("widget");
    // do move
    flowContainer_MOVE(panel, widget, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:FlowPanel wbp:name='panel'>",
        "      <t:MyWidget wbp:name='widget'/>",
        "    </g:FlowPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}