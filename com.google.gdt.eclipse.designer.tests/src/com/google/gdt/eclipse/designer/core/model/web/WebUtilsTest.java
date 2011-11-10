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
package com.google.gdt.eclipse.designer.core.model.web;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.web.WebUtils;

import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

/**
 * Test for {@link WebUtils}.
 * 
 * @author scheglov_ke
 */
public class WebUtilsTest extends AbstractJavaTest {
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
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    do_projectDispose();
    Activator.getStore().setToDefault(Constants.P_WEB_FOLDER);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getWebFolderName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link WebUtils#getWebFolderName(IProject)}.
   */
  public void test_getWebFolderName_emptyJavaProject() throws Exception {
    Activator.getStore().setValue(Constants.P_WEB_FOLDER, "myWar");
    assertFalse(getFolder0("myWar").exists());
    assertEquals("myWar", WebUtils.getWebFolderName(m_project));
    assertEquals("myWar", WebUtils.getWebFolderName(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolderName(IProject)}.
   * <p>
   * Project with "WebContent" folder, imply that WTP project and use this folder.
   */
  public void test_getWebFolderName_hasWebContent() throws Exception {
    getFolder("WebContent");
    assertTrue(getFolder0("WebContent").exists());
    assertEquals("WebContent", WebUtils.getWebFolderName(m_project));
    assertEquals("WebContent", WebUtils.getWebFolderName(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolderName(IProject)}.
   * <p>
   * Project with Maven structure, web folder in "src/main/webapp".
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43760
   */
  public void test_getWebFolderName_mavenFolder_webapp() throws Exception {
    String name = "src/main/webapp";
    getFolder(name);
    assertTrue(getFolder0(name).exists());
    assertEquals(name, WebUtils.getWebFolderName(m_project));
    assertEquals(name, WebUtils.getWebFolderName(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolderName(IProject)}.
   */
  public void test_getWebFolderName_existingGWTProject() throws Exception {
    GTestUtils.configure(GTestUtils.getLocation_20(), m_testProject);
    assertTrue(getFolder0("war").exists());
    assertEquals("war", WebUtils.getWebFolderName(m_project));
    assertEquals("war", WebUtils.getWebFolderName(m_javaProject));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getWebFolder()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link WebUtils#getWebFolder(IProject)}.
   */
  public void test_getWebFolder_emptyJavaProject() throws Exception {
    Activator.getStore().setValue(Constants.P_WEB_FOLDER, "myWar");
    IFolder expected = getFolder0("myWar");
    assertFalse(expected.exists());
    assertEquals(expected, WebUtils.getWebFolder(m_project));
    assertEquals(expected, WebUtils.getWebFolder(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolder(IProject)}.
   * <p>
   * Project with "WebContent" folder, imply that WTP project and use this folder.
   */
  public void test_getWebFolder_hasWebContent() throws Exception {
    IFolder expected = getFolder("WebContent");
    assertTrue(expected.exists());
    assertEquals(expected, WebUtils.getWebFolder(m_project));
    assertEquals(expected, WebUtils.getWebFolder(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolder(IProject)}.
   * <p>
   * Project with Maven structure, web folder in "src/main/webapp".
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43760
   */
  public void test_getWebFolder_mavenFolder_webapp() throws Exception {
    IFolder expected = getFolder("src/main/webapp");
    assertTrue(expected.exists());
    assertEquals(expected, WebUtils.getWebFolder(m_project));
    assertEquals(expected, WebUtils.getWebFolder(m_javaProject));
  }

  /**
   * Test for {@link WebUtils#getWebFolder(IProject)}.
   */
  public void test_getWebFolder_existingGWTProject() throws Exception {
    GTestUtils.configure(GTestUtils.getLocation_20(), m_testProject);
    IFolder expected = getFolder0("war");
    assertTrue(expected.exists());
    assertEquals(expected, WebUtils.getWebFolder(m_project));
    assertEquals(expected, WebUtils.getWebFolder(m_javaProject));
  }
}