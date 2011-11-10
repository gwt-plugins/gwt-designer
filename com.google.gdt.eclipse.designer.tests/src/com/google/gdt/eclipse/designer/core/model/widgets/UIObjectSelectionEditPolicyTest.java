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

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.gef.policy.UIObjectSelectionEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link UIObjectSelectionEditPolicy} in GEF.
 * 
 * @author scheglov_ke
 */
public class UIObjectSelectionEditPolicyTest extends GwtGefTest {
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
  // EAST
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_EAST_pixels_plus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST).dragOn(25, 0).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('175px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_EAST_pixels_minus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST).dragOn(-25, 0).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('125px', '50px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should not allow to set negative width.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47621
   */
  public void test_EAST_pixels_negative() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST).dragOn(-160, 0).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_EAST_percent_plus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.ctrlDown();
    canvas.beginResize(button, IPositionConstants.EAST).dragOn(20, 0).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('70%', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_EAST_percent_minus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.ctrlDown();
    canvas.beginResize(button, IPositionConstants.EAST).dragOn(-15, 0).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('35%', '50px');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WEST
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SOUTH_plus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH).dragOn(0, 25).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '75px');",
        "    }",
        "  }",
        "}");
  }

  public void test_SOUTH_minus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH).dragOn(0, -25).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '25px');",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should not allow to set negative height.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47621
   */
  public void test_SOUTH_negative() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH).dragOn(0, -60).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_SOUTH_percent_plus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.ctrlDown();
    canvas.beginResize(button, IPositionConstants.SOUTH).dragOn(0, 20).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '70%');",
        "    }",
        "  }",
        "}");
  }

  public void test_SOUTH_percent_minus() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.ctrlDown();
    canvas.beginResize(button, IPositionConstants.SOUTH).dragOn(0, -15).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '35%');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SOUTH_EAST
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SOUTH_EAST() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('150px', '50px');",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH_EAST).dragOn(50, 25).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      add(button);",
        "      button.setSize('200px', '75px');",
        "    }",
        "  }",
        "}");
  }
}
