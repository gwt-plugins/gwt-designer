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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.panels.CaptionPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for <code>CaptionPanel</code>.
 * 
 * @author scheglov_ke
 */
public class CaptionPanelTest extends GwtModelTest {
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
  public void test_parseThis() throws Exception {
    CaptionPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends CaptionPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: com.google.gwt.user.client.ui.CaptionPanel} {this} {}");
    panel.refresh();
    //
    Rectangle bounds = panel.getBounds();
    assertEquals(450, bounds.width);
    assertEquals(300, bounds.height);
  }

  public void test_parseNew() throws Exception {
    CaptionPanelInfo panel =
        parseJavaInfo(
            "public class Test {",
            "  /**",
            "  * @wbp.parser.entryPoint",
            "  */",
            "  public void init() {",
            "    CaptionPanel captionPanel = new CaptionPanel();",
            "  }",
            "}");
    assertHierarchy("{new: com.google.gwt.user.client.ui.CaptionPanel} {local-unique: captionPanel} {/new CaptionPanel()/}");
    panel.refresh();
    //
    Rectangle bounds = panel.getBounds();
    assertEquals(450, bounds.width);
    assertEquals(300, bounds.height);
  }

  /**
   * Property "captionText" can not have value <code>null</code>. We should set some reasonable
   * value.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42093
   */
  public void test_captionText_setDefaultValue() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CaptionPanel captionPanel = new CaptionPanel('Some caption');",
            "      rootPanel.add(captionPanel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CaptionPanelInfo panel = getJavaInfoByName("captionPanel");
    // reset "captionText" to default
    panel.getPropertyByTitle("captionText").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CaptionPanel captionPanel = new CaptionPanel('Default caption');",
        "      rootPanel.add(captionPanel);",
        "    }",
        "  }",
        "}");
  }
}