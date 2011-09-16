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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.RowLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.RowLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.VirtualLayoutDataCreationSupport;
import com.google.gdt.eclipse.designer.gwtext.model.layout.VirtualLayoutDataVariableSupport;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.NotImplementedException;

/**
 * Tests for {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutTest extends GwtExtModelTest {
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
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ContainerInfo} without explicit layout has {@link DefaultLayoutInfo}.
   */
  public void test_Layout_defaultImplicit() throws Exception {
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
    //
    LayoutInfo layout = container.getLayout();
    assertInstanceOf(DefaultLayoutInfo.class, layout);
    // layouts are not displayed in tree
    {
      IObjectPresentation presentation = layout.getPresentation();
      assertFalse(presentation.isVisible());
    }
    // "default" layout has no LayoutData
    try {
      ReflectionUtils.invokeMethod(
          layout,
          "getDefaultVirtualDataObject(com.google.gdt.eclipse.designer.model.widgets.WidgetInfo)",
          (WidgetInfo) null);
      fail();
    } catch (NotImplementedException e) {
    }
    // layout has GWTState same as RootPanel
    assertThat(layout.getState()).isSameAs(frame.getState());
  }

  /**
   * Test {@link LayoutInfo#isActive()}.
   */
  public void test_Layout_isActive() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Panel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // "default" is active
    LayoutInfo defaultLayout = panel.getLayout();
    assertTrue(defaultLayout.isActive());
    assertSame(panel, defaultLayout.getContainer());
    // set RowLayout
    LayoutInfo rowLayout;
    {
      rowLayout = createJavaInfo("com.gwtext.client.widgets.layout.RowLayout");
      panel.setLayout(rowLayout);
      assertHierarchy(
          "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/}",
          "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
      assertSame(rowLayout, panel.getLayout());
    }
    // RowLayout is active now
    assertTrue(rowLayout.isActive());
    assertFalse(defaultLayout.isActive());
    // both layouts bounds to same container
    assertSame(panel, rowLayout.getContainer());
    assertSame(panel, defaultLayout.getContainer());
  }

  /**
   * Test {@link LayoutInfo} properties.
   */
  public void test_Layout_properties() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    //
    Property layoutProperty = panel.getPropertyByTitle("Layout");
    assertNotNull(layoutProperty);
    assertTrue(layoutProperty.isModified());
    assertEquals("(com.gwtext.client.widgets.layout.RowLayout)", getPropertyText(layoutProperty));
    {
      Property[] subProperties = getSubProperties(layoutProperty);
      assertThat(subProperties).hasSize(3);
      assertThat(PropertyUtils.getTitles(subProperties)).contains(
          "extraCls",
          "renderHidden",
          "spacing");
    }
    // delete layout
    layoutProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor("public class Test extends Panel {", "  public Test() {", "  }", "}");
  }

  /**
   * Test for {@link DefaultLayoutInfo} properties.
   */
  public void test_Layout_defaultLayoutProperties() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Panel {",
            "  public Test() {",
            "  }",
            "}");
    //
    Property layoutProperty = panel.getPropertyByTitle("Layout");
    assertNotNull(layoutProperty);
    assertTrue(layoutProperty.isModified());
    assertEquals("(default)", getPropertyText(layoutProperty));
    {
      Property[] subProperties = getSubProperties(layoutProperty);
      assertThat(subProperties).isEmpty();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData management, support for virtual
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_LayoutDataMng_addLayoutData_whenSetLayout() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(label)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}");
    // set RowLayout
    LayoutInfo rowLayout = createJavaInfo("com.gwtext.client.widgets.layout.RowLayout");
    panel.setLayout(rowLayout);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(label)/ /setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
  }

  public void test_LayoutDataMng_addLayoutData_whenAddWidget() throws Exception {
    PanelInfo panel =
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
    RowLayoutInfo layout = (RowLayoutInfo) panel.getLayout();
    // add Label widget
    WidgetInfo label = createJavaInfo("com.gwtext.client.widgets.form.Label");
    layout.command_CREATE(label, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label('New Label');",
        "      add(label);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label('New Label')/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
  }

  public void test_LayoutDataMng_addLayoutData_whenMoveWidgetTo() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    {",
            "      Panel inner = new Panel();",
            "      inner.setLayout(new RowLayout());",
            "      add(inner);",
            "    }",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(inner)/ /add(label)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: inner} {/new Panel()/ /inner.setLayout(new RowLayout())/ /add(inner)/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/inner.setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}");
    PanelInfo inner = (PanelInfo) panel.getChildrenWidgets().get(0);
    WidgetInfo label = panel.getChildrenWidgets().get(1);
    // move "label" to "inner"
    ((RowLayoutInfo) inner.getLayout()).command_MOVE(label, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    {",
        "      Panel inner = new Panel();",
        "      inner.setLayout(new RowLayout());",
        "      {",
        "        Label label = new Label();",
        "        inner.add(label);",
        "      }",
        "      add(inner);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(inner)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: inner} {/new Panel()/ /inner.setLayout(new RowLayout())/ /add(inner)/ /inner.add(label)/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/inner.setLayout(new RowLayout())/}",
        "    {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /inner.add(label)/}",
        "      {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
  }

  public void test_LayoutDataMng_deleteLayoutData_whenMoveWidgetFrom() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Panel inner = new Panel();",
            "      add(inner);",
            "    }",
            "    {",
            "      Label label = new Label();",
            "      add(label, new RowLayoutData(50));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(inner)/ /add(label, new RowLayoutData(50))/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: inner} {/new Panel()/ /add(inner)/}",
        "    {implicit-layout: default} {implicit-layout} {}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label, new RowLayoutData(50))/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayoutData} {empty} {/add(label, new RowLayoutData(50))/}");
    PanelInfo inner = (PanelInfo) panel.getChildrenWidgets().get(0);
    WidgetInfo label = panel.getChildrenWidgets().get(1);
    // move "label" to "inner"
    inner.getLayout().command_MOVE(label, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Panel inner = new Panel();",
        "      {",
        "        Label label = new Label();",
        "        inner.add(label);",
        "      }",
        "      add(inner);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(inner)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: inner} {/new Panel()/ /add(inner)/ /inner.add(label)/}",
        "    {implicit-layout: default} {implicit-layout} {}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}",
        "    {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /inner.add(label)/}");
  }

  public void test_LayoutDataMng_deleteLayoutData_whenReplaceLayout() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // initial state
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    // set ColumnLayout
    {
      LayoutInfo rowLayout = createJavaInfo("com.gwtext.client.widgets.layout.ColumnLayout");
      panel.setLayout(rowLayout);
    }
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/add(label)/ /setLayout(new ColumnLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.ColumnLayoutData} {virtual-layout-data} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing {@link LayoutDataInfo}.
   */
  public void test_parseLayoutData() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new RowLayoutData(50));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label, new RowLayoutData(50))/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label, new RowLayoutData(50))/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayoutData} {empty} {/add(label, new RowLayoutData(50))/}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    assertVisible(rowData, false);
  }

  /**
   * Test for {@link VirtualLayoutDataVariableSupport} and {@link VirtualLayoutDataCreationSupport}.
   */
  public void test_LayoutData_virtualVariable() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    // 
    VirtualLayoutDataVariableSupport variable =
        (VirtualLayoutDataVariableSupport) rowData.getVariableSupport();
    assertTrue(variable.isDefault());
    assertEquals("(virtual layout data)", variable.getTitle());
    // no target (not sure why)
    try {
      variable.getStatementTarget();
      fail();
    } catch (IllegalStateException e) {
    }
    // materialize
    {
      NodeTarget target = getNodeStatementTarget(panel, false, 1, 1);
      assertEquals("rowLayoutData.", variable.getAccessExpression(target));
      assertHierarchy(
          "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label, rowLayoutData)/}",
          "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
          "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label, rowLayoutData)/}",
          "    {new: com.gwtext.client.widgets.layout.RowLayoutData} {local-unique: rowLayoutData} {/new RowLayoutData(50)/ /add(label, rowLayoutData)/}");
      assertEditor(
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new RowLayout());",
          "    {",
          "      Label label = new Label();",
          "      RowLayoutData rowLayoutData = new RowLayoutData(50);",
          "      add(label, rowLayoutData);",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * Test for {@link VirtualLayoutDataCreationSupport}.
   */
  public void test_LayoutData_virtualCreationSupport() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    VirtualLayoutDataCreationSupport creationSupport =
        (VirtualLayoutDataCreationSupport) rowData.getCreationSupport();
    // access
    assertNull(creationSupport.getNode());
    assertEquals(
        "virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData",
        creationSupport.toString());
    // validation
    assertFalse(creationSupport.canReorder());
    assertFalse(creationSupport.canReparent());
    // no implicit clipboard
    assertNull(creationSupport.getImplicitClipboard());
    // delete, nothing changed
    {
      String expectedSource = m_lastEditor.getSource();
      String expectedHierarchy = printHierarchy(panel);
      assertTrue(creationSupport.canDelete());
      rowData.delete();
      assertEditor(expectedSource, m_lastEditor);
      assertEquals(expectedHierarchy, printHierarchy(panel));
      assertSame(rowData, RowLayoutInfo.getRowData(label));
    }
  }

  /**
   * Test for materializing {@link LayoutDataInfo} when try to set its property.
   */
  public void test_LayoutData_materialize() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    // set "height"
    rowData.getPropertyByTitle("height(int)").setValue(100);
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label, new RowLayoutData(100))/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label, new RowLayoutData(100))/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayoutData} {empty} {/add(label, new RowLayoutData(100))/}");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData(100));",
        "    }",
        "  }",
        "}");
    // remove LayoutData
    {
      rowData.delete();
      assertEditor(
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new RowLayout());",
          "    {",
          "      Label label = new Label();",
          "      add(label);",
          "    }",
          "  }",
          "}");
      assertHierarchy(
          "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
          "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
          "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
          "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    }
  }

  /**
   * Test for complex "LayoutData" property.
   */
  public void test_LayoutData_property() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label, new RowLayoutData(50));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    // check for "LayoutData" property
    Property layoutDataProperty = label.getPropertyByTitle("LayoutData");
    assertTrue(layoutDataProperty.isModified());
    // sub properties
    {
      Property[] subProperties = getSubProperties(layoutDataProperty);
      assertThat(subProperties).hasSize(2);
      assertEquals("height(int)", subProperties[0].getTitle());
      assertEquals("height(java.lang.String)", subProperties[1].getTitle());
    }
    // delete RowLayoutData
    layoutDataProperty.setValue(Property.UNKNOWN_VALUE);
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }
}