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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.GwtExceptionRewriter;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelCreationSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;

import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.core.model.property.EventsPropertyTest;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Test {@link WidgetInfo}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 */
public class WidgetTest extends GwtModelTest {
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
  public void test_emptyRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    frame.refresh();
    // RootPanel_CreationSupport
    {
      RootPanelCreationSupport creationSupport =
          (RootPanelCreationSupport) frame.getCreationSupport();
      assertEquals("RootPanel.get()", m_lastEditor.getSource(creationSupport.getNode()));
      assertTrue(creationSupport.canDelete());
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // default bounds
    {
      assertEquals(new Rectangle(0, 0, 450, 300), frame.getBounds());
      assertEquals(new Rectangle(0, 0, 450, 300), frame.getModelBounds());
    }
    // check GWT_TopBoundsSupport, set new size
    {
      TopBoundsSupport topBoundsSupport = frame.getTopBoundsSupport();
      topBoundsSupport.setSize(500, 400);
      frame.refresh();
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getBounds());
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getModelBounds());
    }
  }

  public void test_RootPanel_duplicateGet() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    RootPanel.get().setVisible(true);",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /RootPanel.get().setVisible(true)/}");
    frame.refresh();
  }

  public void test_RootPanel_delete() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setVisible(true);",
            "  }",
            "}");
    // delete
    assertTrue(frame.canDelete());
    frame.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }

  public void test_RootPanel_getWithString() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get('id');",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get('id')/}");
    frame.refresh();
  }

  /**
   * GWT Designer allows designing forms using widgets, not widgets itself.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48461
   */
  public void test_parse_noSupportForDirectWidget() throws Exception {
    try {
      parseJavaInfo(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "public class Test extends Widget {",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_DESIGN_WIDGET, e.getCode());
    }
  }

  /**
   * If some non-Java project is required, this should not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47832
   */
  @DisposeProjectAfter
  public void test_parse_requiredNotJavaProject() throws Exception {
    TestProject requiredProject = new TestProject("requiredProject");
    // work with "requiredProject"
    try {
      ProjectUtils.removeNature(requiredProject.getProject(), JavaCore.NATURE_ID);
      m_testProject.addRequiredProject(requiredProject);
      // parse
      dontUseSharedGWTState();
      parseJavaInfo(
          "// filler filler filler filler filler",
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "  }",
          "}");
    } finally {
      requiredProject.dispose();
    }
  }

  public void test_withButton() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Button button = new Button('My text');",
            "    rootPanel.add(button, 10, 20);",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(button, 10, 20)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('My text')/ /rootPanel.add(button, 10, 20)/}");
    // do refresh
    frame.refresh();
    // exactly one widget expected on RootPanel
    {
      Object rootPanel = frame.getObject();
      assertEquals(1, ReflectionUtils.invokeMethod(rootPanel, "getWidgetCount()"));
      // check "text" for Button
      Object button = ReflectionUtils.invokeMethod(rootPanel, "getWidget(int)", 0);
      assertEquals("My text", ReflectionUtils.invokeMethod(button, "getText()"));
    }
  }

  /**
   * If there is RootPanel, it should be rendered, not DialogBox, even if DialogBox has bigger
   * hierarchy.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=2612
   */
  public void test_withDialogBox() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new CheckBox());",
            "    //",
            "    DialogBox dialog = new DialogBox();",
            "    VerticalPanel verticalPanel = new VerticalPanel();",
            "    dialog.setWidget(verticalPanel);",
            "    verticalPanel.add(new Button());",
            "    verticalPanel.add(new Button());",
            "    verticalPanel.add(new Button());",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(new CheckBox())/}",
        "  {new: com.google.gwt.user.client.ui.CheckBox} {empty} {/rootPanel.add(new CheckBox())/}");
    // do refresh
    frame.refresh();
    assertNoErrors(frame);
  }

  /**
   * Creation of inner non-static class should be ignored.
   */
  public void test_nonStaticInnerClass() throws Exception {
    m_waitForAutoBuild = true;
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.add(new MyButton());",
        "  }",
        "  private class MyButton extends Button {",
        "  }",
        "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(new MyButton())/}");
  }

  /**
   * We should be able to parse, even if there are errors in other compilation units.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41120
   */
  public void test_whenCompilationErrors_inOtherClasses() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Bad.java",
        getTestSource(
            "public class Bad extends Button {",
            "  void foo() {",
            "    com.google.gwt.xml.client.XMLParser parser = null;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Good.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Good extends Button {",
            "  // filler filler filler",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Bad bad = new Bad();",
        "      add(bad);",
        "    }",
        "    {",
        "      Good good = new Good();",
        "      add(good);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.FlowPanel} {this} {/add(bad)/ /add(good)/}",
        "  {new: test.client.Bad} {local-unique: bad} {/new Bad()/ /add(bad)/}",
        "  {new: test.client.Good} {local-unique: good} {/new Good()/ /add(good)/}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  /**
   * We should be able to parse, where there is error in some method and this method also declares
   * local type.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48035
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47895
   */
  public void test_whenCompilationError_inMethod() throws Exception {
    dontUseSharedGWTState();
    m_ignoreCompilationProblems = true;
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "  private void foo() {",
        "    Bad bad = new Bad();",
        "    Object myObject = new MyObject() {",
        "    };",
        "  }",
        "}");
    assertHierarchy("{this: com.google.gwt.user.client.ui.FlowPanel} {this} {}");
  }

  /**
   * We should not fail with {@link NullPointerException} when custom subclass of
   * <code>EntryPoint</code> is used, so that we don't see <code>onModuleLoad()</code> method.
   * <p>
   * But we fail with {@link NoEntryPointError}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42379
   */
  public void test_customEntryPoint_subclass() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyEntryPoint.java",
        getTestSource(
            "public class MyEntryPoint implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    onModuleLoad2();",
            "  }",
            "  public void onModuleLoad2() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    try {
      parseJavaInfo(
          "public class Test extends MyEntryPoint {",
          "  public void onModuleLoad2() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "  }",
          "}");
      fail();
    } catch (NoEntryPointError e) {
    }
  }

  /**
   * Test that GWT supports "@wbp.parser.entryPoint".
   */
  public void test_parser_entryPoint() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public void myCustomEntryPoint() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    //
    frame.refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exceptions in constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_constructorEvaluation_actualOnlyException_placeholder(String exceptionNodeSource)
      throws Exception {
    check_constructorEvaluation_placeholder();
    check_constructorEvaluation_actualOnlyException(exceptionNodeSource);
  }

  private void check_constructorEvaluation_placeholder() throws Exception {
    RootPanelInfo frame = (RootPanelInfo) m_lastParseInfo;
    frame.refresh();
    // prepare "MyButton"
    WidgetInfo badComponent = frame.getChildrenWidgets().get(0);
    Object badComponentObject = badComponent.getObject();
    // "MyButton" has placeholder object - Composite
    assertThat(badComponentObject.getClass().getName()).isEqualTo(
        "com.google.gwt.user.client.ui.HTML");
    assertTrue(badComponent.isPlaceholder());
    // "shell" has only one Control child (we should remove partially create MyButton instance)
    {
      List<?> children = frame.getUIObjectUtils().getRootPanelWidgets();
      assertThat(children).hasSize(1).containsOnly(badComponentObject);
    }
  }

  private void check_constructorEvaluation_actualOnlyException(String exceptionNodeSource) {
    List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(badNodes).hasSize(1);
    {
      BadNodeInformation badNode = badNodes.get(0);
      check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "actual");
    }
  }

  private void check_constructorEvaluation_badNode(BadNodeInformation badNode,
      String exceptionNodeSource,
      String exceptionMessage) {
    ASTNode node = badNode.getNode();
    Throwable nodeException = badNode.getException();
    // check node
    assertEquals(exceptionNodeSource, m_lastEditor.getSource(node));
    // check exception
    {
      Throwable e = DesignerExceptionUtils.getRootCause(nodeException);
      assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      assertThat(e.getMessage()).isEqualTo(exceptionMessage);
    }
    // exception should be associated with node
    assertThat(PlaceholderUtils.getExceptions(node)).contains(nodeException);
  }

  /**
   * Good actual constructor.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42522
   */
  public void test_constructorEvaluation_goodActual() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Button());",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    // no exceptions
    assertNoErrors(frame);
    assertFalse(button.isPlaceholder());
  }

  /**
   * Exception in actual constructor. Use default constructor.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42522
   */
  public void test_constructorEvaluation_exceptionActual_goodDefault() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton() {",
            "  }",
            "  public MyButton(int value) {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new MyButton(0));",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    // has exception
    check_constructorEvaluation_actualOnlyException("new MyButton(0)");
    // but not placeholder
    assertFalse(button.isPlaceholder());
  }

  /**
   * Exception in actual constructor. No default constructor, so fail.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42522
   */
  public void test_constructorEvaluation_exceptionActual_noDefault() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton(int value) {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.add(new MyButton(0));",
        "  }",
        "}");
    check_constructorEvaluation_actualOnlyException_placeholder("new MyButton(0)");
  }

  /**
   * Exception in actual constructor. Exception in default too. So, create placeholder.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42522
   */
  public void test_constructorEvaluation_exceptionActual_exceptionDefault() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton() {",
            "    throw new IllegalStateException('default');",
            "  }",
            "  public MyButton(int value) {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.add(new MyButton(0));",
        "  }",
        "}");
    // placeholder should be created
    check_constructorEvaluation_placeholder();
    // check logged exceptions
    {
      String exceptionNodeSource = "new MyButton(0)";
      List<BadNodeInformation> badNodes = m_lastState.getBadRefreshNodes().nodes();
      assertThat(badNodes).hasSize(2);
      {
        BadNodeInformation badNode = badNodes.get(0);
        check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "actual");
      }
      {
        BadNodeInformation badNode = badNodes.get(1);
        check_constructorEvaluation_badNode(badNode, exceptionNodeSource, "default");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Some GWT widgets may behave not so nice and left Element's on RootPanel. Also, same problems
   * may be cause by using some JavaScript's, for example ExtJs. So, we have to remove all
   * Element's, not just remove Widget's from RootPanel.
   */
  public void test_removeAllElements() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Button());",
            "  }",
            "}");
    frame.refresh();
    UIObjectUtils objectUtils = frame.getUIObjectUtils();
    DOMUtils domUtils = frame.getDOMUtils();
    //
    Object rootPanel = objectUtils.getRootPanel();
    Object rootPanelElement = objectUtils.getElement(rootPanel);
    // when RootPanel is live, we have much of Element's
    domUtils.appendChild(rootPanelElement, domUtils.createButton());
    {
      int count = domUtils.getChildCount(rootPanelElement);
      assertThat(count).isGreaterThan(1);
    }
    // dispose, so now RootPanel should be empty
    frame.refresh_dispose();
    // still has elements: "css wait" div, "history" frame, "layout" div
    {
      Object[] children = domUtils.getChildren(rootPanelElement);
      assertThat(children).hasSize(3);
      assertThat(children[0].toString()).contains("__gwt_historyFrame");
      assertThat(children[1].toString()).contains("wbp__wait_stylesheet");
      assertThat(children[2].toString()).contains("-20cm").contains("10cm");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that <code>UIObject.setPixelSize(int,int)</code> is executable.
   */
  public void test_setPixelSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      button.setPixelSize(100, 40);",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    assertEquals(100, button.getBounds().width);
    assertEquals(40, button.getBounds().height);
  }

  /**
   * Test that <code>UIObject.setSize(String,String)</code> is executable.
   */
  public void test_setSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      button.setSize('100px', '40px');",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    assertEquals(100, button.getBounds().width);
    assertEquals(40, button.getBounds().height);
  }

  /**
   * Test that <code>HasHTML.setHTML(String)</code> has nice title "html", not just default "hTML".
   */
  public void test_setHTML() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    assertNotNull(button.getPropertyByTitle("html"));
    assertNull(button.getPropertyByTitle("hTML"));
    assertNull(button.getPropertyByTitle("HTML"));
  }

  /**
   * Test for automatic renaming GWT widgets on "text" property change.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42832
   */
  public void test_renameOnTextPropertyChange() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    IPreferenceStore preferences = GwtToolkitDescription.INSTANCE.getPreferences();
    preferences.setValue(
        IPreferenceConstants.P_VARIABLE_TEXT_MODE,
        IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
    preferences.setValue(IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE, "${text}${class_name}");
    button.getPropertyByTitle("html").setValue("Some text");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button someTextButton = new Button();",
        "      someTextButton.setHTML('Some text');",
        "      rootPanel.add(someTextButton);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Event listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * In GWT event handlers may have several parameters, for example see
   * <code>KeyboardListener</code>.
   */
  public void test_eventListener_multipleParameters() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    EventsPropertyTest.ensureListenerMethod(button, "keyboard", "onKeyDown");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      button.addKeyboardListener(new KeyboardListenerAdapter() {",
        "        @Override",
        "        public void onKeyDown(Widget sender, char keyCode, int modifiers) {",
        "        }",
        "      });",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * <code>Button</code> has <code>ClickListener</code> in constructor, so when we ask for
   * "click/onClick" no second listener should be added.
   */
  public void test_eventListener_listenerInConstructor() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button('My HTML', new ClickListener() {",
            "        public void onClick(Widget sender) {int marker;}",
            "      });",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    MethodDeclaration onClickMethod =
        EventsPropertyTest.ensureListenerMethod(
            button,
            "click(com.google.gwt.user.client.ui.ClickListener)",
            "onClick");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button('My HTML', new ClickListener() {",
        "        public void onClick(Widget sender) {int marker;}",
        "      });",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    assertEquals(
        "public void onClick(Widget sender) {int marker;}",
        m_lastEditor.getSource(onClickMethod));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_reparseOnCss() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      button.setStyleName('test');",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    assertThat(button.getBounds().width).isLessThan(200);
    // initially no refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      frame.getBroadcast(EditorActivatedListener.class).invoke(request);
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
            "  width: 500px;",
            "}"));
    waitForAutoBuild();
    // now refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      frame.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertTrue(request.isRefreshRequested());
    }
    // do refresh, as requested
    refresh();
    assertThat(button.getBounds().width).isEqualTo(500);
  }

  @DisposeProjectAfter
  public void test_reparseOnCss_andDontHurtCoordinates() throws Exception {
    dontUseSharedGWTState();
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "    setStyleName('test');",
            "  }",
            "}");
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
      int width = image.getBounds().width;
      int height = image.getBounds().height;
      assertRGB(getPixelRGB(image, 0, 0), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, width - 1, 0), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, 0, height - 1), 0xFF, 0, 0);
      assertRGB(getPixelRGB(image, width - 1, height - 1), 0xFF, 0, 0);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests for GWTExceptionRewriter which need UI.
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test rewrite for com.google.gwt.core.ext.UnableToCompleteException.
   */
  @DisposeProjectAfter
  public void test_GWTExceptionRewriter_UnableToCompleteException() throws Exception {
    Throwable e = prepareThrowable();
    assertThat(e.getClass().getName()).isEqualTo(
        "com.google.gwt.core.ext.UnableToCompleteException");
    // try plain
    {
      DesignerException result = (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(e);
      assertThat(result.getCode()).isEqualTo(HostedModeException.MODULE_LOADING_ERROR2);
      // at testing time we don't have logger
      assertEquals("<none>", result.getParameters()[0]);
    }
    // try wrapped (1 level)
    {
      Throwable wrapper = new RuntimeException(e);
      DesignerException result = (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(wrapper);
      assertThat(result.getCode()).isEqualTo(HostedModeException.MODULE_LOADING_ERROR2);
    }
    // try wrapped (2 levels)
    {
      Throwable wrapper_1 = new InvocationTargetException(e);
      Throwable wrapper_2 = new RuntimeException(wrapper_1);
      DesignerException result =
          (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(wrapper_2);
      assertThat(result.getCode()).isEqualTo(HostedModeException.MODULE_LOADING_ERROR2);
    }
  }

  private Throwable prepareThrowable() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/Module.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <badTagName/>",
            "</module>"));
    waitForAutoBuild();
    // parse
    try {
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "  }",
          "}");
    } catch (Throwable e) {
      return e;
    }
    return null;
  }
}