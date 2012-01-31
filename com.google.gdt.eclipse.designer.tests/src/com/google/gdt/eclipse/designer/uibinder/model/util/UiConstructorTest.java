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

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link UiConstructorSupport}.
 * 
 * @author scheglov_ke
 */
public class UiConstructorTest extends UiBinderModelTest {
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
  protected void tearDown() throws Exception {
    super.tearDown();
    {
      do_projectDispose();
      waitForAutoBuild();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noUiConstructor() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // no @UiConstructor property
    {
      Property constructorProperty = PropertyUtils.getByPath(button, "UiConstructor");
      assertNull(constructorProperty);
    }
    // ask second time for coverage
    {
      Property constructorProperty = PropertyUtils.getByPath(button, "UiConstructor");
      assertNull(constructorProperty);
    }
  }

  /**
   * Has constructor marked with @UiConstructor, but it has no parameters, so no properties.
   */
  public void test_hasUiConstructor_noParameters() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // no UiConstructor property
    assertNull(button.getPropertyByTitle("UiConstructor"));
  }

  public void test_hasUiConstructor_disabled() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton(int foo, String bar) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='UiConstructor.disabled'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' foo='1' bar='abc'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // no UiConstructor property
    assertSame(null, button.getPropertyByTitle("UiConstructor"));
  }

  public void test_hasUiConstructor() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton(int foo, String bar) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' foo='1' bar='abc'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // "foo"
    {
      Property fooProperty = PropertyUtils.getByPath(button, "UiConstructor/foo");
      assertNotNull(fooProperty);
      assertNotNull(fooProperty.getEditor());
      assertEquals(1, fooProperty.getValue());
    }
    // "bar"
    {
      Property barProperty = PropertyUtils.getByPath(button, "UiConstructor/bar");
      assertNotNull(barProperty);
      assertNotNull(barProperty.getEditor());
      assertEquals("abc", barProperty.getValue());
    }
  }

  public void test_operations() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton(int foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' foo='1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // prepare property
    Property property = PropertyUtils.getByPath(button, "UiConstructor/foo");
    // initial value
    assertEquals(1, property.getValue());
    // set new value
    property.setValue(2);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' foo='2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    // no delete
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' foo='2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_CREATE() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton(String attString, boolean attBoolean, int attInt, double attDouble) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // do create
    WidgetInfo button = createObject("test.client.MyButton");
    flowContainer_CREATE(panel, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton attString='String' attBoolean='false' attInt='0' attDouble='0'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_CREATE_unsupportedType() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "import com.google.gwt.uibinder.client.UiConstructor;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  @UiConstructor",
            "  public MyButton(long attLong) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // do create
    try {
      WidgetInfo button = createObject("test.client.MyButton");
      flowContainer_CREATE(panel, button, null);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(": long");
    }
  }
}