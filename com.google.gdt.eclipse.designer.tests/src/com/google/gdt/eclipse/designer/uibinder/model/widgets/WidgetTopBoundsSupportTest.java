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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

/**
 * Test {@link WidgetTopBoundsSupport}.
 * 
 * @author scheglov_ke
 */
public class WidgetTopBoundsSupportTest extends UiBinderModelTest {
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
  public void test_noSizeInvocations() throws Exception {
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // initial size
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // set bounds
    int newWidth = 500;
    int newHeight = 350;
    panel.getTopBoundsSupport().setSize(newWidth, newHeight);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    // check that size applied
    refresh();
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(newWidth, bounds.width);
      assertEquals(newHeight, bounds.height);
    }
  }

  /**
   * Test that zero size does not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47814
   */
  public void test_zeroSize() throws Exception {
    WidgetInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    // initially 450x300
    {
      Image image = panel.getImage();
      assertEquals(450, image.getBounds().width);
      assertEquals(300, image.getBounds().height);
    }
    // set -100x-100 size, no exception
    {
      panel.getTopBoundsSupport().setSize(-100, -100);
      refresh();
      // however 1x1 image generated
      Image image = panel.getImage();
      assertEquals(1, image.getBounds().width);
      assertEquals(1, image.getBounds().height);
    }
  }
}