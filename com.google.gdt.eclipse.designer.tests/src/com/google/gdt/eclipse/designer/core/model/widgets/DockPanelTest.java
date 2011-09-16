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

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.DockPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link DockPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class DockPanelTest extends GwtModelTest {
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
   * Even empty <code>DockPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DockPanel panel = new DockPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    DockPanelInfo panel = (DockPanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(140);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  /**
   * <code>DockPanel</code> has <code>"horizontalAlignment"</code> and
   * <code>"verticalAlignment"</code> properties.
   */
  public void test_alignmentProperties() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DockPanel panel = new DockPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    DockPanelInfo panel = (DockPanelInfo) frame.getChildrenWidgets().get(0);
    assertNotNull(panel.getPropertyByTitle("horizontalAlignment"));
    assertNotNull(panel.getPropertyByTitle("verticalAlignment"));
  }

  /**
   * Direction should be reflected in child {@link WidgetInfo} title.
   */
  public void test_DirectionInTitle() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DockPanel panel = new DockPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1, DockPanel.WEST);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2, DockPanel.NORTH);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DockPanelInfo panel = (DockPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // check title decorations
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_1);
      assertThat(title).startsWith("WEST - ");
    }
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_2);
      assertThat(title).startsWith("NORTH - ");
    }
  }

  /**
   * Test for contributed "Direction" property.
   */
  public void test_DirectionProperty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DockPanel panel = new DockPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button, DockPanel.WEST);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DockPanelInfo panel = (DockPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // check that "Direction" property exists
    Property directionProperty;
    {
      directionProperty = button.getPropertyByTitle("Direction");
      assertNotNull(directionProperty);
      // same instance each time
      assertSame(directionProperty, button.getPropertyByTitle("Direction"));
      // presentation
      assertEquals("Direction", directionProperty.getTitle());
      assertTrue(directionProperty.getCategory().isSystem());
      assertTrue(directionProperty.isModified());
    }
    // current value
    assertEquals("WEST", getPropertyText(directionProperty));
    // set new value
    {
      Object northObject = ReflectionUtils.getFieldObject(panel.getObject(), "NORTH");
      directionProperty.setValue(northObject);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DockPanel panel = new DockPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, DockPanel.NORTH);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for contributed "Cell" property.
   */
  public void test_CellProperty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DockPanel panel = new DockPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button, DockPanel.WEST);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DockPanelInfo panel = (DockPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // check that "Cell" property exists
    Property cellProperty;
    {
      cellProperty = button.getPropertyByTitle("Cell");
      assertNotNull(cellProperty);
      // same instance each time
      assertSame(cellProperty, button.getPropertyByTitle("Cell"));
      // presentation
      assertEquals("Cell", cellProperty.getTitle());
      assertTrue(cellProperty.getCategory().isSystem());
      assertFalse(cellProperty.isModified());
      assertEquals("(cell properties)", getPropertyText(cellProperty));
    }
    // sub-properties
    Property[] subProperties = getSubProperties(cellProperty);
    // check "width"
    {
      Property widthProperty = getPropertyByTitle(subProperties, "width");
      assertFalse(widthProperty.isModified());
      assertEquals("", widthProperty.getValue());
      // set new value
      widthProperty.setValue("5cm");
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "      panel.setCellWidth(button, '5cm');",
          "    }",
          "  }",
          "}");
      assertTrue(widthProperty.isModified());
      assertEquals("5cm", widthProperty.getValue());
      // set different value
      widthProperty.setValue("10cm");
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "      panel.setCellWidth(button, '10cm');",
          "    }",
          "  }",
          "}");
      assertTrue(widthProperty.isModified());
      assertEquals("10cm", widthProperty.getValue());
      // remove value
      widthProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "    }",
          "  }",
          "}");
      assertFalse(widthProperty.isModified());
      assertEquals("", widthProperty.getValue());
      // remove again - no changes
      {
        String expectedSource = m_lastEditor.getSource();
        widthProperty.setValue(Property.UNKNOWN_VALUE);
        assertEditor(expectedSource, m_lastEditor);
        assertFalse(widthProperty.isModified());
        assertEquals("", widthProperty.getValue());
      }
    }
    // check "height"
    {
      Property heightProperty = getPropertyByTitle(subProperties, "height");
      assertFalse(heightProperty.isModified());
      assertEquals("", heightProperty.getValue());
      // set new value
      heightProperty.setValue("5cm");
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "      panel.setCellHeight(button, '5cm');",
          "    }",
          "  }",
          "}");
      assertTrue(heightProperty.isModified());
      assertEquals("5cm", heightProperty.getValue());
      // remove value
      heightProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "    }",
          "  }",
          "}");
      assertFalse(heightProperty.isModified());
      assertEquals("", heightProperty.getValue());
    }
    // check "horizontalAlignment"
    {
      Class<?> hasAlignmentClass =
          m_lastLoader.loadClass("com.google.gwt.user.client.ui.HasHorizontalAlignment");
      Property alignmentProperty = getPropertyByTitle(subProperties, "horizontalAlignment");
      assertFalse(alignmentProperty.isModified());
      assertEquals("ALIGN_LEFT", getPropertyText(alignmentProperty));
      // set new value
      alignmentProperty.setValue(ReflectionUtils.getFieldObject(hasAlignmentClass, "ALIGN_RIGHT"));
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "      panel.setCellHorizontalAlignment(button, HasHorizontalAlignment.ALIGN_RIGHT);",
          "    }",
          "  }",
          "}");
      assertTrue(alignmentProperty.isModified());
      assertEquals("ALIGN_RIGHT", getPropertyText(alignmentProperty));
      // remove value
      alignmentProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "    }",
          "  }",
          "}");
      assertFalse(alignmentProperty.isModified());
      assertEquals("ALIGN_LEFT", getPropertyText(alignmentProperty));
    }
    // check "verticalAlignment"
    {
      Class<?> hasAlignmentClass =
          m_lastLoader.loadClass("com.google.gwt.user.client.ui.HasVerticalAlignment");
      Property alignmentProperty = getPropertyByTitle(subProperties, "verticalAlignment");
      assertFalse(alignmentProperty.isModified());
      assertEquals("ALIGN_TOP", getPropertyText(alignmentProperty));
      // set new value
      alignmentProperty.setValue(ReflectionUtils.getFieldObject(hasAlignmentClass, "ALIGN_BOTTOM"));
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "      panel.setCellVerticalAlignment(button, HasVerticalAlignment.ALIGN_BOTTOM);",
          "    }",
          "  }",
          "}");
      assertTrue(alignmentProperty.isModified());
      assertEquals("ALIGN_BOTTOM", getPropertyText(alignmentProperty));
      // remove value
      alignmentProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DockPanel panel = new DockPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      panel.add(button, DockPanel.WEST);",
          "    }",
          "  }",
          "}");
      assertFalse(alignmentProperty.isModified());
      assertEquals("ALIGN_TOP", getPropertyText(alignmentProperty));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockPanelInfo#command_CREATE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    DockPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    panel.setDirection(newButton, "WEST");
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.WEST);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DockPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    DockPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, DockPanel.WEST);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, DockPanel.NORTH);",
            "    }",
            "  }",
            "}");
    refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, DockPanel.NORTH);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, DockPanel.WEST);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for copy/paste child of {@link DockPanelInfo}.
   */
  public void test_clipboard_onIt() throws Exception {
    final DockPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockPanel {",
            "  public Test() {",
            "    {",
            "      Button existing = new Button();",
            "      add(existing, DockPanel.WEST);",
            "    }",
            "  }",
            "}");
    refresh();
    //
    {
      WidgetInfo existingButton = getJavaInfoByName("existing");
      doCopyPaste(existingButton, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          panel.command_CREATE2(copy, null);
          panel.setDirection(copy, "EAST");
        }
      });
    }
    assertEditor(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button existing = new Button();",
        "      add(existing, DockPanel.WEST);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.EAST);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for copy/paste {@link DockPanelInfo} with its children.
   */
  public void test_clipboard_it() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      DockPanel panel = new DockPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button();",
            "        panel.add(button_1, DockPanel.WEST);",
            "      }",
            "      {",
            "        Button button_2 = new Button();",
            "        panel.add(button_2, DockPanel.NORTH);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      DockPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          frame.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DockPanel panel = new DockPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, DockPanel.WEST);",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, DockPanel.NORTH);",
        "      }",
        "    }",
        "    {",
        "      DockPanel dockPanel = new DockPanel();",
        "      rootPanel.add(dockPanel);",
        "      {",
        "        Button button = new Button();",
        "        dockPanel.add(button, DockPanel.WEST);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        dockPanel.add(button, DockPanel.NORTH);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}