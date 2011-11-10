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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;

import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.InsValue;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link ContentPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class ContentPanelTest extends GxtModelTest {
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
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    panel.refresh();
    // animation should be disabled
    assertEquals(false, ReflectionUtils.invokeMethod(panel.getObject(), "getAnimCollapse()"));
  }

  /**
   * When we exit from binary execution flow into AST, we render <code>Widget</code>. But after this
   * we can not call <code>setAnimCollapse()</code> method.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48049
   */
  public void test_parse_whenExitFromBinaryExecutionFlow() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "  protected void onResize(int width, int height) {",
            "    super.onResize(width, height);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    panel.refresh();
    // animation should be disabled
    assertEquals(false, ReflectionUtils.invokeMethod(panel.getObject(), "getAnimCollapse()"));
    // ..and size is still good, even if we override onResize()
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
  }

  /**
   * We don't need "client area insets" for not rendered ContentPanel, and we can not ask them in
   * any case.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43588
   */
  public void test_ifNotRendered() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends CardPanel {",
            "  public Test() {",
            "    {",
            "      ContentPanel panel_1 = new ContentPanel();",
            "      add(panel_1);",
            "    }",
            "    {",
            "      ContentPanel panel_2 = new ContentPanel();",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    container.refresh();
    // only first ContentPanel is rendered, but no exceptions
    {
      ContentPanelInfo panel_1 = getJavaInfoByName("panel_1");
      assertTrue(panel_1.isRendered());
    }
    {
      ContentPanelInfo panel_2 = getJavaInfoByName("panel_2");
      assertFalse(panel_2.isRendered());
    }
  }

  /**
   * In <code>ContentPanel</code> was not created and replaced with placeholder, we should not call
   * its methods and fail because of this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43698
   */
  public void test_ifExceptionInCreation() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends ContentPanel {",
            "  public MyPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MyPanel panel = new MyPanel();",
            "    add(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyPanel} {local-unique: panel} {/new MyPanel()/ /add(panel)/}");
    container.refresh();
    // "panel" is placeholder, but ContentPanel
    ContentPanelInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.isPlaceholder());
    assertInstanceOf("com.extjs.gxt.ui.client.widget.ContentPanel", panel.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Client area insets
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clientAreaInsets() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // check insets
    Insets insets = panel.getClientAreaInsets();
    assertEquals(new Insets(26, 1, 1, 1), insets);
  }

  /**
   * Check that "model" bounds of <code>Button</code> on <code>ContentPanel</code> is valid,
   * calculated with applying "client area insets" of <code>ContentPanel</code>.
   */
  public void test_clientAreaInsets_appliedToModelBounds_ofChildren() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = getJavaInfoByName("button");
    //
    Insets insets = panel.getClientAreaInsets();
    Rectangle buttonParentBounds = button.getBounds();
    Rectangle buttonModelBounds = button.getModelBounds();
    assertEquals(new Insets(26, 1, 1, 1), insets);
    assertEquals(buttonParentBounds.x, buttonModelBounds.x + insets.left);
    assertEquals(buttonParentBounds.y, buttonModelBounds.y + insets.top);
  }

  /**
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=4767
   */
  public void test_clientAreaInsets_whenOnFitLayout() throws Exception {
    parseJavaInfo(
        "public class Test extends Dialog {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      ContentPanel contentPanel = new ContentPanel();",
        "      contentPanel.setLayout(new AbsoluteLayout());",
        "      add(contentPanel, new FitData(10));",
        "      {",
        "        Button button = new Button();",
        "        contentPanel.add(button, new AbsoluteData(20, 30));",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    ContentPanelInfo contentPanel = getJavaInfoByName("contentPanel");
    ComponentInfo button = getJavaInfoByName("button");
    //
    Insets insets = contentPanel.getClientAreaInsets();
    Rectangle buttonParentBounds = button.getBounds();
    Rectangle buttonModelBounds = button.getModelBounds();
    assertEquals(new Insets(36, 11, 11, 11), insets);
    assertEquals(new Point(20, 30), buttonModelBounds.getLocation());
    assertEquals(buttonParentBounds.x, buttonModelBounds.x + insets.left);
    assertEquals(buttonParentBounds.y, buttonModelBounds.y + insets.top);
  }

  public void test_clientAreaInsets_withTopBottomComponents() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button top = new Button();",
            "      setTopComponent(top);",
            "    }",
            "    {",
            "      Button bottom = new Button();",
            "      setBottomComponent(bottom);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // check insets
    Insets insets = panel.getClientAreaInsets();
    Insets expected =
        Expectations.get(new Insets(48, 1, 23, 1), new InsValue[]{
            new InsValue("Flanker-Windows", new Insets(48, 1, 23, 1)),
            new InsValue("SABLIN-AA", new Insets(46, 1, 23, 1))});
    assertEquals(expected, insets);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setTopComponent()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContentPanelInfo#hasTopComponent()}.
   */
  public void test_hasTopComponent() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Text text = new Text();",
            "      setTopComponent(text);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {/setTopComponent(text)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.Text} {local-unique: text} {/new Text()/ /setTopComponent(text)/}");
    panel.refresh();
    // initially "text" is "top component"
    ComponentInfo text = getJavaInfoByName("text");
    assertTrue(panel.hasTopComponent());
    assertThat(text.getAssociation()).isInstanceOfAny(InvocationChildAssociation.class);
    // delete, no "top component"
    text.delete();
    assertFalse(panel.hasTopComponent());
    assertEditor(
        "// filler filler filler",
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link ContentPanelInfo#setTopComponent(ComponentInfo)}.
   */
  public void test_setTopComponent_CREATE() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    panel.refresh();
    assertFalse(panel.hasTopComponent());
    // set "button" as "top component"
    ComponentInfo button = createButton();
    panel.setTopComponent(button);
    panel.refresh();
    assertEditor(
        "// filler filler filler",
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      setTopComponent(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {/setTopComponent(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /setTopComponent(button)/}");
    assertTrue(panel.hasTopComponent());
  }

  /**
   * Test for {@link ContentPanelInfo#setTopComponent(ComponentInfo)}.
   */
  public void test_setTopComponent_MOVE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      ContentPanel panel = new ContentPanel();",
            "      add(panel);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    //
    ContentPanelInfo panel = getJavaInfoByName("panel");
    ComponentInfo button = getJavaInfoByName("button");
    panel.setTopComponent(button);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      ContentPanel panel = new ContentPanel();",
        "      {",
        "        Button button = new Button();",
        "        panel.setTopComponent(button);",
        "      }",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setBottomComponent()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContentPanelInfo#hasBottomComponent()}.
   */
  public void test_hasBottomComponent() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Text text = new Text();",
            "      setBottomComponent(text);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {/setBottomComponent(text)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.Text} {local-unique: text} {/new Text()/ /setBottomComponent(text)/}");
    panel.refresh();
    // initially "text" is "bottom component"
    ComponentInfo text = getJavaInfoByName("text");
    assertTrue(panel.hasBottomComponent());
    assertThat(text.getAssociation()).isInstanceOfAny(InvocationChildAssociation.class);
    // delete, no "bottom component"
    text.delete();
    assertFalse(panel.hasBottomComponent());
    assertEditor(
        "// filler filler filler",
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link ContentPanelInfo#setBottomComponent(ComponentInfo)}.
   */
  public void test_setBottomComponent_CREATE() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    panel.refresh();
    assertFalse(panel.hasBottomComponent());
    // set "button" as "bottom component"
    ComponentInfo button = createButton();
    panel.setBottomComponent(button);
    panel.refresh();
    assertEditor(
        "// filler filler filler",
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      setBottomComponent(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {/setBottomComponent(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /setBottomComponent(button)/}");
    assertTrue(panel.hasBottomComponent());
  }

  /**
   * Test for {@link ContentPanelInfo#setBottomComponent(ComponentInfo)}.
   */
  public void test_setBottomComponent_MOVE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      ContentPanel panel = new ContentPanel();",
            "      add(panel);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    //
    ContentPanelInfo panel = getJavaInfoByName("panel");
    ComponentInfo button = getJavaInfoByName("button");
    panel.setBottomComponent(button);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      ContentPanel panel = new ContentPanel();",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomComponent(button);",
        "      }",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ButtonBar
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Support for understanding <code>ContentPanel.addButton(Button)</code>.
   */
  public void test_ButtonBar_parse() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button otherButton = new Button();",
            "      add(otherButton);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      addButton(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.ContentPanel} {this} {/add(otherButton)/ /addButton(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: otherButton} {/new Button()/ /add(otherButton)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /addButton(button)/}");
    // association using "addButton()" invocation
    ButtonInfo button = getJavaInfoByName("button");
    assertThat(button.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
    // only "button" is on "ButtonBar"
    assertThat(panel.getButtonBarButtons()).containsExactly(button);
  }

  /**
   * Test for {@link ContentPanelInfo#getButtonBarBounds()}.
   */
  public void test_ButtonBar_getButtonBarBounds_withButton() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      addButton(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    Rectangle buttonBarBounds = panel.getButtonBarBounds();
    assertThat(buttonBarBounds.width).isGreaterThan(400);
    assertThat(buttonBarBounds.height).isGreaterThan(25);
    assertThat(buttonBarBounds.x).isEqualTo(5);
    Integer expected =
        Expectations.get(300 - 5 - buttonBarBounds.height, new IntValue[]{
            new IntValue("Flanker-Windows", new Integer(300 - 5 - buttonBarBounds.height)),
            new IntValue("SABLIN-AA", new Integer(300 - 6 - buttonBarBounds.height))});
    assertThat(buttonBarBounds.y).isEqualTo(expected);
  }

  /**
   * Test for {@link ContentPanelInfo#getButtonBarBounds()}.
   */
  public void test_ButtonBar_getButtonBarBounds_noButton() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    Rectangle buttonBarBounds = panel.getButtonBarBounds();
    assertEquals(new Rectangle(0, 0, 0, 0), buttonBarBounds);
  }

  /**
   * Test for {@link ContentPanelInfo#command_ButtonBar_CREATE(ButtonInfo, ButtonInfo)}.
   */
  public void test_ButtonBar_CREATE() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // create new Button
    ButtonInfo button = createButton();
    panel.command_ButtonBar_CREATE(button, null);
    // only "button" is on "ButtonBar"
    assertThat(panel.getButtonBarButtons()).containsExactly(button);
  }

  /**
   * Test for {@link ContentPanelInfo#command_ButtonBar_MOVE(ButtonInfo, ButtonInfo)}.
   */
  public void test_ButtonBar_MOVE() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      addButton(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addButton(button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ButtonInfo button_1 = getJavaInfoByName("button_1");
    ButtonInfo button_2 = getJavaInfoByName("button_2");
    // move "button_2" before "button_1"
    panel.command_ButtonBar_MOVE(button_2, button_1);
    assertEditor(
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      addButton(button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addButton(button_1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Problem with "parent" of top/bottom components
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>Component</code> set as "top" should have <code>ContentPanel</code> as parent.
   * <p>
   * http://www.extjs.com/forum/showthread.php?p=396823
   */
  public void test_setTopComponent_parent() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      setTopComponent(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button = getJavaInfoByName("button");
    Object buttonObject = button.getObject();
    Object buttonObjectParent = ScriptUtils.evaluate("getParent()", buttonObject);
    assertNotNull(buttonObjectParent);
    assertSame(panel.getObject(), buttonObjectParent);
  }

  /**
   * <code>Component</code> set as "bottom" should have <code>ContentPanel</code> as parent.
   * <p>
   * http://www.extjs.com/forum/showthread.php?p=396823
   */
  public void test_setBottomComponent_parent() throws Exception {
    ContentPanelInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      setBottomComponent(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button = getJavaInfoByName("button");
    Object buttonObject = button.getObject();
    Object buttonObjectParent = ScriptUtils.evaluate("getParent()", buttonObject);
    assertNotNull(buttonObjectParent);
    assertSame(panel.getObject(), buttonObjectParent);
  }

  /**
   * <code>ContentPanel.setTopComponent()</code> does not set parent, so we don't show such exposed
   * widget. So, we have to fix this in our model.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=3700#p12705
   */
  public void test_setTopComponent_parent_exposedChild() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  private ContentPanel m_panel = new ContentPanel();",
            "  private Button m_button = new Button();",
            "  public MyComposite() {",
            "    m_panel.setTopComponent(m_button);",
            "    initComponent(m_panel);",
            "    m_panel.layout();",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MyComposite composite = new MyComposite();",
        "    add(composite);",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(composite)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyComposite} {local-unique: composite} {/new MyComposite()/ /add(composite)/}",
        "    {method: public com.extjs.gxt.ui.client.widget.button.Button test.client.MyComposite.getButton()} {property} {}");
  }
}