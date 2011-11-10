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
package com.google.gdt.eclipse.designer.core.description;

import com.google.gdt.eclipse.designer.GwtDescriptionVersionsProviderFactory;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.IJavaProject;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GwtDescriptionVersionsProviderFactory}.
 * 
 * @author scheglov_ke
 */
public class GwtDescriptionVersionsProviderFactoryTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    do_projectDispose();
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * "Decorates" given lines of source, usually adds required imports.
   */
  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.*;",
            "import com.google.gwt.user.client.*;",
            "import com.google.gwt.user.client.ui.*;"}, lines);
    return lines;
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
  // DescriptionVersionsProviderFactory_GWT
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_factory_notGWTProject() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    parseSource(
        "test",
        "Test.java",
        getSource(
            "import javax.swing.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}"));
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        GwtDescriptionVersionsProviderFactory.INSTANCE;
    // not GWT project, so no provider
    IDescriptionVersionsProvider provider = providerFactory.getProvider(javaProject, m_lastLoader);
    assertNull(provider);
    // no version
    assertThat(providerFactory.getVersions(javaProject, m_lastLoader)).isEmpty();
  }

  public void test_factory20() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    GTestUtils.configure(GTestUtils.getLocation_20(), m_testProject);
    GTestUtils.createModule(m_testProject, "test.Module");
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        GwtDescriptionVersionsProviderFactory.INSTANCE;
    IDescriptionVersionsProvider provider = providerFactory.getProvider(javaProject, m_lastLoader);
    {
      Class<?> componentClass = m_lastLoader.loadClass("com.google.gwt.user.client.ui.Button");
      List<String> versions = provider.getVersions(componentClass);
      assertThat(versions).containsExactly("2.0");
    }
  }

  public void test_factory21() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    GTestUtils.configure(GTestUtils.getLocation_21(), m_testProject);
    GTestUtils.createModule(m_testProject, "test.Module");
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        GwtDescriptionVersionsProviderFactory.INSTANCE;
    IDescriptionVersionsProvider provider = providerFactory.getProvider(javaProject, m_lastLoader);
    {
      Class<?> componentClass = m_lastLoader.loadClass("com.google.gwt.user.client.ui.Button");
      List<String> versions = provider.getVersions(componentClass);
      assertThat(versions).containsExactly("2.1", "2.0");
    }
  }

  public void test_factory22() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    GTestUtils.configure(GTestUtils.getLocation_22(), m_testProject);
    GTestUtils.createModule(m_testProject, "test.Module");
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        GwtDescriptionVersionsProviderFactory.INSTANCE;
    IDescriptionVersionsProvider provider = providerFactory.getProvider(javaProject, m_lastLoader);
    {
      Class<?> componentClass = m_lastLoader.loadClass("com.google.gwt.user.client.ui.Button");
      List<String> versions = provider.getVersions(componentClass);
      assertThat(versions).containsExactly("2.2", "2.1", "2.0");
    }
  }
}