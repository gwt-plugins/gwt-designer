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

import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

/**
 * Test for {@link UIObjectSizeSupport}.
 * 
 * @author scheglov_ke
 */
public class UIObjectSizeSupportTest extends UiBinderModelTest {
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
  public void test_clipboard() throws Exception {
    final WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' width='6cm' height='30mm'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button, new PasteProcedure<WidgetInfo>() {
      public void run(WidgetInfo copy) throws Exception {
        flowContainer_CREATE(panel, copy, null);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm' height='30mm'/>",
        "    <g:Button height='30mm' width='6cm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(String,String)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSize_setWidthHeight() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    button.getSizeSupport().setSize("100px", "2cm");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px' height='2cm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSize_setWidth() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='10px' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    button.getSizeSupport().setSize("5cm", null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='5cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSize_setHeight() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm' height='10px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    button.getSizeSupport().setSize(null, "30mm");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSize_clearWidth() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    button.getSizeSupport().setSize(IUIObjectSizeSupport.NO_SIZE, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSize_clearHeight() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    button.getSizeSupport().setSize(null, IUIObjectSizeSupport.NO_SIZE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='6cm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(Dimension)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(Dimension)}.
   */
  public void test_setSizeDimension_setSize() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='5cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // set new size
    button.getSizeSupport().setSize(new Dimension(100, 50));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(Dimension)}.
   */
  public void test_setSizeDimension_setSize_remove() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // set new size
    button.getSizeSupport().setSize(null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(int,int)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(int,int)}.
   */
  public void test_setSizeInts() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='5cm' height='30mm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // set new size
    button.getSizeSupport().setSize(100, 50);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "Size"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_sizeProperty_noValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size");
    assertFalse(property.isModified());
    assertEquals(null, property.getValue());
    assertEquals(
        "(" + button.getBounds().width + ", " + button.getBounds().height + ")",
        getPropertyText(property));
  }

  public void test_sizeProperty_hasWidth() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size");
    assertTrue(property.isModified());
    assertEquals(null, property.getValue());
    assertEquals("(100px, " + button.getBounds().height + ")", getPropertyText(property));
  }

  public void test_sizeProperty_hasHeight() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size");
    assertTrue(property.isModified());
    assertEquals(null, property.getValue());
    assertEquals("(" + button.getBounds().width + ", 50px)", getPropertyText(property));
  }

  public void test_sizeProperty_clearValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size");
    assertTrue(property.isModified());
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "width"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_widthProperty_noValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size/width");
    assertFalse(property.isModified());
    assertEquals(null, property.getValue());
  }

  public void test_widthProperty_hasValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size/width");
    assertTrue(property.isModified());
    assertEquals("100px", property.getValue());
  }

  public void test_widthProperty_setValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    Property property = PropertyUtils.getByPath(button, "Size/width");
    // set new value
    property.setValue("150px");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='150px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    // clear value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "height"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_heightProperty_noValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size/height");
    assertFalse(property.isModified());
    assertEquals(null, property.getValue());
  }

  public void test_heightProperty_hasValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Size/height");
    assertTrue(property.isModified());
    assertEquals("100px", property.getValue());
  }

  public void test_heightProperty_setValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    Property property = PropertyUtils.getByPath(button, "Size/height");
    // set new value
    property.setValue("150px");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' height='150px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    // clear value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Consider margin, border, padding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_TextBox() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TextBox wbp:name='widget' styleName='myStyle'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo widget = getObjectByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 3 + 4) * 2;
    int clientHeight = 50 - (2 + 3 + 4) * 2;
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TextBox wbp:name='widget' styleName='myStyle' width='"
            + clientWidth
            + "px' height='"
            + clientHeight
            + "px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_FlowPanel() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:FlowPanel wbp:name='widget' styleName='myStyle'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo widget = getObjectByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 3 + 4) * 2;
    int clientHeight = 50 - (2 + 3 + 4) * 2;
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:FlowPanel wbp:name='widget' styleName='myStyle' width='"
            + clientWidth
            + "px' height='"
            + clientHeight
            + "px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_ListBox() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:ListBox wbp:name='widget' styleName='myStyle'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo widget = getObjectByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 0 + 0) * 2;
    int clientHeight = 50 - (2 + 0 + 0) * 2;
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:ListBox wbp:name='widget' styleName='myStyle' width='"
            + clientWidth
            + "px' height='"
            + clientHeight
            + "px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_Button_withDecorations() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='widget' styleName='myStyle'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo widget = getObjectByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 0 + 0) * 2;
    int clientHeight = 50 - (2 + 0 + 0) * 2;
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='widget' styleName='myStyle' width='"
            + clientWidth
            + "px' height='"
            + clientHeight
            + "px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_Button_default() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='widget'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo widget = getObjectByName("widget");
    // decorations
    assertEquals(new Insets(0), widget.getMargins());
    assertEquals(new Insets(2), widget.getBorders());
    assertEquals(new Insets(0), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (0 + 0 + 0) * 2;
    int clientHeight = 50 - (0 + 0 + 0) * 2;
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='widget' width='"
            + clientWidth
            + "px' height='"
            + clientHeight
            + "px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * In theory we should consider decorations, but if there was no refresh() yet, we don't know
   * decorations, so we just apply size as is.
   */
  public void test_considerDecorations_whenSizeDuringCreate() throws Exception {
    final ComplexPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    final WidgetInfo widget = createObject("com.google.gwt.user.client.ui.TextBox");
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        flowContainer_CREATE(frame, widget, null);
        widget.getSizeSupport().setSize(100, 50);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TextBox width='100px' height='50px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    // no tweaks for decorations, so (bounds, in contrast to client) size is bigger than asked
    {
      int margin = 0;
      int border = 2;
      int padding = 1;
      assertEquals(100 + (margin + border + padding) * 2, widget.getBounds().width);
      assertEquals(50 + (margin + border + padding) * 2, widget.getBounds().height);
    }
  }
}