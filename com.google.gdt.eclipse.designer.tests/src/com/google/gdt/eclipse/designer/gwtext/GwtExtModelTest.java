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
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Test for GWT-Ext widgets.
 * 
 * @author sablin_aa
 */
public abstract class GwtExtModelTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean GWTEXT_STARTED = false;

  @Override
  protected void setUp() throws Exception {
    ensureStartedGWTExt();
    super.setUp();
  }

  private static void ensureStartedGWTExt() {
    if (!GWTEXT_STARTED) {
      ParseFactory.disposeSharedGWTState();
      GWTEXT_STARTED = true;
    }
  }

  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    m_testProject.addBundleJars("com.google.gdt.eclipse.designer.GWTExt", "/resources");
    new ConfigureGwtExtOperation(moduleDescription).run(null);
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
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.EntryPoint;",
            "import com.google.gwt.user.client.ui.RootPanel;",
            "import com.google.gwt.user.client.ui.Composite;",
            "import com.gwtext.client.core.*;",
            "import com.gwtext.client.data.*;",
            "import com.gwtext.client.widgets.*;",
            "import com.gwtext.client.widgets.layout.*;",
            "import com.gwtext.client.widgets.form.*;",
            "import com.gwtext.client.widgets.menu.*;",
            "import com.gwtext.client.widgets.tree.*;",
            "import com.gwtext.client.widgets.grid.*;",
            "import com.gwtext.client.widgets.portal.*;"}, lines);
    return lines;
  }
}