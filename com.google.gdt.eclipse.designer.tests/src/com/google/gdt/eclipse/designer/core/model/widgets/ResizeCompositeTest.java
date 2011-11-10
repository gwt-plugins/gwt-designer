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
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test <code>com.google.gwt.user.client.ui.ResizeComposite</code>.
 * 
 * @author scheglov_ke
 */
public class ResizeCompositeTest extends GwtModelTest {
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
  public void test_filled() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends ResizeComposite {",
            "  public Test() {",
            "    {",
            "      LayoutPanel panel = new LayoutPanel();",
            "      initWidget(panel);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.ResizeComposite} {this} {/initWidget(panel)/}",
        "  {new: com.google.gwt.user.client.ui.LayoutPanel} {local-unique: panel} {/new LayoutPanel()/ /initWidget(panel)/}");
    WidgetInfo panel = composite.getWidget();
    // do refresh()
    composite.refresh();
    assertNoErrors(composite);
    assertFalse(composite.isEmpty());
    // check Composite bounds
    {
      Rectangle bounds = composite.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    {
      Image image = composite.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(450);
      assertThat(image.getBounds().height).isEqualTo(300);
    }
    // check Button bounds
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
  }

  public void test_empty() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ResizeComposite {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: com.google.gwt.user.client.ui.ResizeComposite} {this} {}");
    // do refresh()
    composite.refresh();
    assertTrue(composite.isEmpty());
    assertNull(composite.getWidget());
    // no properties
    assertThat(composite.getProperties()).isEmpty();
  }
}