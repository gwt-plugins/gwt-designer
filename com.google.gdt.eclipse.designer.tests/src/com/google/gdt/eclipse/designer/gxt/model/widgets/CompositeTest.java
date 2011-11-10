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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test {@link CompositeInfo}.
 * 
 * @author scheglov_ke
 */
public class CompositeTest extends GxtModelTest {
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
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initComponent(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.Composite} {this} {/initComponent(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /initComponent(button)/}");
    // do refresh()
    composite.refresh();
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
      ComponentInfo button = composite.getComponent();
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    // set new bounds
    {
      composite.getTopBoundsSupport().setSize(500, 250);
      composite.refresh();
      assertEquals(new Rectangle(0, 0, 500, 250), composite.getBounds());
    }
  }

  public void test_preFilled() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initComponent(new Button());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyComposite {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: test.client.MyComposite} {this} {}");
    // do refresh()
    composite.refresh();
    assertFalse(composite.isEmpty());
    assertNull(composite.getComponent());
  }

  public void test_empty() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Composite {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: com.extjs.gxt.ui.client.widget.Composite} {this} {}");
    // do refresh()
    composite.refresh();
    assertTrue(composite.isEmpty());
    assertNull(composite.getComponent());
    // no properties
    assertThat(composite.getProperties()).isEmpty();
  }

  /**
   * Test for using bad <code>Composite</code> replaced with placeholder.
   */
  public void test_whenPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    throw new IllegalStateException('Actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MyComposite composite = new MyComposite();",
        "      add(composite);",
        "    }",
        "    setEnabled(true);",
        "  }",
        "}");
    refresh();
    ComponentInfo composite = getJavaInfoByName("composite");
    // replaced with placeholder
    assertTrue(composite.isPlaceholder());
    assertEquals("com.extjs.gxt.ui.client.widget.Text", composite.getObject().getClass().getName());
  }

  /**
   * Test for using bad <code>Composite</code> without <code>Component</code>.
   */
  public void test_whenNoComponent() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MyComposite composite = new MyComposite();",
        "      add(composite);",
        "    }",
        "  }",
        "}");
    refresh();
    CompositeInfo composite = getJavaInfoByName("composite");
    // "MyComposite" does not set Component, but we should do this (in reality parse with fail otherwise)
    assertTrue(composite.isEmpty());
    {
      Object component = ReflectionUtils.invokeMethod(composite.getObject(), "getComponent()");
      assertNotNull(component);
    }
  }

  /**
   * Some (stupid) users try to use Composite in combination with RootPanel.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44635
   */
  public void test_bugWithRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    frame.refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If Composite has no Widget, we should not show properties.
   */
  public void test_getProperties_noWidget() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Composite {",
            "  public Test() {",
            "  }",
            "}");
    composite.refresh();
    assertThat(composite.getProperties()).isEmpty();
  }

  /**
   * If Composite has Widget, we show properties.
   */
  public void test_getProperties_hasWidget() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initComponent(button);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    // prepare properties
    Property[] properties = composite.getProperties();
    assertThat(properties).isNotEmpty();
    // usual properties
    {
      String[] titles = PropertyUtils.getTitles(properties);
      assertThat(titles).contains("Class");
      assertThat(titles).contains("Size");
      assertThat(titles).contains("styleName");
    }
  }

  /**
   * New method invocations should be added at the end of constructor.
   */
  public void test_this_setProperty() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initComponent(button);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    // add setWidth()
    composite.addMethodInvocation("setWidth(java.lang.String)", "\"150px\"");
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      initComponent(button);",
        "    }",
        "    setWidth('150px');",
        "  }",
        "}");
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  /**
   * New method invocations should be added at the end of constructor.
   */
  public void test_use_setProperty() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initComponent(new Button());",
            "  }",
            "}"));
    waitForAutoBuild();
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      add(composite);",
            "    }",
            "    setEnabled(true);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(composite)/ /setEnabled(true)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: test.client.MyComposite} {local-unique: composite} {/new MyComposite()/ /add(composite)/}");
    container.refresh();
    CompositeInfo composite = getJavaInfoByName("composite");
    // add setWidth()
    composite.addMethodInvocation("setWidth(java.lang.String)", "\"150px\"");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MyComposite composite = new MyComposite();",
        "      add(composite);",
        "      composite.setWidth('150px');",
        "    }",
        "    setEnabled(true);",
        "  }",
        "}");
    // refresh
    container.refresh();
    assertNoErrors(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Apply size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If content widget has forced size, use it.
   */
  public void test_applySize_sizeOfContentWidget() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initComponent(button);",
            "      button.setSize('200px', '100px');",
            "    }",
            "  }",
            "}");
    composite.refresh();
    // check Composite bounds
    Rectangle bounds = composite.getBounds();
    assertEquals(new Rectangle(0, 0, 200, 100), bounds);
  }

  /**
   * If no forced size for content widget, use externally specified size of Composite.
   */
  public void test_applySize_sizeOfComposite() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initComponent(button);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    String source = m_lastEditor.getSource();
    // set external size
    composite.getTopBoundsSupport().setSize(300, 150);
    assertEquals(source, m_lastEditor.getSource());
    composite.refresh();
    // check Composite bounds
    Rectangle bounds = composite.getBounds();
    assertEquals(new Rectangle(0, 0, 300, 150), bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Standard GWT Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * GXT uses "dummy" Element while Component is not attached, and replaces it on parent during
   * attach. But Composite uses same Element as its content, so after such replacement "element"
   * field of Composite is not attached anymore to DOM. We fix this - get "element" from content
   * Widget.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=3609
   */
  public void test_standardComposite_andGXT() throws Exception {
    com.google.gdt.eclipse.designer.model.widgets.CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends com.google.gwt.user.client.ui.Composite {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      initWidget(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /initWidget(button)/}");
    // do refresh()
    composite.refresh();
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
      WidgetInfo button = composite.getWidget();
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    // set new bounds
    {
      composite.getTopBoundsSupport().setSize(500, 250);
      composite.refresh();
      assertEquals(new Rectangle(0, 0, 500, 250), composite.getBounds());
    }
  }
}