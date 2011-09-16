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
package com.google.gdt.eclipse.designer.core.model.property;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.property.css.StylePropertyEditor;
import com.google.gdt.eclipse.designer.model.property.css.StyleSetPropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.controls.CComboBox;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test for {@link StylePropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class StylePropertyEditorTest extends GwtModelTest {
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
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // use better styles
    setFileContent("war/Module.css", getSource(".first {}", ".second {}", ".third {}"));
    forgetCreatedResources();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_defaultValue() throws Exception {
    Property property = getStyleProperty(null);
    assertEquals("", getPropertyText(property));
  }

  public void test_getText_badValue() throws Exception {
    m_ignoreCompilationProblems = true;
    Property property = getStyleProperty("setStyleName(123);");
    assertEquals(null, getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_fillItems() throws Exception {
    Property property = getStyleProperty("setStyleName('second')");
    // add items
    addComboPropertyItems(property);
    setComboPropertySelection(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("first", "second", "third");
    }
    // "second" is selected
    assertEquals(1, getComboPropertySelection());
  }

  public void test_toPropertyEx() throws Exception {
    Property property = getStyleProperty("setStyleName('second')");
    addComboPropertyItems(property);
    setComboPropertyValue(property, "third");
    assertEditor(getStyleSource("setStyleName('third')"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Animate in PropertyTable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_withPropertyTable_selectFromDropDown() throws Exception {
    PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      Property property = getStyleProperty("setStyleName('second')");
      PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      propertyTable.activateEditor(property, null);
      CComboBox combo = (CComboBox) getFieldValue(propertyEditor, "m_combo");
      Text comboText = (Text) getFieldValue(combo, "m_text");
      Table comboTable = ((TableViewer) getFieldValue(combo, "m_table")).getTable();
      // has items
      assertEquals(3, combo.getItemCount());
      assertFalse(combo.isDroppedDown());
      // drop-down in async
      waitEventLoop(0);
      assertTrue(combo.isDroppedDown());
      // "second" selected
      assertEquals(1, comboTable.getSelectionIndex());
      // move selection up/down
      {
        EventSender eventSender = new EventSender(comboText);
        // 0
        eventSender.keyDown(SWT.ARROW_UP);
        assertEquals(0, comboTable.getSelectionIndex());
        // 2
        eventSender.keyDown(SWT.ARROW_UP);
        assertEquals(2, comboTable.getSelectionIndex());
        // 0
        eventSender.keyDown(SWT.ARROW_DOWN);
        assertEquals(0, comboTable.getSelectionIndex());
        // done
        eventSender.keyDown('\r');
      }
      // "first" was selected
      assertEditor(getStyleSource("setStyleName('first')"));
    } finally {
      propertyTable.dispose();
    }
  }

  public void test_withPropertyTable_useText() throws Exception {
    PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      Property property = getStyleProperty("setStyleName('second')");
      PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      propertyTable.activateEditor(property, null);
      CComboBox combo = (CComboBox) getFieldValue(propertyEditor, "m_combo");
      Text comboText = (Text) getFieldValue(combo, "m_text");
      EventSender eventSender = new EventSender(comboText);
      // has items
      assertEquals(3, combo.getItemCount());
      assertFalse(combo.isDroppedDown());
      // drop-down in async
      waitEventLoop(0);
      assertTrue(combo.isDroppedDown());
      // send ESC, close
      {
        eventSender.keyDown(SWT.ESC);
        assertFalse(combo.isDroppedDown());
      }
      // set text
      combo.setSelectionText("myStyle");
      // apply text
      eventSender.keyDown('\r');
      assertEditor(getStyleSource("setStyleName('myStyle')"));
    } finally {
      propertyTable.dispose();
    }
  }

  public void test_withPropertyTable_useEscape() throws Exception {
    PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      Property property = getStyleProperty("setStyleName('second')");
      PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      propertyTable.activateEditor(property, null);
      CComboBox combo = (CComboBox) getFieldValue(propertyEditor, "m_combo");
      Text comboText = (Text) getFieldValue(combo, "m_text");
      EventSender eventSender = new EventSender(comboText);
      // active
      assertSame(propertyEditor, propertyTable.forTests_getActiveEditor());
      // drop-down in async
      waitEventLoop(0);
      assertTrue(combo.isDroppedDown());
      // send ESC, close
      {
        eventSender.keyDown(SWT.ESC);
        assertFalse(combo.isDroppedDown());
      }
      // send ESC, deactivate
      {
        eventSender.keyDown(SWT.ESC);
        assertSame(null, propertyTable.forTests_getActiveEditor());
      }
    } finally {
      propertyTable.dispose();
    }
  }

  public void test_withPropertyTable_quickSearch() throws Exception {
    dontUseSharedGWTState();
    setFileContent("war/Module.css", getSource(".aaa {}", ".bb1 {}", ".bb2 {}", ".ccc {}"));
    PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      Property property = getStyleProperty(null);
      PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      // send "b", activate editor
      new EventSender(propertyTable).keyDown(0, 'b');
      // prepare controls
      CComboBox combo = (CComboBox) getFieldValue(propertyEditor, "m_combo");
      Text comboText = (Text) getFieldValue(combo, "m_text");
      Table comboTable = ((TableViewer) getFieldValue(combo, "m_table")).getTable();
      EventSender eventSender = new EventSender(comboText);
      // drop-down in async
      waitEventLoop(0);
      assertTrue(combo.isDroppedDown());
      // state after sending "b"
      assertEquals(2, comboTable.getItemCount());
      assertEquals(-1, comboTable.getSelectionIndex());
      // send DOWN, select "bb2"
      {
        eventSender.keyDown(SWT.ARROW_DOWN);
        assertEquals(0, comboTable.getSelectionIndex());
        eventSender.keyDown(SWT.ARROW_DOWN);
        assertEquals(1, comboTable.getSelectionIndex());
      }
      // apply selection
      eventSender.keyDown('\r');
      assertEditor(getStyleSource("setStyleName('bb2')"));
    } finally {
      propertyTable.dispose();
    }
  }

  /**
   * There was bug with entering exact name label of item and pressing Enter.
   */
  public void test_withPropertyTable_quickSearchExact() throws Exception {
    dontUseSharedGWTState();
    PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      Property property = getStyleProperty(null);
      PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      // send "s", activate editor
      new EventSender(propertyTable).keyDown(0, 's');
      // prepare controls
      CComboBox combo = (CComboBox) getFieldValue(propertyEditor, "m_combo");
      Text comboText = (Text) getFieldValue(combo, "m_text");
      Table comboTable = ((TableViewer) getFieldValue(combo, "m_table")).getTable();
      EventSender eventSender = new EventSender(comboText);
      // drop-down in async
      waitEventLoop(0);
      assertTrue(combo.isDroppedDown());
      // use exactly "second"
      comboText.setText("second");
      assertEquals(1, comboTable.getItemCount());
      assertEquals(-1, comboTable.getSelectionIndex());
      // apply selection
      eventSender.keyDown('\r');
      assertEditor(getStyleSource("setStyleName('second')"));
    } finally {
      propertyTable.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS Style Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_styleDialog() throws Exception {
    final PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
    try {
      final Property property = getStyleProperty(null);
      final PropertyEditor propertyEditor = property.getEditor();
      // activate
      propertyTable.setInput(new Property[]{property});
      propertyTable.setActiveProperty(property);
      propertyTable.activateEditor(property, null);
      // open dialog and use "Cancel"
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          openCssStyleEditorDialog(propertyTable, property, propertyEditor);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("CSS Style Editor");
          context.clickButton("Cancel");
        }
      });
      // open dialog and use "OK"
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          openCssStyleEditorDialog(propertyTable, property, propertyEditor);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("CSS Style Editor");
          context.clickButton("OK");
        }
      });
      assertEditor(getStyleSource(null));
      // open dialog and use "Apply"
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          openCssStyleEditorDialog(propertyTable, property, propertyEditor);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          context.useShell("CSS Style Editor");
          // make selection in rules
          org.eclipse.swt.widgets.List rulesList =
              context.findWidgets(org.eclipse.swt.widgets.List.class).get(1);
          rulesList.select(1);
          // apply style
          context.clickButton("Apply");
        }
      });
      // "second" was selected
      assertEditor(getStyleSource("setStyleName('second')"));
    } finally {
      propertyTable.dispose();
    }
  }

  @SuppressWarnings("unchecked")
  private static void openCssStyleEditorDialog(PropertyTable propertyTable,
      Property property,
      PropertyEditor propertyEditor) throws Exception {
    String signature =
        MessageFormat.format(
            "onClick({0},{1})",
            "org.eclipse.wb.internal.core.model.property.table.PropertyTable",
            "org.eclipse.wb.internal.core.model.property.Property");
    PropertyEditorPresentation compoundPresentation = propertyEditor.getPresentation();
    List<PropertyEditorPresentation> presentations =
        (List<PropertyEditorPresentation>) ReflectionUtils.getFieldObject(
            compoundPresentation,
            "m_presentations");
    PropertyEditorPresentation presentation = presentations.get(1);
    ReflectionUtils.invokeMethod(presentation, signature, propertyTable, property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Semantics
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No style declaration, so no semantic sub-properties.
   */
  public void test_semantics_noStyle() throws Exception {
    Property styleProperty = getStyleProperty(null);
    assertThat(PropertyUtils.getChildren(styleProperty)).isEmpty();
  }

  /**
   * Test for {@link StyleSimpleValuePropertyEditor}.
   */
  public void test_semantics_simpleValue() throws Exception {
    dontUseSharedGWTState();
    IFile styleFile = setFileContent("war/Module.css", getSource(".style {", "  color: red;", "}"));
    // prepare property
    final Property property;
    {
      Property styleProperty = getStyleProperty("setStyleName('style')");
      property = PropertyUtils.getByPath(styleProperty, "color");
    }
    // initial state
    assertEquals(true, property.isModified());
    assertEquals("red", getPropertyText(property));
    // remove "color" value
    property.setValue(Property.UNKNOWN_VALUE);
    assertEquals(false, property.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(getSource(".style {", "}"), getFileContent(styleFile));
  }

  public void test_semantics_color() throws Exception {
    dontUseSharedGWTState();
    IFile styleFile = setFileContent("war/Module.css", getSource(".style {", "}"));
    // prepare property
    final Property property;
    {
      Property styleProperty = getStyleProperty("setStyleName('style')");
      property = PropertyUtils.getByPath(styleProperty, "color");
      assertNotNull(property);
    }
    // initial state
    assertEquals(null, getPropertyText(property));
    callPaint(property);
    // set value
    property.setValue("red");
    assertEquals("red", getPropertyText(property));
    callPaint(property);
    assertEquals(getSource(".style {", "  color: red;", "}"), getFileContent(styleFile));
    // animate dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        PropertyEditorPresentation presentation = property.getEditor().getPresentation();
        ReflectionUtils.invokeMethod(
            presentation,
            "onClick(org.eclipse.wb.internal.core.model.property.table.PropertyTable,"
                + "org.eclipse.wb.internal.core.model.property.Property)",
            null,
            property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Color chooser");
        {
          List<ColorsGridComposite> grids = context.findWidgets(ColorsGridComposite.class);
          ColorsGridComposite colorsComposite = grids.get(1);
          EventSender sender = new EventSender(colorsComposite);
          sender.moveTo(30, 50).click();
        }
        context.clickButton("OK");
      }
    });
    assertEquals(getSource(".style {", "  color: cornflowerblue;", "}"), getFileContent(styleFile));
    // animate Text widget
    {
      PropertyTable propertyTable = new PropertyTable(TEST_COMBO_SHELL, SWT.NONE);
      try {
        PropertyEditor propertyEditor = property.getEditor();
        propertyTable.setInput(new Property[]{property});
        propertyTable.setActiveProperty(property);
        // press "b", activate editor
        new EventSender(propertyTable).keyDown(0, 'b');
        // set text and press Enter
        {
          CComboBox combo = (CComboBox) ReflectionUtils.getFieldObject(propertyEditor, "m_combo");
          Text comboText = (Text) ReflectionUtils.getFieldObject(combo, "m_text");
          // drop-down in async
          waitEventLoop(0);
          assertTrue(combo.isDroppedDown());
          // animate
          EventSender eventSender = new EventSender(comboText);
          eventSender.keyDown(SWT.ESC);
          combo.setSelectionText("green");
          eventSender.keyDown(SWT.CR);
        }
      } finally {
        propertyTable.dispose();
      }
      assertEquals(getSource(".style {", "  color: green;", "}"), getFileContent(styleFile));
    }
  }

  /**
   * Test for "margin" property and "sided" properties in general.
   */
  public void test_semantics_margin() throws Exception {
    dontUseSharedGWTState();
    IFile styleFile = setFileContent("war/Module.css", getSource(".style {", "}"));
    // prepare property
    final Property property;
    final Property leftProperty;
    {
      Property styleProperty = getStyleProperty("setStyleName('style')");
      property = PropertyUtils.getByPath(styleProperty, "margin");
      leftProperty = PropertyUtils.getByPath(property, "left");
    }
    // initial state
    assertEquals(false, property.isModified());
    assertEquals(false, leftProperty.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(null, getPropertyText(leftProperty));
    // set "margin" value
    property.setValue("10px");
    assertEquals(true, property.isModified());
    assertEquals(true, leftProperty.isModified());
    assertEquals("10px", getPropertyText(property));
    assertEquals("10px", getPropertyText(leftProperty));
    assertEquals(getSource(".style {", "  margin: 10px;", "}"), getFileContent(styleFile));
    // set "margin-left" value
    leftProperty.setValue("20px");
    assertEquals(true, property.isModified());
    assertEquals(true, leftProperty.isModified());
    assertEquals("10px 10px 10px 20px", getPropertyText(property));
    assertEquals("20px", getPropertyText(leftProperty));
    assertEquals(
        getSource(".style {", "  margin: 10px 10px 10px 20px;", "}"),
        getFileContent(styleFile));
    // remove "margin-left" value
    leftProperty.setValue(Property.UNKNOWN_VALUE);
    assertEquals(false, leftProperty.isModified());
    assertEquals(null, getPropertyText(leftProperty));
    // remove "margin" value
    property.setValue(Property.UNKNOWN_VALUE);
    assertEquals(false, property.isModified());
    assertEquals(false, leftProperty.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(null, getPropertyText(leftProperty));
    assertEquals(getSource(".style {", "}"), getFileContent(styleFile));
  }

  /**
   * Test for "border" property.
   */
  public void test_semantics_border() throws Exception {
    dontUseSharedGWTState();
    IFile styleFile = setFileContent("war/Module.css", getSource(".style {", "}"));
    // prepare property
    final Property property;
    final Property widthProperty;
    final Property styleProperty;
    final Property colorProperty;
    {
      Property styleNameProperty = getStyleProperty("setStyleName('style')");
      property = PropertyUtils.getByPath(styleNameProperty, "border");
      widthProperty = PropertyUtils.getByPath(property, "width");
      styleProperty = PropertyUtils.getByPath(property, "style");
      colorProperty = PropertyUtils.getByPath(property, "color");
    }
    // initial state
    assertEquals(false, property.isModified());
    assertEquals(false, widthProperty.isModified());
    assertEquals(false, styleProperty.isModified());
    assertEquals(false, colorProperty.isModified());
    assertEquals("", getPropertyText(property));
    assertEquals(null, getPropertyText(widthProperty));
    assertEquals(null, getPropertyText(styleProperty));
    assertEquals(null, getPropertyText(colorProperty));
    // set "sub" value
    widthProperty.setValue("10");
    styleProperty.setValue("solid");
    colorProperty.setValue("red");
    assertEquals(true, property.isModified());
    assertEquals(true, widthProperty.isModified());
    assertEquals(true, styleProperty.isModified());
    assertEquals(true, colorProperty.isModified());
    assertEquals("10px solid red", getPropertyText(property));
    assertEquals("10px", getPropertyText(widthProperty));
    assertEquals("solid", getPropertyText(styleProperty));
    assertEquals("red", getPropertyText(colorProperty));
    assertEquals(getSource(".style {", "  border: 10px solid red;", "}"), getFileContent(styleFile));
    // set different "color-left" value
    {
      PropertyUtils.getByPath(colorProperty, "left").setValue("lime");
      assertEquals("10px solid (red red red lime)", getPropertyText(property));
      assertEquals("10px", getPropertyText(widthProperty));
      assertEquals("solid", getPropertyText(styleProperty));
      assertEquals("red red red lime", getPropertyText(colorProperty));
      assertEquals(
          getSource(
              ".style {",
              "  border-top: 10px solid red;",
              "  border-right: 10px solid red;",
              "  border-bottom: 10px solid red;",
              "  border-left: 10px solid lime;",
              "}"),
          getFileContent(styleFile));
    }
  }

  /**
   * Test for {@link StyleSetPropertyEditor}, used for "text/decoration".
   */
  public void test_semantics_styleSet() throws Exception {
    dontUseSharedGWTState();
    IFile styleFile = setFileContent("war/Module.css", getSource(".style {", "}"));
    // prepare property
    final Property property;
    final Property underlineProperty;
    {
      Property styleProperty = getStyleProperty("setStyleName('style')");
      property = PropertyUtils.getByPath(styleProperty, "text/decoration");
      underlineProperty = PropertyUtils.getByPath(property, "underline");
    }
    // initial state
    assertEquals(false, property.isModified());
    assertEquals(false, underlineProperty.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(false, underlineProperty.getValue());
    // set "underline" value
    underlineProperty.setValue(true);
    assertEquals(true, property.isModified());
    assertEquals(true, underlineProperty.isModified());
    assertEquals("underline", getPropertyText(property));
    assertEquals(true, underlineProperty.getValue());
    assertEquals(
        getSource(".style {", "  text-decoration: underline;", "}"),
        getFileContent(styleFile));
    // remove "underline" value
    underlineProperty.setValue(Property.UNKNOWN_VALUE);
    assertEquals(false, property.isModified());
    assertEquals(false, underlineProperty.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(false, underlineProperty.getValue());
    assertEquals(getSource(".style {", "}"), getFileContent(styleFile));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property getStyleProperty(String styleLine) throws Exception {
    WidgetInfo panel = parseJavaInfo(getStyleSource(styleLine));
    refresh();
    return panel.getPropertyByTitle("styleName");
  }

  private static String getStyleSource(String styleLine) {
    String[] styleLines =
        styleLine != null ? new String[]{"    " + styleLine + ";"} : ArrayUtils.EMPTY_STRING_ARRAY;
    return getSource3(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {"}, styleLines, new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "  }",
        "}"});
  }

  private static void callPaint(Property property) throws Exception {
    int width = 100;
    int height = 50;
    Image image = new Image(null, width, height);
    GC gc = new GC(image);
    try {
      property.getEditor().paint(property, gc, 0, 0, width, height);
    } finally {
      gc.dispose();
      image.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo property editor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Shell TEST_COMBO_SHELL = new Shell();
  private static final CComboBox TEST_COMBO = new CComboBox(TEST_COMBO_SHELL, SWT.NONE);

  /**
   * Fill combo with items.
   */
  protected static void addComboPropertyItems(Property property) {
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "addItems("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CComboBox)";
    TEST_COMBO.removeAll();
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
  }

  /**
   * @return items from combo.
   */
  protected static List<String> getComboPropertyItems() {
    List<String> items = Lists.newArrayList();
    int itemCount = TEST_COMBO.getItemCount();
    for (int i = 0; i < itemCount; i++) {
      items.add(TEST_COMBO.getItemLabel(i));
    }
    return items;
  }

  /**
   * @return the selection index in combo.
   */
  protected static int getComboPropertySelection() {
    return TEST_COMBO.getSelectionIndex();
  }

  /**
   * Sets the selection index in combo, usually to use then
   * {@link #setComboPropertySelection(Property)} and validate result using
   * {@link #getComboPropertySelection()}.
   */
  protected static void setComboPropertySelection(int index) {
    TEST_COMBO.setSelectionIndex(index);
  }

  /**
   * Sets selection which corresponds to the value of {@link Property}.
   */
  protected static void setComboPropertySelection(Property property) {
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "selectItem("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CComboBox)";
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
  }

  /**
   * Simulates user selection of item with given index, updates {@link Property}.
   */
  protected static void setComboPropertyValue(Property property, String text) {
    TEST_COMBO.setEditText(text);
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "toPropertyEx("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CComboBox)";
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
  }
}