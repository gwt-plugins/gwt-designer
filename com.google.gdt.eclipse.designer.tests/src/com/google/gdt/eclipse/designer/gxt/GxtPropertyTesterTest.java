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
package com.google.gdt.eclipse.designer.gxt;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.gxt.actions.ConfigureExtGwtOperation;
import com.google.gdt.eclipse.designer.gxt.actions.GxtPropertyTester;
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
 * Test for {@link GxtPropertyTester}.
 * 
 * @author scheglov_ke
 */
public class GxtPropertyTesterTest extends GwtModelTest {
  private static final GxtPropertyTester propertyTester = new GxtPropertyTester();

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
    assertFalse(receiver, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
  }

  /**
   * Test for {@link GxtPropertyTester#PROPERTY_IS_CONFIGURED}.
   */
  @DisposeProjectAfter
  public void test_isConfigured() throws Exception {
    ModuleDescription moduleDescription = getTestModuleDescription();
    IFile file = getFileSrc("/test/client/Module.java");
    // initially not configure
    {
      assertFalse(file, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // configure
    {
      m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION + "/gxt.jar");
      new ConfigureExtGwtOperation(moduleDescription, ExtGwtTests.GXT_LOCATION).run(null);
    }
    // true: for source file in module
    {
      assertTrue(file, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // true: for IMethod in source
    {
      IMethod receiver = m_javaProject.findType("test.client.Module").getMethods()[0];
      assertTrue(receiver, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
    // false: for IJavaProject
    {
      IJavaProject receiver = m_javaProject;
      assertFalse(receiver, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
    }
  }

  /**
   * Test for {@link GxtPropertyTester#PROPERTY_IS_CONFIGURED}.
   */
  @DisposeProjectAfter
  public void test_isConfigured_hasJar_butNotModule() throws Exception {
    ModuleDescription moduleDescription = getTestModuleDescription();
    IFile moduleFile = getTestModuleFile();
    String originalModule = getFileContent(moduleFile);
    // configure, but restore module
    m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION + "/gxt.jar");
    new ConfigureExtGwtOperation(moduleDescription, ExtGwtTests.GXT_LOCATION).run(null);
    setFileContent(moduleFile, originalModule);
    // ...so not configured
    {
      IFile file = getFileSrc("/test/client/Module.java");
      assertFalse(file, GxtPropertyTester.PROPERTY_IS_CONFIGURED);
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
   * @return the {@link GxtPropertyTester#test(Object, String, Object[], Object)} result.
   */
  private static boolean doTest(Object receiver, String property) {
    return propertyTester.test(receiver, property, null, null);
  }
}