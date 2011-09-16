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

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.property.ImageUrlPropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.tests.designer.tests.common.PropertyNoValue;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Test for {@link ImageUrlPropertyEditor}.
 * 
 * @author sablin_aa
 */
public class ImageUrlPropertyEditorTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
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
  public void test_noValue() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = new Image((String) null);",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo image = getJavaInfoByName("image");
    //
    Property property = image.getPropertyByTitle("url");
    assertEquals(null, getPropertyText(property));
  }

  public void test_value() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = new Image('1.png');",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo image = getJavaInfoByName("image");
    //
    Property property = image.getPropertyByTitle("url");
    assertEquals("1.png", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openDialog() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = new Image((String) null);",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo image = getJavaInfoByName("image");
    //
    final Property property = image.getPropertyByTitle("url");
    animateDialog(property);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = new Image('2.png');",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
  }

  public void test_openDialog_withShell() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Image image = new Image((String) null);",
        "      rootPanel.add(image);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo image = getJavaInfoByName("image");
    //
    final Object[] result = new Object[1];
    PropertyEditor propertyEditor =
        new ImageUrlPropertyEditor(DesignerPlugin.getShell(), image.getState());
    final Property property = new PropertyNoValue(propertyEditor) {
      @Override
      public String getTitle() {
        return "url";
      }

      @Override
      public void setValue(Object value) throws Exception {
        result[0] = value;
      }
    };
    animateDialog(property);
    assertEquals("2.png", result[0]);
  }

  private static void animateDialog(final Property property) throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("url");
        {
          Text text = context.findFirstWidget(Text.class);
          text.setText("2.png");
        }
        {
          TreeItem item = context.getTreeItem("2.png");
          UiContext.setSelection(item);
        }
        context.clickButton("OK");
      }
    });
  }
}