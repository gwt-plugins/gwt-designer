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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.uibinder.IExceptionConstants;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.refactoring.RenameSupport;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link NameSupport}.
 * 
 * @author scheglov_ke
 */
public class NameSupportTest extends UiBinderModelTest {
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
  /**
   * UiBinder generator generates statements like <code>owner.fieldName = widgetInstance;</code>.
   * But we pass <code>null</code> as owner, because we can not create it. So, there should be tweak
   * in generator to check for "no owner" case.
   */
  @DisposeProjectAfter
  public void test_UiField_andNoOwner() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel flowPanel;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='flowPanel'/>",
        "</ui:UiBinder>");
    refresh();
    assertNoErrors();
  }

  /**
   * UiBinder checks that each "@UiField" has corresponding widget with "ui:field" attribute. But at
   * design time we may remove attribute and "@UiField". Unfortunately we can not say GWT that Java
   * source was changed. So, this check should be disabled in UiBinder generator (I should send new
   * patch).
   */
  @DisposeProjectAfter
  public void test_UiField_hasJavaField_noFieldAttribute() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel noSuchWidget;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
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
  // getName()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getName_noName() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String name = NameSupport.getName(panel);
    assertEquals(null, name);
  }

  public void test_getName_hasName() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='flowPanel'/>",
            "</ui:UiBinder>");
    refresh();
    // has name
    {
      String name = NameSupport.getName(panel);
      assertEquals("flowPanel", name);
    }
    // ...and it is included into presentation text
    {
      String text = ObjectsLabelProvider.INSTANCE.getText(panel);
      assertEquals("g:FlowPanel - flowPanel", text);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ensureName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}
   */
  public void test_ensureName_hasName() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='flowPanel'/>",
            "</ui:UiBinder>");
    refresh();
    //
    String name = NameSupport.ensureName(panel);
    assertEquals("flowPanel", name);
  }

  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}
   */
  public void test_ensureName_newName() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String name = NameSupport.ensureName(panel);
    assertEquals("flowPanel", name);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='flowPanel'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel flowPanel;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}
   */
  public void test_ensureName_uniqueName_hasAttribute() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button button;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_1' ui:field='button'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    String name = NameSupport.ensureName(button_2);
    assertEquals("button_1", name);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_1' ui:field='button'/>",
        "    <g:Button wbp:name='button_2' ui:field='button_1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button button;",
        "  @UiField Button button_1;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}.
   * <p>
   * Type has field with conflicting name, not "@UiField", just some field.
   */
  public void test_ensureName_uniqueName_hasField() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  Object flowPanel;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String name = NameSupport.ensureName(panel);
    assertEquals("flowPanel_1", name);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='flowPanel_1'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel flowPanel_1;",
        "  Object flowPanel;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}
   */
  public void test_ensureName_nameInParameter() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    XmlObjectUtils.setParameter(panel, NamesManager.NAME_PARAMETER, "myPanel");
    String name = NameSupport.ensureName(panel);
    assertEquals("myPanel", name);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='myPanel'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel myPanel;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#ensureName(XmlObjectInfo)}
   */
  public void test_ensureName_nameInPreferences() throws Exception {
    // set descriptions
    {
      List<ComponentNameDescription> descriptions = Lists.newArrayList();
      descriptions.add(new ComponentNameDescription("com.google.gwt.user.client.ui.FlowPanel",
          "flowPan",
          "fPan"));
      NamesManager.setNameDescriptions(TOOLKIT, descriptions);
    }
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String name = NameSupport.ensureName(panel);
    assertEquals("flowPan", name);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='flowPan'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel flowPan;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NameSupport#removeName(XmlObjectInfo)}.
   */
  public void test_removeName_noName() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    NameSupport.removeName(panel);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#removeName(XmlObjectInfo)}.
   */
  public void test_removeName_doRemove() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button button;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiHandler('button')",
            "  void onButtonClick(ClickEvent event) {",
            "  }",
            "}"));
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
    NameSupport.removeName(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   */
  public void test_setName_newName_afterLastUiField() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiField Widget someUiFieldA;",
            "  @UiField Widget someUiFieldB;",
            "  int foo;",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    NameSupport.setName(panel, "myPanel");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='myPanel'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  @UiField Widget someUiFieldA;",
        "  @UiField Widget someUiFieldB;",
        "  @UiField FlowPanel myPanel;",
        "  int foo;",
        "}");
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   */
  public void test_setName_newName_afterBinderCreate() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  int foo;",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    NameSupport.setName(panel, "myPanel");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='myPanel'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel myPanel;",
        "  int foo;",
        "}");
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   * <p>
   * Probably impossible situation.
   */
  public void test_setName_newName_endOfType() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    NameSupport.setName(panel, "myPanel");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='myPanel'/>",
        "</ui:UiBinder>");
    assertJava(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  @UiField FlowPanel myPanel;",
        "}");
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   */
  public void test_setName_updateName() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel oldName;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='oldName'/>",
            "</ui:UiBinder>");
    refresh();
    //
    NameSupport.setName(panel, "newName");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='newName'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel newName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   * <p>
   * Update also "@UiHandler". Don't update handler name, because it does not start with
   * "onOldName".
   */
  public void test_setName_updateName_noHandlerRename() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button oldName;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiHandler('oldName')",
            "  void onButtonClick(ClickEvent event) {",
            "  }",
            "}"));
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='oldName'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    NameSupport.setName(button, "newName");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='newName'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button newName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  @UiHandler('newName')",
        "  void onButtonClick(ClickEvent event) {",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   * <p>
   * Update also "@UiHandler". Update also handler name, because it starts with "onOldName".
   */
  public void test_setName_updateName_doHandlerRename() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button oldName;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiHandler('oldName')",
            "  void onOldNameClick(ClickEvent event) {",
            "  }",
            "  private void someMethod() {",
            "    onOldNameClick(null);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/TestUser.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "public class TestUser {",
            "  private void useTest(Test test) {",
            "    test.onOldNameClick(null);",
            "  }",
            "}"));
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='oldName'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    NameSupport.setName(button, "newName");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='newName'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button newName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  @UiHandler('newName')",
        "  void onNewNameClick(ClickEvent event) {",
        "  }",
        "  private void someMethod() {",
        "    onNewNameClick(null);",
        "  }",
        "}");
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "public class TestUser {",
            "  private void useTest(Test test) {",
            "    test.onNewNameClick(null);",
            "  }",
            "}"),
        getFileContentSrc("test/client/TestUser.java"));
  }

  /**
   * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
   * <p>
   * Update also "@UiHandler". Update only renaming widget artifacts, but ignore others.
   */
  public void test_setName_updateName_doHandlerRename_skipOthers() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button button;",
            "  @UiField Button button_1;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiHandler('button')",
            "  void onButtonClick(ClickEvent event) {",
            "  }",
            "  @UiHandler('button_1')",
            "  void onButton_1Click(ClickEvent event) {",
            "  }",
            "}"));
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='button'/>",
        "    <g:Button ui:field='button_1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    NameSupport.setName(button, "buttonA");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' ui:field='buttonA'/>",
        "    <g:Button ui:field='button_1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button buttonA;",
        "  @UiField Button button_1;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  @UiHandler('buttonA')",
        "  void onButtonAClick(ClickEvent event) {",
        "  }",
        "  @UiHandler('button_1')",
        "  void onButton_1Click(ClickEvent event) {",
        "  }",
        "}");
  }

  /**
   * Change name using rename of "@UiField" in Java.
   * <p>
   * When we use {@link NameSupport#setName(String)} we update <code>*.ui.xml</code> template
   * ourself, but we want to test also rename participant feature of changing template.
   */
  public void test_setName_usingRenameSupport() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel oldName;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='oldName'/>",
        "</ui:UiBinder>");
    refresh();
    // do @UiField rename
    {
      UiBinderContext context = (UiBinderContext) m_lastContext;
      IField modelField = context.getFormType().getField("oldName");
      RenameSupport renameSupport =
          RenameSupport.create(modelField, "newName", RenameSupport.UPDATE_REFERENCES);
      renameSupport.perform(DesignerPlugin.getShell(), DesignerPlugin.getActiveWorkbenchWindow());
    }
    assertEquals(
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='newName'/>",
            "</ui:UiBinder>"),
        getFileContentSrc("test/client/Test.ui.xml"));
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel newName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * We should not fail when we rename field in non-UiBinder file.
   * <p>
   * http://forums.instantiations.com/viewtopic.php?f=1&t=6215
   */
  @DisposeProjectAfter
  public void test_setName_notUiBinder() throws Exception {
    IType type =
        createModelType(
            "test",
            "Test.java",
            getSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public class Test {",
                "  private int myField;",
                "}"));
    IField field = type.getField("myField");
    // track logged exception
    addExceptionsListener();
    try {
      RenameSupport renameSupport =
          RenameSupport.create(field, "newName", RenameSupport.UPDATE_REFERENCES);
      renameSupport.perform(DesignerPlugin.getShell(), DesignerPlugin.getActiveWorkbenchWindow());
    } finally {
      removeExceptionsListener();
    }
    // no exceptions
    assertNoLoggedExceptions();
    // valid result
    assertEquals(
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  private int newName;",
            "}"),
        getFileContentSrc("test/Test.java"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // validateName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NameSupport#validateName(XmlObjectInfo, String)}.
   */
  public void test_validateName_good() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String errorMessage = NameSupport.validateName(panel, "myPanel");
    assertThat(errorMessage).isNull();
  }

  /**
   * Test for {@link NameSupport#validateName(XmlObjectInfo, String)}.
   */
  public void test_validateName_badIdentifier() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String errorMessage = NameSupport.validateName(panel, "-badIdentifier");
    assertThat(errorMessage).contains("is not a valid Java identifier");
  }

  /**
   * Test for {@link NameSupport#validateName(XmlObjectInfo, String)}.
   */
  public void test_validateName_duplicateField() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel myPanel;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    String errorMessage = NameSupport.validateName(panel, "myPanel");
    assertThat(errorMessage).contains("myPanel").contains("already exists");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // createFieldProvided()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link NameSupport#ensureFieldProvided_onCreate(XmlObjectInfo)}.
   */
  public void test_createFieldProvided() throws Exception {
    prepare_createFieldProvided();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // do CREATE
    {
      WidgetInfo newButton = createObject("test.client.MyButton");
      flowContainer_CREATE(panel, newButton, null);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyButton ui:field='myButton'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField(provided=true) MyButton<Number, String> myButton = new MyButton<Number, String>(5);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * Test for {@link NameSupport#ensureFieldProvided_onCreate(XmlObjectInfo)}.
   * <p>
   * We should show exception when GWT version is less than 2.1.1
   * <p>
   * TODO(scheglov) 20120210 Disabled because of memory leaks.
   */
  public void _test_createFieldProvided_whenWrongVersion() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_2_1_0());
    prepare_createFieldProvided();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo newButton = createObject("test.client.MyButton");
    // do CREATE
    try {
      flowContainer_CREATE(panel, newButton, null);
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.UI_FIELD_FACTORY_FEATURE, e.getCode());
    }
  }

  private void prepare_createFieldProvided() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton<T, S> extends Button {",
            "  public MyButton(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.client.MyButton<%T%, %S%>(5)]]></source>",
            "    <typeParameters>",
            "      <typeParameter name='T' type='java.lang.Number' title='Generic type &lt;T&gt;'/>",
            "      <typeParameter name='S' type='java.lang.String' title='Generic type &lt;S&gt;'/>",
            "    </typeParameters>",
            "  </creation>",
            "  <parameters>",
            "    <parameter name='UiBinder.createFieldProvided'>true</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that deleting widget removes its "@UiField" and handlers.
   */
  public void test_delete_whenExisting() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField Button button;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "  @UiHandler('button')",
            "  void onButtonClick(ClickEvent event) {",
            "  }",
            "}"));
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
    // do delete
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  /**
   * There was problem that when widget was just added, then broadcast listener to remove its "name"
   * worked before broadcast listener to remove event handlers. So, we were not able to get name and
   * remove handlers.
   */
  public void test_delete_whenJustAdded() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // add new Button
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(panel, newButton, null);
    // open listener
    {
      final Property property = PropertyUtils.getByPath(newButton, "Events/onClick");
      ExecutionUtils.run(newButton, new RunnableEx() {
        public void run() throws Exception {
          ReflectionUtils.invokeMethod(property, "openListener()");
        }
      });
      waitEventLoop(0);
    }
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField Button button;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "  @UiHandler('button')",
        "  void onButtonClick(ClickEvent event) {",
        "  }",
        "}");
    // do delete
    newButton.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getObject() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button/>",
            "    <g:Button ui:field='myButton'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // check names
    assertEquals(null, NameSupport.getName(button_1));
    assertEquals("myButton", NameSupport.getName(button_2));
    // use getObject()
    assertSame(null, NameSupport.getObject(panel, "noSuchName"));
    assertSame(button_2, NameSupport.getObject(panel, "myButton"));
  }
}