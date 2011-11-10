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
package com.google.gdt.eclipse.designer.core.util;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.util.resources.DefaultResourcesProvider;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Test for {@link DefaultResourcesProvider}.
 * 
 * @author scheglov_ke
 */
public class DefaultResourcesProviderTest extends AbstractJavaTest {
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
      waitForAutoBuild();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    waitForAutoBuild();
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
  /**
   * Test that {@link DefaultResourcesProvider#dispose()} closes JAR files.
   */
  public void test_dispose() throws Exception {
    // add "test.jar" into project classpath
    addTestJar();
    // create and right away dispose DefaultResourcesProvider
    {
      IResourcesProvider provider = new DefaultResourcesProvider(m_javaProject);
      provider.dispose();
    }
    // dispose project, so delete create "test.jar", if DefaultResourcesProvider is not disposed,
    // it will keep "test.jar" opened, so we will not able to delete it
    do_projectDispose();
  }

  /**
   * Test for {@link DefaultResourcesProvider#getResourceAsStream(String)}.
   */
  @DisposeProjectAfter
  public void test_getResourceAsStream() throws Exception {
    addTestJar();
    // prepare IResourcesProvider
    IResourcesProvider provider = getTestModuleResourceProvider();
    // work with IResourcesProvider
    try {
      // no such resource
      assertNotExistingResource(provider, "no/such/resource.txt");
      // "src" folder is included
      assertExistingResource(provider, "test/client/Module.java");
      assertExistingResource(provider, "/test/client/Module.java");
      // from "test.jar"
      assertExistingResource(provider, "jar/folder/hello.txt");
      // "bin" folder is NOT included
      assertNotExistingResource(provider, "test/client/Module.class");
    } finally {
      provider.dispose();
    }
  }

  /**
   * Test for {@link DefaultResourcesProvider#listFiles(String)}.
   */
  public void test_listFiles_1() throws Exception {
    IResourcesProvider provider = getTestModuleResourceProvider();
    // work with IResourcesProvider
    try {
      // from project
      {
        List<String> files = provider.listFiles("test");
        assertThat(files).contains("Module.gwt.xml", "client/Module.java");
        assertFalse(files.contains("client/Module.class"));
      }
      // from jar
      {
        List<String> files = provider.listFiles("com/google/gwt/xml");
        assertThat(files).contains("XML.gwt.xml", "client/");
      }
    } finally {
      provider.dispose();
    }
  }

  /**
   * Test for {@link DefaultResourcesProvider#listFiles(String)}. <br>
   * Referenced/required projects.
   */
  @DisposeProjectAfter
  public void test_listFiles_2() throws Exception {
    TestProject myProject = new TestProject("myProject");
    try {
      GTestUtils.configure(myProject);
      GTestUtils.createModule(myProject, "my.library.Library");
      m_testProject.addRequiredProject(myProject);
      // prepare IResourcesProvider
      IResourcesProvider provider = getTestModuleResourceProvider();
      // work with IResourcesProvider
      try {
        // from "my.library.Library" project
        {
          List<String> files = provider.listFiles("my");
          assertThat(files).doesNotHaveDuplicates().contains(
              "library/Library.gwt.xml",
              "library/client/Library.java");
          assertFalse(files.contains("library/client/Library.class"));
        }
      } finally {
        provider.dispose();
      }
    } finally {
      myProject.dispose();
    }
  }

  /**
   * Test for {@link DefaultResourcesProvider#listFiles(String)}.
   * <p>
   * We should ignore invalid output locations.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43514
   */
  @DisposeProjectAfter
  public void test_listFiles_invalidOutputFolder() throws Exception {
    m_javaProject.setOutputLocation(new Path("/TestProject/bin2"), null);
    // prepare IResourcesProvider
    IResourcesProvider provider = getTestModuleResourceProvider();
    // work with IResourcesProvider
    try {
      List<String> files = provider.listFiles("test");
      assertThat(files).contains("Module.gwt.xml", "client/Module.java");
      assertFalse(files.contains("client/Module.class"));
    } finally {
      provider.dispose();
    }
  }

  /**
   * Some (not so smart) users try to include <code>*.properties</code> file into classpath.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42828
   */
  @DisposeProjectAfter
  public void test_propertiesFileInClasspath() throws Exception {
    File tempFile = File.createTempFile("myFile", ".properties");
    try {
      ProjectUtils.addJar(m_javaProject, tempFile.getAbsolutePath(), null);
      // create IResourcesProvider, should ignore *.properties file in classpath
      IResourcesProvider provider = new DefaultResourcesProvider(m_javaProject);
      provider.dispose();
    } finally {
      tempFile.delete();
    }
  }

  /**
   * Test for {@link DefaultResourcesProvider#listFiles(String)}.
   * <p>
   * Reference not existing {@link IProject}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44871
   */
  @DisposeProjectAfter
  public void test_listFiles_notExistingProject() throws Exception {
    TestProject myProject = new TestProject("myProject");
    try {
      m_testProject.addRequiredProject(myProject);
      myProject.dispose();
      // prepare IResourcesProvider
      IResourcesProvider provider = getTestModuleResourceProvider();
      // work with IResourcesProvider
      try {
        List<String> files = provider.listFiles("test");
        assertThat(files).contains("Module.gwt.xml", "client/Module.java");
      } finally {
        provider.dispose();
      }
    } finally {
      myProject.dispose();
    }
  }

  /**
   * Test for {@link DefaultResourcesProvider#listFiles(String)}.
   * <p>
   * Reference existing {@link IProject}, but now {@link IJavaProject}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?47084
   */
  @DisposeProjectAfter
  public void test_listFiles_notJavaProject() throws Exception {
    TestProject myProject = new TestProject("myProject");
    try {
      m_testProject.addRequiredProject(myProject);
      ProjectUtils.removeNature(myProject.getProject(), JavaCore.NATURE_ID);
      // prepare IResourcesProvider
      IResourcesProvider provider = getTestModuleResourceProvider();
      // work with IResourcesProvider
      try {
        List<String> files = provider.listFiles("test");
        assertThat(files).contains("Module.gwt.xml", "client/Module.java");
      } finally {
        provider.dispose();
      }
    } finally {
      myProject.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Asserts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that {@link IResourcesProvider} does not contain resource with given path.
   */
  private static void assertNotExistingResource(IResourcesProvider provider, String path)
      throws Exception {
    InputStream stream = provider.getResourceAsStream(path);
    try {
      assertNull(stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Asserts that {@link IResourcesProvider} contains resource with given path.
   */
  private static void assertExistingResource(IResourcesProvider provider, String path)
      throws Exception {
    InputStream stream = provider.getResourceAsStream(path);
    assertNotNull(stream);
    stream.close();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds "test.jar" with single <code>"jar/folder/hello.txt"</code> file.
   */
  private void addTestJar() throws Exception {
    String jarLocation = getFile("test.jar").getLocation().toPortableString();
    // create "test.jar"
    {
      JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarLocation));
      jarOutputStream.putNextEntry(new ZipEntry("jar/folder/hello.txt"));
      jarOutputStream.write("Hello!".getBytes());
      jarOutputStream.closeEntry();
      jarOutputStream.close();
    }
    // add "test.jar" into classpath
    m_javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    ProjectUtils.addExternalJar(m_javaProject, jarLocation, null);
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IResourcesProvider} for standard test module.
   */
  private static IResourcesProvider getTestModuleResourceProvider() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
    return new DefaultResourcesProvider(moduleDescription);
  }
}
