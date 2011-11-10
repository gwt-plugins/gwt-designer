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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.ImplicitLayoutCreationSupport;
import com.google.gdt.eclipse.designer.gwtext.model.layout.ImplicitLayoutVariableSupport;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 */
public class ContainerTest extends GwtExtModelTest {
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
  // Implicit layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ContainerInfo} without explicit layout has {@link DefaultLayoutInfo}.
   */
  public void test_defaultLayout() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Panel;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Panel container = new Panel();",
            "    rootPanel.add(container);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    // 
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(container)/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: container} {/new Panel()/ /rootPanel.add(container)/}",
        "    {implicit-layout: default} {implicit-layout} {}");
    ContainerInfo container = (ContainerInfo) frame.getChildrenWidgets().get(0);
    assertTrue(container.hasLayout());
  }

  /**
   * Test for {@link ContainerInfo#getLayout()}.
   */
  public void test_getLayoutException() throws Exception {
    ContainerInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Panel;",
            "public class Test extends Panel {",
            "  public Test() {",
            "  }",
            "}");
    // remove Layout (using ObjectInfo, to prevent implicit layout creation)
    frame.removeChild(frame.getLayout());
    try {
      frame.getLayout();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * Test for implicit layout in {@link ContainerInfo}.
   */
  public void test_implicitLayout() throws Exception {
    prepareRowPanel();
    ContainerInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends RowPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    assertHierarchy(
        "{this: test.client.RowPanel} {this} {}",
        "  {implicit-layout: com.gwtext.client.widgets.layout.RowLayout} {implicit-layout} {}");
    assertTrue(panel.hasLayout());
    LayoutInfo layout = panel.getLayout();
    // check association
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    // check creation support
    {
      ImplicitLayoutCreationSupport creationSupport =
          (ImplicitLayoutCreationSupport) layout.getCreationSupport();
      assertEquals(panel.getCreationSupport().getNode(), creationSupport.getNode());
      assertEquals(
          "implicit-layout: com.gwtext.client.widgets.layout.RowLayout",
          creationSupport.toString());
      // validation
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
      // no clipboard
      assertNull(creationSupport.getImplicitClipboard());
    }
    // check variable
    {
      ImplicitLayoutVariableSupport variableSupport =
          (ImplicitLayoutVariableSupport) layout.getVariableSupport();
      assertTrue(variableSupport.isDefault());
      assertEquals("implicit-layout", variableSupport.toString());
      assertEquals("(implicit layout)", variableSupport.getTitle());
      // name
      assertFalse(variableSupport.hasName());
      try {
        variableSupport.getName();
        fail();
      } catch (IllegalStateException e) {
      }
      try {
        variableSupport.setName("foo");
        fail();
      } catch (IllegalStateException e) {
      }
      // conversion
      assertFalse(variableSupport.canConvertLocalToField());
      try {
        variableSupport.convertLocalToField();
        fail();
      } catch (IllegalStateException e) {
      }
      assertFalse(variableSupport.canConvertFieldToLocal());
      try {
        variableSupport.convertFieldToLocal();
        fail();
      } catch (IllegalStateException e) {
      }
    }
    // check association
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
  }

  /**
   * Test for materializing implicit {@link LayoutInfo}.
   */
  public void test_implicitLayout_2() throws Exception {
    prepareRowPanel();
    ContainerInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends RowPanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
    // materialize by asking for expression
    {
      NodeTarget target = getNodeBlockTarget(panel, false);
      String accessExpression = layout.getVariableSupport().getAccessExpression(target);
      assertEquals("rowLayout.", accessExpression);
    }
    // check creation/variable/association
    assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
    assertInstanceOf(LocalUniqueVariableSupport.class, layout.getVariableSupport());
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends RowPanel {",
        "  public Test() {",
        "    RowLayout rowLayout = (RowLayout) getLayout();",
        "  }",
        "}");
  }

  /**
   * Test for parsing materialized implicit layout (with {@link CastExpression}).
   */
  public void test_implicitLayout_3() throws Exception {
    prepareRowPanel();
    ContainerInfo panel =
        parseJavaInfo(
            "import com.gwtext.client.widgets.layout.RowLayout;",
            "public class Test extends RowPanel {",
            "  public Test() {",
            "    RowLayout rowLayout = (RowLayout) getLayout();",
            "  }",
            "}");
    // initial state
    {
      LayoutInfo layout = panel.getLayout();
      {
        CreationSupport creationSupport = layout.getCreationSupport();
        assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
        assertTrue(creationSupport.canDelete());
      }
      {
        VariableSupport variableSupport = layout.getVariableSupport();
        assertInstanceOf(LocalUniqueVariableSupport.class, variableSupport);
        assertEquals("rowLayout", variableSupport.getName());
      }
      assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    }
    // check for "de-materializing" implicit layout
    {
      panel.getLayout().delete();
      assertEditor(
          "import com.gwtext.client.widgets.layout.RowLayout;",
          "public class Test extends RowPanel {",
          "  public Test() {",
          "  }",
          "}");
      // still implicit layout
      LayoutInfo layout = panel.getLayout();
      assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
      assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
      assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
      assertEquals(
          "implicit-layout: com.gwtext.client.widgets.layout.RowLayout",
          layout.getCreationSupport().toString());
    }
  }

  private void prepareRowPanel() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/RowPanel.java",
        getTestSource(
            "import com.gwtext.client.widgets.Panel;",
            "import com.gwtext.client.widgets.layout.RowLayout;",
            "public class RowPanel extends Panel {",
            "  public RowPanel() {",
            "    setLayout(new RowLayout());",
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
   * If new children are added after association, then <code>Container.doLayout()</code> should be
   * used.
   */
  public void test_doLayout() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Panel panel = new Panel();",
            "      rootPanel.add(panel);",
            "      // add Button after Panel association",
            "      panel.add(new Button('New button'));",
            "      // ...so doLayout() required",
            "      panel.doLayout();",
            "    }",
            "  }",
            "}");
    frame.refresh();
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    Rectangle bounds = button.getBounds();
    assertThat(bounds.width).isGreaterThan(75);
    assertThat(bounds.height).isGreaterThan(20);
  }

  /**
   * Children to {@link ContainerInfo} should be added before association.
   */
  public void test_addChild() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Panel panel = new Panel();",
            "    rootPanel.add(panel);",
            "    panel.setPixelSize(300, 200);",
            "  }",
            "}");
    frame.refresh();
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getChildrenWidgets()).isEmpty();
    // add new Button
    WidgetInfo button = createWidget("com.gwtext.client.widgets.Button");
    JavaInfoUtils.add(
        button,
        AssociationObjects.invocationChild("%parent%.add(%child%)", true),
        panel,
        null);
    assertThat(panel.getChildrenWidgets()).containsOnly(button);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Panel panel = new Panel();",
        "    {",
        "      Button button = new Button('New Button');",
        "      panel.add(button);",
        "    }",
        "    rootPanel.add(panel);",
        "    panel.setPixelSize(300, 200);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Panel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertInstanceOf(DefaultLayoutInfo.class, panel.getLayout());
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    // set RowLayout
    {
      LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.RowLayout");
      panel.setLayout(layout);
      assertEditor(
          "// filler filler filler",
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new RowLayout());",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/}",
          "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
      assertSame(layout, panel.getLayout());
    }
    // set ColumnLayout
    {
      LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.ColumnLayout");
      panel.setLayout(layout);
      assertEditor(
          "// filler filler filler",
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new ColumnLayout());",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new ColumnLayout())/}",
          "  {new: com.gwtext.client.widgets.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}");
      assertSame(layout, panel.getLayout());
    }
  }

  /**
   * Test for {@link ContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout_order() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    add(new com.google.gwt.user.client.ui.Button());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(new com.google.gwt.user.client.ui.Button())/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.google.gwt.user.client.ui.Button} {empty} {/add(new com.google.gwt.user.client.ui.Button())/}");
    // set RowLayout
    LayoutInfo layout = createJavaInfo("com.gwtext.client.widgets.layout.RowLayout");
    assertSame(ComponentOrderFirst.INSTANCE, layout.getDescription().getOrder());
    panel.setLayout(layout);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    add(new com.google.gwt.user.client.ui.Button());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(new com.google.gwt.user.client.ui.Button())/ /setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.google.gwt.user.client.ui.Button} {empty} {/add(new com.google.gwt.user.client.ui.Button())/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
  }

  /**
   * Test for deleting {@link LayoutInfo} and replacing with default one.
   */
  public void test_deleteLayout() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
    // delete layout
    panel.getLayout().delete();
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    assertEditor("public class Test extends Panel {", "  public Test() {", "  }", "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ID
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContainerInfo#getID(WidgetInfo)} and
   * {@link ContainerInfo#getWidgetByID(String)}.
   */
  public void test_ID() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    add(new com.gwtext.client.widgets.form.Label());",
            "    add(new com.google.gwt.user.client.ui.Button());",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(1);
    // getID()
    String labelID = ContainerInfo.getID(label);
    String buttonID = ContainerInfo.getID(button);
    assertThat(labelID).startsWith("ext-gen");
    assertThat(buttonID).startsWith("ext-gen");
    // getWidgetByID
    assertSame(null, panel.getWidgetByID("no such ID"));
    assertSame(label, panel.getWidgetByID(labelID));
    assertSame(button, panel.getWidgetByID(buttonID));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Placeholder
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_placeholder_hasLayout() throws Exception {
    dontUseSharedGWTState();
    String[] lines =
        {
            "public class MyContainer extends Container {",
            "  public MyContainer() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"};
    setFileContentSrc("test/client/MyContainer.java", getTestSource(lines));
    setFileContentSrc(
        "test/client/MyContainer.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.has'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    add(new MyContainer());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyContainer} {empty} {/add(new MyContainer())/}");
    // no Layout for placeholder
    ContainerInfo placeholder = (ContainerInfo) panel.getChildrenWidgets().get(0);
    assertTrue(placeholder.isPlaceholder());
    assertFalse(placeholder.hasLayout());
    // only "actual" exception
    panel.refresh();
    assert_placeholderException();
  }

  public void test_placeholder_Panel() throws Exception {
    dontUseSharedGWTState();
    String[] lines =
        {
            "public class MyContainer extends Panel {",
            "  public MyContainer() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"};
    setFileContentSrc("test/client/MyContainer.java", getTestSource(lines));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    add(new MyContainer());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyContainer} {empty} {/add(new MyContainer())/}");
    // no Layout for placeholder
    ContainerInfo placeholder = (ContainerInfo) panel.getChildrenWidgets().get(0);
    assertTrue(placeholder.isPlaceholder());
    assertFalse(placeholder.hasLayout());
    // only "actual" exception
    panel.refresh();
    assert_placeholderException();
  }

  private void assert_placeholderException() {
    List<BadNodeInformation> nodes = m_lastState.getBadRefreshNodes().nodes();
    assertThat(nodes).hasSize(1);
    BadNodeInformation node = nodes.get(0);
    Throwable exception = node.getException();
    exception = DesignerExceptionUtils.getRootCause(exception);
    assertThat(exception).isExactlyInstanceOf(IllegalStateException.class);
    assertEquals("actual", exception.getMessage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Set Layout" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ContainerInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_1() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Panel;",
            "public class Test extends Panel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertTrue(panel.hasLayout());
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager;
    {
      IMenuManager menuManager = getContextMenu(panel);
      layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNotNull(layoutManager);
    }
    // check for existing actions
    assertNotNull(findChildAction(layoutManager, "AbsoluteLayout"));
    assertNotNull(findChildAction(layoutManager, "RowLayout"));
    assertNotNull(findChildAction(layoutManager, "ColumnLayout"));
    assertNotNull(findChildAction(layoutManager, "BorderLayout"));
    assertNotNull(findChildAction(layoutManager, "AnchorLayout"));
    assertNotNull(findChildAction(layoutManager, "FormLayout"));
    assertNotNull(findChildAction(layoutManager, "FitLayout"));
    assertNotNull(findChildAction(layoutManager, "AccordionLayout"));
    assertNotNull(findChildAction(layoutManager, "CardLayout"));
    assertNotNull(findChildAction(layoutManager, "TableLayout"));
    assertNotNull(findChildAction(layoutManager, "HorizontalLayout"));
    assertNotNull(findChildAction(layoutManager, "VerticalLayout"));
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "TableLayout");
      action.run();
      assertEditor(
          "import com.gwtext.client.widgets.Panel;",
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new TableLayout(1));",
          "  }",
          "}");
    }
  }

  /**
   * No "Set Layout" sub-menu if <code>Container</code> has no layout.
   */
  public void test_setLayoutMenu_2() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import com.gwtext.client.widgets.FocusPanel;",
            "public class Test extends FocusPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // no layout
    assertFalse(panel.hasLayout());
    // ...so, no "Set layout" menu
    {
      IMenuManager menuManager = getContextMenu(panel);
      IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNull(layoutManager);
    }
  }
}