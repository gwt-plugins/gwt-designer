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
package com.google.gdt.eclipse.designer.core.model.web;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.web.ServletElement;
import com.google.gdt.eclipse.designer.model.web.ServletMappingElement;
import com.google.gdt.eclipse.designer.model.web.WebAppElement;
import com.google.gdt.eclipse.designer.model.web.WebDocumentEditContext;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.model.web.WelcomeFileElement;
import com.google.gdt.eclipse.designer.model.web.WelcomeFileListElement;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Test for web.xml file and its model.
 * 
 * @author scheglov_ke
 */
public class WebModelTest extends AbstractJavaTest {
  private static IFile m_webFile;
  private WebDocumentEditContext m_editContext;
  private String m_webContent;
  private WebAppElement m_webElement;

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
      GTestUtils.configure(GTestUtils.getLocation_20(), m_testProject);
    }
    // create module
    {
      GTestUtils.createModule(m_testProject, "test.Module");
      m_webFile = getFile("war/WEB-INF/web.xml");
    }
  }

  @Override
  protected void tearDown() throws Exception {
    if (m_editContext != null) {
      m_editContext.disconnect();
      m_editContext = null;
    }
    // remove module
    if (m_testProject != null) {
      IFolder folder = m_testProject.getJavaProject().getProject().getFolder("src");
      deleteFiles(folder);
    }
    // remove other things
    super.tearDown();
  }

  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // welcome-file
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link WelcomeFileElement}, parse and update existing.
   */
  public void test_WelcomeFileElement_update() throws Exception {
    parse(
        "<web-app>",
        "  <welcome-file-list>",
        "    <welcome-file>MyFile.html</welcome-file>",
        "  </welcome-file-list>",
        "</web-app>");
    assertThat(m_webElement.getChildren()).hasSize(1);
    // prepare WelcomeFileListElement
    WelcomeFileListElement welcomeFileListElement;
    {
      List<WelcomeFileListElement> welcomeListElements = m_webElement.getWelcomeFileListElements();
      assertThat(welcomeListElements).hasSize(1);
      welcomeFileListElement = welcomeListElements.get(0);
    }
    // prepare WelcomeFileElement
    WelcomeFileElement welcomeFile;
    {
      List<WelcomeFileElement> welcomeFiles = welcomeFileListElement.getWelcomeFiles();
      assertThat(welcomeFiles).hasSize(1);
      welcomeFile = welcomeFiles.get(0);
    }
    // initial state
    assertEquals("MyFile.html", welcomeFile.getName());
    // set new "name" value
    {
      welcomeFile.setName("newName.html");
      assertEquals("newName.html", welcomeFile.getName());
      m_webContent = StringUtils.replace(m_webContent, "MyFile", "newName");
      assertUpdatedModuleFile(m_webContent);
    }
    // toString()
    assertEquals(
        getSource(
            "<web-app>",
            "  <welcome-file-list>",
            "    <welcome-file>newName.html</welcome-file>",
            "  </welcome-file-list>",
            "</web-app>"),
        m_webElement.toString());
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
  /**
   * Parses module from {@link IFile}.
   */
  public void test_parseFromFile() throws Exception {
    WebAppElement webElement = WebUtils.readModule(m_webFile);
    List<DocumentElement> elements = webElement.getChildren(DocumentElement.class);
    assertThat(elements).isNotEmpty();
  }

  /**
   * Parse empty web.xml file.
   */
  public void test_parseEmpty() throws Exception {
    parse(new String[]{
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<!-- ------------- -->",
        "<web-app/>"});
    assertThat(m_webElement.getChildren()).isEmpty();
    // toString()
    assertEquals("<web-app/>\n", m_webElement.toString());
  }

  /**
   * Parse file with some default element.
   */
  public void test_parseNonSpecial() throws Exception {
    parse("<web-app>", "  <some-tag attr='value'/>", "</web-app>");
    assertThat(m_webElement.getChildren()).hasSize(1);
    // toString()
    assertEquals(
        getSourceDQ("<web-app>", "  <some-tag attr='value'/>", "</web-app>"),
        m_webElement.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ServletElement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link ServletElement}, parse and update existing.
   */
  public void test_ServletElement_update() throws Exception {
    parse(
        "<web-app>",
        "  <servlet>",
        "    <servlet-name>someName</servlet-name>",
        "    <servlet-class>some.Class</servlet-class>",
        "  </servlet>",
        "</web-app>");
    assertThat(m_webElement.getChildren()).hasSize(1);
    //
    List<ServletElement> servletElements = m_webElement.getServletElements();
    ServletElement servletElement = servletElements.get(0);
    assertThat(servletElements).hasSize(1);
    assertEquals("someName", servletElement.getName());
    assertEquals("some.Class", servletElement.getClassName());
    // set new "name" value
    {
      servletElement.setName("newName");
      m_webContent = StringUtils.replace(m_webContent, "someName", "newName");
      assertUpdatedModuleFile(m_webContent);
    }
    // set new "class" value
    {
      servletElement.setClassName("new.Class");
      m_webContent = StringUtils.replace(m_webContent, "some.Class", "new.Class");
      assertUpdatedModuleFile(m_webContent);
    }
    // toString()
    assertEquals(
        getSource(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>newName</servlet-name>",
            "    <servlet-class>new.Class</servlet-class>",
            "  </servlet>",
            "</web-app>"),
        m_webElement.toString());
  }

  /**
   * Tests for {@link ServletElement}, create new.
   */
  public void test_ServletElement_new() throws Exception {
    parse("<web-app>", "</web-app>");
    assertThat(m_webElement.getChildren()).isEmpty();
    //
    ServletElement servletElement = new ServletElement();
    m_webElement.addChild(servletElement);
    assertEquals(null, servletElement.getName());
    assertEquals(null, servletElement.getClassName());
    // set attributes
    servletElement.setName("newName");
    servletElement.setClassName("new.Class");
    assertEquals("newName", servletElement.getName());
    assertEquals("new.Class", servletElement.getClassName());
    // toString()
    assertEquals(
        getSource(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>newName</servlet-name>",
            "    <servlet-class>new.Class</servlet-class>",
            "  </servlet>",
            "</web-app>"),
        m_webElement.toString());
  }

  /**
   * Tests for {@link ServletMappingElement}, parse and update existing.
   */
  public void test_ServletMappingElement_update() throws Exception {
    parse(
        "<web-app>",
        "  <servlet-mapping>",
        "    <servlet-name>someName</servlet-name>",
        "    <url-pattern>some/pattern</url-pattern>",
        "  </servlet-mapping>",
        "</web-app>");
    assertThat(m_webElement.getChildren()).hasSize(1);
    //
    List<ServletMappingElement> elements = m_webElement.getServletMappingElements();
    ServletMappingElement element = elements.get(0);
    assertThat(elements).hasSize(1);
    assertEquals("someName", element.getName());
    assertEquals("some/pattern", element.getPattern());
    // set new "name" value
    {
      element.setName("newName");
      m_webContent = StringUtils.replace(m_webContent, "someName", "newName");
      assertUpdatedModuleFile(m_webContent);
    }
    // set new "pattern" value
    {
      element.setPattern("new/pattern");
      m_webContent = StringUtils.replace(m_webContent, "some/pattern", "new/pattern");
      assertUpdatedModuleFile(m_webContent);
    }
    // toString()
    assertEquals(
        getSource(
            "<web-app>",
            "  <servlet-mapping>",
            "    <servlet-name>newName</servlet-name>",
            "    <url-pattern>new/pattern</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        m_webElement.toString());
  }

  /**
   * Tests for {@link ServletMappingElement}, create new.
   */
  public void test_ServletMappingElement_new() throws Exception {
    parse("<web-app>", "</web-app>");
    assertThat(m_webElement.getChildren()).isEmpty();
    //
    ServletMappingElement element = new ServletMappingElement();
    m_webElement.addChild(element);
    assertEquals(null, element.getName());
    assertEquals(null, element.getPattern());
    // set attributes
    element.setName("newName");
    element.setPattern("new/pattern");
    assertEquals("newName", element.getName());
    assertEquals("new/pattern", element.getPattern());
    // toString()
    assertEquals(
        getSource(
            "<web-app>",
            "  <servlet-mapping>",
            "    <servlet-name>newName</servlet-name>",
            "    <url-pattern>new/pattern</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        m_webElement.toString());
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
    // set web.xml content
    {
      m_webContent = getSource(lines);
      ByteArrayInputStream input = new ByteArrayInputStream(m_webContent.getBytes());
      m_webFile.setContents(input, true, false, null);
    }
    // prepare new edit context
    m_editContext = new WebDocumentEditContext(m_webFile);
    // prepare web-app element
    m_webElement = m_editContext.getWebAppElement();
  }

  /**
   * Commits {@link #m_editContext} and asserts that {@link #m_webFile} has expected contents.
   */
  private void assertUpdatedModuleFile(String expectedContent) throws Exception {
    m_editContext.commit();
    assertEquals(expectedContent, IOUtils2.readString(m_webFile));
  }
}