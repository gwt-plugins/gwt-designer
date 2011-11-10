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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.WindowInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.WindowTopBoundsSupport;

import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link WindowInfo} and {@link WindowTopBoundsSupport}.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class WindowTest extends GwtExtModelTest {
  private static final int HEIGHT_DELTA = 20;

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
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_sizeFromResource() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Window {",
            "  public Test() {",
            "  }",
            "}");
    window.refresh();
    // default size
    {
      Rectangle bounds = window.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // set new size
    {
      window.getTopBoundsSupport().setSize(400, 200);
      window.refresh();
      //
      Rectangle bounds = window.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200, bounds.height);
    }
  }

  public void test_setSize_Integer() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setSize(350, 250);",
            "  }",
            "}");
    window.refresh();
    // source size
    {
      Rectangle bounds = window.getBounds();
      assertEquals(350, bounds.width);
      assertEquals(250 + HEIGHT_DELTA, bounds.height);
    }
    // set new size
    {
      window.getTopBoundsSupport().setSize(400, 200);
      window.refresh();
      Rectangle bounds = window.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200 + HEIGHT_DELTA, bounds.height);
      // check source
      assertEditor(
          "public class Test extends Window {",
          "  public Test() {",
          "    setSize(400, 200);",
          "  }",
          "}");
    }
  }

  /**
   * Resizing "Window" on big value causes crash. This happens because we set Window size before
   * Shell size. So, when Windows centers itself, its position may be negative. We need to fix its
   * position.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43341
   */
  public void test_setSize_Integer_big() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setSize(200, 300);",
            "  }",
            "}");
    window.refresh();
    // set new size
    window.getTopBoundsSupport().setSize(200, 600);
    window.refresh();
    {
      Rectangle bounds = window.getBounds();
      assertEquals(200, bounds.width);
      assertEquals(600 + HEIGHT_DELTA, bounds.height);
    }
    assertEditor(
        "public class Test extends Window {",
        "  public Test() {",
        "    setSize(200, 600);",
        "  }",
        "}");
  }

  public void test_setSize_String() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setSize('350px', '250px');",
            "  }",
            "}");
    window.refresh();
    // source size
    {
      Rectangle bounds = window.getBounds();
      assertEquals(350, bounds.width);
      assertEquals(250 + HEIGHT_DELTA, bounds.height);
    }
    // set new size
    {
      window.getTopBoundsSupport().setSize(400, 200);
      window.refresh();
      // check source
      assertEditor(
          "public class Test extends Window {",
          "  public Test() {",
          "    setSize('400px', '200px');",
          "  }",
          "}");
      // check values
      Rectangle bounds = window.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200 + HEIGHT_DELTA, bounds.height);
    }
  }

  public void test_setWH_Integer() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setWidth(350);",
            "    setHeight(250);",
            "  }",
            "}");
    window.refresh();
    // source size
    {
      Rectangle bounds = window.getBounds();
      assertEquals(350, bounds.width);
      assertEquals(250 + HEIGHT_DELTA, bounds.height);
    }
    // set new size
    {
      window.getTopBoundsSupport().setSize(400, 200);
      window.refresh();
      Rectangle bounds = window.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200 + HEIGHT_DELTA, bounds.height);
      // check source
      assertEditor(
          "public class Test extends Window {",
          "  public Test() {",
          "    setWidth(400);",
          "    setHeight(200);",
          "  }",
          "}");
    }
  }

  public void test_setWH_String() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setWidth('350px');",
            "    setHeight('250px');",
            "  }",
            "}");
    window.refresh();
    // source size
    {
      Rectangle bounds = window.getBounds();
      assertEquals(350, bounds.width);
      assertEquals(250 + HEIGHT_DELTA, bounds.height);
    }
    // set new size
    {
      window.getTopBoundsSupport().setSize(400, 200);
      window.refresh();
      Rectangle bounds = window.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200 + HEIGHT_DELTA, bounds.height);
      // check source
      assertEditor(
          "public class Test extends Window {",
          "  public Test() {",
          "    setWidth('400px');",
          "    setHeight('200px');",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There is problem that <code>Window.setLayout()</code> complains that widget is already
   * rendered.
   */
  public void test_setLayout() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    assertNoErrors(window);
    // refresh
    window.refresh();
    assertNoErrors(window);
  }

  /**
   * There was problem with <code>BorderLayout</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41241
   */
  public void test_BorderLayout() throws Exception {
    WindowInfo window =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    assertNoErrors(window);
    // refresh
    window.refresh();
    assertNoErrors(window);
  }
}