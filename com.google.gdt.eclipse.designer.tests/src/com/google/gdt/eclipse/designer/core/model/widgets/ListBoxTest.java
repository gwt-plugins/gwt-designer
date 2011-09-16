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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for <code>ListBox</code>.
 * 
 * @author scheglov_ke
 */
public class ListBoxTest extends GwtModelTest {
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
  public void test_items() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ListBox listBox = new ListBox();",
            "      rootPanel.add(listBox);",
            "      listBox.setVisibleItemCount(5);",
            "      listBox.addItem('text_0');",
            "      listBox.addItem('text_1', 'value_1');",
            "      listBox.insertItem('text_2', 2);",
            "      listBox.insertItem('text_3', 'value_3', 3);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo listBox = frame.getChildrenWidgets().get(0);
    Object listBoxObject = listBox.getObject();
    // check
    assertEquals(4, ReflectionUtils.invokeMethod(listBoxObject, "getItemCount()"));
    // item: 0
    assertEquals("text_0", ReflectionUtils.invokeMethod(listBoxObject, "getItemText(int)", 0));
    assertEquals("text_0", ReflectionUtils.invokeMethod(listBoxObject, "getValue(int)", 0));
    // item: 1
    assertEquals("text_1", ReflectionUtils.invokeMethod(listBoxObject, "getItemText(int)", 1));
    assertEquals("value_1", ReflectionUtils.invokeMethod(listBoxObject, "getValue(int)", 1));
    // item: 2
    assertEquals("text_2", ReflectionUtils.invokeMethod(listBoxObject, "getItemText(int)", 2));
    assertEquals("text_2", ReflectionUtils.invokeMethod(listBoxObject, "getValue(int)", 2));
    // item: 3
    assertEquals("text_3", ReflectionUtils.invokeMethod(listBoxObject, "getItemText(int)", 3));
    assertEquals("value_3", ReflectionUtils.invokeMethod(listBoxObject, "getValue(int)", 3));
  }

  public void test_itemsProperty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ListBox listBox = new ListBox();",
            "      rootPanel.add(listBox);",
            "      listBox.setVisibleItemCount(5);",
            "      listBox.addItem('text_0');",
            "      listBox.addItem('text_1');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo listBox = frame.getChildrenWidgets().get(0);
    // prepare "items" property
    Property itemsProperty = listBox.getPropertyByTitle("items");
    assertNotNull(itemsProperty);
    // initial state
    {
      assertEquals("[text_0,text_1]", getPropertyText(itemsProperty));
      assertTrue(itemsProperty.isModified());
      assertTrue(ArrayUtils.isEquals(new String[]{"text_0", "text_1"}, itemsProperty.getValue()));
    }
  }

  public void test_setSelectedIndex() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ListBox listBox = new ListBox();",
            "      rootPanel.add(listBox);",
            "      listBox.setVisibleItemCount(5);",
            "      listBox.addItem('item_0');",
            "      listBox.addItem('item_1');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo listBox = frame.getChildrenWidgets().get(0);
    listBox.getPropertyByTitle("selectedIndex").setValue(1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ListBox listBox = new ListBox();",
        "      rootPanel.add(listBox);",
        "      listBox.setVisibleItemCount(5);",
        "      listBox.addItem('item_0');",
        "      listBox.addItem('item_1');",
        "      listBox.setSelectedIndex(1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_asList() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // do create
    WidgetInfo listBox = createJavaInfo("com.google.gwt.user.client.ui.ListBox");
    frame.command_CREATE2(listBox, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ListBox listBox = new ListBox();",
        "      rootPanel.add(listBox);",
        "      listBox.setVisibleItemCount(5);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_asCombo() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // do create
    WidgetInfo comboBox = createJavaInfo("com.google.gwt.user.client.ui.ListBox", "combo");
    frame.command_CREATE2(comboBox, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ListBox comboBox = new ListBox();",
        "      rootPanel.add(comboBox);",
        "    }",
        "  }",
        "}");
  }
}