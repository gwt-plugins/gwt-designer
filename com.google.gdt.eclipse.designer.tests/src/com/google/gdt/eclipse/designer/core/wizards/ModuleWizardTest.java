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
import com.google.gdt.eclipse.designer.wizards.model.module.ModuleWizard;
import com.google.gdt.eclipse.designer.wizards.model.project.ProjectWizard;

import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link ModuleWizard}.
 * 
 * @author scheglov_ke
 */
public class ModuleWizardTest extends AbstractWizardTest {
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
  private static interface ModuleContents {
    String getWebFolderName();

    String[] getWebXmlContentExpected();

    String[] getGwtXmlContentExpected();
  }

  private static void assertModule_20(ModuleContents contents) throws Exception {
    assertTrue(getFolder0("src/com/mycompany/myapp").exists());
    assertTrue(getFolder0("src/com/mycompany/myapp/client").exists());
    assertTrue(getFolder0("src/com/mycompany/myapp/server").exists());
    assertFalse(getFolder0("src/com/mycompany/myapp/public").exists());
    {
      String module = getFileContent("src/com/mycompany/myapp/MyApp.gwt.xml");
      module = StringUtils.replace(module, "\r\n", "\n");
      assertEquals(getSourceDQ(contents.getGwtXmlContentExpected()), module);
    }
    assertTrue(getFile("src/com/mycompany/myapp/client/MyApp.java").exists());
    assertTrue(getFile(contents.getWebFolderName() + "/MyApp.css").exists());
    assertTrue(getFile(contents.getWebFolderName() + "/MyApp.html").exists());
    // WEB-INF/web.xml
    {
      IFile webXml = getFile(contents.getWebFolderName() + "/WEB-INF/web.xml");
      assertTrue(webXml.exists());
      String webXmlContent = StringUtils.replace(getFileContent(webXml), "\r\n", "\n");
      assertEquals(getSourceDQ(contents.getWebXmlContentExpected()), webXmlContent);
    }
  }

  private static class Module20Contents implements ModuleContents {
    @Override
    public String getWebFolderName() {
      return "war";
    }

    @Override
    public String[] getWebXmlContentExpected() {
      return new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<!DOCTYPE web-app",
          "  PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN'",
          "  'http://java.sun.com/dtd/web-app_2_3.dtd'>",
          "",
          "<web-app>",
          "",
          "  <!-- Default page to serve -->",
          "  <welcome-file-list>",
          "    <welcome-file>MyApp.html</welcome-file>",
          "  </welcome-file-list>",
          "",
          "</web-app>"};
    }

    @Override
    public String[] getGwtXmlContentExpected() {
      return new String[]{
          "<module>",
          "  <inherits name='com.google.gwt.user.User'/>",
          "  <inherits name='com.google.gwt.user.theme.standard.Standard'/>",
          "  <entry-point class='com.mycompany.myapp.client.MyApp'/>",
          "</module>"};
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 2.0
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openWizard_20() throws Exception {
    configureGWTProject(GTestUtils.getLocation_20());
    animateModuleWizard();
    assertModule_20();
  }

  public void test_openWizard_20_exist_web_xml() throws Exception {
    configureGWTProject(GTestUtils.getLocation_20());
    IFile webXml = getFile("war/WEB-INF/web.xml");
    setFileContent(
        webXml,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<!DOCTYPE web-app\r\nPUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\r\n\"http://java.sun.com/dtd/web-app_2_3.dtd\">\r\n<web-app>\r\n<!-- test -->\r\n</web-app>\r\n");
    animateModuleWizard();
    assertModule_20(new Module20Contents() {
      @Override
      public String[] getWebXmlContentExpected() {
        return new String[]{
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<!DOCTYPE web-app",
            "PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN'",
            "'http://java.sun.com/dtd/web-app_2_3.dtd'>",
            "<web-app>",
            "<!-- test -->",
            "</web-app>"};
      }
    });
  }

  /**
   * It was requested to make "web" folder name configurable.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41284
   */
  public void test_openWizard_20_differentWebFolder_inPreferences() throws Exception {
    Activator.getStore().setValue(Constants.P_WEB_FOLDER, "myWar");
    configureGWTProject(GTestUtils.getLocation_20());
    animateModuleWizard();
    assertModule_20(new Module20Contents() {
      @Override
      public String getWebFolderName() {
        return "myWar";
      }
    });
  }

  public static void assertModule_20() throws Exception {
    assertModule_20(new Module20Contents());
    {
      String html = getFileContent("war/MyApp.html");
      assertThat(html).startsWith("<!doctype html>");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 2.1
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_openWizard_21() throws Exception {
    configureGWTProject(GTestUtils.getLocation_21());
    animateModuleWizard();
    assertModule_20();
  }

  public void test_openWizard_MVP() throws Exception {
    configureGWTProject(GTestUtils.getLocation_21());
    animateModuleWizard(true);
    assertModule_20(new Module20Contents() {
      @Override
      public String[] getGwtXmlContentExpected() {
        return new String[]{
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='com.google.gwt.user.theme.standard.Standard'/>",
            "  <inherits name='com.google.gwt.place.Place'/>",
            "  <inherits name='com.google.gwt.activity.Activity'/>",
            "  <entry-point class='com.mycompany.myapp.client.MyApp'/>",
            "  <replace-with class='com.mycompany.myapp.client.ClientFactoryImpl'>",
            "    <when-type-is class='com.mycompany.myapp.client.ClientFactory'/>",
            "  </replace-with>",
            "</module>"};
      }
    });
    {
      String html = getFileContent("war/MyApp.html");
      assertThat(html).startsWith("<!doctype html>");
    }
    {
      // ClientFactory
      assertTrue(getFile("src/com/mycompany/myapp/client/ClientFactory.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/ClientFactoryImpl.java").exists());
      // View
      assertTrue(getFile("src/com/mycompany/myapp/client/ui/SampleView.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/ui/SampleViewImpl.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/ui/SampleViewImpl.ui.xml").exists());
      // MVP
      assertTrue(getFile("src/com/mycompany/myapp/client/activity/SampleActivity.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/place/SamplePlace.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/mvp/AppActivityMapper.java").exists());
      assertTrue(getFile("src/com/mycompany/myapp/client/mvp/AppPlaceHistoryMapper.java").exists());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureGWTProject(String gwtLocation) throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, gwtLocation);
    ProjectWizard.configureProjectAsGWTProject(m_javaProject);
  }

  private void animateModuleWizard() throws Exception {
    animateModuleWizard(false);
  }

  private void animateModuleWizard(final boolean useMvpFramework) throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new ModuleWizard(), new StructuredSelection(m_javaProject));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New GWT Module");
        context.getTextByLabel("&Module name:").setText("MyApp");
        context.getTextByLabel("&Package name:").setText("com.mycompany.myapp");
        if (useMvpFramework) {
          context.selectButton("Create EntryPoint and public resources", true);
          context.selectButton("Use MVP framework", true);
        }
        context.clickButton("&Finish");
      }
    });
  }
}