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
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectSizeSupport;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link UIObjectSizeSupport}.
 * 
 * @author scheglov_ke
 */
public class UIObjectSizeSupportTest extends GwtModelTest {
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
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that <code>UIObject.setPixelSize(int,int)</code> is executable.
   */
  public void test_setPixelSize() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    assertEquals(100, button.getBounds().width);
    assertEquals(40, button.getBounds().height);
  }

  /**
   * Test that <code>UIObject.setSize(String,String)</code> is executable.
   */
  public void test_setSize() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    assertEquals(100, button.getBounds().width);
    assertEquals(40, button.getBounds().height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button0 = new Button();",
            "      rootPanel.add(button0);",
            "      button0.setSize('100px', '40px');",
            "    }",
            "  }",
            "}");
    refresh();
    // do copy/paste
    {
      WidgetInfo button0 = getJavaInfoByName("button0");
      doCopyPaste(button0, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button0 = new Button();",
        "      rootPanel.add(button0);",
        "      button0.setSize('100px', '40px');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '40px');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(Dimension)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(Dimension)}.
   */
  public void test_setSizeDimension_setSize() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize(new Dimension(100, 50));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(Dimension)}.
   */
  public void test_setSizeDimension_setSize_remove() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize(null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(int,int)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(int, int)}.
   */
  public void test_setSizeInts() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: width and height
    button.getSizeSupport().setSize(100, 200);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '200px');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSize(String,String)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setSize(String,String)</code> is used.
   */
  public void test_setSizeStrings_setSize_setBoth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: width and height
    button.getSizeSupport().setSize("10cm", "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10cm', '20mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setSize(String,String)</code> is used. Arguments are invalid, so can not be
   * evaluated as {@link String} or {@link Integer}, so default value is used.
   */
  public void test_setSizeStrings_setSize_invalidArguments() throws Exception {
    m_ignoreCompilationProblems = true;
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize(a, b);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: width and height
    button.getSizeSupport().setSize("10cm", "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10cm', '20mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setSize(String,String)</code> is used.
   */
  public void test_setSizeStrings_setSize_setWidth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: only width
    button.getSizeSupport().setSize("11cm", null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('11cm', '20px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setSize(String,String)</code> is used.
   */
  public void test_setSizeStrings_setSize_setHeight() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: only height
    button.getSizeSupport().setSize(null, "21mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '21mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSizeStrings_setSize_removeBoth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: remove width/height
    button.getSizeSupport().setSize(IUIObjectSizeSupport.NO_SIZE, IUIObjectSizeSupport.NO_SIZE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSizeStrings_setSize_removeWidth_keepHeight() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: remove width, don't change height
    button.getSizeSupport().setSize(IUIObjectSizeSupport.NO_SIZE, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('20px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   */
  public void test_setSizeStrings_setSize_removeHeight_keepWidth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size: remove height, don't change width
    button.getSizeSupport().setSize(null, IUIObjectSizeSupport.NO_SIZE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('10px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setPixelSize(int,int)</code> is used.
   */
  public void test_setSizeStrings_setPixelSize() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setPixelSize(10, 20);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("100px", "50px");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * No any size element set, add <code>UIObject.setSize(String,String)</code>.
   */
  public void test_setSizeStrings_noSize_setBoth() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("100px", "50px");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
    assertEquals(new Dimension(100, 50), button.getBounds().getSize());
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * No any size element set, set width, add <code>UIObject.setWidth(String)</code>.
   */
  public void test_setSizeStrings_noSize_setWidth() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("100px", null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('100px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * No any size element set, set height, add <code>UIObject.setHeight(String)</code>.
   */
  public void test_setSizeStrings_noSize_setHeight() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize(null, "50px");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('50px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setWidth(String)</code> exists, so <code>UIObject.setSize(String,String)</code>
   * should be added instead of it.
   */
  public void test_setSizeStrings_hasWidth_setBoth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('10px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("10cm", "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10cm', '20mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setWidth(String)</code> exists and we set only "width", so existing
   * <code>UIObject.setWidth(String)</code> should be updated.
   */
  public void test_setSizeStrings_hasWidth_setWidth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('10px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("10cm", null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('10cm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setWidth(String)</code> exists, so <code>UIObject.setSize(String,String)</code>
   * should be added instead of it.
   */
  public void test_setSizeStrings_hasWidth_setHeight() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setWidth('10px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize(null, "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10px', '20mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setWidth(String)</code> exists, so <code>UIObject.setSize(String,String)</code>
   * should be added instead of it.
   */
  public void test_setSizeStrings_hasHeight_setBoth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("10cm", "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10cm', '20mm');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setHeight(String)</code> exists, so <code>UIObject.setSize(String,String)</code>
   * should be added instead of it.
   */
  public void test_setSizeStrings_hasHeight_setWidth() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize("10cm", null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('10cm', '20px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link UIObjectSizeSupport#setSize(String, String)}.
   * <p>
   * <code>UIObject.setHeight(String)</code> exists and we set only "height", so existing
   * <code>UIObject.setHeight(String)</code> should be updated.
   */
  public void test_setSizeStrings_hasHeight_setHeight() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('20px');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // set new size
    button.getSizeSupport().setSize(null, "20mm");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setHeight('20mm');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Size" complex property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple width/height properties of UIObject should be marked as advanced.
   */
  public void test_sizeProperty_simpleWidthHeight() throws Exception {
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
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    assertTrue(button.getPropertyByTitle("width").getCategory().isHidden());
    assertTrue(button.getPropertyByTitle("height").getCategory().isHidden());
  }

  public void test_sizeProperty_noSize() throws Exception {
    Property sizeProperty = getSizeProperty("");
    assertTrue(sizeProperty.getCategory().isSystem());
    // value for this property is always "null"
    assertNull(sizeProperty.getValue());
    // no any size-related invocations, so property is not modified
    assertFalse(sizeProperty.isModified());
    // text, we don't know exact value, but know format
    assertThat(getPropertyText(sizeProperty)).contains("(").contains(",").contains(")");
  }

  public void test_sizeProperty_setSize() throws Exception {
    Property sizeProperty = getSizeProperty("button.setSize('10cm', '20mm');");
    assertTrue(sizeProperty.getCategory().isSystem());
    // value for this property is always "null"
    assertNull(sizeProperty.getValue());
    // we have "setSize()" invocation, so property is modified
    assertTrue(sizeProperty.isModified());
    // text
    assertEquals("(10cm, 20mm)", getPropertyText(sizeProperty));
    // setValue() does nothing
    {
      String expectedSource = m_lastEditor.getSource();
      sizeProperty.setValue(null);
      assertEditor(expectedSource, m_lastEditor);
    }
    // sub-properties
    {
      Property[] subProperties = getSubProperties(sizeProperty);
      // check "width"
      {
        Property widthProperty = subProperties[0];
        assertEquals("width", widthProperty.getTitle());
        assertTrue(widthProperty.isModified());
        assertEquals("10cm", widthProperty.getValue());
        // reset width
        setSizeProperty(widthProperty, Property.UNKNOWN_VALUE);
        assertEditor(getSizeSource("button.setHeight('20mm');"), m_lastEditor);
        // restore width
        setSizeProperty(widthProperty, "5in");
        assertEditor(getSizeSource("button.setSize('5in', '20mm');"), m_lastEditor);
      }
      // check "height"
      {
        Property heightProperty = subProperties[1];
        assertEquals("height", heightProperty.getTitle());
        assertTrue(heightProperty.isModified());
        assertEquals("20mm", heightProperty.getValue());
        // reset width
        setSizeProperty(heightProperty, Property.UNKNOWN_VALUE);
        assertEditor(getSizeSource("button.setWidth('5in');"), m_lastEditor);
        // restore width
        setSizeProperty(heightProperty, "2cm");
        assertEditor(getSizeSource("button.setSize('5in', '2cm');"), m_lastEditor);
      }
    }
  }

  public void test_sizeProperty_removeSize() throws Exception {
    Property sizeProperty = getSizeProperty("button.setSize('10cm', '20mm');");
    assertTrue(sizeProperty.getCategory().isSystem());
    // use setValue() to remove size
    sizeProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Try to set "width" for RootPanel.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?40908
   */
  public void test_sizeProperty_RootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    Property property = PropertyUtils.getByPath(frame, "Size/width");
    property.setValue("200px");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setWidth('200px');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Consider margin, border, padding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_TextBox() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TextBox widget = new TextBox();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo widget = getJavaInfoByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 3 + 4) * 2;
    int clientHeight = 50 - (2 + 3 + 4) * 2;
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TextBox widget = new TextBox();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "      widget.setSize('" + clientWidth + "px', '" + clientHeight + "px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_AbsolutePanel() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel widget = new AbsolutePanel();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo widget = getJavaInfoByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 3 + 4) * 2;
    int clientHeight = 50 - (2 + 3 + 4) * 2;
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      AbsolutePanel widget = new AbsolutePanel();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "      widget.setSize('" + clientWidth + "px', '" + clientHeight + "px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_ListBox() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ListBox widget = new ListBox();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo widget = getJavaInfoByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 0 + 0) * 2;
    int clientHeight = 50 - (2 + 0 + 0) * 2;
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ListBox widget = new ListBox();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "      widget.setSize('" + clientWidth + "px', '" + clientHeight + "px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_Button_withDecorations() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSource(
            "/* filler filler filler filler filler */",
            ".myStyle {",
            "  margin: 2px;",
            "  border: 3px solid red;",
            "  padding: 4px;",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button widget = new Button();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo widget = getJavaInfoByName("widget");
    // decorations
    assertEquals(new Insets(2), widget.getMargins());
    assertEquals(new Insets(3), widget.getBorders());
    assertEquals(new Insets(4), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (2 + 0 + 0) * 2;
    int clientHeight = 50 - (2 + 0 + 0) * 2;
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button widget = new Button();",
        "      widget.setStyleName('myStyle');",
        "      rootPanel.add(widget);",
        "      widget.setSize('" + clientWidth + "px', '" + clientHeight + "px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Formally "setSize()" sets size of client area. This size does not include decorations such as
   * margin, border and padding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47140
   */
  @DisposeProjectAfter
  public void test_considerDecorations_Button_default() throws Exception {
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button widget = new Button();",
        "      rootPanel.add(widget);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo widget = getJavaInfoByName("widget");
    // decorations
    assertEquals(new Insets(0), widget.getMargins());
    assertEquals(new Insets(2), widget.getBorders());
    assertEquals(new Insets(0), widget.getPaddings());
    // set new size, tweak to take decorations into account
    widget.getSizeSupport().setSize(100, 50);
    assertEquals(new Dimension(100, 50), widget.getBounds().getSize());
    // source
    int clientWidth = 100 - (0 + 0 + 0) * 2;
    int clientHeight = 50 - (0 + 0 + 0) * 2;
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button widget = new Button();",
        "      rootPanel.add(widget);",
        "      widget.setSize('" + clientWidth + "px', '" + clientHeight + "px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * In theory we should consider decorations, but if there was no refresh() yet, we don't know
   * decorations, so we just apply size as is.
   */
  public void test_considerDecorations_whenSizeDuringCreate() throws Exception {
    final ComplexPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    refresh();
    //
    final WidgetInfo widget = createJavaInfo("com.google.gwt.user.client.ui.TextBox");
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        flowContainer_CREATE(frame, widget, null);
        widget.getSizeSupport().setSize(100, 50);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TextBox textBox = new TextBox();",
        "      rootPanel.add(textBox);",
        "      textBox.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
    // no tweaks for decorations, so (bounds, in contrast to client) size is bigger than asked
    {
      int margin = 0;
      int border = 2;
      int padding = 1;
      assertEquals(100 + (margin + border + padding) * 2, widget.getBounds().width);
      assertEquals(50 + (margin + border + padding) * 2, widget.getBounds().height);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set new value for {@link Property} as edit operation.
   */
  private void setSizeProperty(final Property property, final Object value) throws Exception {
    ExecutionUtils.run(m_lastParseInfo, new RunnableEx() {
      public void run() throws Exception {
        property.setValue(value);
      }
    });
  }

  /**
   * @return the "Size" property for standard RootPanel/Button test case.
   */
  private Property getSizeProperty(String sizeString) throws Exception {
    parseSource("test.client", "Test.java", getSizeSource(sizeString));
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    return button.getPropertyByTitle("Size");
  }

  /**
   * @return the source for standard RootPanel/Button test case.
   */
  private String getSizeSource(String sizeString) {
    return getTestSource(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      " + sizeString,
        "    }",
        "  }",
        "}");
  }
}