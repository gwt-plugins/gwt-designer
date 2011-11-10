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
package com.google.gdt.eclipse.designer.smartgwt;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.smart.actions.ConfigureSmartGwtOperation;
import com.google.gdt.eclipse.designer.smart.actions.SmartGwtPropertyTester;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

/**
 * Test for {@link SmartGwtPropertyTester}.
 * 
 * @author scheglov_ke
 */
public class SmartGwtPropertyTesterTest extends GwtModelTest {
  private static final SmartGwtPropertyTester propertyTester = new SmartGwtPropertyTester();

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
  // Java element
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test non-Java element (but valid property).
   */
  public void test_notJavaElement() throws Exception {
    Object receiver = this;
    assertFalse(receiver, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
  }

  /**
   * Test for {@link SmartGwtPropertyTester#PROPERTY_IS_CONFIGURED}.
   */
  @DisposeProjectAfter
  public void test_isConfigured() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    IFile file = getFileSrc("/test/client/Module.java");
    // initially not configure
    {
      assertFalse(file, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // configure
    configureForSmartGWT_inTests(moduleFile);
    // true: for source file in module
    {
      assertTrue(file, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // true: for IMethod in source
    {
      IMethod receiver = m_javaProject.findType("test.client.Module").getMethods()[0];
      assertTrue(receiver, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // false: for IJavaProject
    {
      IJavaProject receiver = m_javaProject;
      assertFalse(receiver, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
  }

  /**
   * Test for {@link SmartGwtPropertyTester#PROPERTY_IS_CONFIGURED}.
   */
  @DisposeProjectAfter
  public void test_isConfigured_hasJar_butNotModule() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    String originalModule = getFileContent(moduleFile);
    // configure, but restore module
    configureForSmartGWT_inTests(moduleFile);
    setFileContent(moduleFile, originalModule);
    // ...so not configured
    {
      IFile file = getFileSrc("/test/client/Module.java");
      assertFalse(file, SmartGwtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
  }

  private void configureForSmartGWT_inTests(IFile moduleFile) throws Exception {
    m_testProject.addExternalJar(SmartGwtTests.LOCATION + "/smartgwt.jar");
    m_testProject.addExternalJar(SmartGwtTests.LOCATION + "/smartgwt-skins.jar");
    {
      ModuleDescription moduleDescription = getTestModuleDescription();
      new ConfigureSmartGwtOperation(moduleDescription, SmartGwtTests.LOCATION).run(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertTrue(Object receiver, String property) {
    boolean result = propertyTester.test(receiver, property, null, null);
    assertTrue(result);
  }

  private static void assertFalse(Object receiver, String property) {
    boolean result = propertyTester.test(receiver, property, null, null);
    assertFalse(result);
  }

  /**
   * @return the {@link SmartGwtPropertyTester#test(Object, String, Object[], Object)} result.
   */
  private static boolean doTest(Object receiver, String property) {
    return propertyTester.test(receiver, property, null, null);
  }
}