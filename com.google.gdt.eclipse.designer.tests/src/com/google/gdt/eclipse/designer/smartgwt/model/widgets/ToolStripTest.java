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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.ToolStripCanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.ToolStripInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.ToolStripResizerInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ToolStripInfo}.
 * 
 * @author sablin_aa
 */
public class ToolStripTest extends SmartGwtModelTest {
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
  public void test_parse() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    ToolStrip toolStrip = new ToolStrip();",
            "    toolStrip.setVertical(true);",
            "    toolStrip.addMember(new Label('New Label'));",
            "    toolStrip.addResizer();",
            "    toolStrip.addFormItem(new TextItem('New_TextItem'));",
            "    toolStrip.addButton(new ToolStripButton('New Button'));",
            "    toolStrip.addFill();",
            "    canvas.addChild(toolStrip);",
            "    toolStrip.setRect(35, 25, 200, 200);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    // check ToolStrip
    ToolStripInfo toolStrip = canvas.getChildren(ToolStripInfo.class).get(0);
    assertThat(toolStrip.isHorizontal()).isFalse();
    // check children
    assertThat(toolStrip.getChildrenReal().size()).isEqualTo(4);
    assertThat(toolStrip.getCanvases().length).isEqualTo(4);
    List<AbstractComponentInfo> children = toolStrip.getChildren(AbstractComponentInfo.class);
    assertThat(children.size()).isEqualTo(5);
    // check bounds
    {
      // label
      AbstractComponentInfo label = children.get(0);
      assertThat(label).isInstanceOf(CanvasInfo.class);
      Integer x = Expectations.get(13, new IntValue[]{new IntValue("flanker-linux", 12)});
      Integer y = Expectations.get(0, new IntValue[]{new IntValue("flanker-linux", 1)});
      assertThat(label.getModelBounds()).isEqualTo(new Rectangle(x, y, 198, 100));
    }
    {
      // resizer
      AbstractComponentInfo resizer = children.get(1);
      assertThat(resizer).isInstanceOf(ToolStripResizerInfo.class);
      Integer y = Expectations.get(100, new IntValue[]{new IntValue("flanker-linux", 101)});
      assertThat(resizer.getModelBounds()).isEqualTo(
          new Rectangle(0, y, toolStrip.getModelBounds().width, ToolStripResizerInfo.DEFAULT_SIZE));
    }
    {
      // text item
      AbstractComponentInfo text = children.get(2);
      assertThat(text).isInstanceOf(FormItemInfo.class);
      Integer width = Expectations.get(225, new IntValue[]{new IntValue("flanker-linux", 221)});
      assertThat(text.getModelBounds()).isEqualTo(new Rectangle(-3, 111, width, 22));
    }
    {
      // button
      AbstractComponentInfo button = children.get(3);
      assertThat(button).isInstanceOf(CanvasInfo.class);
      Integer x = Expectations.get(77, new IntValue[]{new IntValue("flanker-linux", 76)});
      Integer y = Expectations.get(136, new IntValue[]{new IntValue("flanker-linux", 137)});
      Integer width = Expectations.get(71, new IntValue[]{new IntValue("flanker-linux", 70)});
      assertThat(button.getModelBounds()).isEqualTo(
          new Rectangle(x, y, width, CanvasTest.BUTTON_HEIGHT));
    }
    {
      // fill
      AbstractComponentInfo fill = children.get(4);
      assertThat(fill).isInstanceOf(ToolStripCanvasInfo.class);
      Integer x = Expectations.get(14, new IntValue[]{new IntValue("flanker-linux", 12)});
      assertThat(fill.getModelBounds()).isEqualTo(new Rectangle(x, 159, 198, 40));
    }
  }

  public void test_parse_horizontal() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    ToolStrip toolStrip = new ToolStrip();",
            "    toolStrip.addMember(new Label('New Label'));",
            "    toolStrip.addResizer();",
            "    toolStrip.addButton(new ToolStripButton('New Button'));",
            "    toolStrip.addFill();",
            "    canvas.addChild(toolStrip);",
            "    toolStrip.setRect(35, 25, 200, 200);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    // check ToolStrip
    ToolStripInfo toolStrip = canvas.getChildren(ToolStripInfo.class).get(0);
    assertThat(toolStrip.isHorizontal()).isTrue();
    List<AbstractComponentInfo> children = toolStrip.getChildren(AbstractComponentInfo.class);
    assertThat(children.size()).isEqualTo(4);
    // check bounds
    {
      // label
      AbstractComponentInfo label = children.get(0);
      assertThat(label).isInstanceOf(CanvasInfo.class);
      Integer expected = Expectations.get(0, new IntValue[]{new IntValue("flanker-linux", 1)});
      assertThat(label.getModelBounds()).isEqualTo(new Rectangle(expected, expected, 100, 198));
    }
    {
      // resizer
      AbstractComponentInfo resizer = children.get(1);
      assertThat(resizer).isInstanceOf(ToolStripResizerInfo.class);
      Integer expected = Expectations.get(100, new IntValue[]{new IntValue("flanker-linux", 101)});
      assertThat(resizer.getModelBounds()).isEqualTo(
          new Rectangle(expected,
              0,
              ToolStripResizerInfo.DEFAULT_SIZE,
              toolStrip.getModelBounds().height));
    }
    {
      // button
      AbstractComponentInfo button = children.get(2);
      assertThat(button).isInstanceOf(CanvasInfo.class);
      Integer x = Expectations.get(114, new IntValue[]{new IntValue("flanker-linux", 115)});
      Integer y = Expectations.get(88, new IntValue[]{new IntValue("flanker-linux", 89)});
      Integer width = Expectations.get(71, new IntValue[]{new IntValue("flanker-linux", 70)});
      assertThat(button.getModelBounds()).isEqualTo(
          new Rectangle(x, y, width, CanvasTest.BUTTON_HEIGHT));
    }
    {
      // fill
      AbstractComponentInfo fill = children.get(3);
      assertThat(fill).isInstanceOf(ToolStripCanvasInfo.class);
      Integer x = Expectations.get(186, new IntValue[]{new IntValue("flanker-linux", 185)});
      Integer width = Expectations.get(13, new IntValue[]{new IntValue("flanker-linux", 14)});
      assertThat(fill.getModelBounds()).isEqualTo(new Rectangle(x, 1, width, 198));
    }
  }

  /**
   * Add <code>com.smartgwt.client.widgets.toolbar.ToolStripButton</code>.
   */
  public void test_addButton() throws Exception {
    ToolStripInfo toolStrip = createDefaultModule();
    // create new MenuButton
    AbstractComponentInfo newButton =
        createJavaInfo("com.smartgwt.client.widgets.toolbar.ToolStripButton");
    {
      FlowContainer flowContainer = new FlowContainerFactory(toolStrip, false).get().get(0);
      assertTrue(flowContainer.validateComponent(newButton));
      flowContainer.command_CREATE(newButton, toolStrip.getChildrenReal().get(1));
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    ToolStrip toolStrip = new ToolStrip();",
        "    toolStrip.setVertical(true);",
        "    toolStrip.addMember(new Label('New Label'));",
        "    {",
        "      ToolStripButton toolStripButton = new ToolStripButton('New Button');",
        "      toolStrip.addButton(toolStripButton);",
        "    }",
        "    toolStrip.addMember(new Button('New Button'));",
        "    canvas.addChild(toolStrip);",
        "    toolStrip.setRect(35, 25, 200, 200);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Add <code>com.smartgwt.client.widgets.toolbar.ToolStripMenuButton</code>.
   */
  public void test_addMenuButton() throws Exception {
    ToolStripInfo toolStrip = createDefaultModule();
    // create new MenuButton
    AbstractComponentInfo newMenuButton =
        createJavaInfo("com.smartgwt.client.widgets.toolbar.ToolStripMenuButton");
    {
      FlowContainer flowContainer = new FlowContainerFactory(toolStrip, false).get().get(1);
      assertTrue(flowContainer.validateComponent(newMenuButton));
      flowContainer.command_CREATE(newMenuButton, toolStrip.getChildrenReal().get(1));
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    ToolStrip toolStrip = new ToolStrip();",
        "    toolStrip.setVertical(true);",
        "    toolStrip.addMember(new Label('New Label'));",
        "    {",
        "      ToolStripMenuButton toolStripMenuButton = new ToolStripMenuButton('New MenuButton');",
        "      toolStrip.addMenuButton(toolStripMenuButton);",
        "    }",
        "    toolStrip.addMember(new Button('New Button'));",
        "    canvas.addChild(toolStrip);",
        "    toolStrip.setRect(35, 25, 200, 200);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Add <code>com.smartgwt.client.widgets.Canvas</code>.
   */
  public void test_addCanvas() throws Exception {
    ToolStripInfo toolStrip = createDefaultModule();
    // create new Slider
    AbstractComponentInfo newCanvas = createJavaInfo("com.smartgwt.client.widgets.Slider");
    {
      FlowContainer flowContainer = new FlowContainerFactory(toolStrip, false).get().get(2);
      assertTrue(flowContainer.validateComponent(newCanvas));
      flowContainer.command_CREATE(newCanvas, toolStrip.getChildrenReal().get(1));
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    ToolStrip toolStrip = new ToolStrip();",
        "    toolStrip.setVertical(true);",
        "    toolStrip.addMember(new Label('New Label'));",
        "    {",
        "      Slider slider = new Slider();",
        "      toolStrip.addMember(slider);",
        "    }",
        "    toolStrip.addMember(new Button('New Button'));",
        "    canvas.addChild(toolStrip);",
        "    toolStrip.setRect(35, 25, 200, 200);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Add <code>com.smartgwt.client.widgets.form.fields.FormItem</code>.
   */
  public void test_addFormItem() throws Exception {
    ToolStripInfo toolStrip = createDefaultModule();
    // create new TextItem
    AbstractComponentInfo newFormItem =
        createJavaInfo("com.smartgwt.client.widgets.form.fields.TextItem");
    {
      FlowContainer flowContainer = new FlowContainerFactory(toolStrip, false).get().get(3);
      assertTrue(flowContainer.validateComponent(newFormItem));
      flowContainer.command_CREATE(newFormItem, toolStrip.getChildrenReal().get(1));
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    ToolStrip toolStrip = new ToolStrip();",
        "    toolStrip.setVertical(true);",
        "    toolStrip.addMember(new Label('New Label'));",
        "    {",
        "      TextItem textItem = new TextItem('newTextItem_3', 'New TextItem');",
        "      toolStrip.addFormItem(textItem);",
        "    }",
        "    toolStrip.addMember(new Button('New Button'));",
        "    canvas.addChild(toolStrip);",
        "    toolStrip.setRect(35, 25, 200, 200);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Add <code>com.smartgwt.client.widgets.toolbar.ToolStripSpacer</code>.
   */
  public void test_addSpacer() throws Exception {
    ToolStripInfo toolStrip = createDefaultModule();
    // create new Spacer
    AbstractComponentInfo newSpacer =
        createJavaInfo("com.smartgwt.client.widgets.toolbar.ToolStripSpacer");
    {
      FlowContainer flowContainer = new FlowContainerFactory(toolStrip, false).get().get(4);
      assertTrue(flowContainer.validateComponent(newSpacer));
      flowContainer.command_CREATE(newSpacer, toolStrip.getChildrenReal().get(1));
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    ToolStrip toolStrip = new ToolStrip();",
        "    toolStrip.setVertical(true);",
        "    toolStrip.addMember(new Label('New Label'));",
        "    {",
        "      ToolStripSpacer toolStripSpacer = new ToolStripSpacer(30);",
        "      toolStrip.addSpacer(toolStripSpacer);",
        "    }",
        "    toolStrip.addMember(new Button('New Button'));",
        "    canvas.addChild(toolStrip);",
        "    toolStrip.setRect(35, 25, 200, 200);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  private ToolStripInfo createDefaultModule() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    ToolStrip toolStrip = new ToolStrip();",
            "    toolStrip.setVertical(true);",
            "    toolStrip.addMember(new Label('New Label'));",
            "    toolStrip.addMember(new Button('New Button'));",
            "    canvas.addChild(toolStrip);",
            "    toolStrip.setRect(35, 25, 200, 200);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    return canvas.getChildren(ToolStripInfo.class).get(0);
  }
}