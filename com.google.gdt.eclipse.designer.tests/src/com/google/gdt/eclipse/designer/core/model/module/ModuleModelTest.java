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
package com.google.gdt.eclipse.designer.core.model.module;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.module.EntryPointElement;
import com.google.gdt.eclipse.designer.model.module.ExcludeElement;
import com.google.gdt.eclipse.designer.model.module.ExtendPropertyElement;
import com.google.gdt.eclipse.designer.model.module.GwtDocumentEditContext;
import com.google.gdt.eclipse.designer.model.module.InheritsElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.module.PublicElement;
import com.google.gdt.eclipse.designer.model.module.ScriptElement;
import com.google.gdt.eclipse.designer.model.module.ServletElement;
import com.google.gdt.eclipse.designer.model.module.SetPropertyFallbackElement;
import com.google.gdt.eclipse.designer.model.module.SourceElement;
import com.google.gdt.eclipse.designer.model.module.StylesheetElement;
import com.google.gdt.eclipse.designer.model.module.SuperSourceElement;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFile;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Test for GWT module (*.gwt.xml file) and its model.
 * 
 * @author scheglov_ke
 */
public class ModuleModelTest extends AbstractJavaTest {
  private IFile m_moduleFile;
  private ModuleDescription m_moduleDescription;
  private GwtDocumentEditContext m_editContext;
  private String m_moduleContent;
  private ModuleElement m_module;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
      GTestUtils.configure(m_testProject);
      GTestUtils.createModule(m_testProject, "test.Module");
    }
    m_moduleFile = getFileSrc("test/Module.gwt.xml");
    m_moduleDescription = Utils.getExactModule(m_moduleFile);
  }

  @Override
  protected void tearDown() throws Exception {
    if (m_editContext != null) {
      m_editContext.disconnect();
      m_editContext = null;
    }
    super.tearDown();
  }

  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses module from {@link IFile}.
   */
  public void test_parseFromFile() throws Exception {
    ModuleElement moduleElement = Utils.readModule(m_moduleDescription);
    assertEquals("test.Module", moduleElement.getId());
    assertEquals("test.Module", moduleElement.getName());
  }

  /**
   * Parse empty GWT module.
   */
  public void test_parseEmpty() throws Exception {
    parse(new String[]{
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<module/>"});
    assertEquals("test.Module", m_module.getName());
    assertThat(m_module.getChildren()).isEmpty();
    // toString()
    assertEquals("<module/>\n", m_module.toString());
  }

  /**
   * Parse GWT module with "rename-to" attribute.
   */
  public void test_renameTo() throws Exception {
    parse("<module rename-to='shortName'/>");
    assertEquals("test.Module", m_module.getId());
    assertEquals("shortName", m_module.getName());
    assertEquals(getSourceDQ(new String[]{"<module rename-to='shortName'/>"}), m_module.toString());
  }

  /**
   * Parse GWT module with some default element.
   */
  public void test_parseNonSpecial() throws Exception {
    parse(new String[]{
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<module>",
        "  <some-tag attr='value'/>",
        "</module>"});
    assertEquals("test.Module", m_module.getName());
    assertThat(m_module.getChildren()).hasSize(1);
    // toString()
    assertEquals(
        getSourceDQ("<module>", "  <some-tag attr='value'/>", "</module>"),
        m_module.toString());
  }

  /**
   * Parse GWT module with "entry-point" element.
   */
  public void test_parseEntryPoint() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <entry-point class='some.class'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<entry-point>" element
    EntryPointElement entryPointElement;
    {
      List<EntryPointElement> entryPointElements = m_module.getEntryPointElements();
      assertThat(entryPointElements).hasSize(1);
      entryPointElement = entryPointElements.get(0);
    }
    // current "class" value
    assertEquals("some.class", entryPointElement.getClassName());
    // set new "class" value
    {
      entryPointElement.setClassName("new.class");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.class", "new.class"));
    }
    // toString()
    assertEquals(
        getSourceDQ("<module>", "  <entry-point class='new.class'/>", "</module>"),
        m_module.toString());
  }

  /**
   * Parse GWT module with "inherits" element.
   */
  public void test_parseInherits() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <inherits name='some.id'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<inherits>" element
    InheritsElement inheritsElement;
    {
      List<InheritsElement> inheritsElements = m_module.getInheritsElements();
      assertThat(inheritsElements).hasSize(1);
      inheritsElement = inheritsElements.get(0);
    }
    // 
    assertSame(inheritsElement, m_module.getInheritsElement("some.id"));
    assertNull(m_module.getInheritsElement("other.id"));
    // current "name" value
    assertEquals("some.id", inheritsElement.getName());
    // set new "name" value
    {
      inheritsElement.setName("new.id");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.id", "new.id"));
    }
  }

  /**
   * Parse GWT module with "public" element.
   */
  public void test_parsePublic() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <public path='some.path'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<public>" element
    PublicElement publicElement;
    {
      List<PublicElement> publicElements = m_module.getPublicElements();
      assertThat(publicElements).hasSize(1);
      publicElement = publicElements.get(0);
    }
    // current "path" value
    assertEquals("some.path", publicElement.getPath());
    // set new "path" value
    {
      publicElement.setPath("new.path");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.path", "new.path"));
    }
  }

  /**
   * Parse GWT module with "script" element.
   */
  public void test_parseScript() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <script src='some.url'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<script>" element
    ScriptElement scriptElement;
    {
      List<ScriptElement> scriptElements = m_module.getScriptElements();
      assertThat(scriptElements).hasSize(1);
      scriptElement = scriptElements.get(0);
    }
    // current "src" value
    assertEquals("some.url", scriptElement.getSrc());
    // set new "src" value
    {
      scriptElement.setSrc("new.url");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.url", "new.url"));
    }
  }

  /**
   * Parse GWT module with "servlet" element.
   */
  public void test_parseServlet() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <servlet path='some.path' class='some.class'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<servlet>" element
    ServletElement servletElement;
    {
      List<ServletElement> servletElements = m_module.getServletElements();
      assertThat(servletElements).hasSize(1);
      servletElement = servletElements.get(0);
    }
    // current values
    assertEquals("some.path", servletElement.getPath());
    assertEquals("some.class", servletElement.getClassName());
    // set new values
    {
      servletElement.setPath("new.path");
      servletElement.setClassName("new.class");
      String expected = StringUtils.replace(m_moduleContent, "some.path", "new.path");
      expected = StringUtils.replace(expected, "some.class", "new.class");
      assertUpdatedModuleFile(expected);
    }
  }

  /**
   * Parse GWT module with "source" element.
   */
  public void test_parseSource() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <source path='some.path'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<source>" element
    SourceElement sourceElement;
    {
      List<SourceElement> sourceElements = m_module.getSourceElements();
      assertThat(sourceElements).hasSize(1);
      sourceElement = sourceElements.get(0);
    }
    // current "path" value
    assertEquals("some.path", sourceElement.getPath());
    // set new "path" value
    {
      sourceElement.setPath("new.path");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.path", "new.path"));
    }
  }

  /**
   * Parse GWT module with "source" element with "exclude" sub-element.
   */
  public void test_parseSource_withExclude() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <source path='some.path'>",
        "    <exclude name='some.name'/>",
        "  </source>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<source>" element
    SourceElement sourceElement;
    {
      List<SourceElement> sourceElements = m_module.getSourceElements();
      assertThat(sourceElements).hasSize(1);
      sourceElement = sourceElements.get(0);
    }
    // current "path" value
    assertEquals("some.path", sourceElement.getPath());
    // ExcludeElement
    {
      List<ExcludeElement> excludeElements = sourceElement.getExcludeElements();
      assertThat(excludeElements).hasSize(1);
      ExcludeElement excludeElement = excludeElements.get(0);
      assertEquals("some.name", excludeElement.getName());
    }
  }

  /**
   * Parse GWT module with "super-source" element.
   */
  public void test_parseSuperSource() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <super-source path='some.path'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<super-source>" element
    SuperSourceElement sourceElement;
    {
      List<SuperSourceElement> sourceElements = m_module.getSuperSourceElements();
      assertThat(sourceElements).hasSize(1);
      sourceElement = sourceElements.get(0);
    }
    // current "path" value
    assertEquals("some.path", sourceElement.getPath());
    // set new "path" value
    {
      sourceElement.setPath("new.path");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.path", "new.path"));
    }
  }

  /**
   * Parse GWT module with "stylesheet " element.
   */
  public void test_parseStylesheet() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <stylesheet  src='some.src'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<stylesheet >" element
    StylesheetElement stylesheetElement;
    {
      List<StylesheetElement> stylesheetElements = m_module.getStylesheetElements();
      assertThat(stylesheetElements).hasSize(1);
      stylesheetElement = stylesheetElements.get(0);
    }
    // current "src" value
    assertEquals("some.src", stylesheetElement.getSrc());
    // set new "src" value
    {
      stylesheetElement.setSrc("new.src");
      assertUpdatedModuleFile(StringUtils.replace(m_moduleContent, "some.src", "new.src"));
    }
  }

  /**
   * Parse GWT module with "extend-property" element.
   */
  public void test_parseExtendProperty() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <extend-property name='some.name' values='some.values'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<extend-property>" element
    ExtendPropertyElement propertyElement;
    {
      List<ExtendPropertyElement> propertyElements = m_module.getExtendPropertyElements();
      assertThat(propertyElements).hasSize(1);
      propertyElement = propertyElements.get(0);
    }
    // current attribute values
    assertEquals("some.name", propertyElement.getName());
    assertEquals("some.values", propertyElement.getValues());
    // set new attribute value
    {
      propertyElement.setName("new.name");
      propertyElement.setValues("new.values");
      {
        String newContent = m_moduleContent;
        newContent = StringUtils.replace(newContent, "some.name", "new.name");
        newContent = StringUtils.replace(newContent, "some.values", "new.values");
        assertUpdatedModuleFile(newContent);
      }
    }
  }

  /**
   * Parse GWT module with "set-property-fallback" element.
   */
  public void test_parseSetPropertyFallback() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <set-property-fallback name='some.name' value='some.value'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(1);
    // prepare single "<set-property-fallback>" element
    SetPropertyFallbackElement element;
    {
      List<SetPropertyFallbackElement> elements = m_module.getSetPropertyFallbackElements();
      assertThat(elements).hasSize(1);
      element = elements.get(0);
    }
    // current attribute values
    assertEquals("some.name", element.getName());
    assertEquals("some.value", element.getValue());
    // set new attribute value
    {
      element.setName("new.name");
      element.setValue("new.value");
      {
        String newContent = m_moduleContent;
        newContent = StringUtils.replace(newContent, "some.name", "new.name");
        newContent = StringUtils.replace(newContent, "some.value", "new.value");
        assertUpdatedModuleFile(newContent);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSourceFolders()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ModuleElement#getSourceFolders()}. No "super-source" or "source", so default
   * "client" used.
   */
  public void test_getSourceFolders_0() throws Exception {
    parse(new String[]{
        "<!-- ------------------------------------------------- -->",
        "<module>",
        "</module>"});
    assertThat(m_module.getChildren()).isEmpty();
    {
      List<String> sourceFolders = m_module.getSourceFolders();
      assertThat(sourceFolders).hasSize(1).containsOnly("client");
    }
  }

  /**
   * Test for {@link ModuleElement#getSourceFolders()}. Both "super-source" and "source" element.
   */
  public void test_getSourceFolders_1() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <super-source path='super-path'/>",
        "  <source path='normal-path'/>",
        "</module>"});
    assertThat(m_module.getChildren()).hasSize(2);
    {
      List<String> sourceFolders = m_module.getSourceFolders();
      assertThat(sourceFolders).hasSize(2).containsOnly("super-path", "normal-path");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addInheritsElement() throws Exception {
    parse(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "  <otherTag value='some.value'/>",
        "</module>"});
    // no "inherits" - add as first
    m_module.addInheritsElement("some.name.1");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "  <inherits name='some.name.1'/>",
        "  <otherTag value='some.value'/>",
        "</module>"}), m_module.toString());
    // add after last existing "inherits"
    m_module.addInheritsElement("some.name.2");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "  <inherits name='some.name.1'/>",
        "  <inherits name='some.name.2'/>",
        "  <otherTag value='some.value'/>",
        "</module>"}), m_module.toString());
  }

  /**
   * Test for {@link ModuleElement#addStylesheetElement(String)}.
   */
  public void test_addStylesheetElement() throws Exception {
    parse(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "</module>"});
    m_module.addStylesheetElement("some.src");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "  <stylesheet src='some.src'/>",
        "</module>"}), m_module.toString());
  }

  /**
   * Test for {@link ModuleElement#addScriptElement(String)}.
   */
  public void test_addScriptElement() throws Exception {
    parse(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "</module>"});
    m_module.addScriptElement("some.src");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module foo='0123456789.0123456789.0123456789.0123456789.0123456789'>",
        "  <script src='some.src'/>",
        "</module>"}), m_module.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isInSourceFolder()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ModuleElement#isInSourceFolder(String)}.
   */
  public void test_isInSourceFolder_implicitSource() throws Exception {
    parse(new String[]{
        "<!-- ------------------------------------------------------ -->",
        "<module>",
        "</module>"});
    assertFalse(m_module.isInSourceFolder("foo"));
    assertTrue(m_module.isInSourceFolder("client"));
    assertTrue(m_module.isInSourceFolder("client/bar"));
  }

  /**
   * Test for {@link ModuleElement#isInSourceFolder(String)}.
   */
  public void test_isInSourceFolder_explicitSource() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <source path='myClient'/>",
        "</module>"});
    assertFalse(m_module.isInSourceFolder("client"));
    assertTrue(m_module.isInSourceFolder("myClient"));
    assertTrue(m_module.isInSourceFolder("myClient/foo"));
  }

  /**
   * Test for {@link ModuleElement#isInSourceFolder(String)}.
   */
  public void test_isInSourceFolder_explicitSource_withExclude() throws Exception {
    parse(new String[]{
        "<!-- -------------------------------- -->",
        "<module>",
        "  <source path='myClient'>",
        "    <exclude name='**/service/**'/>",
        "  </source>",
        "</module>"});
    assertFalse(m_module.isInSourceFolder("client"));
    assertTrue(m_module.isInSourceFolder("myClient"));
    assertTrue(m_module.isInSourceFolder("myClient/foo"));
    assertFalse(m_module.isInSourceFolder("myClient/foo/service"));
    assertFalse(m_module.isInSourceFolder("myClient/foo/service/bar"));
    assertFalse(m_module.isInSourceFolder("myClient/foo/bar/service/baz"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses GWT module file.
   */
  private void parse(String... lines) throws Exception {
    // set new module content
    {
      m_moduleContent = getSource(lines);
      ByteArrayInputStream input = new ByteArrayInputStream(m_moduleContent.getBytes());
      m_moduleFile.setContents(input, true, false, null);
    }
    // prepare new edit context
    m_editContext = new GwtDocumentEditContext(m_moduleFile);
    // prepare module element
    m_module = m_editContext.getModuleElement();
    m_module.setId(m_moduleDescription.getId());
    m_module.finalizeLoading();
  }

  /**
   * Commits {@link #m_editContext} and asserts that {@link #m_moduleFile} has expected contents.
   */
  private void assertUpdatedModuleFile(String expectedContent) throws Exception {
    m_editContext.commit();
    assertEquals(expectedContent, IOUtils2.readString(m_moduleFile));
  }
}