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
package com.google.gdt.eclipse.designer.gpe;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

/**
 * Test for {@link Utils}.
 * 
 * @author scheglov_ke
 */
public class EntryPointsModuleFilterTest extends AbstractJavaTest {
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
  public void test_0() throws Exception {
    IFolder folder = getFolderSrc("test");
    // prepare modules
    getTestModuleFile().delete(true, null);
    setFileContentSrc("test/aModule.gwt.xml", "<module/>");
    setFileContentSrc(
        "test/bModule.gwt.xml",
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <entry-point class='no.Matter'/>",
            "</module>"));
    // not GPE project
    {
      ModuleDescription module = Utils.getSingleModule(folder);
      assertEquals("test.aModule", module.getId());
    }
    // make it GPE project
    ProjectUtils.addNature(m_project, Constants.GPE_NATURE_ID);
    {
      ModuleDescription module = Utils.getSingleModule(folder);
      assertEquals("test.bModule", module.getId());
    }
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
}