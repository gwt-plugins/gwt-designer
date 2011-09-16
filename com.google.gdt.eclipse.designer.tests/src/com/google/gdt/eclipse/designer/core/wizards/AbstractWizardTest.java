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
package com.google.gdt.eclipse.designer.core.wizards;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;

import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

/**
 * Abstract test for GWT wizards.
 * 
 * @author scheglov_ke
 */
public class AbstractWizardTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty("wbp.noGPE", "true");
    if (m_testProject == null) {
      do_projectCreate();
    }
    m_javaProject = m_testProject.getJavaProject();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    System.clearProperty("wbp.noGPE");
    do_projectDispose();
    Activator.getStore().setValue(Constants.P_GWT_LOCATION, GTestUtils.getLocation());
    Activator.getStore().setToDefault(Constants.P_WEB_FOLDER);
  }
}