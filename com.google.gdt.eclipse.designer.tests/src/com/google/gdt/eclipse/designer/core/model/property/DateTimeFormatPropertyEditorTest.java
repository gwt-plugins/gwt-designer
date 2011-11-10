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
package com.google.gdt.eclipse.designer.core.model.property;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.property.DateTimeFormatPropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link DateTimeFormatPropertyEditor}.
 * 
 * @author sablin_aa
 */
public class DateTimeFormatPropertyEditorTest extends GwtModelTest {
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
    configureProject();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    Property formatProperty = button.getPropertyByTitle("format");
    assertFalse(formatProperty.isModified());
    assertNull(getPropertyText(formatProperty));
    assertThat(formatProperty.getEditor()).isInstanceOf(DateTimeFormatPropertyEditor.class);
  }

  public void test_value() throws Exception {
    configureProject();
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.i18n.client.DateTimeFormat;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setFormat(new MyFormat(DateTimeFormat.getShortDateTimeFormat()));",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    Property formatProperty = button.getPropertyByTitle("format");
    Class<?> formatClass = m_lastLoader.loadClass("com.google.gwt.i18n.client.DateTimeFormat");
    // check initial value
    {
      Object shortFormat = ReflectionUtils.invokeMethod(formatClass, "getShortDateTimeFormat()");
      assertTrue(formatProperty.isModified());
      assertThat(getPropertyText(formatProperty)).isEqualTo(
          (String) ReflectionUtils.invokeMethod(shortFormat, "getPattern()"));
    }
    // set new value
    {
      Object fullFormat = ReflectionUtils.invokeMethod(formatClass, "getFullDateTimeFormat()");
      formatProperty.setValue(fullFormat);
      assertThat(getPropertyText(formatProperty)).isEqualTo(
          (String) ReflectionUtils.invokeMethod(fullFormat, "getPattern()"));
      assertEditor(
          "import com.google.gwt.i18n.client.DateTimeFormat;",
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      button.setFormat(new MyFormat(DateTimeFormat.getFullDateTimeFormat()));",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
    }
    // set custom value
    {
      String customFormatString = "yyyy mm dd";
      Object customFormat =
          ReflectionUtils.invokeMethod(
              formatClass,
              "getFormat(java.lang.String)",
              customFormatString);
      formatProperty.setValue(customFormat);
      assertThat(getPropertyText(formatProperty)).isEqualTo(customFormatString);
      assertEditor(
          "import com.google.gwt.i18n.client.DateTimeFormat;",
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      button.setFormat(new MyFormat(DateTimeFormat.getFormat('"
              + customFormatString
              + "')));",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureProject() throws Exception {
    setFileContentSrc(
        "test/client/MyFormat.java",
        getTestSource(
            "import com.google.gwt.i18n.client.DateTimeFormat;",
            "public class MyFormat {",
            "  DateTimeFormat m_format;",
            "  public MyFormat(DateTimeFormat format) {",
            "    m_format = format;",
            "  }",
            "  public DateTimeFormat getDateTimeFormat() {",
            "    return m_format;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public void setFormat(MyFormat format) {",
            "  }",
            "}"));
    setFileContentSrc("test/client/MyButton.wbp-component.xml", DesignerTestCase.getSourceDQ(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <property id='setFormat(test.client.MyFormat)'>",
        "    <editor id='gwt.dateTimeFormat'>",
        "      <parameter name='extract'>value.getDateTimeFormat()</parameter>",
        "      <parameter name='source'>new MyFormat(%value%)</parameter>",
        "    </editor>",
        "  </property>",
        "</component>"));
    waitForAutoBuild();
  }
}