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

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;

import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

/**
 * There was problem that using first version 2.2 and then version 2.3 cause exception because of
 * using 2.2 for parsing 2.3 modules (with new elements and attributes).
 * 
 * @author scheglov_ke
 */
public class VersionsTest extends GwtModelTest {
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
  public void test_version22() throws Exception {
    configureFor(GTestUtils.getLocation_22());
    // try to parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  @DisposeProjectAfter
  public void test_version23() throws Exception {
    configureFor(GTestUtils.getLocation_23());
    // try to parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureFor(String location) throws Exception {
    do_projectDispose();
    do_projectCreate();
    GTestUtils.configure(location, m_testProject);
    GTestUtils.createModule(m_testProject, "test.Module");
  }
}