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
package com.google.gdt.eclipse.designer.gxt.uibinder.model;

import com.google.gdt.eclipse.designer.gxt.ExtGwtTests;
import com.google.gdt.eclipse.designer.gxt.actions.ConfigureExtGwtOperation;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract super class for GXT UiBinder tests.
 * 
 * @author scheglov_ke
 */
public abstract class GxtUiBinderModelTest extends UiBinderModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures test module, for example for using some specific library.
   */
  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    super.configureModule(moduleDescription);
    m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION + "/gxt.jar");
    new ConfigureExtGwtOperation(moduleDescription, ExtGwtTests.GXT_LOCATION).run(null);
    // don't use "strict" mode
    {
      String html = getFileContent("war/Module.html");
      html = StringUtils.remove(html, "<!doctype html>");
      setFileContent("war/Module.html", html);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getJavaSource_decorate(String... lines) {
    lines = CodeUtils.join(new String[]{"import com.extjs.gxt.ui.client.widget.grid.*;"}, lines);
    lines = super.getJavaSource_decorate(lines);
    return lines;
  }

  @Override
  protected String getTestSource_namespaces() {
    String newLine = "\n\t";
    return super.getTestSource_namespaces()
        + newLine
        + " xmlns:xg='urn:import:com.extjs.gxt.ui.client.widget.grid'";
  }
}