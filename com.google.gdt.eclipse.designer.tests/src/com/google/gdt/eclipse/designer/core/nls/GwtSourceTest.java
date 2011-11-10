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
package com.google.gdt.eclipse.designer.core.nls;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.nls.GwtSource;
import com.google.gdt.eclipse.designer.nls.GwtSourceNewComposite;
import com.google.gdt.eclipse.designer.nls.SourceParameters;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.common.SourceClassParameters;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.core.nls.NlsTestUtils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * Test for {@link GwtSource}.
 * 
 * @author scheglov_ke
 */
public class GwtSourceTest extends GwtModelTest {
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

  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(0);
    super.tearDown();
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
  public void test_noSource_StringLiteral() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle('My title');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  public void test_noSource_invocationWithArguments() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(getMyText('My title'));",
            "  }",
            "  private String getMyText(String s) {",
            "    return s;",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  public void test_noSource_invocationWithExpression() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(new Object().toString());",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    assertEquals(0, support.getSources().length);
  }

  public void test_parse() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    // check that we have GWTSource
    GwtSource source;
    {
      AbstractSource[] sources = support.getSources();
      assertEquals(1, sources.length);
      source = (GwtSource) sources[0];
    }
    // check getBundleComment()
    assertEquals(
        "GWT variable: CONSTANTS",
        ReflectionUtils.invokeMethod(source, "getBundleComment()"));
    // check that "Button.text" is correct
    frame.refresh();
    assertEquals("My title", ReflectionUtils.invokeMethod(frame.getObject(), "getTitle()"));
  }

  public void test_possibleSources() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertThat(editableSources).hasSize(1);
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.client.MyConstants (Constants in variable/field 'CONSTANTS')",
        editableSource.getLongTitle());
  }

  /**
   * Empty *.properties file should not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47189
   */
  public void test_possibleSources_noComment() throws Exception {
    ensureI18N();
    setFileContentSrc("test/client/MyConstants.properties", getSourceDQ("rootPanel_title=My title"));
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertThat(editableSources).hasSize(1);
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.client.MyConstants (Constants in variable/field 'CONSTANTS')",
        editableSource.getLongTitle());
    assertThat(editableSource.getKeys()).contains("rootPanel_title");
  }

  /**
   * Empty *.properties file should not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45213
   */
  public void test_possibleSources_emptyFile() throws Exception {
    ensureI18N();
    setFileContentSrc("test/client/MyConstants.properties", "");
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertThat(editableSources).hasSize(1);
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.client.MyConstants (Constants in variable/field 'CONSTANTS')",
        editableSource.getLongTitle());
    assertThat(editableSource.getKeys()).isEmpty();
  }

  /**
   * We should be able to identify possible source even if it has not "Constants" suffix.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47189
   */
  public void test_possibleSources_differentName() throws Exception {
    ensureI18N();
    setFileContentSrc("test/client/MyMessages.properties", "");
    setFileContentSrc(
        "test/client/MyMessages.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyMessages extends Constants {",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    //
    List<IEditableSource> editableSources = editableSupport.getEditableSources();
    assertThat(editableSources).hasSize(1);
    IEditableSource editableSource = editableSources.get(0);
    assertEquals(
        "test.client.MyMessages (Constants in variable/field 'CONSTANTS')",
        editableSource.getLongTitle());
    assertThat(editableSource.getKeys()).isEmpty();
  }

  public void test_addLocale_removeLocale() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    //
    // STAGE #1: add locale
    //
    {
      IEditableSupport editableSupport = support.getEditable();
      IEditableSource editableSource = editableSupport.getEditableSources().get(0);
      // check initial locales
      {
        LocaleInfo[] locales = editableSource.getLocales();
        assertEquals(1, locales.length);
        assertEquals("(default)", locales[0].getTitle());
      }
      // add locales
      {
        editableSource.addLocale(new LocaleInfo(new Locale("it")), LocaleInfo.DEFAULT);
        editableSource.addLocale(new LocaleInfo(new Locale("fr")), null);
      }
      // check new locales
      {
        LocaleInfo[] locales = editableSource.getLocales();
        assertEquals(3, locales.length);
        assertEquals("(default)", locales[0].getTitle());
        assertEquals("it", locales[1].getTitle());
        assertEquals("fr", locales[2].getTitle());
      }
      // apply commands
      support.applyEditable(editableSupport);
      // checks
      {
        // *.properties: default
        {
          String newProperties = getFileContentSrc("test/client/MyConstants.properties");
          assertTrue(newProperties.contains("rootPanel_title=My title"));
        }
        // *.properties: it
        {
          String newProperties = getFileContentSrc("test/client/MyConstants_it.properties");
          assertTrue(newProperties.contains("rootPanel_title=My title"));
        }
        // *.properties: fr
        {
          String newProperties = getFileContentSrc("test/client/MyConstants_fr.properties");
          assertFalse(newProperties.contains("rootPanel_title=My title"));
        }
        // module
        {
          String module = getFileContentSrc("test/Module.gwt.xml");
          assertTrue(module.contains("<extend-property name=\"locale\" values=\"fr,it\"/>"));
        }
      }
    }
    //
    // STAGE #2: remove locale
    //
    {
      IEditableSupport editableSupport = support.getEditable();
      IEditableSource editableSource = editableSupport.getEditableSources().get(0);
      // remove locales
      {
        editableSource.removeLocale(new LocaleInfo(new Locale("it")));
        editableSource.removeLocale(new LocaleInfo(new Locale("fr")));
        support.applyEditable(editableSupport);
      }
      // check new locales
      {
        LocaleInfo[] locales = editableSource.getLocales();
        assertEquals(1, locales.length);
        assertEquals("(default)", locales[0].getTitle());
      }
      // checks
      {
        // *.properties: default
        {
          String newProperties = getFileContentSrc("test/client/MyConstants.properties");
          assertTrue(newProperties.contains("rootPanel_title=My title"));
        }
        // no "it" and "fr"
        assertFalse(getFileSrc("test/client/MyConstants_it.properties").exists());
        assertFalse(getFileSrc("test/client/MyConstants_fr.properties").exists());
        // module
        {
          String module = getFileContentSrc("test/Module.gwt.xml");
          assertTrue(module.contains("<extend-property name=\"locale\" values=\"\"/>"));
        }
      }
    }
    // clean up
    {
      waitEventLoop(0);
      do_projectDispose();
    }
  }

  /**
   * Test for {@link GwtSource#apply_addKey(String)}.
   */
  public void test_apply_addKey() throws Exception {
    ensureI18N();
    setFileContentSrc("test/client/MyConstants.properties", getSourceDQ("#GWT variable: CONSTANTS"));
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "}"));
    waitForAutoBuild();
    //
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    String frameSource = m_lastEditor.getSource();
    // add key
    GwtSource source = new GwtSource(frame, "test.client.MyConstants", null);
    source.apply_addKey("newKey");
    // checks
    assertEditor(frameSource, m_lastEditor);
    {
      String accessor = getFileContentSrc("test/client/MyConstants.java");
      assertThat(accessor).contains("String newKey();");
    }
  }

  public void test_externalize() throws Exception {
    ensureI18N();
    setFileContentSrc("test/client/MyConstants.properties", getSourceDQ("#GWT variable: CONSTANTS"));
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "}"));
    waitForAutoBuild();
    //
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle('My title');",
            "    rootPanel.setStyleName('My style');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // prepare possible source
    IEditableSource editableSource;
    {
      List<IEditableSource> editableSources = editableSupport.getEditableSources();
      assertEquals(1, editableSources.size());
      editableSource = editableSources.get(0);
    }
    // do externalize
    {
      StringPropertyInfo propertyInfo;
      // text
      propertyInfo = new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("title"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
      // title
      propertyInfo =
          new StringPropertyInfo((GenericProperty) frame.getPropertyByTitle("styleName"));
      editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    }
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
        "    rootPanel.setStyleName(CONSTANTS.rootPanel_styleName());",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/client/MyConstants.java");
      assertEquals(
          getSourceDQ(
              "package test.client;",
              "import com.google.gwt.i18n.client.Constants;",
              "public interface MyConstants extends Constants {",
              "  String rootPanel_title();",
              "  String rootPanel_styleName();",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      assertTrue(newProperties.contains("#GWT variable: CONSTANTS"));
      assertTrue(newProperties.contains("rootPanel_title=My title"));
      assertTrue(newProperties.contains("rootPanel_styleName=My style"));
    }
  }

  public void test_renameKey() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do rename
    editableSource.renameKey("rootPanel_title", "rootPanel_title2");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setTitle(CONSTANTS.rootPanel_title2());",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/client/MyConstants.java");
      assertEquals(
          getSourceDQ(
              "package test.client;",
              "import com.google.gwt.i18n.client.Constants;",
              "public interface MyConstants extends Constants {",
              "  String rootPanel_title2();",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      assertFalse(newProperties.contains("rootPanel_title=My title"));
      assertTrue(newProperties.contains("rootPanel_title2=My title"));
    }
  }

  /**
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=2654#p10517
   */
  public void test_sortByKeys() throws Exception {
    createAccessorAndProperties();
    setFileContentSrc(
        "test/client/MyConstants.properties",
        getSource(
            "#GWT variable: CONSTANTS",
            "a_longKey=000",
            "rootPanel_title=My title",
            "z_longKey=999"));
    waitForAutoBuild();
    //
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
            "  }",
            "}");
    frame.refresh();
    //
    frame.getPropertyByTitle("title").setValue("New title");
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      newProperties = StringUtils.replace(newProperties, "\r\n", "\n");
      assertEquals(
          getSource(
              "#GWT variable: CONSTANTS",
              "a_longKey=000",
              "rootPanel_title=New title",
              "z_longKey=999"),
          newProperties);
    }
  }

  /**
   * Key in {@link GwtSource} may use '.', but name of method should use '_'.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45809
   */
  public void test_useKey() throws Exception {
    ensureI18N();
    setFileContentSrc(
        "test/client/MyConstants.properties",
        getSourceDQ("#GWT variable: CONSTANTS", "rootPanel.title=My title", "button_text=my text"));
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "  @Key('rootPanel.title')",
            "  String rootPanel_title();",
            "  String button_text();",
            "}"));
    waitForAutoBuild();
    //
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Button(CONSTANTS.button_text()));",
            "  }",
            "}");
    frame.refresh();
    GenericProperty property = (GenericProperty) frame.getPropertyByTitle("title");
    // set key
    {
      NlsSupport support = NlsSupport.get(frame);
      AbstractSource source = support.getSources()[0];
      source.useKey(property, "rootPanel.title");
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
        "    rootPanel.add(new Button(CONSTANTS.button_text()));",
        "  }",
        "}");
  }

  public void test_internalize() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    IEditableSource editableSource = editableSupport.getEditableSources().get(0);
    // do internalize
    editableSource.internalizeKey("rootPanel_title");
    // apply commands
    support.applyEditable(editableSupport);
    // check
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setTitle('My title');",
        "  }",
        "}");
    {
      String messages = getFileContentSrc("test/client/MyConstants.java");
      assertEquals(
          getSourceDQ(
              "package test.client;",
              "import com.google.gwt.i18n.client.Constants;",
              "public interface MyConstants extends Constants {",
              "  String rootPanel_title();",
              "}"),
          messages);
    }
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      assertFalse(newProperties.contains("rootPanel_title=My title"));
    }
  }

  @DisposeProjectAfter
  public void test_create() throws Exception {
    // recreate project
    {
      tearDown();
      do_projectDispose();
      setUp();
    }
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle('My title');",
            "  }",
            "}");
    NlsSupport support = NlsSupport.get(frame);
    IEditableSupport editableSupport = support.getEditable();
    // prepare editable source
    IEditableSource editableSource = NlsTestUtils.createEmptyEditable("test.client.MyConstants");
    editableSource.setKeyGeneratorStrategy(GwtSource.GWT_KEY_GENERATOR);
    // prepare parameters
    SourceParameters parameters = new SourceParameters();
    IJavaProject javaProject = m_lastEditor.getJavaProject();
    {
      parameters.m_constant = new SourceClassParameters();
      SourceClassParameters constant = parameters.m_constant;
      constant.m_sourceFolder = javaProject.findPackageFragmentRoot(new Path("/TestProject/src"));
      constant.m_package =
          javaProject.findPackageFragment(new Path("/TestProject/src/test/client"));
      constant.m_packageFolder = (IFolder) constant.m_package.getUnderlyingResource();
      constant.m_packageName = constant.m_package.getElementName();
      constant.m_className = "MyConstants";
      constant.m_fullClassName = "test.client.MyConstants";
      constant.m_exists = false;
    }
    parameters.m_fieldName = "CONSTANTS";
    // add source
    {
      SourceDescription sourceDescription =
          new SourceDescription(GwtSource.class, GwtSourceNewComposite.class);
      editableSupport.addSource(editableSource, sourceDescription, parameters);
    }
    // do externalize
    StringPropertyInfo propertyInfo = editableSupport.getProperties(frame).get(0);
    editableSupport.externalizeProperty(propertyInfo, editableSource, true);
    // apply commands
    support.applyEditable(editableSupport);
    // checks
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
        "  }",
        "}");
    // Constants
    {
      String messages = getFileContentSrc("test/client/MyConstants.java");
      messages = StringUtils.replace(messages, "\r\n", "\n");
      assertEquals(
          getSourceDQ(
              "package test.client;",
              "",
              "import com.google.gwt.i18n.client.Constants;",
              "",
              "public interface MyConstants extends Constants {",
              "  String rootPanel_title();",
              "}"),
          messages);
    }
    // *.properties
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      assertTrue(newProperties.contains("#GWT variable: CONSTANTS"));
      assertTrue(newProperties.contains("rootPanel_title=My title"));
    }
    // module
    {
      String module = getFileContentSrc("test/Module.gwt.xml");
      assertTrue(module.contains("com.google.gwt.i18n.I18N"));
    }
    // refresh, check that CONSTANTS is used and setTitle() executed
    {
      frame.refresh();
      assertEquals("My title", ReflectionUtils.invokeMethod(frame.getObject(), "getTitle()"));
    }
    // execute scheduled actions
    waitEventLoop(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live component" and NLS
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get live image of <code>Button</code> widget, when auto-externalize is ON.
   */
  public void test_liveImage_whenExternalized() throws Exception {
    createAccessorAndProperties();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyConstants CONSTANTS = GWT.create(MyConstants.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setTitle(CONSTANTS.rootPanel_title());",
            "  }",
            "}");
    frame.refresh();
    // remember old NLS state
    String properties = getFileContentSrc("test/client/MyConstants.properties");
    String messages = getFileContentSrc("test/client/MyConstants.java");
    // ask image
    {
      WidgetInfo button = createJavaInfo("com.google.gwt.user.client.ui.Button", null);
      Image image = button.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isGreaterThan(50);
      assertThat(image.getBounds().height).isGreaterThan(20);
    }
    // check that NLS state is not changed
    {
      String newProperties = getFileContentSrc("test/client/MyConstants.properties");
      String newMessages = getFileContentSrc("test/client/MyConstants.java");
      assertEquals(properties, newProperties);
      assertEquals(messages, newMessages);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If <code>Constants</code> passed into constructor, we know how to create it (well, if in
   * project not exactly this interface, but some its subclass, union of several interfaces passed,
   * we will fail; is in "Showcase" GWT sample).
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43671
   */
  public void test_Constants_asConstructorParameter() throws Exception {
    createAccessorAndProperties();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends HorizontalPanel {",
            "  public MyPanel(MyConstants constants) {",
            "    setTitle(constants.rootPanel_title());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    useStrictEvaluationMode(false);
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends MyPanel {",
            "  public Test(MyConstants constants) {",
            "    super(constants);",
            "  }",
            "}");
    panel.refresh();
    assertEquals("My title", (String) ReflectionUtils.invokeMethod(panel.getObject(), "getTitle()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureI18N() throws Exception {
    String moduleContent = getFileContentSrc("test/Module.gwt.xml");
    if (!moduleContent.contains("com.google.gwt.i18n.I18N")) {
      setFileContentSrc("test/Module.gwt.xml", getDoubleQuotes2(new String[]{
          "<module>",
          "  <inherits name='com.google.gwt.user.User'/>",
          "  <inherits name='com.google.gwt.i18n.I18N'/>",
          "  <entry-point class='test.client.Module'/>",
          "</module>"}));
      waitForAutoBuild();
    }
  }

  /**
   * Creates accessor class and default properties for key <code>rootPanel_title</code>.
   */
  private void createAccessorAndProperties() throws Exception {
    ensureI18N();
    setFileContentSrc(
        "test/client/MyConstants.properties",
        getSourceDQ("#GWT variable: CONSTANTS", "rootPanel_title=My title"));
    setFileContentSrc(
        "test/client/MyConstants.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.i18n.client.Constants;",
            "public interface MyConstants extends Constants {",
            "  String rootPanel_title();",
            "}"));
    waitForAutoBuild();
  }
}