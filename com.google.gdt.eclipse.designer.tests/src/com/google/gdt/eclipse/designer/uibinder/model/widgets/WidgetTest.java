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

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.uibinder.IExceptionConstants;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.RectValue;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link WidgetInfo}.
 * 
 * @author scheglov_ke
 */
public class WidgetTest extends UiBinderModelTest {
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
   * We should check that GWT includes patches for UiBinder support in GWT Designer.
   */
  @DisposeProjectAfter
  public void test_wrongVersion() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_20());
    // parse will fail because GWT 2.0.4 does not support UiBinder WYSIWYG
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel/>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.WRONG_VERSION, e.getCode());
    }
  }

  /**
   * Test (not direct, will be checked after test class) that {@link GwtState} is disposed even if
   * GWT module definition was incorrect.
   */
  @DisposeProjectAfter
  public void test_wrongModule() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/Module.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <badTagName/>",
            "</module>"));
    // parse will fail because of wrong module
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel/>",
          "</ui:UiBinder>");
    } catch (Throwable e) {
      assertEquals("com.google.gwt.core.ext.UnableToCompleteException", e.getClass().getName());
    }
  }

  public void test_emptyFlowPanel() throws Exception {
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
    refresh();
    //
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
  }

  public void test_FlowPanel_withButton() throws Exception {
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' text='AAA'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:Button wbp:name='button' text='AAA'>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    {
      Rectangle expected =
          Expectations.get(
              new Rectangle(0, 0, 49, 24),
              new RectValue[]{new RectValue("scheglov-macpro", new Rectangle(0, 0, 49, 26))});
      assertEquals(expected, button.getBounds());
    }
    // property value
    assertEquals("AAA", button.getPropertyByTitle("text").getValue());
  }

  /**
   * Test that <code>HasHTML.setHTML(String)</code> has nice title "html", not just default "hTML".
   */
  public void test_setHTML() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // "html" property
    assertNotNull(button.getPropertyByTitle("html"));
    assertNull(button.getPropertyByTitle("hTML"));
    assertNull(button.getPropertyByTitle("HTML"));
    // set value
    button.getPropertyByTitle("html").setValue("newValue");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'>newValue</g:Button>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * For property "width" there are no getter, so it will has value only it remembered as attribute
   * value. See {@link XmlObjectInfo#registerAttributeValue(String, Object)}.
   */
  public void test_propertyValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' width='5cm'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // property value
    assertEquals("5cm", button.getPropertyByTitle("width").getValue());
  }

  /**
   * UiBinder templates use "UTF-8" charset, so we should use this encoding for values.
   */
  public void test_setPropertyValue_UTF8() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // XML model should use "UTF-8"
    assertEquals("UTF-8", button.getElement().getModel().getCharset());
    // set value
    button.getPropertyByTitle("text").setValue("\u0410\u0411\u0412");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='\u0410\u0411\u0412'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * "PushButton" has "setDown()" method, but it is protected, so we should not create
   * {@link Property} for it.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47725
   */
  public void test_noPropertyForProtectedMethod() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:PushButton wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // no "down" property
    String[] propertyTitles = PropertyUtils.getTitles(button.getProperties());
    assertThat(propertyTitles).excludes("down");
  }

  /**
   * "TextBox" has "setDirectionEstimator(boolean)" and "setDirectionEstimator(DirectionEstimator)"
   * methods, but UiBinder does not allow such ambiguous setters, so we should not create
   * {@link Property} for it.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47769
   */
  public void test_noAmbiguousProperties() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TextBox wbp:name='textBox'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo textBox = getObjectByName("textBox");
    // no "directionEstimator" property
    String[] propertyTitles = PropertyUtils.getTitles(textBox.getProperties());
    assertThat(propertyTitles).excludes("directionEstimator", "directionEstimator(boolean)");
  }

  /**
   * Test that custom widget can be rendered as "root".
   */
  public void test_customWidget() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyPanel/>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyPanel>");
    refresh();
    //
    assertEquals("test.client.MyPanel", panel.getObject().getClass().getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // @UiField rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that for GWT before 2.1.1 we don't support @UiField(provided=true).
   */
  @DisposeProjectAfter
  public void test_UiField_noProvidedSupport() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_2_1_0());
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) Button button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel>",
          "    <g:Button wbp:name='button' ui:field='button'/>",
          "  </g:FlowPanel>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.UI_FIELD_FACTORY_FEATURE, e.getCode());
    }
  }

  /**
   * Test that we support @UiField(provided=true).
   * <p>
   * {@link ComponentDescription} provides special "UiBinder.createInstance" creation script.
   */
  @DisposeProjectAfter
  public void test_UiField_useScript() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "    setText(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='UiBinder.createInstance'>new test.client.MyButton('abc')</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) MyButton button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals("abc", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
  }

  /**
   * Test that we support @UiField(provided=true).
   * <p>
   * Has default constructor, so use it.
   */
  @DisposeProjectAfter
  public void test_UiField_useDefaultConstructor() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) MyButton button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // we have actual object
    assertEquals("test.client.MyButton", button.getObject().getClass().getName());
  }

  /**
   * Test that we support @UiField(provided=true).
   * <p>
   * Has default constructor, so use it. But it causes exception, show it in good way.
   */
  @DisposeProjectAfter
  public void test_UiField_useDefaultConstructor_causesException() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton() {",
            "    throw new Error();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) MyButton button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel>",
          "    <t:MyButton wbp:name='button' ui:field='button'/>",
          "  </g:FlowPanel>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.UI_FIELD_EXCEPTION, e.getCode());
    }
  }

  /**
   * Test that we support @UiField(provided=true).
   * <p>
   * No default constructor, so use shortest constructor with default values as arguments.
   */
  @DisposeProjectAfter
  public void test_UiField_useConstructor_defaultArguments() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "    setText(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) MyButton button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals("<dynamic>", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // @UiFactory rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that for GWT before 2.1.1 we don't support @UiFactory.
   */
  @DisposeProjectAfter
  public void test_UiFactory_noSupport() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_2_1_0());
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiFactory Button fooMethod() {",
            "    return new Button();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel>",
          "    <g:Button wbp:name='button'/>",
          "  </g:FlowPanel>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.UI_FIELD_FACTORY_FEATURE, e.getCode());
    }
  }

  /**
   * Test that we support @UiFactory.
   * <p>
   * {@link ComponentDescription} provides special "UiBinder.createInstance" creation script.
   */
  @DisposeProjectAfter
  public void test_UiFactory_useScript() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "    setText(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='UiBinder.createInstance'>",
            "      new test.client.MyButton(args[0] + ':' + args[1])",
            "    </parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiFactory MyButton fooMethod(String text, int level) {",
            "    return new MyButton(text + level);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' text='abc' level='2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals("abc:2", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
  }

  /**
   * Test that we support @UiFactory.
   * <p>
   * Has default constructor, so use it.
   */
  @DisposeProjectAfter
  public void test_UiFactory_useDefaultConstructor() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiFactory MyButton fooMethod() {",
            "    return new MyButton('abc');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
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
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals("", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
  }

  /**
   * Test that we support @UiFactory.
   * <p>
   * Has default constructor, so use it. But it causes exception, show it in good way.
   */
  @DisposeProjectAfter
  public void test_UiFactory_useDefaultConstructor_causesException() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "    throw new Error();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiFactory MyButton fooMethod() {",
            "    return new MyButton('abc');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel>",
          "    <t:MyButton wbp:name='button'/>",
          "  </g:FlowPanel>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.UI_FACTORY_EXCEPTION, e.getCode());
    }
  }

  /**
   * Test that we support @UiFactory.
   * <p>
   * No default constructor, so use shortest constructor with default values as arguments.
   */
  @DisposeProjectAfter
  public void test_UiFactory_useConstructor_defaultArguments() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(String text) {",
            "    setText(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiFactory MyButton fooMethod() {",
            "    return new MyButton('abc');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
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
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals("<dynamic>", ReflectionUtils.invokeMethod(buttonObject, "getText()"));
  }

  /**
   * Test that we support @UiField(provided=true).
   * <p>
   * Use constructor, but try also "INSTANCE" field for argument types.
   */
  @DisposeProjectAfter
  public void test_UiField_useConstructor_fieldInstance() throws Exception {
    setFileContentSrc(
        "test/client/MyParameter.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyParameter {",
            "  static MyParameter INSTANCE = new MyParameter();",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public MyButton(MyParameter parameter) {",
            "    setEnabled(parameter != null);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) MyButton button;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // we have actual object
    Object buttonObject = button.getObject();
    assertEquals("test.client.MyButton", buttonObject.getClass().getName());
    assertEquals(true, ReflectionUtils.invokeMethod(buttonObject, "isEnabled()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binder interface
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There was shortcut - use always "Binder" class. It seems that sometimes user use different
   * names.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47575
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_differentName() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface MyTemplate extends UiBinder<Widget, Test> {}",
            "  private static final MyTemplate binder = GWT.create(MyTemplate.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * We should support @UiTemplate annotation.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47917
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_UiTemplate_localName() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  @UiTemplate('MyTemplate.ui.xml')",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    _parse(
        "src/test/client/MyTemplate.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"));
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * We should support @UiTemplate annotation.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47917
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_UiTemplate_qualifiedName() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  @UiTemplate('test.client.templates.MyTemplate.ui.xml')",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    _parse(
        "src/test/client/templates/MyTemplate.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"));
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * If there is {@link IType} with same name as template, but it has NOT "Binder" interface, then
   * we should not fail and should try to find @UiTemplate annotation.
   * <p>
   * https://groups.google.com/forum/#!topic/google-web-toolkit/gfc0Ggft9iA
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_UiTemplate_hasFormInterface() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyTemplate.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyTemplate {",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  @UiTemplate('MyTemplate.ui.xml')",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    _parse(
        "src/test/client/MyTemplate.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"));
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * We should support opening <code>*.ui.xml</code> templates in Maven "resources" folder.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47855
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_maven() throws Exception {
    dontUseSharedGWTState();
    GTestUtils.configureMavenProject();
    // prepare Java source
    setFileContent(
        "src/main/java/test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    _parse(
        "src/main/resources/test/client/Test.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"));
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * Test for case when there are no "companion" form class.
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_noForm() throws Exception {
    dontUseSharedGWTState();
    getFileSrc("test/client/Test.java").delete(true, null);
    waitForAutoBuild();
    // parse
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel/>",
          "</ui:UiBinder>");
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_TYPE, e.getCode());
    }
  }

  /**
   * Test for case when there are no "Binder" class.
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_noBinder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  public Test() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel/>",
          "</ui:UiBinder>");
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_TYPE, e.getCode());
    }
  }

  /**
   * We should support @UiTemplate annotation, even for Binder in inner class.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48532
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_inInnerClass() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends PopupPanel {",
            "  public static class InfoWidget extends Composite {",
            "    @UiTemplate('MyTemplate.ui.xml')",
            "    interface Binder extends UiBinder<Widget, InfoWidget> {}",
            "    private static final Binder binder = GWT.create(Binder.class);",
            "    public InfoWidget() {",
            "      initWidget(binder.createAndBindUi(this));",
            "    }",
            "  }",
            "}",
            ""));
    waitForAutoBuild();
    // parse
    _parse(
        "src/test/client/MyTemplate.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"));
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>");
  }

  /**
   * Some users try to open "html" file in "war", which is not in Java package.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47654
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_notInPackage() throws Exception {
    try {
      _parse(
          "war/Test.ui.xml",
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<ui:UiBinder>",
              "  <g:FlowPanel/>",
              "</ui:UiBinder>"));
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_PACKAGE, e.getCode());
    }
  }

  /**
   * Some users try to open "ui.xml" file in project root.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48266
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_notPackage_inProjectRoot() throws Exception {
    try {
      _parse(
          "Test.ui.xml",
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<ui:UiBinder>",
              "  <g:FlowPanel/>",
              "</ui:UiBinder>"));
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_PACKAGE, e.getCode());
    }
  }

  /**
   * Some users try to open "ui.xml" file in source folder.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48312
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_notPackage_inSourceFolder() throws Exception {
    try {
      _parse(
          "src/Test.ui.xml",
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<ui:UiBinder>",
              "  <g:FlowPanel/>",
              "</ui:UiBinder>"));
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_PACKAGE, e.getCode());
    }
  }

  /**
   * Some users try to open "ui.xml" file which is directly in module package, not in client
   * package. This can not work because we will not able to load "UiBinder" interface from this non
   * client package.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48156
   */
  @DisposeProjectAfter
  public void test_interfaceBinder_notInClientPackage() throws Exception {
    try {
      _parse(
          "src/test/Test.ui.xml",
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<ui:UiBinder>",
              "  <g:FlowPanel/>",
              "</ui:UiBinder>"));
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NOT_CLIENT_PACKAGE, e.getCode());
    }
  }

  /**
   * Right now we support only widgets based UiBinder.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47790
   */
  @DisposeProjectAfter
  public void test_onlyBeWidgetBased() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "import com.google.gwt.dom.client.Element;",
            "public class Test extends UIObject {",
            "  interface Binder extends UiBinder<Element, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    setElement(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    try {
      parse(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <div/>",
          "</ui:UiBinder>");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.ONLY_WIDGET_BASED, e.getCode());
    }
  }

  /**
   * Compilation error in form class should not cause {@link Class} absence, so "Binder" absence and
   * crash during rendering or UiBinder template.
   * <p>
   * TODO remove or introduce this feature back into GWT trunk
   */
  @DisposeProjectAfter
  public void _test_whenCompilationError_inFormClass() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "import com.google.gwt.dom.client.Element;",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  private void foo() {",
            "    button.bar();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    refresh();
    assertNoErrors();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_reparseOnCss() throws Exception {
    dontUseSharedGWTState();
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' styleName='test'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // initially "button" is narrow
    assertThat(button.getBounds().width).isLessThan(100);
    // initially no refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
    // update CSS
    setFileContent(
        "war/Module.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".test {",
            "  width: 200px;",
            "}"));
    waitForAutoBuild();
    // now refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertTrue(request.isRefreshRequested());
    }
    // do refresh, as requested
    refresh();
    assertThat(button.getBounds().width).isEqualTo(200);
  }

  @DisposeProjectAfter
  public void test_reparseOnCss_andDontHurtCoordinates() throws Exception {
    dontUseSharedGWTState();
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel styleName='test'/>",
            "</ui:UiBinder>");
    refresh();
    // update CSS
    setFileContent(
        "war/Module.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".test {",
            "  border: red 10px solid;",
            "}"));
    waitForAutoBuild();
    // now refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertTrue(request.isRefreshRequested());
    }
    // do refresh, as requested
    refresh();
    // assert that screen shot is correct and border is fully included
    {
      Image image = panel.getImage();
      System.out.println(image.getBounds());
      {
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[]{image.getImageData()};
        imageLoader.save("c:/temp/1.png", SWT.IMAGE_PNG);
      }
      int width = image.getBounds().width;
      int height = image.getBounds().height;
      assertRGB(getPixelRGB(image, 0, 0), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, width - 1, 0), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, 0, height - 1), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, width - 1, height - 1), 0xFF, 0, 0);
    }
  }
}