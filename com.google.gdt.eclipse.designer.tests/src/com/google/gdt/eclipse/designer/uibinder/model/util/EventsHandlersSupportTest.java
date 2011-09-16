/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link EventHandlersSupport}.
 * 
 * @author scheglov_ke
 */
public class EventsHandlersSupportTest extends UiBinderModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureModule(null);
  }

  @Override
  protected void tearDown() throws Exception {
    DesignerPlugin.getActivePage().closeAllEditors(false);
    super.tearDown();
  }

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
  public void test_Button_hasHandlers() throws Exception {
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
    //
    assertNotNull(PropertyUtils.getByPath(button, "Events/onBlur"));
    assertNotNull(PropertyUtils.getByPath(button, "Events/onClick"));
    assertNotNull(PropertyUtils.getByPath(button, "Events/onFocus"));
    assertNotNull(PropertyUtils.getByPath(button, "Events/onKeyDown"));
    assertNotNull(PropertyUtils.getByPath(button, "Events/onKeyPress"));
    assertNotNull(PropertyUtils.getByPath(button, "Events/onKeyUp"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMethod()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethod_canNotHave_noName_attribute() throws Exception {
    createTestClass("// nothing");
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
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
  }

  public void test_getMethod_canNotHave_noName_inJava() throws Exception {
    createTestClass("// nothing");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertNotNull(property);
    assertFalse(property.isModified());
  }

  public void test_getMethod_canNotHave_noEventType() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "  @UiHandler('button')",
        "  void handleBlur(BlurEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
  }

  public void test_getMethod_canHave_butCommented() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "  //@UiHandler('button')",
        "  //void someMethod(ClickEvent event) {");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
  }

  public void test_getMethod_canHave_noUiHandlerAnnotation() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "  //@UiHandler('button')",
        "  void handleBlur(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
  }

  public void test_getMethod_hasExisting() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertTrue(property.isModified());
    assertEquals("someMethod", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // openListener()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openListener_hasExisting() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertTrue(property.isModified());
    assertEquals("someMethod", getPropertyText(property));
    // do open
    openListener(property);
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    // no changes in Java
    assertJava(decorateTestClassLines(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }"));
    // method is under cursor
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("@UiHandler(");
  }

  public void test_openListener_doubleClick() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertTrue(property.isModified());
    assertEquals("someMethod", getPropertyText(property));
    // do open
    doPropertyDoubleClick(property, null);
    waitEventLoop(0);
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    // no changes in Java
    assertJava(decorateTestClassLines(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }"));
    // method is under cursor
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("@UiHandler(");
  }

  public void test_openListener_generateNew() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler");
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
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
    // do open
    openListener(property);
    // XML and Java updated
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(decorateTestClassLines(
        "  @UiField Button button;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiHandler('button')",
        "  void onButtonClick(ClickEvent event) {",
        "  }"));
    assertTrue(property.isModified());
    assertEquals("onButtonClick", getPropertyText(property));
    // Java editor opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
      // method is under cursor
      String source = getJavaSource_afterCursor(activeEditor);
      assertThat(source).startsWith("@UiHandler(");
    }
  }

  public void test_openListener_generateNew_whenHasOtherHandler() throws Exception {
    createTestClass(
        "  @UiField Button existing;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiHandler('existing')",
        "  void onExistingClick(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button ui:field='existing'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
    // do open
    openListener(property);
    // XML and Java updated
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button ui:field='existing'/>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(decorateTestClassLines(
        "  @UiField Button existing;",
        "  @UiField Button button;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiHandler('existing')",
        "  void onExistingClick(ClickEvent event) {",
        "  }",
        "  @UiHandler('button')",
        "  void onButtonClick(ClickEvent event) {",
        "  }"));
    assertTrue(property.isModified());
    assertEquals("onButtonClick", getPropertyText(property));
    // Java editor opened
    {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
      // method is under cursor
      String source = getJavaSource_afterCursor(activeEditor);
      assertThat(source).startsWith("@UiHandler(\"button");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeListener()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeListener_noName() throws Exception {
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
    //
    Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertFalse(property.isModified());
    // try to remove, nothing happens
    property.setValue(Property.UNKNOWN_VALUE);
  }

  public void test_removeListener_hasExisting() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void someMethod(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    final Property property = PropertyUtils.getByPath(button, "Events/onClick");
    assertTrue(property.isModified());
    assertEquals("someMethod", getPropertyText(property));
    // do remove
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        property.setValue(Property.UNKNOWN_VALUE);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("OK");
      }
    });
    // no handler now
    assertFalse(property.isModified());
    assertJava(decorateTestClassLines(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  @UiField Button button;",
        "  @UiHandler('button')",
        "  void onButtonClick(ClickEvent event) {",
        "  }");
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    IMenuManager contextMenu = getContextMenu(button);
    // check action for existing "onClick" event
    {
      IAction action = findChildAction(contextMenu, "onClick -> onButtonClick");
      assertNotNull(action);
      assertSame(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR, action.getImageDescriptor());
      // run, no change expected
      String expectedSource = getJavaSourceToAssert();
      action.run();
      assertEquals(expectedSource, getJavaSourceToAssert());
    }
    // add new method using action
    {
      IMenuManager manager2 = findChildMenuManager(contextMenu, "Add event handler");
      IAction action = findChildAction(manager2, "onFocus");
      assertNotNull(action);
      // run, new method should be added
      action.run();
      waitEventLoop(0);
      DesignerPlugin.getActiveEditor().doSave(null);
      assertJava(decorateTestClassLines(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "  @UiField Button button;",
          "  @UiHandler('button')",
          "  void onButtonClick(ClickEvent event) {",
          "  }",
          "  @UiHandler('button')",
          "  void onButtonFocus(FocusEvent event) {",
          "  }"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTestClass(String... lines) throws Exception {
    lines = decorateTestClassLines(lines);
    setFileContentSrc("test/client/Test.java", getJavaSource(lines));
    waitForAutoBuild();
  }

  private static String[] decorateTestClassLines(String... lines) {
    return CodeUtils.join(
        new String[]{
            "public class Test extends Composite {",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);"},
        lines,
        new String[]{"}"});
  }

  private static void openListener(final Property property) throws Exception {
    XmlObjectInfo object = ((XmlProperty) property).getObject();
    ExecutionUtils.run(object, new RunnableEx() {
      public void run() throws Exception {
        ReflectionUtils.invokeMethod(property, "openListener()");
      }
    });
    waitEventLoop(0);
  }

  private String getJavaSource_afterCursor(IEditorPart activeEditor) throws Exception {
    ITextEditor textEditor = (ITextEditor) activeEditor;
    ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
    return getJavaSourceToAssert().substring(selection.getOffset());
  }
}