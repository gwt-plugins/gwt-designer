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
package com.google.gdt.eclipse.designer.core.builders;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.builders.GwtBuilder;
import com.google.gdt.eclipse.designer.builders.participant.MyCompilationParticipant;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;

import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GwtBuilder}.
 * 
 * @author scheglov_ke
 */
public class GwtBuilderTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // ensure project
    if (m_testProject == null) {
      do_projectCreate();
      GTestUtils.configure(GTestUtils.getLocation(), m_testProject);
    }
    // create module
    {
      GTestUtils.createModule(m_testProject, "test.Module");
      waitForAutoBuild();
    }
    // enable builder
    Activator.getDefault().getPreferenceStore().setValue(
        Constants.P_BUILDER_CHECK_CLIENT_CLASSPATH,
        true);
  }

  @Override
  protected void tearDown() throws Exception {
    Activator.getDefault().getPreferenceStore().setToDefault(
        Constants.P_BUILDER_CHECK_CLIENT_CLASSPATH);
    // remove module
    if (m_testProject != null) {
      IFolder folder = m_testProject.getJavaProject().getProject().getFolder("src");
      deleteFiles(folder);
    }
    // remove other things
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
  // RemoteService
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_emptyService() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ("package test.client;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  public void test_noImpl() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "}"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ("package test.client;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    // no Impl initially, so no it now too
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
  }

  /**
   * Fields and inner types should be removed.
   */
  public void test_serviceWithExtraElements() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  int MAGIC = 5;",
            "  public static class Utils {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ("package test.client;", "public interface MyServiceAsync {", "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Fields and inner types should be removed.
   */
  public void test_serviceKeepCustomImports() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSource(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "import com.google.gwt.dom.client.Document;",
            "public interface MyService extends RemoteService {",
            "  Document getDocument();",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSource(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSource(
            "package test.client;",
            "import com.google.gwt.dom.client.Document;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getDocument(AsyncCallback<Document> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSource(
            "package test.server;",
            "import com.google.gwt.dom.client.Document;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public Document getDocument() {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Annotations should be removed.
   */
  public void test_serviceWithAnnotation_noJavadoc() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;",
            "@RemoteServiceRelativePath('MyService')",
            "public interface MyService extends RemoteService {",
            "  // filler filler filler",
            "}"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSource(
            "package test.client;",
            "public interface MyServiceAsync {",
            "  // filler filler filler",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
  }

  /**
   * Annotations should be removed.
   */
  public void test_serviceWithAnnotation_withJavadoc() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;",
            "/**",
            "  * Some javadoc.",
            "  */",
            "@RemoteServiceRelativePath('MyService')",
            "public interface MyService extends RemoteService {",
            "}"));
    assertNull(m_javaProject.findType("test.server.MyServiceImpl"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "/**",
            "  * Some javadoc.",
            "  */",
            "public interface MyServiceAsync {",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
  }

  /**
   * Single method that returns {@link String}.
   */
  public void test_serviceMethod_String() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  String getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getValue(AsyncCallback<String> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public String getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that returns <code>int</code>.
   */
  public void test_serviceMethod_int() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  int getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getValue(AsyncCallback<Integer> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public int getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return 0;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that returns <code>int</code>.
   */
  public void test_serviceMethod_Request() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  /** @wbp.gwt.Request */",
            "  int getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.http.client.Request;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  Request getValue(AsyncCallback<Integer> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public int getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return 0;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that returns <code>float</code>.
   */
  public void test_serviceMethod_float() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  float getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getValue(AsyncCallback<Float> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public float getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return 0;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that returns <code>void</code>.
   */
  public void test_serviceMethod_void() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  void getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getValue(AsyncCallback<Void> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public void getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    ",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that returns {@link List} of {@link String}'s.
   */
  public void test_serviceMethod_ListOfString() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  List<String> getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  void getValue(AsyncCallback<List<String>> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import java.util.List;",
            "",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public List<String> getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that uses deprecated <code>"@gwt.typeArgs"</code>.
   */
  public void test_serviceMethod_gwtTypeArgs() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  /**",
            "  * Some description.",
            "  * @gwt.typeArgs <java.lang.String>",
            "  */",
            "  List getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  /**",
            "  * Some description.",
            "  */",
            "  void getValue(AsyncCallback<List> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import java.util.List;",
            "",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public List getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that uses <code>"@return"</code> tag without comment.
   */
  public void test_serviceMethod_returnWithoutComment() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  /**",
            "  * Some description.",
            "  * @return",
            "  */",
            "  String getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  /**",
            "  * Some description.",
            "  */",
            "  void getValue(AsyncCallback<String> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public String getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  /**
   * Single method that uses <code>"@return"</code> tag with comment.
   */
  public void test_serviceMethod_returnWithComment() throws Exception {
    setFileContentSrc(
        "test/client/MyService.java",
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface MyService extends RemoteService {",
            "  /**",
            "  * Some description.",
            "  * @return the message",
            "  */",
            "  String getValue() throws Exception;",
            "}"));
    setFileContentSrc(
        "test/server/MyServiceImpl.java",
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "}"));
    // after building Async should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import java.util.List;",
            "import com.google.gwt.user.client.rpc.AsyncCallback;",
            "public interface MyServiceAsync {",
            "  /**",
            "  * Some description.",
            "  * @param callback the callback to return the message",
            "  */",
            "  void getValue(AsyncCallback<String> callback);",
            "}"),
        getFileContentSrc("test/client/MyServiceAsync.java"));
    assertEquals(
        getSourceDQ(
            "package test.server;",
            "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
            "import test.client.MyService;",
            "public class MyServiceImpl extends RemoteServiceServlet implements MyService {",
            "",
            "  @Override",
            "  public String getValue() throws Exception {",
            "    // TODO Auto-generated method stub",
            "    return null;",
            "  }",
            "}"),
        getFileContentSrc("test/server/MyServiceImpl.java"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inheritance for RemoteService
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * See http://fogbugz.instantiations.com/fogbugz/default.php?37950
   */
  public void test_serviceInheritance_samePackage() throws Exception {
    setFileContentSrc(
        "test/client/SuperService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface SuperService extends RemoteService {",
            "}"));
    // after building Async for "Super" should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ("package test.client;", "public interface SuperServiceAsync {", "}"),
        getFileContentSrc("test/client/SuperServiceAsync.java"));
    // now create "Sub" service and validate its Sync
    setFileContentSrc(
        "test/client/SubService.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface SubService extends SuperService {",
            "}"));
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "public interface SubServiceAsync extends SuperServiceAsync {",
            "}"),
        getFileContentSrc("test/client/SubServiceAsync.java"));
  }

  /**
   * See http://fogbugz.instantiations.com/fogbugz/default.php?37950
   */
  public void test_serviceInheritance_differentPackage() throws Exception {
    setFileContentSrc(
        "test/client/sup/SuperService.java",
        getSourceDQ(
            "package test.client.sup;",
            "import com.google.gwt.user.client.rpc.RemoteService;",
            "public interface SuperService extends RemoteService {",
            "}"));
    // after building Async for "Super" should exist
    waitForAutoBuild();
    assertEquals(
        getSourceDQ("package test.client.sup;", "public interface SuperServiceAsync {", "}"),
        getFileContentSrc("test/client/sup/SuperServiceAsync.java"));
    // now create "Sub" service and validate its Sync
    setFileContentSrc(
        "test/client/SubService.java",
        getSourceDQ(
            "package test.client;",
            "import test.client.sup.SuperService;",
            "public interface SubService extends SuperService {",
            "}"));
    waitForAutoBuild();
    assertEquals(
        getSourceDQ(
            "package test.client;",
            "import test.client.sup.SuperService;",
            "import test.client.sup.SuperServiceAsync;",
            "public interface SubServiceAsync extends SuperServiceAsync {",
            "}"),
        getFileContentSrc("test/client/SubServiceAsync.java"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rebuild on *.gwt.xml change
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When module file changed, this may cause that some types from GWT inherited GWT modules may
   * become valid/invalid. So, we should rebuild to allow {@link MyCompilationParticipant} to
   * validate.
   */
  public void test_rebuildOnModuleFileChange() throws Exception {
    MyCompilationParticipant.ENABLED = true;
    try {
      IFile sourceFile =
          setFileContentSrc(
              "test/client/Test.java",
              getSourceDQ(
                  "package test.client;",
                  "public class Test {",
                  "  public Test() {",
                  "    com.google.gwt.xml.client.XMLParser parser = null;",
                  "  }",
                  "}"));
      // module "com.google.gwt.xml.XML" is not inherited, so we have marker
      {
        waitForAutoBuild();
        IMarker[] markers = GTestUtils.getMyMarkers(sourceFile);
        assertThat(markers).hasSize(1);
      }
      // change Module.gwt.xml, include module "com.google.gwt.xml.XML", so source become valid
      {
        setFileContentSrc("test/Module.gwt.xml", getDoubleQuotes2(new String[]{
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <entry-point class='test.client.Module'/>",
            "  <inherits name='com.google.gwt.xml.XML'/>",
            "</module>"}));
        waitForAutoBuild();
        IMarker[] markers = GTestUtils.getMyMarkers(sourceFile);
        assertThat(markers).isEmpty();
      }
    } finally {
      MyCompilationParticipant.ENABLED = false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test down
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }
}