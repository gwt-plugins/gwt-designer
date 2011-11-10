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
package com.google.gdt.eclipse.designer.uibinder.gef;

import com.google.gdt.eclipse.designer.uibinder.editor.ExposePropertySupport;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.editor.structure.property.IPropertiesMenuContributor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link ExposePropertySupport}.
 * 
 * @author scheglov_ke
 */
public class ExposePropertySupportTest extends UiBinderModelTest {
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
  public void test_validOrInvalidProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // "Class" is not generic property
    {
      IAction action = getExposeAction(button, "Class");
      assertNull(action);
    }
    // "enabled" in valid property
    {
      IAction action = getExposeAction(button, "enabled");
      assertNotNull(action);
    }
  }

  /**
   * We support only "setter" based properties.
   */
  @DisposeProjectAfter
  public void test_fieldBaseProperty() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "  public String myTextField;",
            "}"));
    // parse
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // "myTextField" in not method property
    {
      IAction action = getExposeAction(button, "myTextField");
      assertNull(action);
    }
  }

  /**
   * @return the "Expose property..." {@link IAction}, which is contributed for given
   *         {@link Property}, may be <code>null</code>.
   */
  private static IAction getExposeAction(Property property) throws Exception {
    IMenuManager manager = new MenuManager();
    manager.add(new Separator(IPropertiesMenuContributor.GROUP_EDIT));
    // ask for contributions
    ExposePropertySupport.INSTANCE.contributeMenu(manager, property);
    return findChildAction(manager, "Expose property...");
  }

  /**
   * @return the "Expose property..." {@link IAction}, which is contributed for given
   *         {@link WidgetInfo}'s property, may be <code>null</code>.
   */
  private static IAction getExposeAction(WidgetInfo widget, String propertyName) throws Exception {
    Property property = widget.getPropertyByTitle(propertyName);
    return getExposeAction(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_validate() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  private int getFoo() {return 0;}",
            "  private void setBar(boolean bar) {}",
            "}"));
    // parse
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // prepare action
    IAction action = getExposeAction(button, "enabled");
    // invalid identifier
    {
      String message = call_validate(action, "bad-name");
      assertTrue(message.contains("identifier"));
    }
    // getter already exists
    {
      String message = call_validate(action, "foo");
      assertTrue(message.contains("getFoo()"));
    }
    // setter already exists
    {
      String message = call_validate(action, "bar");
      assertTrue(message.contains("setBar(boolean)"));
    }
    // OK
    assertNull(call_validate(action, "someUniqueProperty"));
  }

  private static String call_validate(IAction action, String exposedName) throws Exception {
    return (String) ReflectionUtils.invokeMethod(action, "validate(java.lang.String)", exposedName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property with primitive type.
   */
  public void test_getPreviewSource_primitive() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public boolean getButtonEnabled() {",
            "    return widget.isEnabled();",
            "  }",
            "  public void setButtonEnabled(boolean enabled) {",
            "    widget.setEnabled(enabled);",
            "  }",
            "..."),
        call_getPreview(button, "enabled", "buttonEnabled", true));
  }

  /**
   * Test case when parameter of setter conflicts with existing {@link VariableDeclaration}.
   */
  @DisposeProjectAfter
  public void test_getPreviewSource_parameter() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  private boolean enabled;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    // parse
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public boolean getButtonEnabled() {",
            "    return widget.isEnabled();",
            "  }",
            "  public void setButtonEnabled(boolean enabled_1) {",
            "    widget.setEnabled(enabled_1);",
            "  }",
            "..."),
        call_getPreview(button, "enabled", "buttonEnabled", true));
  }

  /**
   * Property with qualified type name.
   */
  public void test_getPreviewSource_qualified() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public String getButtonText() {",
            "    return widget.getText();",
            "  }",
            "  public void setButtonText(String text) {",
            "    widget.setText(text);",
            "  }",
            "..."),
        call_getPreview(button, "text", "buttonText", true));
  }

  /**
   * <code>protected</code> modifier for exposed.
   */
  public void test_getPreviewSource_protected() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  protected boolean getButtonEnabled() {",
            "    return widget.isEnabled();",
            "  }",
            "  protected void setButtonEnabled(boolean enabled) {",
            "    widget.setEnabled(enabled);",
            "  }",
            "..."),
        call_getPreview(button, "enabled", "buttonEnabled", false));
  }

  private static String call_getPreview(WidgetInfo widget,
      String propertyName,
      String exposedName,
      boolean isPublic) throws Exception {
    UiBinderContext context = widget.getContext();
    String initialSource = context.getDocument().get();
    // prepare action
    IAction action;
    {
      action = getExposeAction(widget, propertyName);
      assertTrue(action.isEnabled());
    }
    // get preview
    String previewSource;
    {
      assertNull(call_validate(action, exposedName));
      previewSource =
          (String) ReflectionUtils.invokeMethod2(
              action,
              "getPreviewSource",
              boolean.class,
              isPublic);
    }
    // assert that source is not changed
    assertEquals(initialSource, context.getDocument().get());
    // OK
    return previewSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // expose()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Expose <code>String</code> property.
   */
  @DisposeProjectAfter
  public void test_expose_String() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    call_expose(button, "text", "buttonText", true);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button button;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  public String getButtonText() {",
        "    return button.getText();",
        "  }",
        "  public void setButtonText(String text) {",
        "    button.setText(text);",
        "  }",
        "}");
  }

  private static void call_expose(WidgetInfo widget,
      String propertyName,
      String exposedName,
      final boolean isPublic) throws Exception {
    final IAction action = getExposeAction(widget, propertyName);
    // do expose
    assertNull(call_validate(action, exposedName));
    ExecutionUtils.run(widget, new RunnableEx() {
      public void run() throws Exception {
        ReflectionUtils.invokeMethod2(action, "expose", boolean.class, isPublic);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog UI
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_animateDialog() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    assertNotNull(button);
    // prepare action
    final IAction action = getExposeAction(button, "text");
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Expose property");
        // prepare widgets
        Text textWidget = context.getTextByLabel("Property name:");
        StyledText previewWidget = (StyledText) context.getControlAfterLabel("Preview:");
        Button okButton = context.getButtonByText("OK");
        // initial state
        {
          assertEquals("buttonText", textWidget.getText());
          assertThat(previewWidget.getText()).contains("getButtonText()");
          assertTrue(okButton.isEnabled());
        }
        // set wrong property name
        {
          textWidget.setText("wrong name");
          assertThat(previewWidget.getText()).isEqualTo("No preview");
          assertFalse(okButton.isEnabled());
        }
        // set good name again
        {
          textWidget.setText("myText");
          assertThat(previewWidget.getText()).contains("getMyText()");
          assertTrue(okButton.isEnabled());
        }
        // OK
        context.clickButton(okButton);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button button;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  public String getMyText() {",
        "    return button.getText();",
        "  }",
        "  public void setMyText(String text) {",
        "    button.setText(text);",
        "  }",
        "}");
  }
}
