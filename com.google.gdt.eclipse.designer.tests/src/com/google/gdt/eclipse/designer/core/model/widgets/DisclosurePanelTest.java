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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>DisclosurePanel</code>.
 * 
 * @author scheglov_ke
 */
public class DisclosurePanelTest extends GwtModelTest {
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
  /**
   * To allow its extracting into NLS, property "headerText" should be moved into top level
   * properties.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41035
   */
  public void test_headerTextProperty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new DisclosurePanel('My header'));",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo panel = frame.getChildrenWidgets().get(0);
    // "headerText" 
    Property property = panel.getPropertyByTitle("headerText");
    assertNotNull(property);
    assertEquals("My header", property.getValue());
  }

  public void test_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // Button
    {
      WidgetInfo button = createWidget("com.google.gwt.user.client.ui.Button");
      Image image = button.getImage();
      assertThat(image).isNotNull();
      //
      Rectangle bounds = image.getBounds();
      assertThat(bounds.width).isGreaterThan(70);
      assertThat(bounds.height).isGreaterThan(20);
    }
    // DisclosurePanel
    {
      WidgetInfo panel = createWidget("com.google.gwt.user.client.ui.DisclosurePanel");
      Image image = panel.getImage();
      assertThat(image).isNotNull();
      assertThat(image.getBounds()).isEqualTo(new Rectangle(0, 0, 250, 200));
    }
  }
}