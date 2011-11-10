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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleDescription;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.IModuleFilter;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.Version;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Test for {@link Utils}.
 * 
 * @author scheglov_ke
 */
public class UtilsTest extends AbstractJavaTest {
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
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation());
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
  // Environment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#hasGPE()}.
   */
  public void test_hasGPE() throws Exception {
    // to use UiBinder we run tests with GPE
    assertTrue(Utils.hasGPE());
  }

  /**
   * Test for {@link Utils#hasGPE()}.
   */
  public void test_hasGPE_falseInTesting() throws Exception {
    System.setProperty("wbp.noGPE", "true");
    try {
      assertFalse(Utils.hasGPE());
    } finally {
      System.clearProperty("wbp.noGPE");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Libraries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getGWTLocation(IProject)}.<br>
   * Absolute path to the <code>gwt-user.jar</code> in classpath.
   */
  public void test_getGWTLocation_fromAbsolute() throws Exception {
    assertEquals(GTestUtils.getLocation(), Utils.getGWTLocation(m_project));
    assertEquals(
        GTestUtils.getLocation() + "/gwt-user.jar",
        Utils.getUserLibPath(m_project).toPortableString());
  }

  /**
   * Test for {@link Utils#getGWTLocation(IProject)}.<br>
   * <code>null</code> as {@link IProject}, so default GWT location used.
   */
  public void test_getGWTLocation_nullProject() throws Exception {
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, "/some/folder");
    assertEquals("/some/folder", Utils.getGWTLocation(null));
    assertEquals("/some/folder/gwt-user.jar", Utils.getUserLibPath(null).toPortableString());
  }

  /**
   * Test for {@link Utils#getGWTLocation(IProject)}.<br>
   * Use <code>GWT_HOME</code> variable in classpath.
   */
  @DisposeProjectAfter
  public void test_getGWTLocation_from_GWT_HOME() throws Exception {
    // recreate project, so it will not use any gwt-user.jar at all
    do_projectDispose();
    do_projectCreate();
    // use GWT_HOME variable
    {
      IJavaProject javaProject = m_testProject.getJavaProject();
      IClasspathEntry entry =
          JavaCore.newVariableEntry(new Path("GWT_HOME/gwt-user.jar"), null, null);
      ProjectUtils.addClasspathEntry(javaProject, entry);
    }
    // do check
    String location_20 = GTestUtils.getLocation_20();
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, location_20);
    assertEquals(location_20, Utils.getGWTLocation(m_project));
    assertEquals(location_20 + "/gwt-user.jar", Utils.getUserLibPath(m_project).toPortableString());
  }

  /**
   * Test for {@link Utils#getGWTLocation(IProject)}.<br>
   * <code>gwt-user.jar</code> has different name.
   */
  @DisposeProjectAfter
  public void test_getGWTLocation_otherUserName() throws Exception {
    // recreate project, so it will not use any gwt-user.jar at all
    do_projectDispose();
    do_projectCreate();
    // prepare File with gwt-user.jar content, but random name
    File gwtUserFile;
    {
      gwtUserFile = File.createTempFile("gwtUser_", ".jar").getCanonicalFile();
      gwtUserFile.deleteOnExit();
      FileUtils.copyFile(new File(GTestUtils.getLocation() + "/gwt-user.jar"), gwtUserFile);
    }
    // prepare File with gwt-dev-windows.jar content, but random name
    File gwtDevFile;
    {
      gwtDevFile = new File(gwtUserFile.getParentFile(), "gwt-dev.jar");
      gwtDevFile.deleteOnExit();
      FileUtils.copyFile(new File(GTestUtils.getLocation() + "/gwt-dev.jar"), gwtDevFile);
    }
    // use temporary File in classpath
    {
      IJavaProject javaProject = m_testProject.getJavaProject();
      IClasspathEntry entry =
          JavaCore.newLibraryEntry(new Path(gwtUserFile.getAbsolutePath()), null, null);
      ProjectUtils.addClasspathEntry(javaProject, entry);
    }
    // do check
    {
      String expected = new Path(gwtUserFile.getParent()).toPortableString();
      assertEquals(expected, Utils.getGWTLocation(m_project));
    }
    {
      String expected = new Path(gwtUserFile.getAbsolutePath()).toPortableString();
      assertEquals(expected, Utils.getUserLibPath(m_project).toPortableString());
    }
    // check getDevLibPath()
    {
      // we have gwt-dev-windows.jar in same folder as gwt-user.jar
      {
        String expected = new Path(gwtDevFile.getAbsolutePath()).toPortableString();
        assertEquals(expected, Utils.getDevLibPath(m_project).toPortableString());
      }
      // remove gwt-dev-windows.jar from folder of gwt-user.jar, so dev jar from default GWT used
      {
        gwtDevFile.delete();
        String expected = GTestUtils.getLocation() + "/gwt-dev.jar";
        assertEquals(expected, Utils.getDevLibPath(m_project).toPortableString());
      }
    }
  }

  /**
   * Test for {@link Utils#getGWTLocation(IProject)}.<br>
   * Not a GWT project.
   */
  @DisposeProjectAfter
  public void test_getGWTLocation_notGWT() throws Exception {
    // recreate project, so it will not use any gwt-user.jar at all
    do_projectDispose();
    do_projectCreate();
    // do check
    assertNull(Utils.getGWTLocation(m_project));
    assertNull(Utils.getUserLibPath(m_project));
  }

  /**
   * Test for {@link Utils#getUserLibPath(IProject)}.
   */
  public void test_getUserLibPath() throws Exception {
    assertEquals(
        GTestUtils.getLocation() + "/gwt-user.jar",
        Utils.getUserLibPath(m_project).toPortableString());
  }

  /**
   * Test for {@link Utils#getDevLibPath(IProject)}. After GWT 2.0 version.
   */
  @DisposeProjectAfter
  public void test_getDevLibPath_after20() throws Exception {
    do_projectDispose();
    do_projectCreate();
    GTestUtils.configure(GTestUtils.getLocation_21(), m_testProject);
    assertEquals(
        GTestUtils.getLocation_21() + "/gwt-dev.jar",
        Utils.getDevLibPath(m_project).toPortableString());
  }

  /**
   * Test for {@link Utils#getDevLibPath(IProject)}. For Maven.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48259
   */
  @DisposeProjectAfter
  public void test_getDevLibPath_maven() throws Exception {
    do_projectDispose();
    do_projectCreate();
    // prepare Maven-like structure
    String gwtLocation = GTestUtils.getLocation_22();
    String gwtUserDir = getFolder("libs/gwt/gwt-user/2.2.0").getLocation().toPortableString();
    String gwtDevDir = getFolder("libs/gwt/gwt-dev/2.2.0").getLocation().toPortableString();
    String userLocation = gwtUserDir + "/gwt-user-2.2.0.jar";
    String devLocation = gwtDevDir + "/gwt-dev-2.2.0.jar";
    FileUtils.copyFile(new File(gwtLocation, "gwt-user.jar"), new File(userLocation), false);
    FileUtils.copyFile(new File(gwtLocation, "gwt-dev.jar"), new File(devLocation), false);
    ProjectUtils.addExternalJar(m_javaProject, userLocation, null);
    m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
    // find gwt-dev.jar relative to gwt-user.jar
    assertEquals(devLocation, Utils.getDevLibPath(m_project).toPortableString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVersion()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getDefaultVersion()}.
   */
  public void test_getDefaultVersion() throws Exception {
    check_getDefaultVersion(GTestUtils.getLocation_20(), Utils.GWT_2_0);
    check_getDefaultVersion(GTestUtils.getLocation_2_1_0(), Utils.GWT_2_1);
    check_getDefaultVersion(GTestUtils.getLocation_21(), Utils.GWT_2_1_1);
    check_getDefaultVersion(GTestUtils.getLocation_22(), Utils.GWT_2_2);
    // no version
    check_getDefaultVersion("", Utils.GWT_2_2);
  }

  /**
   * Checks {@link Utils#getDefaultVersion()}.
   */
  private static void check_getDefaultVersion(String gwtLocation, Version expected)
      throws Exception {
    String oldLocation = Activator.getGWTLocation();
    try {
      Activator.setGWTLocation(gwtLocation);
      Version actual = Utils.getDefaultVersion();
      assertEquals(expected, actual);
    } finally {
      Activator.setGWTLocation(oldLocation);
    }
  }

  /**
   * Test for {@link Utils#getVersion(IJavaProject)}.
   */
  @DisposeProjectAfter
  public void test_getVersion() throws Exception {
    check_getVersion(GTestUtils.getLocation_20(), Utils.GWT_2_0);
    check_getVersion(GTestUtils.getLocation_2_1_0(), Utils.GWT_2_1);
    check_getVersion(GTestUtils.getLocation_21(), Utils.GWT_2_1_1);
    check_getVersion(GTestUtils.getLocation_22(), Utils.GWT_2_2);
    // no version
    check_getVersion("", Utils.GWT_2_2);
  }

  /**
   * Checks {@link Utils#getVersion(IJavaProject)} and {@link Utils#getVersion(IProject)}.
   */
  private void check_getVersion(String gwtLocation, Version expected) throws Exception {
    try {
      do_projectDispose();
      do_projectCreate();
      GTestUtils.configure(gwtLocation, m_testProject);
      // use IJavaProject
      {
        Version actual = Utils.getVersion(m_javaProject);
        assertEquals(expected, actual);
      }
      // use IProject
      {
        Version actual = Utils.getVersion(m_project);
        assertEquals(expected, actual);
      }
    } finally {
      do_projectDispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getExactModule(Object)}.
   */
  public void test_getExactModule() throws Exception {
    {
      IFile file = getFile(".project");
      assertNull(Utils.getExactModule(file));
    }
    {
      IFile file = getFileSrc("/test/Module.gwt.xml");
      assertNotNull(Utils.getExactModule(file));
    }
  }

  /**
   * Test for {@link Utils#getSimpleModuleName(IFile)}.
   */
  public void test_getSimpleModuleName() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertEquals("Module", module.getSimpleName());
  }

  /**
   * Test for {@link Utils#getModuleId(IFile)}.<br>
   * Module file in package.
   */
  public void test_getModuleId_1() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertEquals("test.Module", module.getId());
  }

  /**
   * Test for {@link Utils#getModuleId(IFile)}.<br>
   * Module file in root of source folder.
   */
  public void test_getModuleId_2() throws Exception {
    IFile file = setFileContentSrc("TopLevel.gwt.xml", "");
    ModuleDescription module = new DefaultModuleDescription(file);
    assertEquals("TopLevel", module.getId());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Searching module files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getModule(IJavaProject, String)}.
   */
  public void test_getModule() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    assertModuleDescriptionPath(
        "src/test/Module.gwt.xml",
        Utils.getModule(javaProject, "test.Module"));
    assertNull(Utils.getModule(javaProject, "no.such.Module"));
  }

  /**
   * Test for {@link Utils#getModules(IJavaProject)}.<br>
   * Single default module.
   */
  public void test_getModules_inProject_1() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    List<ModuleDescription> modules = Utils.getModules(javaProject);
    assertThat(modules).hasSize(1);
    assertModuleDescriptionPath("src/test/Module.gwt.xml", modules.get(0));
  }

  /**
   * Test for {@link Utils#getModules(IJavaProject)}.<br>
   * Default module + new module.
   */
  @DisposeProjectAfter
  public void test_getModules_inProject_2() throws Exception {
    GTestUtils.createModule(m_testProject, "second.MyModule");
    // ask for module files
    IJavaProject javaProject = m_testProject.getJavaProject();
    List<ModuleDescription> moduleFiles = Utils.getModules(javaProject);
    assertThat(moduleFiles).hasSize(2);
    // files
    assertModuleDescriptionPath("src/second/MyModule.gwt.xml", moduleFiles.get(0));
    assertModuleDescriptionPath("src/test/Module.gwt.xml", moduleFiles.get(1));
  }

  /**
   * Test for {@link Utils#getModuleFiles(IFolder, boolean)}.
   */
  /*public void test_getModuleFiles_inFolder() throws Exception {
    // "src" itself has no modules
    {
      List<IFile> moduleFiles = Utils.getModuleFiles(m_project.getFolder("src"), false);
      assertThat(moduleFiles).isEmpty();
    }
    // "src" has module in children
    {
      List<IFile> moduleFiles = Utils.getModuleFiles(m_project.getFolder("src"), true);
      assertThat(moduleFiles).hasSize(1);
      assertResourcePath("src/test/Module.gwt.xml", moduleFiles.get(0));
    }
  }*/
  /**
   * Test for {@link Utils#getModuleFilesUp(IFolder)}.
   */
  /*public void test_getModuleFilesUp() throws Exception {
    // "src" has no module
    {
      List<IFile> moduleFiles = Utils.getModuleFilesUp(m_project.getFolder("src"));
      assertThat(moduleFiles).isEmpty();
    }
    // "test" package has module
    {
      IPackageFragment pkg = m_testProject.getPackage("test");
      List<IFile> moduleFiles = Utils.getModuleFilesUp((IFolder) pkg.getUnderlyingResource());
      assertThat(moduleFiles).hasSize(1);
      assertResourcePath("src/test/Module.gwt.xml", moduleFiles.get(0));
    }
    // "test.client" package has module
    {
      IPackageFragment pkg = m_testProject.getPackage("test.client");
      List<IFile> moduleFiles = Utils.getModuleFilesUp((IFolder) pkg.getUnderlyingResource());
      assertThat(moduleFiles).hasSize(1);
      assertResourcePath("src/test/Module.gwt.xml", moduleFiles.get(0));
    }
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // "inherit" checks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#inheritsModule(IFile, String)}.
   */
  @DisposeProjectAfter
  public void test_inheritsModule() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    // no "second" initially
    assertFalse(Utils.inheritsModule(module, "second.MyModule"));
    // create "second", still not inherited
    GTestUtils.createModule(m_testProject, "second.MyModule");
    assertFalse(Utils.inheritsModule(module, "second.MyModule"));
    // add elements into module
    DefaultModuleProvider.modify(module, new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        moduleElement.addInheritsElement("second.MyModule");
      }
    });
    // yes, inherits
    assertTrue(Utils.inheritsModule(module, "second.MyModule"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Single module file access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getSingleModule(IResource)}.
   */
  @DisposeProjectAfter
  public void test_getSingleModule_IResource() throws Exception {
    // NO
    {
      // .project is not element of GWT module
      {
        IResource resource = getFile(".project");
        assertNull(Utils.getSingleModule(resource));
      }
      // IProject is not element of GWT module
      {
        IResource resource = m_project;
        assertNull(Utils.getSingleModule(resource));
      }
      // some IFolder not related with Java package
      {
        IFolder folder = ensureFolderExists("someFolder");
        assertNull(Utils.getSingleModule(folder));
      }
      // some Java file outside of GWT module
      {
        IResource resource =
            setFileContentSrc(
                "test2/Test.java",
                getSourceDQ(
                    "// filler filler filler filler filler",
                    "// filler filler filler filler filler",
                    "package test2;",
                    "public class Test {",
                    "}"));
        assertNull(Utils.getSingleModule(resource));
      }
    }
    // YES
    {
      // use Module.gwt.xml as IResource
      {
        IResource resource = getFileSrc("test/Module.gwt.xml");
        assertModuleDescriptionPath("src/test/Module.gwt.xml", Utils.getSingleModule(resource));
      }
      // use "client" folder as IResource
      {
        IResource resource = getFolderSrc("test/client");
        assertModuleDescriptionPath("src/test/Module.gwt.xml", Utils.getSingleModule(resource));
      }
    }
  }

  /**
   * Test for {@link Utils#getSingleModule(ICompilationUnit)}.
   */
  public void test_getSingleModule_ICompilationUnit() throws Exception {
    IType entryPointType = m_testProject.getJavaProject().findType("test.client.Module");
    ICompilationUnit compilationUnit = entryPointType.getCompilationUnit();
    assertModuleDescriptionPath("src/test/Module.gwt.xml", Utils.getSingleModule(compilationUnit));
  }

  /**
   * Test for {@link Utils#getSingleModule(IPackageFragment)}.
   */
  public void test_getSingleModule_IPackageFragment() throws Exception {
    IType entryPointType = m_testProject.getJavaProject().findType("test.client.Module");
    IPackageFragment packageFragment = entryPointType.getPackageFragment();
    assertModuleDescriptionPath("src/test/Module.gwt.xml", Utils.getSingleModule(packageFragment));
  }

  /**
   * Test for {@link Utils#getSingleModule(IType)}.
   */
  public void test_getSingleModule_IType() throws Exception {
    IType entryPointType = m_testProject.getJavaProject().findType("test.client.Module");
    assertModuleDescriptionPath("src/test/Module.gwt.xml", Utils.getSingleModule(entryPointType));
  }

  /**
   * Test for {@link Utils#getSingleModule(ICompilationUnit)}.
   * <p>
   * Uses <code>gwtd.module.use</code> marker to force using marked module.
   */
  @DisposeProjectAfter
  public void test_getSingleModule_useMarker() throws Exception {
    getTestModuleFile().delete(true, null);
    setFileContentSrc("test/aModule.gwt.xml", "<module/>");
    setFileContentSrc("test/bModule.gwt.xml", "<module/> <!-- gwtd.module.use -->");
    setFileContentSrc("test/cModule.gwt.xml", "<module/>");
    // do check
    IFolder folder = getFolderSrc("test");
    ModuleDescription module = Utils.getSingleModule(folder);
    assertEquals("test.bModule", module.getId());
  }

  /**
   * Test for {@link Utils#getSingleModule(ICompilationUnit)}.
   * <p>
   * Uses {@link IModuleFilter} to keep "second" module alive.
   */
  @DisposeProjectAfter
  public void test_getSingleModule_IModuleFilter() throws Exception {
    getTestModuleFile().delete(true, null);
    setFileContentSrc("test/aModule.gwt.xml", "<module/>");
    setFileContentSrc("test/bModule.gwt.xml", "<module/>");
    // install and use IModuleFilter
    String extPointId = "com.google.gdt.eclipse.designer.moduleProviders";
    try {
      TestUtils.setContributionBundle(com.google.gdt.eclipse.designer.tests.Activator.getDefault().getBundle());
      TestUtils.addDynamicExtension(extPointId, "<filter class='"
          + MyModuleFilter.class.getName()
          + "'/>");
      // do check
      IFolder folder = getFolderSrc("test");
      ModuleDescription module = Utils.getSingleModule(folder);
      assertEquals("test.bModule", module.getId());
    } finally {
      TestUtils.removeDynamicExtension(extPointId);
      TestUtils.setContributionBundle(null);
    }
  }

  /**
   * Removes "test.aModule" module.
   */
  public static final class MyModuleFilter implements IModuleFilter {
    @Override
    public List<ModuleDescription> filter(List<ModuleDescription> modules) throws Exception {
      List<ModuleDescription> filtered = Lists.newArrayList();
      for (ModuleDescription module : modules) {
        if (!module.getId().equals("test.aModule")) {
          filtered.add(module);
        }
      }
      return filtered;
    }
  }

  /**
   * Test for {@link Utils#getSingleModule(IResource)}.
   * <p>
   * Maven-like project. Module file in "resources".
   */
  @DisposeProjectAfter
  public void test_getSingleModule_maven_1() throws Exception {
    GTestUtils.configureMavenProject();
    // use "client" folder as IResource
    {
      IResource resource = getFolder("src/main/java/test/client");
      ModuleDescription module = Utils.getSingleModule(resource);
      assertModuleDescriptionPath("src/main/resources/test/Module.gwt.xml", module);
    }
    // use resource from "resources" folder
    {
      IResource resource = setFileContent("src/main/resources/test/client/MyResource.txt", "");
      ModuleDescription module = Utils.getSingleModule(resource);
      assertModuleDescriptionPath("src/main/resources/test/Module.gwt.xml", module);
    }
  }

  /**
   * Test for {@link Utils#getSingleModule(IResource)}.
   * <p>
   * Maven-like project. Module file in "java".
   */
  @DisposeProjectAfter
  public void test_getSingleModule_maven_2() throws Exception {
    GTestUtils.configureMavenProject();
    // move module file into "java"
    {
      IFile moduleFile = getFile("src/main/resources/test/Module.gwt.xml");
      moduleFile.move(new Path("/TestProject/src/main/java/test/Module.gwt.xml"), true, null);
    }
    // use project
    {
      IResource resource = m_project;
      ModuleDescription module = Utils.getSingleModule(resource);
      assertNull(module);
    }
    // use "client" folder as IResource
    {
      IResource resource = getFolder("src/main/java/test/client");
      ModuleDescription module = Utils.getSingleModule(resource);
      assertModuleDescriptionPath("src/main/java/test/Module.gwt.xml", module);
    }
    // use resource from "resources" folder
    {
      IResource resource = setFileContent("src/main/resources/test/client/MyResource.txt", "");
      ModuleDescription module = Utils.getSingleModule(resource);
      assertModuleDescriptionPath("src/main/java/test/Module.gwt.xml", module);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module packages/folders access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ModuleDescription#getModuleFolder()} .
   */
  public void test_getModuleFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertResourcePath("src/test", module.getModuleFolder());
  }

  /**
   * Test for {@link ModuleDescription#getModulePackage()}.
   */
  public void test_getModulePackage() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertEquals("test", module.getModulePackage().getElementName());
  }

  /**
   * Test for {@link ModuleDescription#getModulePublicFolder()}.
   */
  public void test_getModulePublicFolder_1() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    IResource publicFolder = module.getModulePublicFolder();
    assertResourcePath("src/test/public", publicFolder);
  }

  /**
   * Test for {@link ModuleDescription#getModulePublicFolder()}.
   */
  @DisposeProjectAfter
  public void test_getModulePublicFolder_2() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    m_testProject.getPackage("test.myPublicFolder");
    setFileContent(
        moduleFile,
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <public path='myPublicFolder'/>",
            "</module>"));
    //
    ModuleDescription module = getTestModuleDescription();
    IResource publicFolder = module.getModulePublicFolder();
    assertResourcePath("src/test/myPublicFolder", publicFolder);
  }

  /**
   * Test for {@link Utils#getRootSourcePackage(IPackageFragment)}.<br>
   * Ask for "client" package itself.
   */
  public void test_getRootSourcePackage_1() throws Exception {
    IPackageFragment pkg = m_testProject.getPackage("test.client");
    IPackageFragment root = Utils.getRootSourcePackage(pkg);
    assertEquals("test.client", root.getElementName());
  }

  /**
   * Test for {@link Utils#getRootSourcePackage(IPackageFragment)}.<br>
   * Ask for child of "client" package.
   */
  @DisposeProjectAfter
  public void test_getRootSourcePackage_2() throws Exception {
    IPackageFragment pkg = m_testProject.getPackage("test.client.rpc");
    IPackageFragment root = Utils.getRootSourcePackage(pkg);
    assertEquals("test.client", root.getElementName());
  }

  /**
   * Test for {@link Utils#isModuleSourcePackage(IPackageFragment)}.
   */
  @DisposeProjectAfter
  public void test_isModuleSourcePackage() throws Exception {
    // no, "server" is not "source" package
    {
      IPackageFragment pkg = m_testProject.getPackage("test.server");
      assertFalse(Utils.isModuleSourcePackage(pkg));
    }
    // OK, really "source" package
    {
      IPackageFragment pkg = m_testProject.getPackage("test.client");
      assertTrue(Utils.isModuleSourcePackage(pkg));
    }
    // new "source" package in Module.gwt.xml
    {
      IPackageFragment pkg = m_testProject.getPackage("test.newClient");
      // initially not "source" package
      assertFalse(Utils.isModuleSourcePackage(pkg));
      // add "newClient" to "source" folders
      {
        IFile moduleFile = getFileSrc("test/Module.gwt.xml");
        setFileContent(
            moduleFile,
            getSourceDQ(
                "<!-- filler filler filler filler filler -->",
                "<module>",
                "  <source path='newClient'/>",
                "</module>"));
        waitForAutoBuild();
      }
      // OK, now "newClient" is "source" package
      assertTrue(Utils.isModuleSourcePackage(pkg));
    }
  }

  /**
   * Test for {@link Utils#isModuleSourcePackage(IPackageFragment)}.
   */
  @DisposeProjectAfter
  public void test_isModuleSourcePackage_withExcludeElements() throws Exception {
    IPackageFragment inClientPkg = m_testProject.getPackage("test.client.foo");
    IPackageFragment theServicePkg = m_testProject.getPackage("test.client.foo.service");
    IPackageFragment inServicePkg = m_testProject.getPackage("test.client.foo.service.bar");
    // include "client" into "source" folders, but exclude "service" sub-folders
    {
      IFile moduleFile = getFileSrc("test/Module.gwt.xml");
      setFileContent(
          moduleFile,
          getSourceDQ(
              "<!-- filler filler filler filler filler -->",
              "<module>",
              "  <source path='client'>",
              "    <exclude name='**/service/**'/>",
              "  </source>",
              "</module>"));
      waitForAutoBuild();
    }
    // check packages
    assertTrue(Utils.isModuleSourcePackage(inClientPkg));
    assertFalse(Utils.isModuleSourcePackage(theServicePkg));
    assertFalse(Utils.isModuleSourcePackage(inServicePkg));
  }

  /**
   * Test for {@link Utils#isModuleSourcePackage(IPackageFragment)}.
   */
  @DisposeProjectAfter
  public void test_isModuleSourcePackage_withRenameTo() throws Exception {
    IPackageFragment inClientPkg = m_testProject.getPackage("test.client");
    IPackageFragment inServerPkg = m_testProject.getPackage("test.server");
    // use "rename-to" attribute
    {
      IFile moduleFile = getFileSrc("test/Module.gwt.xml");
      setFileContent(moduleFile, "<module rename-to='shortName'/>");
      waitForAutoBuild();
    }
    // check packages
    assertTrue(Utils.isModuleSourcePackage(inClientPkg));
    assertFalse(Utils.isModuleSourcePackage(inServerPkg));
  }

  /**
   * Test for {@link Utils#isModuleSourcePackage(IPackageFragment)}.
   * <p>
   * If module inherits from other module, then it includes its source/client packages.
   * <p>
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=6626
   */
  @DisposeProjectAfter
  public void test_isModuleSourcePackage_withInherits() throws Exception {
    IPackageFragment inClientPkg = m_testProject.getPackage("test.webclient");
    IPackageFragment inServerPkg = m_testProject.getPackage("test.server");
    //
    getFileSrc("test/Module.gwt.xml").delete(true, null);
    setFileContentSrc(
        "test/ModuleB.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <source path='webclient'/>",
            "</module>"));
    setFileContentSrc(
        "test/ModuleA.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='test.ModuleB'/>",
            "</module>"));
    // check packages
    assertTrue(Utils.isModuleSourcePackage(inClientPkg));
    assertFalse(Utils.isModuleSourcePackage(inServerPkg));
  }

  /**
   * Test for {@link Utils#isModuleSourcePackage(IPackageFragment)}.
   * <p>
   * http://forums.instantiations.com/viewtopic.php?f=11&t=5300
   */
  @DisposeProjectAfter
  public void test_isModuleSourcePackage_maven() throws Exception {
    getFolder("src").delete(true, null);
    getFolder("src/main/java");
    getFolder("src/main/resources");
    m_testProject.removeSourceFolder("/TestProject/src");
    m_testProject.addSourceFolder("/TestProject/src/main/java");
    m_testProject.addSourceFolder("/TestProject/src/main/resources");
    waitForAutoBuild();
    // create GWT module
    GTestUtils.createModule(m_testProject, "test.Module");
    // move module file into "resources"
    {
      IFile moduleFile = getFile("src/main/java/test/Module.gwt.xml");
      getFolder("src/main/resources/test");
      moduleFile.move(new Path("/TestProject/src/main/resources/test/Module.gwt.xml"), true, null);
    }
    // OK, really "source" package
    {
      IPackageFragment pkg = m_testProject.getPackage("test.client");
      assertTrue(Utils.isModuleSourcePackage(pkg));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module reading
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#readModule(IFile)}, without problems.
   */
  public void test_readModule_IFile_OK() throws Exception {
    ModuleDescription moduleDescription = getTestModuleDescription();
    ModuleElement moduleElement = Utils.readModule(moduleDescription);
    assertEquals("test.Module", moduleElement.getName());
  }

  /**
   * Test for {@link Utils#readModule(IFile)}, with problems.
   */
  @DisposeProjectAfter
  public void test_readModule_IFile_bad() throws Exception {
    // set invalid content
    setFileContentSrc("test/Module.gwt.xml", "<module>");
    // fails during read
    try {
      ModuleDescription moduleDescription = getTestModuleDescription();
      Utils.readModule(moduleDescription);
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.INVALID_MODULE_FILE, e.getCode());
      assertThat(e.getParameters()[0]).endsWith("test.Module");
    }
  }

  /**
   * Test for {@link Utils#readModule(String, java.io.InputStream)}, without problems.
   */
  public void test_readModule_InputStream_OK() throws Exception {
    String moduleString = getSourceDQ("<module/>");
    ModuleElement moduleElement =
        Utils.readModule("my.external.Module", new ByteArrayInputStream(moduleString.getBytes()));
    assertEquals("my.external.Module", moduleElement.getName());
    assertThat(moduleElement.getChildren()).isEmpty();
  }

  /**
   * Test for {@link Utils#readModule(String, java.io.InputStream)}, with problems.
   */
  @DisposeProjectAfter
  public void test_readModule_InputStream_bad() throws Exception {
    String moduleId = "my.external.Module";
    String moduleString = getSourceDQ("<module>");
    InputStream inputStream = new ByteArrayInputStream(moduleString.getBytes());
    try {
      Utils.readModule(moduleId, inputStream);
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.INVALID_MODULE_FILE, e.getCode());
      assertThat(e.getParameters()).contains(moduleId);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getFilesForResources(IFile, Collection)}. <br>
   * Resource in module "public" folder.
   */
  public void test_getFilesForResources_publicFolder() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    IFile expectedFile = setFileContentSrc("test/public/1.txt", "");
    List<IFile> files = Utils.getFilesForResources(moduleFile, Lists.newArrayList("1.txt"));
    assertThat(files).containsOnly(expectedFile);
  }

  /**
   * Test for {@link Utils#getFilesForResources(IFile, Collection)}. <br>
   * Resource in "war" folder.
   */
  public void test_getFilesForResources_warFolder() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    IFile expectedFile = setFileContent("war/1.txt", "");
    List<IFile> files = Utils.getFilesForResources(moduleFile, Lists.newArrayList("1.txt"));
    assertThat(files).containsOnly(expectedFile);
  }

  /**
   * Test for {@link Utils#getFilesForResources(IFile, Collection)}. <br>
   * Not existing resource.
   */
  public void test_getFilesForResources_noSuchResource() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    List<IFile> files =
        Utils.getFilesForResources(moduleFile, Lists.newArrayList("NoSuchResource.txt"));
    assertThat(files).isEmpty();
  }

  /**
   * Test for {@link Utils#getFilesForResources(IFile, Collection)}. <br>
   * Resource from required/inherited module in different {@link IProject}.
   */
  @DisposeProjectAfter
  public void test_getFilesForResources_inherited() throws Exception {
    TestProject libProject = new TestProject("libProject");
    try {
      // prepare module "the.Library"
      {
        GTestUtils.createModule(libProject, "the.Library");
        setFileContentSrc(
            libProject.getProject(),
            "the/public/sub/folder/libResource.txt",
            "some content");
      }
      // require "libProject" from "TestProject"
      m_testProject.addRequiredProject(libProject);
      // configure module "test.Module" to inherit from "the.Library"
      IFile moduleFile = getFileSrc("test/Module.gwt.xml");
      setFileContent(
          moduleFile,
          getSourceDQ(
              "<!-- filler filler filler filler filler -->",
              "<module>",
              "  <inherits name='the.Library'/>",
              "</module>"));
      waitForAutoBuild();
      // check resources
      List<IFile> files =
          Utils.getFilesForResources(
              moduleFile,
              Lists.newArrayList("Module.html", "sub/folder/libResource.txt"));
      assertThat(files).hasSize(2);
      assertResourcePath("war/Module.html", files.get(0));
      assertResourcePath("src/the/public/sub/folder/libResource.txt", files.get(1));
    } finally {
      libProject.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getResource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getResource(IFile, String)}.
   */
  public void test_getResource_warFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent("war/1.txt", "");
    assert_getResource_notNull(module, "1.txt");
  }

  /**
   * Test for {@link Utils#getResource(IFile, String)}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43760
   */
  @DisposeProjectAfter
  public void test_getResource_mavenFolder_webapp() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent("src/main/webapp/1.txt", "");
    assert_getResource_notNull(module, "1.txt");
  }

  /**
   * Test for {@link Utils#getResource(IFile, String)}.
   */
  public void test_getResource_publicFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContentSrc("test/public/1.txt", "");
    assert_getResource_notNull(module, "test.Module/1.txt");
  }

  /**
   * Test for {@link Utils#getResource(IFile, String)}.
   */
  @DisposeProjectAfter
  public void test_getResource_publicFolder_renameTo() throws Exception {
    {
      IFile moduleFile = getFileSrc("test/Module.gwt.xml");
      setFileContent(moduleFile, "<module rename-to='myModule'/>");
    }
    setFileContentSrc("test/public/1.txt", "");
    waitForAutoBuild();
    ModuleDescription module = getTestModuleDescription();
    //
    assert_getResource_null(module, "test.Module/1.txt");
    assert_getResource_notNull(module, "myModule/1.txt");
    assert_getResource_notNull(module, "1.txt");
  }

  /**
   * Test for {@link Utils#getResource(IFile, String)}.
   */
  public void test_getResource_no() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assert_getResource_null(module, "test.Module/noSuchResource.txt");
  }

  private static void assert_getResource_notNull(ModuleDescription module, String path)
      throws Exception {
    InputStream inputStream = Utils.getResource(module, path);
    IOUtils.closeQuietly(inputStream);
    assertNotNull(inputStream);
  }

  private static void assert_getResource_null(ModuleDescription module, String path)
      throws Exception {
    InputStream inputStream = Utils.getResource(module, path);
    IOUtils.closeQuietly(inputStream);
    assertNull(inputStream);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isExistingResource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#isExistingResource(IFile, String)}.
   */
  public void test_isExistingResource_warFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertTrue(Utils.isExistingResource(module, "Module.html"));
  }

  /**
   * Test for {@link Utils#isExistingResource(IFile, String)}.
   */
  public void test_isExistingResource_publicFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContentSrc("test/public/1.txt", "");
    assertTrue(Utils.isExistingResource(module, "test.Module/1.txt"));
  }

  /**
   * Test for {@link Utils#isExistingResource(IFile, String)}.
   */
  public void test_isExistingResource_no() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    assertFalse(Utils.isExistingResource(module, "test.Module/noSuchResource.txt"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // More resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   */
  public void test_getHTMLFile_warFolder() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    IFile htmlFile = getFile("war/Module.html");
    assertEquals(htmlFile, Utils.getHTMLFile(module));
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   * <p>
   * By default our test web.xml has "welcome-file", but we want to test without it.
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_warFolder_withoutWebXML() throws Exception {
    // delete web.xml
    getFile("war/WEB-INF/web.xml").delete(true, null);
    // check
    ModuleDescription module = getTestModuleDescription();
    IFile htmlFile = getFile("war/Module.html");
    assertEquals(htmlFile, Utils.getHTMLFile(module));
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   * <p>
   * Sometimes users try to use empty <code>web.xml</code> file. We should ignore it.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?46031
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_warFolder_emptyWebXML() throws Exception {
    // empty web.xml we can point on it
    setFileContent("war/WEB-INF/web.xml", "");
    // check
    ModuleDescription module = getTestModuleDescription();
    IFile htmlFile = getFile("war/Module.html");
    assertEquals(htmlFile, Utils.getHTMLFile(module));
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   * <p>
   * Sometimes users try to use invalid <code>web.xml</code> file. We should show better error.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?46836
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_warFolder_invalidWebXML() throws Exception {
    setFileContent("war/WEB-INF/web.xml", "invalid content");
    ModuleDescription module = getTestModuleDescription();
    try {
      Utils.getHTMLFile(module);
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(IExceptionConstants.INVALID_WEB_XML, de.getCode());
    }
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_warFolder_useWelcomeFile() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    // replace default Module.html with file alternative name
    getFile("war/Module.html").delete(true, null);
    IFile htmlFile = setFileContent("war/EntryPoint.html", "<html/>");
    // we can not find "EntryPoint.html", because it has non-default name
    assertEquals(null, Utils.getHTMLFile(module));
    // but with web.xml we can point on it
    setFileContent(
        "war/WEB-INF/web.xml",
        getSource(
            "<web-app>",
            "  <welcome-file-list>",
            "    <welcome-file>EntryPoint.html</welcome-file>",
            "  </welcome-file-list>",
            "</web-app>"));
    assertEquals(htmlFile, Utils.getHTMLFile(module));
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   * <p>
   * Exception {@link IExceptionConstants#NO_MODULE} should not be masked.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47616
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_warFolder_useWelcomeFile_whenNoModule() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent(
        getTestModuleFile(),
        getSource(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='no.such.Module'/>",
            "</module>"));
    waitForAutoBuild();
    //
    try {
      Utils.getHTMLFile(module);
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_MODULE, e.getCode());
    }
  }

  /**
   * Test for {@link Utils#getHTMLFile(IFile)}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45214
   */
  @DisposeProjectAfter
  public void test_getHTMLFile_notExistingSourceFolder() throws Exception {
    // add "src2"
    m_testProject.addSourceFolder("/TestProject/src2");
    //
    ModuleDescription module = getTestModuleDescription();
    IFile htmlFile = Utils.getHTMLFile(module);
    assertResourcePath("war/Module.html", htmlFile);
  }

  /**
   * Test for {@link Utils#getDefaultHTMLName(String)}.
   */
  public void test_getDefaultHTMLName() throws Exception {
    assertEquals("Module.html", Utils.getDefaultHTMLName("test.Module"));
    assertEquals("TheModule.html", Utils.getDefaultHTMLName("my.long.name.for.TheModule"));
  }

  /**
   * Test for {@link Utils#getCssResources(IFile)}.<br>
   * Only default <code>Module.css</code> resource from HTML.
   */
  public void test_getCssResources_fromHTML() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    List<String> resources = Utils.getCssResources(module);
    assertThat(resources).containsOnly("Module.css");
  }

  /**
   * Test for combination {@link Utils#getHTMLFile(IFile)} and {@link Utils#getCssResources(IFile)}.
   */
  @DisposeProjectAfter
  public void test_getCssResources_useWelcomeFile_inSubFolder() throws Exception {
    IFile moduleFile = getTestModuleFile();
    ModuleDescription module = getTestModuleDescription();
    // replace default Module.html with file alternative name
    getFile("war/Module.html").delete(true, null);
    IFile htmlFile =
        setFileContent(
            "war/sub/EntryPoint.html",
            getSource(
                "<html>",
                "  <head>",
                "    <link type='text/css' rel='stylesheet' href='resources/MyStyles.css'/>",
                "  </head>",
                "</html>"));
    IFile cssFile = setFileContent("war/sub/resources/MyStyles.css", "");
    // we can not find "EntryPoint.html", because it has non-default name
    assertEquals(null, Utils.getHTMLFile(module));
    // but with web.xml we can point on it
    setFileContent(
        "war/WEB-INF/web.xml",
        getSource(
            "<web-app>",
            "  <welcome-file-list>",
            "    <welcome-file>sub/EntryPoint.html</welcome-file>",
            "  </welcome-file-list>",
            "</web-app>"));
    assertEquals(htmlFile, Utils.getHTMLFile(module));
    {
      List<String> cssResources = Utils.getCssResources(module);
      assertThat(cssResources).containsExactly("sub/resources/MyStyles.css");
      {
        String resource = cssResources.get(0);
        IFile fileForResource = Utils.getFileForResource(moduleFile, resource);
        assertEquals(cssFile, fileForResource);
      }
    }
  }

  /**
   * Test for {@link Utils#getCssResources(IFile)}.<br>
   * Delete default HTML, so no CSS resources at all.
   */
  @DisposeProjectAfter
  public void test_getCssResources_noHTML() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    // delete Module.html
    getFile("war/Module.html").delete(true, null);
    // no CSS resources
    List<String> resources = Utils.getCssResources(module);
    assertThat(resources).isEmpty();
  }

  /**
   * Test for {@link Utils#getCssResources(IFile)}.<br>
   * Use <code>stylesheet</code> element in module file.
   */
  @DisposeProjectAfter
  public void test_getCssResources_fromModule() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent(
        getTestModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <stylesheet src='fromModule.css'/>",
            "</module>"));
    waitForAutoBuild();
    // CSS resources from both HTML and *.gwt.xml should be returned
    List<String> resources = Utils.getCssResources(module);
    assertThat(resources).containsOnly("Module.css", "fromModule.css");
  }

  /**
   * Test for {@link Utils#getCssResources(IFile)}.<br>
   * Use path with leading "/" string.
   */
  @DisposeProjectAfter
  public void test_getCssResources_fromModule_fromRoot() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent("war/Module.html", "<html/>");
    setFileContent(
        getTestModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <stylesheet src='/css/fromModule.css'/>",
            "</module>"));
    waitForAutoBuild();
    // CSS resource should be returned
    List<String> resources = Utils.getCssResources(module);
    assertThat(resources).containsOnly("css/fromModule.css");
  }

  /**
   * Test for {@link Utils#getCssResources(IFile)}.<br>
   * Use path with leading "../" string.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?46049
   */
  @DisposeProjectAfter
  public void test_getCssResources_fromModule_forFileInWar() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent("war/Module.html", "<html/>");
    setFileContent(
        getTestModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <stylesheet src='../css/fromModule.css'/>",
            "</module>"));
    waitForAutoBuild();
    // CSS resource should be returned
    List<String> resources = Utils.getCssResources(module);
    assertThat(resources).containsOnly("css/fromModule.css");
  }

  /**
   * Test for {@link Utils#getScriptResources(IFile)}.<br>
   * No scripts, because <code>.nocache.js</code> from HTML is ignored.
   */
  public void test_getScriptResources_1() throws Exception {
    assertThat(getFileContent("war/Module.html")).contains(".nocache.js");
    ModuleDescription moduleDescription = getTestModuleDescription();
    List<String> resources = Utils.getScriptResources(moduleDescription);
    assertThat(resources).isEmpty();
  }

  /**
   * Test for {@link Utils#getScriptResources(IFile)}.<br>
   * Add references on new scripts from HTML and module file.
   */
  @DisposeProjectAfter
  public void test_getScriptResources_2() throws Exception {
    IFile moduleFile = getTestModuleFile();
    ModuleDescription moduleDescription = getTestModuleDescription();
    setFileContentSrc(
        "test/public/Module.html",
        getSourceDQ(
            "<html>",
            "  <body>",
            "    <script language='javascript' src='test.Module.nocache.js'></script>",
            "    <script language='javascript' src='fromHTML.js'></script>",
            "  </body>",
            "</html>"));
    setFileContent(
        moduleFile,
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <script src='fromModule.js'/>",
            "</module>"));
    waitForAutoBuild();
    //
    List<String> resources = Utils.getScriptResources(moduleDescription);
    assertThat(resources).containsOnly("fromModule.js", "fromHTML.js");
  }

  /**
   * Test for {@link Utils#getScriptResources(IFile)}.
   * <p>
   * We don't want to show Google Maps widget, and even load its script. Script requires key, and
   * users often fail to provide it. In this case script shows warning and locks-up Eclipse.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43650
   */
  @DisposeProjectAfter
  public void test_getScriptResources_ignoreGoogleMaps() throws Exception {
    IFile moduleFile = getTestModuleFile();
    ModuleDescription moduleDescription = getTestModuleDescription();
    setFileContentSrc("test/public/Module.html", "");
    setFileContent(
        moduleFile,
        getSource(
            "<module>",
            "  <script src='http://maps.google.com/maps?gwt=1&amp;file=api&amp;v=2'/>/>",
            "</module>"));
    waitForAutoBuild();
    //
    List<String> resources = Utils.getScriptResources(moduleDescription);
    assertThat(resources).isEmpty();
  }

  /**
   * Test for {@link Utils#getScriptResources(IFile)}.<br>
   * Use "script" tag without "src" attribute.
   */
  @DisposeProjectAfter
  public void test_getScriptResources_noSrcInScript() throws Exception {
    ModuleDescription moduleDescription = getTestModuleDescription();
    setFileContentSrc(
        "test/public/Module.html",
        getSourceDQ(
            "<html>",
            "  <body>",
            "    <script type='text/javascript'>some script</script>",
            "  </body>",
            "</html>"));
    waitForAutoBuild();
    //
    List<String> resources = Utils.getScriptResources(moduleDescription);
    assertThat(resources).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDefaultLocale()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getDefaultLocale(IFile)}.
   */
  public void test_getDefaultLocale_noOverride() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    // do check
    String defaultLocale = Utils.getDefaultLocale(module);
    assertEquals("default", defaultLocale);
  }

  /**
   * Test for {@link Utils#getDefaultLocale(IFile)}.
   */
  @DisposeProjectAfter
  public void test_getDefaultLocale_doOverride() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContent(
        getTestModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='com.google.gwt.i18n.I18N'/>",
            "  <extend-property name='locale' values='en,es'/>",
            "  <set-property name='locale' value='en,es'/>",
            "  <set-property-fallback name='locale' value='en'/>",
            "</module>"));
    // do check
    String defaultLocale = Utils.getDefaultLocale(module);
    assertEquals("en", defaultLocale);
  }

  /**
   * Test for {@link Utils#getDefaultLocale(IFile)}.
   */
  @DisposeProjectAfter
  public void test_getDefaultLocale_doOverride_inLibrary() throws Exception {
    // prepare module "the.Library"
    {
      IFile libraryModuleFile = GTestUtils.createModule(m_testProject, "the.Library");
      setFileContent(
          libraryModuleFile,
          getSourceDQ(
              "<!-- filler filler filler filler filler -->",
              "<module>",
              "  <inherits name='com.google.gwt.i18n.I18N'/>",
              "  <extend-property name='locale' values='en,es'/>",
              "  <set-property name='locale' value='en,es'/>",
              "  <set-property-fallback name='locale' value='en'/>",
              "</module>"));
    }
    // configure module
    ModuleDescription module = getTestModuleDescription();
    setFileContent(
        getTestModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='the.Library'/>",
            "</module>"));
    // do check
    String defaultLocale = Utils.getDefaultLocale(module);
    assertEquals("en", defaultLocale);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDocType()
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_getDocType_no() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContentSrc(
        "test/public/Module.html",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<html>",
            "  <body>",
            "    filler filler filler",
            "  </body>",
            "</html>"));
    assertEquals("", Utils.getDocType(module));
  }

  @DisposeProjectAfter
  public void test_getDocType_has() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    setFileContentSrc(
        "test/public/Module.html",
        getSourceDQ(
            "<!doctype html>",
            "<html>",
            "  <body>",
            "    filler filler filler",
            "  </body>",
            "</html>"));
    assertEquals("<!doctype html>", Utils.getDocType(module));
  }

  @DisposeProjectAfter
  public void test_getDocType_noHTML() throws Exception {
    ModuleDescription module = getTestModuleDescription();
    getFile("war/Module.html").delete(true, null);
    assertThat(Utils.getDocType(module)).isNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RemoteService interface/impl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#isRemoteService(IResource)}.
   */
  public void test_isRemoteService_IResource() throws Exception {
    // XML file is NOT RemoteService
    {
      IResource resource = getFileSrc("test/Module.gwt.xml");
      assertTrue(resource.exists());
      assertFalse(Utils.isRemoteService(resource));
    }
    // EntryPoint is NOT RemoteService
    {
      IResource resource = getFileSrc("test/client/Module.java");
      assertTrue(resource.exists());
      assertFalse(Utils.isRemoteService(resource));
    }
    // new RemoteService
    {
      IType type = GTestUtils.createTestService(this)[0];
      IResource resource = type.getUnderlyingResource();
      assertThat(resource).isInstanceOf(IFile.class);
      assertTrue(resource.exists());
      assertTrue(Utils.isRemoteService(resource));
    }
  }

  /**
   * Test for {@link Utils#isRemoteService(IJavaElement)}.
   */
  public void test_isRemoteService_IJavaElement() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    // EntryPoint is NOT RemoteService
    {
      IJavaElement element = javaProject.findType("test.client.Module");
      assertFalse(Utils.isRemoteService(element));
    }
    // new RemoteService
    {
      IJavaElement element = GTestUtils.createTestService(this)[0];
      assertTrue(Utils.isRemoteService(element));
    }
  }

  /**
   * Test for {@link Utils#isRemoteServiceImpl(IJavaElement)}.
   */
  public void test_isRemoteServiceImpl() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    // IJavaProject is NOT RemoteService impl
    {
      IJavaElement element = javaProject;
      assertFalse(Utils.isRemoteServiceImpl(element));
    }
    // EntryPoint is NOT RemoteService impl
    {
      IJavaElement element = javaProject.findType("test.client.Module");
      assertFalse(Utils.isRemoteServiceImpl(element));
    }
    // new RemoteService impl
    {
      IType element = GTestUtils.createTestService(this)[1];
      // type itself is RemoteService impl
      assertTrue(Utils.isRemoteServiceImpl(element));
      // ...and enclosing IType too
      assertTrue(Utils.isRemoteServiceImpl(element.getCompilationUnit()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryPoint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#isEntryPoint(IJavaElement)}.
   */
  public void test_isEntryPoint() throws Exception {
    IJavaProject javaProject = m_testProject.getJavaProject();
    // IJavaProject is NOT EntryPoint
    {
      IJavaElement element = javaProject;
      assertFalse(Utils.isEntryPoint(element));
    }
    // existing EntryPoint
    {
      IJavaElement element = javaProject.findType("test.client.Module");
      assertTrue(Utils.isEntryPoint(element));
    }
    // RemoteService impl is NOT EntryPoint
    {
      IType element = GTestUtils.createTestService(this)[0];
      assertFalse(Utils.isEntryPoint(element));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Progress
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getNonNullMonitor(IProgressMonitor)}.
   */
  public void test_getNonNullMonitor() throws Exception {
    // not "null" as argument
    {
      IProgressMonitor monitor = EasyMock.createStrictMock(IProgressMonitor.class);
      assertSame(monitor, Utils.getNonNullMonitor(monitor));
    }
    // "null" as argument
    {
      IProgressMonitor monitor = Utils.getNonNullMonitor(null);
      assertNotNull(monitor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Projects
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#getProject(String)}.
   */
  public void test_getProject() throws Exception {
    {
      IProject project = Utils.getProject("TestProject");
      assertNotNull(project);
      assertTrue(project.exists());
    }
    {
      IProject project = Utils.getProject("NoSuchProject");
      assertNotNull(project);
      assertFalse(project.exists());
    }
  }

  /**
   * Test for {@link Utils#getJavaProject(String)}.
   */
  public void test_getJavaProject() throws Exception {
    {
      IJavaProject javaProject = Utils.getJavaProject("TestProject");
      assertNotNull(javaProject);
      assertTrue(javaProject.exists());
    }
    {
      IJavaProject javaProject = Utils.getJavaProject("NoSuchProject");
      assertNotNull(javaProject);
      assertFalse(javaProject.exists());
    }
  }

  /**
   * Test for {@link Utils#isGWTProject(IJavaProject)} and {@link Utils#isGWTProject(IProject)}.
   */
  public void test_isGWTProject() throws Exception {
    IJavaProject javaProject = Utils.getJavaProject("TestProject");
    assertTrue(Utils.isGWTProject(javaProject));
    assertTrue(Utils.isGWTProject(javaProject.getProject()));
  }

  /**
   * Test for {@link Utils#isGWTProject(IJavaProject)} and {@link Utils#isGWTProject(IProject)}.
   */
  public void test_isGWTProject_newProject() throws Exception {
    TestProject newProject = new TestProject("newProject");
    try {
      IJavaProject javaProject = newProject.getJavaProject();
      IProject project = javaProject.getProject();
      // initially not GWT project
      assertFalse(Utils.isGWTProject(javaProject));
      assertFalse(Utils.isGWTProject(project));
      // convert into GWT project
      GTestUtils.configure(newProject);
      assertTrue(Utils.isGWTProject(javaProject));
      assertTrue(Utils.isGWTProject(project));
      // remove GWT nature, still GWT project
      ProjectUtils.removeNature(project, Constants.NATURE_ID);
      assertTrue(Utils.isGWTProject(javaProject));
      assertTrue(Utils.isGWTProject(project));
    } finally {
      newProject.dispose();
    }
  }

  /**
   * Test for {@link Utils#isGWTProject(IJavaProject)} and {@link Utils#isGWTProject(IProject)}.
   */
  public void test_isGWTProject_noSuchProject() throws Exception {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("noSuchProject");
    IJavaProject javaProject = JavaCore.create(project);
    assertFalse(project.exists());
    assertFalse(javaProject.exists());
    assertFalse(Utils.isGWTProject(javaProject));
  }

  /**
   * Test for {@link Utils#isGWTProject(IJavaProject)} and {@link Utils#isGWTProject(IProject)}.
   */
  @DisposeProjectAfter
  public void test_isGWTProject_closedProject() throws Exception {
    m_project.close(null);
    waitForAutoBuild();
    // exists, but not accessible
    assertTrue(m_project.exists());
    assertFalse(m_project.isOpen());
    assertFalse(m_project.isAccessible());
    // not GWT project
    assertFalse(Utils.isGWTProject(m_javaProject));
    assertFalse(Utils.isGWTProject(m_project));
  }

  /**
   * Test for {@link Utils#isGpeGwtProject(IProject)}.
   */
  public void test_isGpeGwtProject() throws Exception {
    // in tests we don't use GPE projects
    assertFalse(Utils.isGpeGwtProject(m_project));
    // make it GPE project
    ProjectUtils.addNature(m_project, Constants.GPE_NATURE_ID);
    assertTrue(Utils.isGpeGwtProject(m_project));
  }

  /**
   * Test for {@link Utils#getJavaModel()}.
   */
  public void test_getJavaModel() throws Exception {
    IJavaModel javaModel = Utils.getJavaModel();
    assertNotNull(javaModel);
    assertTrue(javaModel.exists());
  }

  /**
   * Test for {@link Utils#getGWTProjects()}.
   */
  public void test_getGWTProjects() throws Exception {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    // initially only one project, and it is GWT project
    {
      assertThat(workspaceRoot.getProjects()).hasSize(1);
      //
      List<IJavaProject> gwtProjects = Utils.getGWTProjects();
      assertThat(gwtProjects).hasSize(1);
      assertEquals("TestProject", gwtProjects.get(0).getElementName());
    }
    // two projects
    {
      TestProject newProject = new TestProject("newProject");
      try {
        // now two projects
        assertThat(workspaceRoot.getProjects()).hasSize(2);
        // ...but still only one GWT project
        {
          List<IJavaProject> gwtProjects = Utils.getGWTProjects();
          assertThat(gwtProjects).hasSize(1);
          assertEquals("TestProject", gwtProjects.get(0).getElementName());
        }
        // convert into GWT project, now two GWT projects
        {
          GTestUtils.configure(newProject);
          List<IJavaProject> gwtProjects = Utils.getGWTProjects();
          assertThat(gwtProjects).hasSize(2);
          Set<String> gwtProjectNames =
              ImmutableSet.of(
                  gwtProjects.get(0).getElementName(),
                  gwtProjects.get(1).getElementName());
          assertThat(gwtProjectNames).containsOnly("TestProject", "newProject");
        }
      } finally {
        newProject.dispose();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Utils#parseUnit(ICompilationUnit)}.
   */
  public void test_parseUnit() throws Exception {
    IType type = m_testProject.getJavaProject().findType("test.client.Module");
    CompilationUnit compilationUnit = Utils.parseUnit(type.getCompilationUnit());
    assertNotNull(compilationUnit);
    assertEquals("Module", DomGenerics.types(compilationUnit).get(0).getName().getIdentifier());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that {@link ModuleDescription} if {@link IFile} based and has expected path relative to
   * its project.
   */
  private static void assertModuleDescriptionPath(String expectedPath, ModuleDescription module) {
    assertNotNull("No module.", module);
    IResource resource = ((DefaultModuleDescription) module).getFile();
    assertResourcePath(expectedPath, resource);
  }

  /**
   * Asserts that {@link IResource} has expected path relative to its project.
   */
  private static void assertResourcePath(String expectedPath, IResource resource) {
    assertNotNull("No resource.", resource);
    String actualPath = resource.getProjectRelativePath().toPortableString();
    assertEquals(expectedPath, actualPath);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IFile} of standard test module.
   */
  private static IFile getTestModuleFile() throws Exception {
    return getFileSrc("test/Module.gwt.xml");
  }

  /**
   * @return the {@link ModuleDescription} of standard test module.
   */
  private static ModuleDescription getTestModuleDescription() throws Exception {
    IFile moduleFile = getTestModuleFile();
    return Utils.getExactModule(moduleFile);
  }
}