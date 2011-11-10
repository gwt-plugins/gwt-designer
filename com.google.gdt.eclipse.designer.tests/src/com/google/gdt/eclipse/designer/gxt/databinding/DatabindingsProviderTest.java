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
package com.google.gdt.eclipse.designer.gxt.databinding;

import com.google.gdt.eclipse.designer.gxt.databinding.ui.property.BindingsProperty;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.form.FormPanelInfo;

/**
 * @author sablin_aa
 * 
 */
public class DatabindingsProviderTest extends GxtModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_property() throws Exception {
    FormPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FormPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    BindingsProperty property = (BindingsProperty) panel.getPropertyByTitle("bindings");
    assertNotNull(property);
  }

  public void test_property_disabled() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyFormPanel.java",
        getTestSource(
            "public class MyFormPanel extends FormPanel {",
            "  public MyFormPanel() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyFormPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='databinding.disable'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    FormPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyFormPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNull(panel.getPropertyByTitle("bindings"));
  }
}