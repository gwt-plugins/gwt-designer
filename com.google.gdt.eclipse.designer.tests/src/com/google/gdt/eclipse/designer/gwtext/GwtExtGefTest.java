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

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.gwtext.actions.ConfigureGwtExtOperation;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

import java.net.URL;

/**
 * Abstract test for generic GWT {@link LayoutEditPolicy}s.
 * 
 * @author scheglov_ke
 */
public class GwtExtGefTest extends GwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    super.configureModule(moduleDescription);
    // add gwtext.jar as external, to avoid locking it
    {
      Bundle bundle = Platform.getBundle("com.google.gdt.eclipse.designer.GWTExt");
      URL gwtExtEntry = bundle.getEntry("\\resources\\gwtext.jar");
      String gwtExtLocation = FileLocator.toFileURL(gwtExtEntry).getPath();
      m_testProject.addExternalJar(gwtExtLocation);
    }
    // configure for GWT-Ext
    new ConfigureGwtExtOperation(moduleDescription).run(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final PanelInfo openPanel(String... lines) throws Exception {
    return (PanelInfo) openJavaInfo(lines);
  }

  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test.client;",
            "import com.google.gwt.core.client.EntryPoint;",
            "import com.google.gwt.user.client.ui.RootPanel;",
            "import com.google.gwt.user.client.ui.Composite;",
            "import com.gwtext.client.core.*;",
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
