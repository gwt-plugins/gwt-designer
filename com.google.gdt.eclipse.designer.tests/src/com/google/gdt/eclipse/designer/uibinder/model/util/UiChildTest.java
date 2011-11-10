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
import com.google.gdt.eclipse.designer.uibinder.model.util.UiChildSupport.Position;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import junit.framework.AssertionFailedError;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link UiChildSupport}.
 * 
 * @author scheglov_ke
 */
public class UiChildTest extends UiBinderModelTest {
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
  public void test_setUp() throws Exception {
    UiBinderContext.disposeSharedGWTState();
  }

  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    prepareMyContainer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing @UiChild annotations into {@link Position}s.
   */
  public void test_positions() throws Exception {
    WidgetInfo container =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button_1' text='1'/>",
            "    </t:topbutton>",
            "    <t:topbutton text='B'>",
            "      <g:Button wbp:name='button_2' text='2'/>",
            "    </t:topbutton>",
            "    <t:bottomWidget>",
            "      <g:TextBox wbp:name='textBox'/>",
            "    </t:bottomWidget>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyContainer>",
        "  <g:Button wbp:name='button_1' text='1'>",
        "  <g:Button wbp:name='button_2' text='2'>",
        "  <g:TextBox wbp:name='textBox'>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    WidgetInfo textBox = getObjectByName("textBox");
    // check Button's text
    assertEquals("1", ReflectionUtils.invokeMethod(button_1.getObject(), "getText()"));
    assertEquals("B", ReflectionUtils.invokeMethod(button_2.getObject(), "getText()"));
    // "container" children are Position-s
    List<ObjectInfo> containerChildren = container.getPresentation().getChildrenTree();
    assertThat(containerChildren).hasSize(2);
    // "bottom" position
    {
      Position position = (Position) containerChildren.get(0);
      assertEquals(
          "com.google.gwt.user.client.ui.Widget",
          position.getWidgetClass().getCanonicalName());
      // presentation
      IObjectPresentation presentation = position.getPresentation();
      assertEquals("bottomWidget", presentation.getText());
      assertNotNull(presentation.getIcon());
      // tree children
      List<ObjectInfo> positionChildren = position.getPresentation().getChildrenTree();
      assertThat(positionChildren).containsExactly(textBox);
    }
    // "top" position
    {
      Position position = (Position) containerChildren.get(1);
      assertEquals(
          "com.google.gwt.user.client.ui.Button",
          position.getWidgetClass().getCanonicalName());
      // presentation
      IObjectPresentation presentation = position.getPresentation();
      assertEquals("topbutton", presentation.getText());
      assertNotNull(presentation.getIcon());
      // tree children
      List<ObjectInfo> positionChildren = position.getPresentation().getChildrenTree();
      assertThat(positionChildren).containsExactly(button_1, button_2);
    }
  }

  /**
   * Test for {@link Position#canAddChild()}.
   */
  public void test_canAddChild() throws Exception {
    WidgetInfo container =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button_1' text='1'/>",
            "    </t:topbutton>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button_2' text='2'/>",
            "    </t:topbutton>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button_3' text='3'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_3 = getObjectByName("button_3");
    Position position = getPosition(container, "topbutton");
    // has 3 buttons, can not add more
    assertFalse(position.canAddChild());
    // delete "button_3", can add new one
    button_3.delete();
    assertTrue(position.canAddChild());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UiChild complex property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for @UiChild annotation {@link Property}.
   */
  public void test_positionProperties_notWidget() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // use artificial "testObject" to ask properties, just to get coverage of not Widget case
    XmlObjectInfo testObject =
        new XmlObjectInfo(m_lastContext, panel.getDescription(), panel.getCreationSupport());
    // no "UiChild" property
    {
      Property uiChildProperty = PropertyUtils.getByPath(testObject, "UiChild");
      assertNull(uiChildProperty);
    }
  }

  /**
   * Test for @UiChild annotation {@link Property}.
   */
  public void test_positionProperties_noUiChildProperties() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:bottomWidget>",
        "      <g:Button wbp:name='button'/>",
        "    </t:bottomWidget>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // no "UiChild" property
    {
      Property uiChildProperty = PropertyUtils.getByPath(button, "UiChild");
      assertNull(uiChildProperty);
    }
  }

  /**
   * Test for @UiChild annotation {@link Property}.
   */
  public void test_positionProperties_hasUiChildProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // has "UiChild" property
    {
      Property uiChildProperty = PropertyUtils.getByPath(button, "UiChild");
      assertNotNull(uiChildProperty);
      Property[] childProperties = PropertyUtils.getChildren(uiChildProperty);
      assertThat(childProperties).hasSize(1);
    }
    Property textProperty = PropertyUtils.getByPath(button, "UiChild/text");
    // "text" property is not modified initially
    assertFalse(textProperty.isModified());
    assertSame(Property.UNKNOWN_VALUE, textProperty.getValue());
    // set value
    textProperty.setValue("My text");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton text='My text'>",
        "      <g:Button wbp:name='button'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    assertTrue(textProperty.isModified());
    assertEquals("My text", textProperty.getValue());
    // remove value
    textProperty.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
    assertFalse(textProperty.isModified());
    assertSame(Property.UNKNOWN_VALUE, textProperty.getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Position#command_CREATE(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    WidgetInfo container =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // do create
    Position position = getPosition(container, "topbutton");
    WidgetInfo newButton = createButton();
    position.command_CREATE(newButton, button);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton>",
        "      <g:Button/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link Position#command_MOVE(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE_reorder() throws Exception {
    WidgetInfo container =
        parse(
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button_1' text='1'/>",
            "    </t:topbutton>",
            "    <t:topbutton text='B'>",
            "      <g:Button wbp:name='button_2' text='2'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // do move
    Position position = getPosition(container, "topbutton");
    position.command_MOVE(button_2, button_1);
    assertXML(
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:topbutton text='B'>",
        "      <g:Button wbp:name='button_2' text='2'/>",
        "    </t:topbutton>",
        "    <t:topbutton>",
        "      <g:Button wbp:name='button_1' text='1'/>",
        "    </t:topbutton>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link Position#command_MOVE(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE_newPosition() throws Exception {
    WidgetInfo container =
        parse(
            "<ui:UiBinder>",
            "  <t:MyContainer>",
            "    <t:topbutton>",
            "      <g:Button wbp:name='button'/>",
            "    </t:topbutton>",
            "  </t:MyContainer>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // do move
    Position position = getPosition(container, "bottomWidget");
    position.command_MOVE(button, null);
    assertXML(
        "<ui:UiBinder>",
        "  <t:MyContainer>",
        "    <t:bottomWidget>",
        "      <g:Button wbp:name='button'/>",
        "    </t:bottomWidget>",
        "  </t:MyContainer>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares "test.client.MyContainer" with 2 @UiChild positions.
   */
  static void prepareMyContainer() throws Exception {
    setFileContentSrc(
        "test/client/MyContainer.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.dom.client.Style.Unit;",
            "import com.google.gwt.user.client.ui.*;",
            "import com.google.gwt.uibinder.client.UiChild;",
            "public class MyContainer extends LayoutPanel {",
            "  private FlowPanel topPanel;",
            "  private FlowPanel bottomPanel;",
            "  public MyContainer() {",
            "    {",
            "      topPanel = new FlowPanel();",
            "      add(topPanel);",
            "      setWidgetLeftRight(topPanel, 0.0, Unit.PX, 0.0, Unit.PX);",
            "      setWidgetTopHeight(topPanel, 0.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "    {",
            "      bottomPanel = new FlowPanel();",
            "      add(bottomPanel);",
            "      setWidgetLeftRight(bottomPanel, 0.0, Unit.PX, 0.0, Unit.PX);",
            "      setWidgetBottomHeight(bottomPanel, 0.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "  @UiChild(limit = 3)",
            "  public void addTopButton(Button button, String text) {",
            "    if (text != null) {",
            "      button.setText(text);",
            "    }",
            "    topPanel.add(button);",
            "  }",
            "  @UiChild(tagname = 'bottomWidget')",
            "  public void addBottomChild(Widget child) {",
            "    bottomPanel.add(child);",
            "  }",
            "}"));
    waitForAutoBuild();
    forgetCreatedResources();
  }

  /**
   * @return the {@link Position} which uses given tag name.
   */
  static Position getPosition(WidgetInfo container, String tag) throws Exception {
    List<ObjectInfo> containerChildren = container.getPresentation().getChildrenTree();
    for (ObjectInfo child : containerChildren) {
      if (child instanceof Position) {
        Position position = (Position) child;
        if (position.getTag().equals(tag)) {
          return position;
        }
      }
    }
    throw new AssertionFailedError("Can not find Position " + tag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    UiBinderContext.disposeSharedGWTState();
    super.test_tearDown();
  }
}