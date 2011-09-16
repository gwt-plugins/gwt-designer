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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.smart.IExceptionConstants;
import com.google.gdt.eclipse.designer.smartgwt.SmartGwtTests;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.jdt.core.IClasspathEntry;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for SmartGWT version.
 * 
 * @author scheglov_ke
 */
public class VersionTest extends SmartGwtModelTest {
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
  @DisposeProjectAfter
  public void test_goodVersion() throws Exception {
    parseJavaInfo(
        "// filler filler filler filler filler",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel.get();",
        "  }",
        "}");
  }

  /**
   * Some users try to use old version of SmartGWT, but tell us that they use new version. We should
   * check version and fail with good message.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48002
   */
  @DisposeProjectAfter
  public void test_oldVersion() throws Exception {
    dontUseSharedGWTState();
    // replace SmartGWT jar, use old version
    {
      IClasspathEntry[] entries = m_javaProject.getRawClasspath();
      for (int i = 0; i < entries.length; i++) {
        IClasspathEntry entry = entries[i];
        if (entry.getPath().toString().endsWith("smartgwt.jar")) {
          entries = (IClasspathEntry[]) ArrayUtils.remove(entries, i);
          break;
        }
      }
      m_javaProject.setRawClasspath(entries, null);
      waitForAutoBuild();
      // add jar for version 2.1
      m_testProject.addExternalJar(SmartGwtTests.LOCATION_OLD + "/smartgwt.jar");
    }
    // try to parse, failure expected
    try {
      parseJavaInfo(
          "// filler filler filler filler filler",
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel.get();",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.INCORRECT_VERSION, e.getCode());
    }
  }

  @DisposeProjectAfter
  public void test_hasSmartGWTjar_butNoModule() throws Exception {
    dontUseSharedGWTState();
    do_projectDispose();
    // create standard GWT module
    {
      do_projectCreate();
      GTestUtils.configure(getGWTLocation_forProject(), m_testProject);
      GTestUtils.createModule(m_testProject, "test.Module");
    }
    // add SmartGWT jar, but don't import module
    m_testProject.addExternalJar(SmartGwtTests.LOCATION_OLD + "/smartgwt.jar");
    // parse, no exception, even for old SmartGWT version
    parseJavaInfo(
        "// filler filler filler filler filler",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel.get();",
        "  }",
        "}");
  }
}