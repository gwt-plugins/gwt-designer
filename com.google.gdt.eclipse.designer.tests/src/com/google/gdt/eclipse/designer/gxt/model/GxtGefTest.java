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
package com.google.gdt.eclipse.designer.gxt.model;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.gxt.ExtGwtTests;
import com.google.gdt.eclipse.designer.gxt.actions.ConfigureExtGwtOperation;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

/**
 * Abstract test for Ext-GWT in editor.
 * 
 * @author scheglov_ke
 */
public class GxtGefTest extends GwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    m_testProject.addExternalJar(ExtGwtTests.GXT_LOCATION + "/gxt.jar");
    new ConfigureExtGwtOperation(moduleDescription, ExtGwtTests.GXT_LOCATION).run(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final LayoutContainerInfo openLayoutContainer(String... lines) throws Exception {
    return (LayoutContainerInfo) openJavaInfo(lines);
  }

  /**
   * "Decorates" given lines of source, usually adds required imports.
   */
  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.EntryPoint;",
            "import com.google.gwt.user.client.ui.RootPanel;",
            "import com.extjs.gxt.ui.client.*;",
            "import com.extjs.gxt.ui.client.Style.*;",
            "import com.extjs.gxt.ui.client.util.*;",
            "import com.extjs.gxt.ui.client.widget.*;",
            "import com.extjs.gxt.ui.client.widget.button.*;",
            "import com.extjs.gxt.ui.client.widget.layout.*;",
            "import com.extjs.gxt.ui.client.widget.form.*;",
            "import com.extjs.gxt.ui.client.widget.menu.*;",
            "import com.extjs.gxt.ui.client.widget.toolbar.*;",
            "import com.extjs.gxt.ui.client.widget.custom.*;",
            "import com.extjs.gxt.ui.client.event.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} with empty GXT <code>Button</code>.
   */
  public final WidgetInfo loadGxtButton() throws Exception {
    return loadCreationTool("com.extjs.gxt.ui.client.widget.button.Button", "empty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Box for GEF
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void prepareBox() throws Exception {
    prepareBox(100, 50);
  }

  @Override
  protected final void prepareBox(int width, int height) throws Exception {
    setFileContentSrc(
        "test/client/Box.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Box extends Button {",
            "  public Box() {",
            "    setSize(" + width + ", " + height + ");",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  @Override
  protected final ComponentInfo loadCreationBox() throws Exception {
    ComponentInfo box = loadCreationTool("test.client.Box");
    box.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    return box;
  }
}
