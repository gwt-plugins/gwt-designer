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
package com.google.gdt.eclipse.designer.core.wizards;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.wizards.model.service.CreateServiceOperation;
import com.google.gdt.eclipse.designer.wizards.model.service.ServiceWizard;

import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.jdt.core.IPackageFragment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link ServiceWizard} and {@link CreateServiceOperation}.
 * 
 * @author scheglov_ke
 */
public class ServiceWizardTest extends AbstractWizardTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    TestUtils.closeAllEditors();
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
  public void test_for20() throws Exception {
    configureProject_createModule_20();
    // set module file
    String moduleXML = "<module rename-to='myModule'/>";
    setFileContentSrc("test/Module.gwt.xml", moduleXML);
    // do create
    IPackageFragment packageFragment = m_testProject.getPackage("test.client");
    new CreateServiceOperation().create(packageFragment, "MyService");
    waitForAutoBuild();
    waitForAutoBuild();
    // validate
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // check web.xml
    {
      String web = getFileContent("war/WEB-INF/web.xml");
      assertThat(web).contains("<servlet-name>MyService</servlet-name>");
      assertThat(web).contains("<servlet-class>test.server.MyServiceImpl</servlet-class>");
      assertThat(web).contains("<url-pattern>/myModule/MyService</url-pattern>");
    }
    // no service in Module.gwt.xml
    {
      String module = getFileContentSrc("test/Module.gwt.xml");
      assertThat(module).isEqualTo(moduleXML);
    }
  }

  /**
   * It was requested to make "web" folder name configurable.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41284
   */
  public void test_for20_differentWebFolder_inPreferences() throws Exception {
    Activator.getStore().setValue(Constants.P_WEB_FOLDER, "myWar");
    configureProject_createModule_20();
    // set module file
    String moduleXML = "<module rename-to='myModule'/>";
    setFileContentSrc("test/Module.gwt.xml", moduleXML);
    // do create
    IPackageFragment packageFragment = m_testProject.getPackage("test.client");
    new CreateServiceOperation().create(packageFragment, "MyService");
    waitForAutoBuild();
    // validate
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // check web.xml
    {
      String web = getFileContent("myWar/WEB-INF/web.xml");
      assertThat(web).contains("<servlet-name>MyService</servlet-name>");
      assertThat(web).contains("<servlet-class>test.server.MyServiceImpl</servlet-class>");
      assertThat(web).contains("<url-pattern>/myModule/MyService</url-pattern>");
    }
    // no service in Module.gwt.xml
    {
      String module = getFileContentSrc("test/Module.gwt.xml");
      assertThat(module).isEqualTo(moduleXML);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureProject_createModule_20() throws Exception {
    configureProject_createModule(GTestUtils.getLocation_20());
  }

  private void configureProject_createModule(String gwtLocation) throws Exception {
    GTestUtils.configure(gwtLocation, m_testProject);
    GTestUtils.createModule(m_testProject, "test.Module");
    waitForAutoBuild();
  }
}