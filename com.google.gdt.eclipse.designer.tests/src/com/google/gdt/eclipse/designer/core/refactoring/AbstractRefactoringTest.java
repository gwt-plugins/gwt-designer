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
package com.google.gdt.eclipse.designer.core.refactoring;

import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.core.GTestUtils;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IType;

/**
 * Abstract test for GWT refactorings.
 * 
 * @author scheglov_ke
 */
public abstract class AbstractRefactoringTest extends AbstractJavaTest {
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
      ProjectUtils.addNature(m_project, Constants.GPE_NATURE_ID);
    }
    prepareTestProject();
  }

  /**
   * Creates project and prepares it for tests.
   */
  protected void prepareTestProject() throws Exception {
    GTestUtils.createModule(m_testProject, "test.Module");
  }

  @Override
  protected void tearDown() throws Exception {
    // remove module
    if (m_testProject != null) {
      IFolder folder = m_testProject.getJavaProject().getProject().getFolder("src");
      deleteFiles(folder);
    }
    // remove other things
    super.tearDown();
  }

  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates empty class <code>test.client.Test</code>.
   */
  protected final IType createEmptyTestType() throws Exception {
    return createModelType(
        "test.client",
        "Test.java",
        getSourceDQ("package test.client;", "public class Test {", "}"));
  }
}