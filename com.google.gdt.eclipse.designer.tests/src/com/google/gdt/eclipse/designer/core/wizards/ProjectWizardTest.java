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
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.project.ProjectWizard;

import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.ResourcesPlugin;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link ProjectWizard}.
 * 
 * @author scheglov_ke
 */
public class ProjectWizardTest extends AbstractWizardTest {
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
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_for20() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation_20());
    ProjectWizard.configureProjectAsGWTProject(m_javaProject);
    assertTrue(Utils.isGWTProject(m_javaProject));
    {
      String classPath = getFileContent(".classpath");
      classPath = StringUtils.replace(classPath, "\r\n", "\n");
      assertEquals(getSourceDQ(new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<classpath>",
          "  <classpathentry kind='con' path='org.eclipse.jdt.launching.JRE_CONTAINER'/>",
          "  <classpathentry kind='src' path='src'/>",
          "  <classpathentry kind='var' path='GWT_HOME/gwt-user.jar'/>",
          "  <classpathentry kind='output' path='war/WEB-INF/classes'/>",
          "</classpath>"}), classPath);
    }
    assertTrue(getFolder0("war").exists());
    assertTrue(getFolder0("war/WEB-INF").exists());
    assertTrue(getFolder0("war/WEB-INF/classes").exists());
    assertTrue(getFolder0("war/WEB-INF/lib").exists());
    assertTrue(getFile("war/WEB-INF/lib/gwt-servlet.jar").exists());
  }

  /**
   * If GWT project was created using GPE, then it has already "war" folder, so we should just add
   * GWTD nature and builder.
   */
  public void test_for20_withExistingWarFolder() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation_20());
    // create "war" folder
    getFolder("war");
    // configure for GWTD
    ProjectWizard.configureProjectAsGWTProject(m_javaProject);
    assertTrue(Utils.isGWTProject(m_javaProject));
    // in reality this is not GPE project, so don't check other things
  }

  /**
   * It was requested to make "web" folder name configurable.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41284
   */
  public void test_for20_differentWebFolder_inPreferences() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation_20());
    Activator.getStore().setValue(Constants.P_WEB_FOLDER, "myWar");
    ProjectWizard.configureProjectAsGWTProject(m_javaProject);
    assertTrue(Utils.isGWTProject(m_javaProject));
    {
      String classPath = getFileContent(".classpath");
      classPath = StringUtils.replace(classPath, "\r\n", "\n");
      assertEquals(getSourceDQ(new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<classpath>",
          "  <classpathentry kind='con' path='org.eclipse.jdt.launching.JRE_CONTAINER'/>",
          "  <classpathentry kind='src' path='src'/>",
          "  <classpathentry kind='var' path='GWT_HOME/gwt-user.jar'/>",
          "  <classpathentry kind='output' path='myWar/WEB-INF/classes'/>",
          "</classpath>"}), classPath);
    }
    assertFalse(getFolder0("war").exists());
    assertTrue(getFolder0("myWar").exists());
    assertTrue(getFolder0("myWar/WEB-INF").exists());
    assertTrue(getFolder0("myWar/WEB-INF/classes").exists());
    assertTrue(getFolder0("myWar/WEB-INF/lib").exists());
    assertTrue(getFile("myWar/WEB-INF/lib/gwt-servlet.jar").exists());
  }

  public void test_for21() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation_21());
    ProjectWizard.configureProjectAsGWTProject(m_javaProject);
    assertTrue(Utils.isGWTProject(m_javaProject));
    {
      String classPath = getFileContent(".classpath");
      classPath = StringUtils.replace(classPath, "\r\n", "\n");
      assertEquals(getSourceDQ(new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<classpath>",
          "  <classpathentry kind='con' path='org.eclipse.jdt.launching.JRE_CONTAINER'/>",
          "  <classpathentry kind='src' path='src'/>",
          "  <classpathentry kind='var' path='GWT_HOME/gwt-user.jar'/>",
          "  <classpathentry kind='output' path='war/WEB-INF/classes'/>",
          "</classpath>"}), classPath);
    }
    assertTrue(getFolder0("war").exists());
    assertTrue(getFolder0("war/WEB-INF").exists());
    assertTrue(getFolder0("war/WEB-INF/classes").exists());
    assertTrue(getFolder0("war/WEB-INF/lib").exists());
    assertTrue(getFile("war/WEB-INF/lib/gwt-servlet.jar").exists());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openWizard_20() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation_20());
    animateProjectWizard();
    try {
      ModuleWizardTest.assertModule_20();
    } finally {
      m_project.delete(true, null);
    }
  }

  private static void animateProjectWizard() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new ProjectWizard(), null);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New GWT Java Project");
        // configure project
        context.getTextByLabel("&Project name:").setText("GWT-test");
        context.clickButton("&Next >");
        // ask creating module
        context.selectButton("Create GWT module", true);
        context.getTextByLabel("&Module name:").setText("MyApp");
        context.getTextByLabel("&Package name:").setText("com.mycompany.myapp");
        // finish
        context.clickButton("&Finish");
      }
    });
    //
    m_project = ResourcesPlugin.getWorkspace().getRoot().getProject("GWT-test");
    assertTrue(m_project.exists());
  }
}