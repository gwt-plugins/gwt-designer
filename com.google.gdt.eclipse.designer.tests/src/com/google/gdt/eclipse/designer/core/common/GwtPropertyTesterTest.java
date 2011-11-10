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
package com.google.gdt.eclipse.designer.core.common;

import com.google.gdt.eclipse.designer.common.GwtPropertyTester;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;

/**
 * Test for {@link GwtPropertyTester}.
 * 
 * @author scheglov_ke
 */
public class GwtPropertyTesterTest extends GwtModelTest {
  private static final GwtPropertyTester propertyTester = new GwtPropertyTester();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_unknownProperty() throws Exception {
    ILog log = DesignerPlugin.getDefault().getLog();
    ILogListener logListener = new ILogListener() {
      public void logging(IStatus status, String plugin) {
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
        assertEquals(IStatus.ERROR, status.getCode());
        assertInstanceOf(IllegalArgumentException.class, status.getException());
      }
    };
    //
    try {
      log.addLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(false);
      {
        Object receiver = m_testProject.getJavaProject();
        assertFalse(doTest(receiver, "no-such-property"));
      }
    } finally {
      log.removeLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_RESOURCE}.
   */
  public void test_isResource() throws Exception {
    // .project is resource
    {
      Object receiver = getFile(".project");
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_RESOURCE));
    }
    // IProject is resource
    {
      Object receiver = m_testProject.getProject();
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_RESOURCE));
    }
    // IJavaProject is adaptable to resource
    {
      Object receiver = m_testProject.getJavaProject();
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_RESOURCE));
    }
    // "this" is not a resource
    {
      Object receiver = this;
      assertFalse(doTest(receiver, GwtPropertyTester.PROPERTY_IS_RESOURCE));
    }
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_GWT_MODULE_ELEMENT}.
   */
  public void test_isModuleElement() throws Exception {
    // Module.gwt.xml is definitely part of its own module :-)
    {
      IFile file = getFileSrc("test/Module.gwt.xml");
      assertTrue(doTest(file, GwtPropertyTester.PROPERTY_IS_GWT_MODULE_ELEMENT));
    }
    // .classpath is in part of any GWT module
    {
      IFile file = getFile(".classpath");
      assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_GWT_MODULE_ELEMENT));
    }
    // not a resource
    {
      Object receiver = this;
      assertFalse(doTest(receiver, GwtPropertyTester.PROPERTY_IS_GWT_MODULE_ELEMENT));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java element
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test non-Java element (but valid property).
   */
  public void test_notJavaElement() throws Exception {
    Object receiver = this;
    assertFalse(doTest(receiver, GwtPropertyTester.PROPERTY_IS_ENTRY_POINT));
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_GWT_PROJECT_ELEMENT}.
   */
  public void test_isGwtProjectElement() throws Exception {
    // bad selection
    {
      Object receiver = null;
      assertFalse(doTest(receiver, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
    }
    // "src" folder, i.e. Java element, but adaptable to IResource
    {
      Object receiver = m_testProject.getSourceFolder();
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
    }
    // IMethod, pure Java element
    {
      IType entryPointType = m_testProject.getJavaProject().findType("test.client.Module");
      Object receiver = entryPointType.getMethods()[0];
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
    }
    // Module.gwt.xml is not Java element, but definitely part of GWT project
    {
      IFile file = getFileSrc("test/Module.gwt.xml");
      assertTrue(doTest(file, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
    }
    // no matter which file to use from GWT project
    {
      IFile file = getFile(".project");
      assertTrue(doTest(file, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
    }
    // create new Java project (not GWT project)
    {
      TestProject newProject = new TestProject("newProject");
      try {
        IFile file = newProject.getProject().getFile(".project");
        assertTrue(file.exists());
        assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_GWT_PROJECT_ELEMENT));
      } finally {
        newProject.dispose();
      }
    }
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_CLIENT_PACKAGE}.
   */
  public void test_isClientPackage() throws Exception {
    // EntryPoint is located in "client"
    {
      IFile file = getFileSrc("test/client/Module.java");
      assertTrue(doTest(file, GwtPropertyTester.PROPERTY_IS_CLIENT_PACKAGE));
    }
    // some Java file in "server"
    {
      IFile file =
          setFileContentSrc(
              "test/server/SomeType.java",
              getSourceDQ("package test.server;", "public class SomeType {", "}"));
      assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_CLIENT_PACKAGE));
    }
    // directly "test.client" package
    {
      Object receiver = m_testProject.getPackage("test.client");
      assertTrue(doTest(receiver, GwtPropertyTester.PROPERTY_IS_CLIENT_PACKAGE));
    }
    // no: Java project itself
    {
      Object receiver = m_testProject.getJavaProject();
      assertFalse(doTest(receiver, GwtPropertyTester.PROPERTY_IS_CLIENT_PACKAGE));
    }
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_ENTRY_POINT}.
   */
  public void test_isEntryPoint() throws Exception {
    // valid EntryPoint implementation
    {
      IFile file = getFileSrc("test/client/Module.java");
      assertTrue(doTest(file, GwtPropertyTester.PROPERTY_IS_ENTRY_POINT));
    }
    // just some Java file
    {
      IFile file =
          setFileContentSrc(
              "test/client/SomeType.java",
              getSourceDQ("package test.client;", "public class SomeType {", "}"));
      assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_ENTRY_POINT));
    }
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_REMOTE_SERVICE} .
   */
  @DisposeProjectAfter
  public void test_isRemoteService() throws Exception {
    // EntryPoint is not RemoteService
    {
      IFile file = getFileSrc("test/client/Module.java");
      assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_REMOTE_SERVICE));
    }
    // good
    {
      // create new RemoteService
      IFile serviceInterface =
          setFileContentSrc(
              "test/client/MyService.java",
              getSourceDQ(
                  "package test.client;",
                  "import com.google.gwt.user.client.rpc.RemoteService;",
                  "public interface MyService extends RemoteService {",
                  "}"));
      assertTrue(doTest(serviceInterface, GwtPropertyTester.PROPERTY_IS_REMOTE_SERVICE));
    }
  }

  /**
   * Test for {@link GwtPropertyTester#PROPERTY_IS_REMOTE_SERVICE_IMPL}.
   */
  @DisposeProjectAfter
  public void test_isRemoteServiceImpl() throws Exception {
    // EntryPoint is not RemoteService implementation
    {
      IFile file = getFileSrc("test/client/Module.java");
      assertFalse(doTest(file, GwtPropertyTester.PROPERTY_IS_REMOTE_SERVICE_IMPL));
    }
    // good
    {
      IType serviceImpl =
          createModelType(
              "test.server",
              "MyServiceImpl.java",
              getSourceDQ(
                  "package test.server;",
                  "import com.google.gwt.user.server.rpc.RemoteServiceServlet;",
                  "public class MyServiceImpl extends RemoteServiceServlet {",
                  "}"));
      waitForAutoBuild();
      assertTrue(doTest(serviceImpl, GwtPropertyTester.PROPERTY_IS_REMOTE_SERVICE_IMPL));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link GwtPropertyTester#test(Object, String, Object[], Object)} result.
   */
  private static boolean doTest(Object receiver, String property) {
    return propertyTester.test(receiver, property, null, null);
  }
}