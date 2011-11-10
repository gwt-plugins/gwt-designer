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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.gwtext.actions.ConfigureGwtExtOperation;
import com.google.gdt.eclipse.designer.gxt.ConfigureExtGwtOperationTest;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

/**
 * Test for {@link ConfigureExtGwtOperationTest}.
 * 
 * @author scheglov_ke
 */
public class ConfigureGwtExtOperationTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(0);
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
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    try {
      ModuleDescription moduleDescription = getTestModuleDescription();
      new ConfigureGwtExtOperation(moduleDescription).run(null);
      waitForAutoBuild();
      // gwtext.jar should be added
      assertTrue(ProjectUtils.hasType(
          m_testProject.getJavaProject(),
          "com.gwtext.client.widgets.Component"));
      assertFileExists("gwtext.jar");
      // public resources should be added
      assertFileExists("src/test/public/js/ext/ext-all.js");
      assertFileExists("src/test/public/js/ext/adapter/ext/ext-base.js");
      assertFileExists("src/test/public/js/ext/resources/css/ext-all.css");
      assertEquals(getDoubleQuotes2(new String[]{
          "<module>",
          "  <inherits name='com.google.gwt.user.User'/>",
          "  <inherits name='com.gwtext.GwtExt'/>",
          "  <entry-point class='test.client.Module'/>",
          "  <stylesheet src='js/ext/resources/css/ext-all.css'/>",
          "  <script src='js/ext/adapter/ext/ext-base.js'/>",
          "  <script src='js/ext/ext-all.js'/>",
          "</module>"}), getFileContentSrc("test/Module.gwt.xml"));
    } finally {
      waitForAutoBuild();
      // manually remove gwtext.jar
      forceDeleteFile(m_project.getFile("gwtext.jar"));
      // 
      do_projectDispose();
    }
  }
}