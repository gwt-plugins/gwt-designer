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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.common.base.Predicate;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.tests.Activator;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.utils.ProjectClassLoaderTest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClasspathEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;

/**
 * Test GWT {@link ClassLoader} problems.
 * 
 * @author scheglov_ke
 */
public class ClassLoaderTest extends GwtModelTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_badFileInClasspath_notJar() throws Exception {
    File tempFile = File.createTempFile("myFile", ".properties");
    try {
      ProjectUtils.addExternalJar(m_javaProject, tempFile.getAbsolutePath(), null);
      //
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "  }",
          "}");
    } finally {
      tempFile.delete();
    }
  }

  @DisposeProjectAfter
  public void test_badFileInClasspath_noSuchFile() throws Exception {
    {
      String jarPathString = File.listRoots()[0].getAbsolutePath() + "noSuchFile.jar";
      ProjectUtils.addExternalJar(m_javaProject, jarPathString, null);
    }
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }

  /**
   * Reference not existing {@link IProject}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47578
   */
  @DisposeProjectAfter
  public void test_reference_notExistingProject() throws Exception {
    // add "myProject"
    TestProject myProject = new TestProject("myProject");
    m_testProject.addRequiredProject(myProject);
    myProject.dispose();
    // try to parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Attempt to parse unit which is not part of module. We should show warning instead of
   * {@link NullPointerException}.
   * <p>
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=6442
   */
  @DisposeProjectAfter
  public void test_noModule() throws Exception {
    try {
      parseSource(
          "test2",
          "Test.java",
          getSourceDQ(
              "// filler filler filler filler filler",
              "package test2;",
              "import com.google.gwt.user.client.ui.FlowPanel;",
              "public class Test extends FlowPanel {",
              "  public Test() {",
              "  }",
              "}"));
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_MODULE_FILE, e.getCode());
    }
  }

  /**
   * We should make sure that we support parsing for Maven projects.
   */
  @DisposeProjectAfter
  public void test_gwtInMavenStructure() throws Exception {
    // remove existing GWT jars from classpath
    ProjectUtils.removeClasspathEntries(m_javaProject, new Predicate<IClasspathEntry>() {
      @Override
      public boolean apply(IClasspathEntry entry) {
        return entry.getPath().toPortableString().contains("gwt-");
      }
    });
    // prepare Maven-like structure
    File userFile;
    File devFile;
    {
      String testLocation = Activator.getDefault().getStateLocation().toPortableString();
      File gwtDirectory = new File(testLocation + "/SDK from Maven");
      // prepare locations
      userFile = new File(gwtDirectory + "/gwt/gwt-user/2.2.0/gwt-user-2.2.0.jar");
      devFile = new File(gwtDirectory + "/gwt/gwt-dev/2.2.0/gwt-dev-2.2.0.jar");
      FileUtils.forceMkdir(userFile.getParentFile());
      FileUtils.forceMkdir(devFile.getParentFile());
      // copy jars
      String sdkLocation = getGWTLocation_forProject();
      FileUtils.copyFile(new File(sdkLocation + "/gwt-user.jar"), userFile, false);
      FileUtils.copyFile(new File(sdkLocation + "/gwt-dev.jar"), devFile, false);
      // use this gwt-user.jar
      ProjectUtils.addExternalJar(m_javaProject, userFile.getAbsolutePath(), null);
      m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
    // try parse
    try {
      parseJavaInfo(
          "// filler filler filler filler filler",
          "public class Test extends FlowPanel {",
          "  public Test() {",
          "  }",
          "}");
    } finally {
      makeGwtJarEmpty(userFile);
      makeGwtJarEmpty(devFile);
    }
  }

  /**
   * There was regression during 2.3.0 release.
   */
  @DisposeProjectAfter
  public void test_gwtInDirectoryWithSpace() throws Exception {
    // remove existing GWT jars from classpath
    ProjectUtils.removeClasspathEntries(m_javaProject, new Predicate<IClasspathEntry>() {
      @Override
      public boolean apply(IClasspathEntry entry) {
        return entry.getPath().toPortableString().contains("gwt-");
      }
    });
    // use GWT jars from directory with spaces
    File gwtDirectory;
    {
      String testLocation = Activator.getDefault().getStateLocation().toPortableString();
      gwtDirectory = new File(testLocation + "/SDK with spaces");
      // copy jars
      String sdkLocation = getGWTLocation_forProject();
      FileUtils.copyFileToDirectory(new File(sdkLocation + "/gwt-user.jar"), gwtDirectory);
      FileUtils.copyFileToDirectory(new File(sdkLocation + "/gwt-dev.jar"), gwtDirectory);
      // add jars into classpath
      m_testProject.addExternalJars(gwtDirectory.getAbsolutePath());
    }
    // try parse
    try {
      parseJavaInfo(
          "// filler filler filler filler filler",
          "public class Test extends FlowPanel {",
          "  public Test() {",
          "  }",
          "}");
    } finally {
      makeGwtJarsEmpty(gwtDirectory);
    }
  }

  /**
   * We can not find "gwt-dev.jar" relative to "gwt-user.jar", but "gwt-dev.jar" is in project
   * classpath. So, we can just use it.
   */
  @DisposeProjectAfter
  public void test_unknownStructure_gwtDevInClasspath() throws Exception {
    // remove existing GWT jars from classpath
    ProjectUtils.removeClasspathEntries(m_javaProject, new Predicate<IClasspathEntry>() {
      @Override
      public boolean apply(IClasspathEntry entry) {
        return entry.getPath().toPortableString().contains("gwt-");
      }
    });
    // prepare non-standard structure
    File userFile;
    File devFile;
    {
      String testLocation = Activator.getDefault().getStateLocation().toPortableString();
      File gwtDirectory = new File(testLocation + "/SDK unknown structure");
      // prepare locations
      userFile = new File(gwtDirectory + "/gwt/gwt-user/gwt-user.jar");
      devFile = new File(gwtDirectory + "/gwt/gwt-dev-2.3.0.jar");
      FileUtils.forceMkdir(userFile.getParentFile());
      FileUtils.forceMkdir(devFile.getParentFile());
      // copy jars
      String sdkLocation = getGWTLocation_forProject();
      FileUtils.copyFile(new File(sdkLocation + "/gwt-user.jar"), userFile, false);
      FileUtils.copyFile(new File(sdkLocation + "/gwt-dev.jar"), devFile, false);
      // use these gwt-user.jar and gwt-dev.jar
      ProjectUtils.addExternalJar(m_javaProject, userFile.getAbsolutePath(), null);
      ProjectUtils.addExternalJar(m_javaProject, devFile.getAbsolutePath(), null);
      m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
    // try parse
    try {
      parseJavaInfo(
          "// filler filler filler filler filler",
          "public class Test extends FlowPanel {",
          "  public Test() {",
          "  }",
          "}");
    } finally {
      makeGwtJarEmpty(userFile);
      makeGwtJarEmpty(devFile);
    }
  }

  /**
   * There was regression during 2.3.0 release.
   */
  @DisposeProjectAfter
  public void test_projectNotInWorkspace() throws Exception {
    ProjectClassLoaderTest.moveProjectIntoWorkspaceSubFolder();
    // try parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * User may declare Generator in project itself, so we should allow to use it, i.e. keep output
   * directories (with *.class files) in {@link ClassLoader}.
   */
  @DisposeProjectAfter
  public void test_generatorInProject() throws Exception {
    m_testProject.addExternalJar(getGWTLocation_forProject() + "/gwt-dev.jar");
    setFileContentSrc(
        "test/client/MyInterface.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "public class MyInterface {",
            "}"));
    setFileContentSrc(
        "test/rebind/MyGenerator.java",
        getSourceDQ(
            "package test.rebind;",
            "import com.google.gwt.core.ext.Generator;",
            "import com.google.gwt.core.ext.GeneratorContext;",
            "import com.google.gwt.core.ext.TreeLogger;",
            "import com.google.gwt.core.ext.UnableToCompleteException;",
            "public class MyGenerator extends Generator {",
            "  @Override",
            "  public String generate(TreeLogger logger, GeneratorContext context, String typeName)",
            "      throws UnableToCompleteException {",
            "    return null;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Module.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <generate-with class='test.rebind.MyGenerator'>",
            "    <when-type-assignable class='test.client.MyInterface'/>",
            "  </generate-with>",
            "</module>"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We can not remove GWT jars which we used for parsing, but we can make them empty, so reduce
   * size of testing workspace, because we usually use RAM disk for speed.
   */
  private static void makeGwtJarsEmpty(File gwtDirectory) throws IOException {
    makeGwtJarEmpty(new File(gwtDirectory, "gwt-user.jar"));
    makeGwtJarEmpty(new File(gwtDirectory, "gwt-dev.jar"));
  }

  /**
   * Makes single {@link File} empty.
   */
  private static void makeGwtJarEmpty(File file) throws IOException {
    // try delete
    try {
      FileUtils.forceDelete(file);
    } catch (Throwable e) {
    }
    // if not possible, make it empty
    IOUtils2.writeBytes(file, ArrayUtils.EMPTY_BYTE_ARRAY);
  }
}