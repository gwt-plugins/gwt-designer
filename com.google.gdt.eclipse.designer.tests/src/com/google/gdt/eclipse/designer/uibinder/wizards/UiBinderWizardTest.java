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
package com.google.gdt.eclipse.designer.uibinder.wizards;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.jdt.core.IPackageFragment;

/**
 * Tests for {@link UiBinderWizard}.
 * 
 * @author scheglov_ke
 */
public class UiBinderWizardTest extends UiBinderModelTest {
  protected IPackageFragment m_packageFragment;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_packageFragment = m_testProject.getPackage("test.client");
  }

  @Override
  protected void tearDown() throws Exception {
    {
      waitEventLoop(0);
      TestUtils.closeAllEditors();
      waitEventLoop(0);
    }
    super.tearDown();
  }
}