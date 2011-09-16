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
package com.google.gdt.eclipse.designer.core;

import com.google.gdt.eclipse.designer.builders.participant.MyCompilationParticipant;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.Utils;

import static org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest.getFile;
import static org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest.getFolder;
import static org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest.setFileContent;
import static org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest.waitForAutoBuild;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities for GWT testing.
 * 
 * @author scheglov_ke
 */
public final class GTestUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT locations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location of default GWT.
   */
  public static String getLocation() {
    return getLocation_trunk();
  }

  /**
   * @return the location of GWT 2.0
   */
  public static String getLocation_20() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-2.0.4");
    StrValue kosta = new StrValue("scheglov-macpro", "/Users/scheglov/GWT/gwt-2.0.4");
    StrValue kosta_linux = new StrValue("scheglov Linux", "/home/scheglov/Work/GWT/gwt-2.0.4");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-2.0.4");
    return Expectations.get("C:/Work/GWT/gwt-2.0.4", new StrValue[]{
        kosta,
        kosta_linux,
        mitin,
        flanker_linux});
  }

  /**
   * @return the location of GWT 2.1.0
   */
  public static String getLocation_2_1_0() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-2.1.0");
    StrValue kosta = new StrValue("scheglov-macpro", "/Users/scheglov/GWT/gwt-2.1.0");
    StrValue kosta_linux = new StrValue("scheglov Linux", "/home/scheglov/Work/GWT/gwt-2.1.0");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-2.1.0");
    return Expectations.get("C:/Work/GWT/gwt-2.1.0", new StrValue[]{
        mitin,
        kosta,
        kosta_linux,
        flanker_linux});
  }

  /**
   * @return the location of GWT 2.1.1
   */
  public static String getLocation_21() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-2.1.1");
    StrValue kosta = new StrValue("scheglov-macpro", "/Users/scheglov/GWT/gwt-2.1.1");
    StrValue kosta_linux = new StrValue("scheglov Linux", "/home/scheglov/Work/GWT/gwt-2.1.1");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-2.1.1");
    return Expectations.get("C:/Work/GWT/gwt-2.1.1", new StrValue[]{
        mitin,
        kosta,
        kosta_linux,
        flanker_linux});
  }

  /**
   * @return the location of GWT 2.2
   */
  public static String getLocation_22() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-2.2-trunk");
    StrValue kosta_mac = new StrValue("scheglov-macpro", "/Users/scheglov/GWT/gwt-2.2.0");
    StrValue kosta_win = new StrValue("scheglov-win", "C:/Work/GWT/gwt-2.2.0");
    StrValue kosta_linux = new StrValue("scheglov Linux", "/home/scheglov/Dropbox/Work/gwt-0.0.0");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-2.2.0");
    return Expectations.get("C:/Work/GWT/gwt-2.2.0", new StrValue[]{
        mitin,
        kosta_mac,
        kosta_win,
        kosta_linux,
        flanker_linux});
  }

  /**
   * @return the location of GWT 2.3
   */
  public static String getLocation_23() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-2.3-trunk");
    StrValue kosta_mac = new StrValue("scheglov-macpro", "/Users/scheglov/GWT/gwt-2.3.0");
    StrValue kosta_win = new StrValue("scheglov-win", "C:/Work/GWT/gwt-2.3.0");
    StrValue kosta_linux = new StrValue("raziel Linux", "/home/scheglov/Work/GWT/gwt-2.3.0");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-2.3.0");
    return Expectations.get("C:/Work/GWT/gwt-2.3.0", new StrValue[]{
        mitin,
        kosta_mac,
        kosta_win,
        kosta_linux,
        flanker_linux});
  }

  /**
   * @return the location of the GWT from trunk.
   */
  public static String getLocation_trunk() {
    StrValue mitin = new StrValue("mitin-aa", "/Users/mitin_aa/gwt/gwt-trunk");
    StrValue kosta =
        new StrValue("scheglov-win", "C:/eclipseGWT/workspace/trunk/build/staging/gwt-0.0.0");
    StrValue flanker_linux = new StrValue("flanker-linux", "/home/flanker/Work/GWT/gwt-trunk");
    return Expectations.get("C:/Work/GWT/gwt-trunk", new StrValue[]{mitin, kosta, flanker_linux});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link TestProject} for using GWT.
   */
  public static void configure(TestProject testProject) throws Exception {
    configure(getLocation(), testProject);
  }

  /**
   * Configures given {@link TestProject} for using specified GWT location.
   */
  public static void configure(String gwtLocation, TestProject testProject) throws Exception {
    IProject project = testProject.getProject();
    // add jars
    testProject.addExternalJar(gwtLocation + "/gwt-user.jar");
    if (Utils.getVersion(project).isHigherOrSame(Utils.GWT_2_2)) {
      testProject.addExternalJar(gwtLocation + "/validation-api-1.0.0.GA.jar");
      testProject.addExternalJar(gwtLocation + "/validation-api-1.0.0.GA-sources.jar");
    }
    ProjectUtils.addNature(project, Constants.NATURE_ID);
    MyCompilationParticipant.ENABLED = false;
    // web folder
    {
      String webFolderName = WebUtils.getWebFolderName(project);
      project.getFolder(new Path(webFolderName)).create(true, true, null);
      project.getFolder(new Path(webFolderName + "/WEB-INF")).create(true, true, null);
    }
  }

  /**
   * Creates new GWT module.
   * 
   * @param moduleId
   *          the id of module, such as <code>com.mycompany.project.ImageViewer</code>.
   * 
   * @return the module {@link IFile}.
   */
  public static IFile createModule(TestProject testProject, String moduleId) throws Exception {
    IProject project = testProject.getProject();
    Assert.isTrue(moduleId.contains("."), "Given module name '%s' is not fully qualifed.", moduleId);
    String packageName = StringUtils.substringBeforeLast(moduleId, ".");
    String shortModuleName = StringUtils.substringAfterLast(moduleId, ".");
    String publicFolderPath = WebUtils.getWebFolderName(project) + "/";
    IPackageFragment modulePackage = testProject.getPackage(packageName);
    // *.gwt.xml
    IFile moduleFile;
    {
      String entryPointTypeName = packageName + ".client." + shortModuleName;
      String source =
          DesignerTestCase.getDoubleQuotes2(new String[]{
              "<module>",
              "  <inherits name='com.google.gwt.user.User'/>",
              //"  <inherits name='com.google.gwt.user.theme.standard.Standard'/>",
              "  <entry-point class='" + entryPointTypeName + "'/>",
              "</module>"});
      moduleFile = testProject.createFile(modulePackage, shortModuleName + ".gwt.xml", source);
    }
    // "client" package
    {
      IPackageFragment clientPackage = testProject.getPackage(packageName + ".client");
      testProject.createUnit(
          clientPackage,
          shortModuleName + ".java",
          DesignerTestCase.getSourceDQ(
              "package " + clientPackage.getElementName() + ";",
              "import com.google.gwt.core.client.EntryPoint;",
              "import com.google.gwt.user.client.ui.RootPanel;",
              "public class " + shortModuleName + " implements EntryPoint {",
              "  public void onModuleLoad() {",
              "    RootPanel rootPanel = RootPanel.get();",
              "  }",
              "}"));
    }
    // "public" resources
    {
      // HTML
      {
        String docType = "";
        if (Utils.getVersion(project).isHigherOrSame(Utils.GWT_2_0)) {
          docType += "<!doctype html>";
        }
        //
        String html =
            DesignerTestCase.getSourceDQ(
                docType,
                "<html>",
                "  <head>",
                "    <title>Wrapper HTML for GWT module</title>",
                "    <meta name='gwt:module' content='" + moduleId + "'/>",
                "    <link type='text/css' rel='stylesheet' href='" + shortModuleName + ".css'/>",
                "  </head>",
                "  <body>",
                "    <script language='javascript' src='" + moduleId + ".nocache.js'></script>",
                "    <iframe id='__gwt_historyFrame' style='width:0;height:0;border:0'></iframe>",
                "  </body>",
                "</html>");
        setFileContent(project, publicFolderPath + "/" + shortModuleName + ".html", html);
      }
      // CSS
      {
        String css =
            DesignerTestCase.getSourceDQ(
                "body {",
                "  background-color: white;",
                "  font: 18px Arial;",
                "}",
                ".gwt-Button {",
                "  overflow: visible;",
                "}",
                "td {",
                "  font: 18px Arial;",
                "  padding: 0px;",
                "}",
                "a {",
                "  color: darkblue;",
                "}",
                ".gwt-TabLayoutPanelTab {",
                "  float: left;",
                "  border: 1px solid #87b3ff;",
                "  padding: 2px;",
                "  cursor: hand;",
                "}",
                ".gwt-TabLayoutPanelTab-selected {",
                "  font-weight: bold;",
                "  background-color: #e8eef7;",
                "  cursor: default;",
                "}");
        setFileContent(project, publicFolderPath + "/" + shortModuleName + ".css", css);
      }
      // images
      {
        TestUtils.createImagePNG(testProject, publicFolderPath + "/1.png", 16, 16);
        TestUtils.createImagePNG(testProject, publicFolderPath + "/2.png", 16, 16);
      }
    }
    // web.xml
    {
      String content =
          DesignerTestCase.getSourceDQ(new String[]{
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<!DOCTYPE web-app",
              "  PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN'",
              "  'http://java.sun.com/dtd/web-app_2_3.dtd'>",
              "",
              "<web-app>",
              "",
              "  <!-- Default page to serve -->",
              "  <welcome-file-list>",
              "    <welcome-file>" + shortModuleName + ".html</welcome-file>",
              "  </welcome-file-list>",
              "",
              "</web-app>"});
      setFileContent(project, publicFolderPath + "/WEB-INF/web.xml", content);
    }
    // "server" package
    testProject.getPackage(packageName + ".server");
    // done
    return moduleFile;
  }

  /**
   * Configures project as Maven-like with GWT module.
   */
  public static void configureMavenProject() throws Exception {
    TestProject testProject = AbstractJavaProjectTest.m_testProject;
    // prepare Maven-like project
    getFolder("src").delete(true, null);
    getFolder("src/main/java");
    getFolder("src/main/resources");
    testProject.removeSourceFolder("/TestProject/src");
    testProject.addSourceFolder("/TestProject/src/main/java");
    testProject.addSourceFolder("/TestProject/src/main/resources");
    waitForAutoBuild();
    // create GWT module
    createModule(testProject, "test.Module");
    // move module file into "resources"
    {
      IFile moduleFile = getFile("src/main/java/test/Module.gwt.xml");
      getFolder("src/main/resources/test");
      moduleFile.move(new Path("/TestProject/src/main/resources/test/Module.gwt.xml"), true, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special GWT classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates types for <code>RemoteService</code> interface and implementation, in
   * <code>"test"</code> package, with name <code>"MyService"</code>.
   * 
   * @return the created {@link IType}'s for interface and implementation.
   */
  public static IType[] createTestService(AbstractJavaTest javaTest) throws Exception {
    return createTestService(javaTest, "MyService");
  }

  /**
   * Creates types for <code>RemoteService</code> interface and implementation, in
   * <code>"test"</code> package.
   * 
   * @return the created {@link IType}'s for interface and implementation.
   */
  public static IType[] createTestService(AbstractJavaTest javaTest, String serviceName)
      throws Exception {
    return createService(javaTest, "test", serviceName);
  }

  /**
   * Creates types for <code>RemoteService</code> interface and implementation.
   * 
   * @return the created {@link IType}'s for interface and implementation.
   */
  public static IType[] createService(AbstractJavaTest javaTest,
      String modulePackageName,
      String serviceName) throws Exception {
    String asyncName = serviceName + "Async";
    String interfacePackageName = modulePackageName + ".client";
    String implementationPackageName = modulePackageName + ".server";
    IType interfaceType =
        javaTest.createModelType(
            interfacePackageName,
            serviceName + ".java",
            DesignerTestCase.getSourceDQ(
                "package " + interfacePackageName + ";",
                "import com.google.gwt.core.client.GWT;",
                "import com.google.gwt.user.client.rpc.RemoteService;",
                "import com.google.gwt.user.client.rpc.ServiceDefTarget;",
                "public interface " + serviceName + " extends RemoteService {",
                "  public static class Util {",
                "    private static " + asyncName + " instance;",
                "    public static " + asyncName + " getInstance() {",
                "      if (instance == null) {",
                "        instance = (" + asyncName + ") GWT.create(" + serviceName + ".class);",
                "        ServiceDefTarget target = (ServiceDefTarget) instance;",
                "        target.setServiceEntryPoint(GWT.getModuleBaseURL() + '"
                    + serviceName
                    + "');",
                "      }",
                "      return instance;",
                "    }",
                "  }",
                "}"));
    IType implementationType =
        javaTest.createModelType(
            implementationPackageName,
            serviceName + "Impl.java",
            DesignerTestCase.getSourceDQ(
                "package " + implementationPackageName + ";",
                "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
                "import " + interfacePackageName + "." + serviceName + ";",
                "public class "
                    + serviceName
                    + "Impl extends RemoteServiceServlet implements "
                    + serviceName
                    + " {",
                "}"));
    return new IType[]{interfaceType, implementationType};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Markers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return markers set by {@link MyCompilationParticipant}.
   */
  public static IMarker[] getMyMarkers(IFile file) throws CoreException {
    return file.findMarkers(MyCompilationParticipant.MARKER_ID, false, IResource.DEPTH_ZERO);
  }
}
