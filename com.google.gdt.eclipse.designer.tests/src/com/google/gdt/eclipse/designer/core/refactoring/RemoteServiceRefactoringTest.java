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
package com.google.gdt.eclipse.designer.core.refactoring;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.refactoring.RemoteServiceDeleteParticipant;
import com.google.gdt.eclipse.designer.refactoring.RemoteServiceImplMoveParticipant;
import com.google.gdt.eclipse.designer.refactoring.RemoteServiceImplRenameParticipant;
import com.google.gdt.eclipse.designer.refactoring.RemoteServiceMoveParticipant;
import com.google.gdt.eclipse.designer.refactoring.RemoteServiceRenameParticipant;

import org.eclipse.wb.tests.designer.core.RefactoringTestUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 * Test <code>RemoteService</code> interface related refactorings.
 * 
 * @author scheglov_ke
 */
public class RemoteServiceRefactoringTest extends AbstractRefactoringTest {
  private IType m_serviceType;
  private IType m_asyncType;
  private IType m_implType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExceptionsListener();
    m_serviceType = m_javaProject.findType("test.client.MyService");
    m_asyncType = m_javaProject.findType("test.client.MyServiceAsync");
    m_implType = m_javaProject.findType("test.server.MyServiceImpl");
    assertNotNull(m_serviceType);
    assertNotNull(m_asyncType);
    assertNotNull(m_implType);
  }

  @Override
  protected void prepareTestProject() throws Exception {
    super.prepareTestProject();
    // first build, required for subsequent RemoteService building
    waitForAutoBuild();
    // create and reference RemoteService
    GTestUtils.createTestService(this);
    setFileContentSrc(
        "/test/Module.gwt.xml",
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <servlet path='/MyService' class='test.server.MyServiceImpl'/>",
            "</module>"));
    setFileContent(
        "war/WEB-INF/web.xml",
        getSourceDQ(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>MyService</servlet-name>",
            "    <servlet-class>test.server.MyServiceImpl</servlet-class>",
            "  </servlet>",
            "  <servlet-mapping>",
            "    <servlet-name>MyService</servlet-name>",
            "    <url-pattern>/test.Test/MyService</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"));
    waitForAutoBuild();
  }

  @Override
  protected void tearDown() throws Exception {
    removeExceptionsListener();
    m_serviceType = null;
    m_asyncType = null;
    m_implType = null;
    super.tearDown();
    assertNoLoggedExceptions();
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
  // Interface rename
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RemoteServiceRenameParticipant}.<br>
   * No utility class for creating "Async" interface. This means that during refactoring there are
   * no conflict between two changes to same {@link IFile} (with <code>RemoteService</code>
   * interface, because <code>Util</code> is in same file).
   */
  public void test_renameRemoteService_noUtil() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "}"));
    waitForAutoBuild();
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // do rename
    RefactoringTestUtils.renameType(m_serviceType, "YourService");
    waitForAutoBuild();
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.YourService"));
    assertNotNull(m_javaProject.findType("test.client.YourServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.YourServiceImpl"));
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface YourService extends RemoteService {",
            "}"),
        getFileContentSrc("test/client/YourService.java"));
    assertYourServiceAsync();
    assertYourServiceImpl();
    assertYourServiceModule();
    assertYourServiceWeb();
  }

  /**
   * Test for {@link RemoteServiceRenameParticipant}.<br>
   * With utility class for creating "Async" interface.
   */
  public void test_renameRemoteService_withUtil() throws Exception {
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // do rename
    RefactoringTestUtils.renameType(m_serviceType, "YourService");
    waitForAutoBuild();
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.YourService"));
    assertNotNull(m_javaProject.findType("test.client.YourServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.YourServiceImpl"));
    assertYourServiceInterface();
    assertYourServiceAsync();
    assertYourServiceImpl();
    assertYourServiceModule();
    assertYourServiceWeb();
  }

  /**
   * Test for {@link RemoteServiceRenameParticipant}.<br>
   * <code>RemoteService</code> exists, but its "Impl" is not referenced in module file.
   */
  public void test_renameRemoteService_notInModule() throws Exception {
    setEmptyModule();
    setEmptyWeb();
    waitForAutoBuild();
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // do rename
    RefactoringTestUtils.renameType(m_serviceType, "YourService");
    waitForAutoBuild();
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.YourService"));
    assertNotNull(m_javaProject.findType("test.client.YourServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.YourServiceImpl"));
    assertYourServiceInterface();
    assertYourServiceAsync();
    assertYourServiceImpl();
    assertEmptyModule();
    assertEmptyWeb();
  }

  /**
   * Test for {@link RemoteServiceRenameParticipant}.<br>
   * <code>RemoteService</code> exists, but no corresponding "Impl" type. So, interface and "Async"
   * can be renamed, but name of servlet in <code>Util</code> is left same as it was.
   */
  public void test_renameRemoteService_noImpl() throws Exception {
    // remove "Impl"
    setEmptyModule();
    setEmptyWeb();
    getFileSrc("test/server/MyServiceImpl.java").delete(true, null);
    waitForAutoBuild();
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // do rename
    RefactoringTestUtils.renameType(m_serviceType, "YourService");
    waitForAutoBuild();
    //waitEventLoop(1000 * 1000);
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.YourService"));
    assertNotNull(m_javaProject.findType("test.client.YourServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertNull(m_javaProject.findType("test.server.YourServiceImpl"));
    assertYourServiceAsync();
    assertYourServiceInterface("MyService");
    assertEmptyModule();
    assertEmptyWeb();
  }

  private void setEmptyModule() throws Exception {
    setFileContentSrc(
        "/test/Module.gwt.xml",
        getSourceDQ("<module>", "  <inherits name='com.google.gwt.user.User'/>", "</module>"));
  }

  private void setEmptyWeb() throws Exception {
    setFileContent("war/WEB-INF/web.xml", "<web-app/>\n");
  }

  private void assertEmptyModule() throws Exception {
    assertEquals(
        getSourceDQ("<module>", "  <inherits name='com.google.gwt.user.User'/>", "</module>"),
        getFileContentSrc("/test/Module.gwt.xml"));
  }

  private void assertEmptyWeb() throws Exception {
    assertEquals("<web-app/>\n", getFileContent("war/WEB-INF/web.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Interface move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RemoteServiceMoveParticipant}.
   */
  public void test_moveRemoteService_withUtil() throws Exception {
    String serviceSource = getFileContentSrc("test/client/MyService.java");
    // do move
    IPackageFragment newPackage = m_testProject.getPackage("test.client.services");
    RefactoringTestUtils.moveType(m_serviceType, newPackage);
    waitForAutoBuild();
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.services.MyService"));
    assertNotNull(m_javaProject.findType("test.client.services.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertEquals(
        serviceSource.replaceAll("package test.client;", "package test.client.services;"),
        getFileContentSrc("test/client/services/MyService.java"));
    assertEquals(
        getSourceDQ("package test.client.services;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/services/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "", // because of organizing imports (com.* is separate imports category)
            "import test.client.services.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
    assertMyServiceModule();
    assertMyServiceWeb();
  }

  /**
   * Test for {@link RemoteServiceMoveParticipant}.
   */
  public void test_moveRemoteService_noUtil() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "}"));
    String serviceSource = getFileContentSrc("test/client/MyService.java");
    // do move
    IPackageFragment newPackage = m_testProject.getPackage("test.client.services");
    RefactoringTestUtils.moveType(m_serviceType, newPackage);
    waitForAutoBuild();
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.services.MyService"));
    assertNotNull(m_javaProject.findType("test.client.services.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertEquals(
        serviceSource.replaceAll("package test.client;", "package test.client.services;"),
        getFileContentSrc("test/client/services/MyService.java"));
    assertEquals(
        getSourceDQ("package test.client.services;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/services/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "", // because of organizing imports (com.* is separate imports category)
            "import test.client.services.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
    assertMyServiceModule();
    assertMyServiceWeb();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Interface delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RemoteServiceDeleteParticipant}.
   */
  public void test_deleteRemoteService() throws Exception {
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertMyServiceImpl();
    assertMyServiceModule();
    assertMyServiceWeb();
    // do delete
    RefactoringTestUtils.deleteType(m_serviceType);
    waitForAutoBuild();
    // checks after refactoring
    assertNull(m_javaProject.findType("test.client.MyService"));
    assertNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertEmptyModule();
    assertEmptyWeb();
  }

  /**
   * Test for {@link RemoteServiceDeleteParticipant}.
   */
  public void test_deleteRemoteService_whenNoImpl() throws Exception {
    m_implType.getCompilationUnit().delete(true, null);
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertMyServiceModule();
    assertMyServiceWeb();
    // do delete
    RefactoringTestUtils.deleteType(m_serviceType);
    waitForAutoBuild();
    // checks after refactoring
    assertNull(m_javaProject.findType("test.client.MyService"));
    assertNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // no Impl, so can not remove it from module
    assertMyServiceModule();
    assertMyServiceWeb();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation rename/move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RemoteServiceImplRenameParticipant}.
   */
  public void test_renameRemoteServiceImpl() throws Exception {
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertMyServiceImpl();
    assertMyServiceModule();
    assertMyServiceWeb();
    // do rename
    {
      RefactoringTestUtils.renameType(m_implType, "YourServiceImpl");
      waitForAutoBuild();
    }
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertNotNull(m_javaProject.findType("test.server.YourServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class YourServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/YourServiceImpl.java"));
    assertEquals(
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <servlet path='/MyService' class='test.server.YourServiceImpl'/>",
            "</module>"),
        getFileContentSrc("/test/Module.gwt.xml"));
    assertEquals(
        getSourceDQ(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>MyService</servlet-name>",
            "    <servlet-class>test.server.YourServiceImpl</servlet-class>",
            "  </servlet>",
            "  <servlet-mapping>",
            "    <servlet-name>MyService</servlet-name>",
            "    <url-pattern>/test.Test/MyService</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        getFileContent("war/WEB-INF/web.xml"));
  }

  /**
   * Test for {@link RemoteServiceImplMoveParticipant}.
   */
  public void test_moveRemoteServiceImpl() throws Exception {
    // check initial state
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNotNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertMyServiceImpl();
    assertMyServiceModule();
    assertMyServiceWeb();
    // do move
    {
      IPackageFragment newPackage = m_testProject.getPackage("test.server.services");
      RefactoringTestUtils.moveType(m_implType, newPackage);
      waitForAutoBuild();
    }
    // checks after refactoring
    assertNotNull(m_javaProject.findType("test.client.MyService"));
    assertNotNull(m_javaProject.findType("test.client.MyServiceAsync"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    assertNotNull(m_javaProject.findType("test.server.services.MyServiceImpl"));
    assertMyServiceInterface();
    assertMyServiceAsync();
    assertEquals(
        getSourceDQ(
            "package test.server.services;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/services/MyServiceImpl.java"));
    assertEquals(
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <servlet path='/MyService' class='test.server.services.MyServiceImpl'/>",
            "</module>"),
        getFileContentSrc("/test/Module.gwt.xml"));
    assertEquals(
        getSourceDQ(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>MyService</servlet-name>",
            "    <servlet-class>test.server.services.MyServiceImpl</servlet-class>",
            "  </servlet>",
            "  <servlet-mapping>",
            "    <servlet-name>MyService</servlet-name>",
            "    <url-pattern>/test.Test/MyService</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        getFileContent("war/WEB-INF/web.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions: initial state
  //
  ////////////////////////////////////////////////////////////////////////////
  private void assertMyServiceInterface() throws Exception {
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.core.client.GWT;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "import com.google.gwt.user.client.rpc.ServiceDefTarget;",
            "public interface MyService extends RemoteService {",
            "  public static class Util {",
            "    private static MyServiceAsync instance;",
            "    public static MyServiceAsync getInstance() {",
            "      if (instance == null) {",
            "        instance = (MyServiceAsync) GWT.create(MyService.class);",
            "        ServiceDefTarget target = (ServiceDefTarget) instance;",
            "        target.setServiceEntryPoint(GWT.getModuleBaseURL() + 'MyService');",
            "      }",
            "      return instance;",
            "    }",
            "  }",
            "}"),
        getFileContentSrc("test/client/MyService.java"));
  }

  private void assertMyServiceAsync() throws Exception {
    assertEquals(
        getSourceDQ("package test.client;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
  }

  private void assertMyServiceImpl() throws Exception {
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  private void assertMyServiceModule() throws Exception {
    assertEquals(
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <servlet path='/MyService' class='test.server.MyServiceImpl'/>",
            "</module>"),
        getFileContentSrc("/test/Module.gwt.xml"));
  }

  private void assertMyServiceWeb() throws Exception {
    assertEquals(
        getSourceDQ(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>MyService</servlet-name>",
            "    <servlet-class>test.server.MyServiceImpl</servlet-class>",
            "  </servlet>",
            "  <servlet-mapping>",
            "    <servlet-name>MyService</servlet-name>",
            "    <url-pattern>/test.Test/MyService</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        getFileContent("war/WEB-INF/web.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions: after operation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void assertYourServiceInterface() throws Exception {
    assertYourServiceInterface("YourService");
  }

  private void assertYourServiceInterface(String servletName) throws Exception {
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.core.client.GWT;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "import com.google.gwt.user.client.rpc.ServiceDefTarget;",
            "public interface YourService extends RemoteService {",
            "  public static class Util {",
            "    private static YourServiceAsync instance;",
            "    public static YourServiceAsync getInstance() {",
            "      if (instance == null) {",
            "        instance = (YourServiceAsync) GWT.create(YourService.class);",
            "        ServiceDefTarget target = (ServiceDefTarget) instance;",
            "        target.setServiceEntryPoint(GWT.getModuleBaseURL() + '" + servletName + "');",
            "      }",
            "      return instance;",
            "    }",
            "  }",
            "}"),
        getFileContentSrc("test/client/YourService.java"));
  }

  private void assertYourServiceAsync() throws Exception {
    assertEquals(
        getSourceDQ("package test.client;", "public interface YourServiceAsync {", "}"),
        getFileContentSrc("test/client/YourServiceAsync.java"));
  }

  private void assertYourServiceImpl() throws Exception {
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.YourService;",
            "public class YourServiceImpl extends RemoteServiceServlet implements YourService {",
            "}"),
        getFileContentSrc("test/server/YourServiceImpl.java"));
  }

  private void assertYourServiceModule() throws Exception {
    assertEquals(
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <servlet path='/YourService' class='test.server.YourServiceImpl'/>",
            "</module>"),
        getFileContentSrc("/test/Module.gwt.xml"));
  }

  private void assertYourServiceWeb() throws Exception {
    assertEquals(
        getSourceDQ(
            "<web-app>",
            "  <servlet>",
            "    <servlet-name>YourService</servlet-name>",
            "    <servlet-class>test.server.YourServiceImpl</servlet-class>",
            "  </servlet>",
            "  <servlet-mapping>",
            "    <servlet-name>YourService</servlet-name>",
            "    <url-pattern>/test.Test/YourService</url-pattern>",
            "  </servlet-mapping>",
            "</web-app>"),
        getFileContent("war/WEB-INF/web.xml"));
  }
}