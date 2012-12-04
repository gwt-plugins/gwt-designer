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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.SectionStackInfo;
import com.google.gdt.eclipse.designer.smart.model.SectionStackSectionInfo;
import com.google.gdt.eclipse.designer.smart.model.TabSetInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test <code>com.smartgwt.client.widgets.layout.SectionStack</code>
 * 
 * @author sablin_aa
 */
public class SectionStackTest extends SmartGwtModelTest {
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
  public void test_parse_this() throws Exception {
    SectionStackInfo stack =
        parseJavaInfo(compiledSource(new String[]{
            "public class Test extends SectionStack {",
            "  public Test() {",
            "    //sectionsPlace",
            "  }",
            "}"}, "this"));
    stack.getTopBoundsSupport().setSize(270, 240);
    stack.refresh();
    assertThat(stack.getWidgets()).isEmpty();
    // check Sections
    List<SectionStackSectionInfo> sections = stack.getSections();
    assertThat(sections.size()).isEqualTo(2);
    SectionStackSectionInfo section_1 = sections.get(0);
    assertThat(section_1.getCanvases().size()).isEqualTo(1);
    SectionStackSectionInfo section_2 = sections.get(1);
    assertThat(section_2.getCanvases().size()).isEqualTo(3);
    //
    assert_sections_bounds(stack);
  }

  public void test_parse_onCanvas() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(compiledSource(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    SectionStack stack = new SectionStack();",
            "    //sectionsPlace",
            "    canvas.addChild(stack);",
            "    stack.setRect(39, 34, 270, 240);",
            "    canvas.draw();",
            "  }",
            "}"}, "stack"));
    canvas.refresh();
    SectionStackInfo stack = (SectionStackInfo) canvas.getWidgets().get(0);
    assertThat(stack.getWidgets()).isEmpty();
    // check Sections
    List<SectionStackSectionInfo> sections = stack.getSections();
    assertThat(sections.size()).isEqualTo(2);
    SectionStackSectionInfo section_1 = sections.get(0);
    assertThat(section_1.getCanvases().size()).isEqualTo(1);
    SectionStackSectionInfo section_2 = sections.get(1);
    assertThat(section_2.getCanvases().size()).isEqualTo(3);
    //
    assert_sections_bounds(stack);
  }

  public void test_parse_onRootPanel() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo rootPanel =
        parseJavaInfo(compiledSource(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    SectionStack stack = new SectionStack();",
            "    //sectionsPlace",
            "    rootPanel.add(stack, 41, 42);",
            "    stack.setSize('270px', '240px');",
            "  }",
            "}"}, "stack"));
    rootPanel.refresh();
    SectionStackInfo stack = (SectionStackInfo) rootPanel.getChildrenWidgets().get(0);
    assertThat(stack.getWidgets()).isEmpty();
    // check Sections
    List<SectionStackSectionInfo> sections = stack.getSections();
    assertThat(sections.size()).isEqualTo(2);
    SectionStackSectionInfo section_1 = sections.get(0);
    assertThat(section_1.getCanvases().size()).isEqualTo(1);
    SectionStackSectionInfo section_2 = sections.get(1);
    assertThat(section_2.getCanvases().size()).isEqualTo(3);
    //
    assert_sections_bounds(stack);
  }

  /**
   * Test bounds for reverse ordered SectionStack.
   */
  public void test_parse_reverse() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(compiledSource(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    SectionStack stack = new SectionStack();",
            "    //sectionsPlace",
            "    canvas.addChild(stack);",
            "    stack.setRect(10, 10, 270, 240);",
            "    canvas.draw();",
            "  }",
            "}"}, "stack"));
    canvas.refresh();
    SectionStackInfo stack = (SectionStackInfo) canvas.getWidgets().get(0);
    stack.getPropertyByTitle("reverseOrder").setValue(true);
    // check Sections
    int width = stack.getModelBounds().width;
    List<SectionStackSectionInfo> sections = stack.getSections();
    assertThat(sections.size()).isEqualTo(2);
    SectionStackSectionInfo section_1 = sections.get(0);
    SectionStackSectionInfo section_2 = sections.get(1);
    // check section 1
    {
      Integer sectionTop =
          Expectations.get(200, new IntValue[]{new IntValue("flanker-desktop", 201)});
      Integer sectionHeight =
          Expectations.get(40, new IntValue[]{new IntValue("flanker-desktop", 39)});
      assertThat(section_1.getModelBounds()).isEqualTo(
          new Rectangle(0, sectionTop, width, sectionHeight));
      List<CanvasInfo> canvases = section_1.getCanvases();
      assertThat(canvases.size()).isEqualTo(1);
      Integer canvasHeight =
          Expectations.get(14, new IntValue[]{new IntValue("flanker-desktop", 13)});
      assertThat(canvases.get(0).getModelBounds()).isEqualTo(
          new Rectangle(1, -1, width - 2, canvasHeight));
    }
    // check section 2
    {
      Integer sectionTop =
          Expectations.get(30, new IntValue[]{new IntValue("flanker-desktop", 31)});
      assertThat(section_2.getCanvases().size()).isEqualTo(3);
      assertThat(section_2.getModelBounds()).isEqualTo(new Rectangle(0, sectionTop, width, 170));
      List<CanvasInfo> canvases = section_2.getCanvases();
      assertThat(canvases.size()).isEqualTo(3);
      assertThat(canvases.get(0).getModelBounds()).isEqualTo(
          new Rectangle(85, 121, CanvasTest.BUTTON_WIDTH, CanvasTest.BUTTON_HEIGHT));
      assertThat(canvases.get(1).getModelBounds()).isEqualTo(new Rectangle(1, 21, width - 2, 100));
      assertThat(canvases.get(2).getModelBounds()).isEqualTo(
          new Rectangle(85, -1, CanvasTest.BUTTON_WIDTH, CanvasTest.BUTTON_HEIGHT));
    }
  }

  /**
   * {@link SectionStackSectionInfo} added before {@link SectionStackInfo} association.
   */
  public void test_addSection() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo rootPanel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    SectionStack stack = new SectionStack();",
            "    rootPanel.add(stack, 41, 42);",
            "    stack.setSize('270px', '200px');",
            "  }",
            "}");
    rootPanel.refresh();
    SectionStackInfo stack = (SectionStackInfo) rootPanel.getChildrenWidgets().get(0);
    assertThat(stack.getChildrenJava()).isEmpty();
    // create new Section
    FlowContainer flowContainer = new FlowContainerFactory(stack, true).get().get(0);
    {
      assertFalse(flowContainer.isHorizontal());
    }
    JavaInfo newSection = createJavaInfo("com.smartgwt.client.widgets.layout.SectionStackSection");
    assertTrue(flowContainer.validateComponent(newSection));
    flowContainer.command_CREATE(newSection, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    SectionStack stack = new SectionStack();",
        "    {",
        "      SectionStackSection sectionStackSection = new SectionStackSection('New Section');",
        "      stack.addSection(sectionStackSection);",
        "    }",
        "    rootPanel.add(stack, 41, 42);",
        "    stack.setSize('270px', '200px');",
        "  }",
        "}");
    // add widget on Section
    CanvasInfo newCanvas = createJavaInfo("com.smartgwt.client.widgets.Canvas");
    flowContainer_CREATE(newSection, newCanvas, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    SectionStack stack = new SectionStack();",
        "    {",
        "      SectionStackSection sectionStackSection = new SectionStackSection('New Section');",
        "      {",
        "        Canvas canvas = new Canvas();",
        "        sectionStackSection.addItem(canvas);",
        "      }",
        "      stack.addSection(sectionStackSection);",
        "    }",
        "    rootPanel.add(stack, 41, 42);",
        "    stack.setSize('270px', '200px');",
        "  }",
        "}");
    newSection.getPropertyByTitle("expanded").setValue(false);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    SectionStack stack = new SectionStack();",
        "    {",
        "      SectionStackSection sectionStackSection = new SectionStackSection('New Section');",
        "      sectionStackSection.setExpanded(false);",
        "      {",
        "        Canvas canvas = new Canvas();",
        "        sectionStackSection.addItem(canvas);",
        "      }",
        "      stack.addSection(sectionStackSection);",
        "    }",
        "    rootPanel.add(stack, 41, 42);",
        "    stack.setSize('270px', '200px');",
        "  }",
        "}");
  }

  /**
   * Test dispose objects when it been not rendered.
   */
  public void test_dispose() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    Tab tab = new Tab('Tab_2');",
            "    {",
            "      SectionStack stack = new SectionStack();",
            "      SectionStackSection sectionStackSection = new SectionStackSection('New Section');",
            "      sectionStackSection.setExpanded(false);",
            "      {",
            "        Canvas canvas = new Canvas();",
            "        sectionStackSection.addItem(canvas);",
            "      }",
            "      stack.addSection(sectionStackSection);",
            "      tab.setPane(stack);",
            "    }",
            "    tabSet.addTab(tab);",
            "    tabSet.draw();",
            "  }",
            "}"});
    tabSet.refresh();
  }

  /**
   * There was problem with <code>TabSet</code> and absolute bounds.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=361760
   */
  public void test_tabSet() throws Exception {
    parseJavaInfo(
        "public class Test extends Window {",
        "  public Test() {",
        "    setSize('400', '300');",
        "    VStack vStack = new VStack();",
        "    vStack.setSize('100%', '100%');",
        "    {",
        "      SectionStack sectionStack = new SectionStack();",
        "      sectionStack.setSize('100%', '100%');",
        "      {",
        "        SectionStackSection section = new SectionStackSection('New Section');",
        "        section.setExpanded(true);",
        "        {",
        "          TabSet tabSet = new TabSet();",
        "          tabSet.setSize('100%', '100%');",
        "          {",
        "            Tab tab = new Tab('newTab');",
        "            tabSet.addTab(tab);",
        "          }",
        "          section.addItem(tabSet);",
        "        }",
        "        sectionStack.addSection(section);",
        "      }",
        "      vStack.addMember(sectionStack);",
        "    }",
        "    addItem(vStack);",
        "  }",
        "}");
    refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void assert_sections_bounds(SectionStackInfo stackInfo) {
    int width = stackInfo.getModelBounds().width;
    List<SectionStackSectionInfo> sections = stackInfo.getSections();
    assertThat(sections.size()).isEqualTo(2);
    // check section 1
    SectionStackSectionInfo sectionInfo_1 = sections.get(0);
    {
      Integer sectionHeight =
          Expectations.get(40, new IntValue[]{new IntValue("flanker-desktop", 39)});
      assertThat(sectionInfo_1.getModelBounds()).isEqualTo(
          new Rectangle(0, 0, width, sectionHeight));
      List<CanvasInfo> canvases = sectionInfo_1.getCanvases();
      assertThat(canvases.size()).isEqualTo(1);
      Integer canvasHeight =
          Expectations.get(14, new IntValue[]{new IntValue("flanker-desktop", 13)});
      assertThat(canvases.get(0).getModelBounds()).isEqualTo(
          new Rectangle(1, 26, width - 2, canvasHeight));
    }
    // check section 2
    SectionStackSectionInfo sectionInfo_2 = sections.get(1);
    {
      Integer sectionHeight =
          Expectations.get(40, new IntValue[]{new IntValue("flanker-desktop", 39)});
      assertThat(sectionInfo_2.getModelBounds()).isEqualTo(
          new Rectangle(0, sectionHeight, width, 170));
      List<CanvasInfo> canvases = sectionInfo_2.getCanvases();
      assertThat(canvases.size()).isEqualTo(3);
      assertThat(canvases.get(0).getModelBounds()).isEqualTo(
          new Rectangle(85, 26, CanvasTest.BUTTON_WIDTH, CanvasTest.BUTTON_HEIGHT));
      assertThat(canvases.get(1).getModelBounds()).isEqualTo(new Rectangle(1, 48, width - 2, 100));
      assertThat(canvases.get(2).getModelBounds()).isEqualTo(
          new Rectangle(85, 148, CanvasTest.BUTTON_WIDTH, CanvasTest.BUTTON_HEIGHT));
    }
  }

  /**
   * @return source with Sections creation source lines.
   */
  private String[] compiledSource(String[] body, String variable) {
    List<String> buffer = Lists.newArrayList();
    for (String line : body) {
      int index = line.indexOf("//sectionsPlace");
      if (index == -1) {
        buffer.add(line);
      } else {
        String sectionsLines[] = getSourceLines(variable, line.substring(0, index));
        for (String sectionsLine : sectionsLines) {
          buffer.add(sectionsLine);
        }
      }
    }
    return buffer.toArray(new String[buffer.size()]);
  }

  private String[] getSourceLines(String variable, String shift) {
    return new String[]{
        shift + variable + ".setDefaultLayoutAlign(Alignment.CENTER);",
        shift + variable + ".setVisibilityMode(VisibilityMode.MULTIPLE);",
        shift + "",
        shift + "SectionStackSection section_1 = new SectionStackSection('Section 1');",
        shift + "section_1.setExpanded(true);",
        shift + "",
        shift + "HTMLFlow flow = new HTMLFlow('<b>HTML</b>Flow');",
        shift + "section_1.addItem(flow);",
        shift + "",
        shift + variable + ".addSection(section_1);",
        shift + "",
        shift + "SectionStackSection section_2 = new SectionStackSection('Section 2');",
        shift + "section_2.setExpanded(true);",
        shift + "",
        shift + "Button button = new Button('Button');",
        shift + "section_2.addItem(button);",
        shift + "",
        shift + "Label label = new Label('Label');",
        shift + "section_2.addItem(label);",
        shift + "",
        shift + "IButton ibutton = new IButton('IButton');",
        shift + "section_2.addItem(ibutton);",
        shift + "",
        shift + variable + ".addSection(section_2);",};
  }
}