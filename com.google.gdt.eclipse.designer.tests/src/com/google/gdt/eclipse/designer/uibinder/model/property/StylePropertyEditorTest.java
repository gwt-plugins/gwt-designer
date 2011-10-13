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
package com.google.gdt.eclipse.designer.uibinder.model.property;

import com.google.gdt.eclipse.designer.model.property.css.StylePropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link StylePropertyEditor} in UiBinder.
 * 
 * @author scheglov_ke
 */
public class StylePropertyEditorTest extends UiBinderModelTest {
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
    Property property = getStyleProperty(null, ArrayUtils.EMPTY_STRING_ARRAY);
    assertEquals("", getPropertyText(property));
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
    Property styleProperty = getStyleProperty(null, ArrayUtils.EMPTY_STRING_ARRAY);
    assertThat(PropertyUtils.getChildren(styleProperty)).isEmpty();
  }

  /**
   * Test for {@link StyleSimpleValuePropertyEditor}.
   */
  public void test_semantics_simpleValue() throws Exception {
    // prepare property
    final Property property;
    {
      Property styleProperty =
          getStyleProperty("myPanel", new String[]{".myPanel {", "  color: red;", "}"});
      property = PropertyUtils.getByPath(styleProperty, "color");
    }
    // initial state
    assertEquals(true, property.isModified());
    assertEquals("red", getPropertyText(property));
    // remove "color" value
    property.setValue(Property.UNKNOWN_VALUE);
    assertEquals(false, property.isModified());
    assertEquals(null, getPropertyText(property));
    assertEquals(
        getTestSource(getStyleSourceLines("myPanel", new String[]{".myPanel {", "}"})),
        m_lastContext.getContent());
    // add "color" value
    property.setValue("pink");
    assertEquals(true, property.isModified());
    assertEquals("pink", getPropertyText(property));
    assertEquals(
        getTestSource(getStyleSourceLines("myPanel", new String[]{
            ".myPanel {",
            "  color: pink;",
            "}"})),
        m_lastContext.getContent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClientBundle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using @gwtd.reload.null to reload {@link ClientBundle}.
   */
  @DisposeProjectAfter
  public void test_ClientBundle_reloadNull() throws Exception {
    setFileContentSrc(
        "test/client/MyResources.java",
        getSourceDQ(
            "package test.client;",
            "",
            "import com.google.gwt.core.client.GWT;",
            "import com.google.gwt.resources.client.ClientBundle;",
            "import com.google.gwt.resources.client.CssResource;",
            "import com.google.gwt.resources.client.CssResource.NotStrict;",
            "",
            "public class MyResources {",
            "  public interface Style extends CssResource {",
            "    String first();",
            "    String second();",
            "  }",
            "  public interface Resources extends ClientBundle {",
            "    @NotStrict",
            "    @Source('MyResources.css')",
            "    Style style();",
            "  }",
            "  /** @gwtd.reload.null */",
            "  private static Resources resources;",
            "  public static Style style() {",
            "    return resources().style();",
            "  }",
            "  public static Resources resources() {",
            "    if (resources == null) {",
            "      resources = GWT.create(Resources.class);",
            "      resources.style().ensureInjected();",
            "    }",
            "    return resources;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyResources.css",
        getSource(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".first {",
            "  color: red;",
            "}",
            ".second {}",
            ".third {}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <ui:with field='resources' type='test.client.MyResources'/>",
            "  <g:FlowPanel styleName='{resources.style.first}'/>",
            "</ui:UiBinder>");
    refresh();
    //
    Property styleProperty = panel.getPropertyByTitle("styleName");
    Property colorProperty = PropertyUtils.getByPath(styleProperty, "color");
    // initial state
    assertEquals("red", getPropertyText(colorProperty));
    assertEquals("red", getComputedStyleAttribute(panel, "color"));
    // set new value
    colorProperty.setValue("lime");
    assertEquals("lime", getPropertyText(colorProperty));
    assertEquals("lime", getComputedStyleAttribute(panel, "color"));
    System.out.println(getFileContentSrc("test/client/MyResources.css"));
  }

  /**
   * Test for using @gwtd.reload.create to reload {@link ClientBundle}.
   */
  @DisposeProjectAfter
  public void test_ClientBundle_reloadCreate() throws Exception {
    setFileContentSrc(
        "test/client/MyResources.java",
        getSourceDQ(
            "package test.client;",
            "",
            "import com.google.gwt.core.client.GWT;",
            "import com.google.gwt.resources.client.ClientBundle;",
            "import com.google.gwt.resources.client.CssResource;",
            "import com.google.gwt.resources.client.CssResource.NotStrict;",
            "",
            "public class MyResources {",
            "  public interface Style extends CssResource {",
            "    String first();",
            "    String second();",
            "  }",
            "  public interface Resources extends ClientBundle {",
            "    @NotStrict",
            "    @Source('MyResources.css')",
            "    Style style();",
            "  }",
            "  /** @gwtd.reload.create */",
            "  private static Resources resources;",
            "  static {",
            "    resources = GWT.create(Resources.class);",
            "    resources.style().ensureInjected();",
            "  }",
            "  public static Style style() {",
            "    return resources.style();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyResources.css",
        getSource(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".first {",
            "  color: red;",
            "}",
            ".second {}",
            ".third {}"));
    waitForAutoBuild();
    // parse
    System.out.println(getFileContentSrc("test/client/MyResources.css"));
    dontUseSharedGWTState();
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <ui:with field='resources' type='test.client.MyResources'/>",
            "  <g:FlowPanel styleName='{resources.style.first}'/>",
            "</ui:UiBinder>");
    refresh();
    //
    Property styleProperty = panel.getPropertyByTitle("styleName");
    Property colorProperty = PropertyUtils.getByPath(styleProperty, "color");
    // initial state
    assertEquals("red", getPropertyText(colorProperty));
    assertEquals("red", getComputedStyleAttribute(panel, "color"));
    // set new value
    colorProperty.setValue("lime");
    assertEquals("lime", getPropertyText(colorProperty));
    assertEquals("lime", getComputedStyleAttribute(panel, "color"));
    System.out.println(getFileContentSrc("test/client/MyResources.css"));
  }

  private static String getComputedStyleAttribute(WidgetInfo widget, String style) throws Exception {
    GwtState state = widget.getState();
    Object widgetElement = widget.getDOMElement();
    return state.getComputedStyle(widgetElement, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property getStyleProperty(String styleName, String[] styleLines) throws Exception {
    WidgetInfo panel = parse(getStyleSourceLines(styleName, styleLines));
    refresh();
    return panel.getPropertyByTitle("styleName");
  }

  private static String[] getStyleSourceLines(String styleName, String[] styleLines) {
    if (styleName == null) {
      return new String[]{
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:FlowPanel/>",
          "</ui:UiBinder>"};
    } else {
      String[] intentedStyleLines = (String[]) ArrayUtils.clone(styleLines);
      for (int i = 0; i < intentedStyleLines.length; i++) {
        intentedStyleLines[i] = "      " + intentedStyleLines[i];
      }
      return CodeUtils.join(new String[]{
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <ui:style>"}, intentedStyleLines, new String[]{
          "  </ui:style>",
          "  <g:FlowPanel styleName='{style." + styleName + "}'/>",
          "</ui:UiBinder>"});
    }
  }
}