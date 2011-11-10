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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link NameProperty}.
 * 
 * @author scheglov_ke
 */
public class NamePropertyTest extends UiBinderModelTest {
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
  public void test_access() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    Property property = panel.getPropertyByTitle("UiField");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    // adapter
    assertThat(getPropertyTooltipText(property)).isNotEmpty();
    assertThat(property.getAdapter(Object.class)).isNull();
    // no value initially
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    assertEquals(null, getPropertyText(property));
    assertEquals(null, getTextEditorText(property));
    // set name
    property.setValue("myPanel");
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
    // has value
    assertTrue(property.isModified());
    assertEquals("myPanel", property.getValue());
    assertEquals("myPanel", getPropertyText(property));
    assertEquals("myPanel", getTextEditorText(property));
    // remove name
    property.setValue(Property.UNKNOWN_VALUE);
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
    // no value again
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    assertEquals(null, getPropertyText(property));
    assertEquals(null, getTextEditorText(property));
  }

  public void test_setName_usingEditor() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  private Object existingName;",
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
    // prepare property
    final Property property = panel.getPropertyByTitle("UiField");
    // no value initially
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // duplicate name
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        setTextEditorText(property, "existingName");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("UiField");
        context.clickButton("OK");
      }
    });
    // still no value
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // set name
    setTextEditorText(property, "myPanel");
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
        "  private Object existingName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
    // has value
    assertTrue(property.isModified());
    assertEquals("myPanel", property.getValue());
  }
}