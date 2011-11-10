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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test {@link CompositeInfo}.
 * 
 * @author scheglov_ke
 */
public class CompositeTest extends GwtModelTest {
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
            "      initWidget(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(button)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /initWidget(button)/}");
    WidgetInfo button = composite.getWidget();
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

  public void test_withVerticalPanel() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    initWidget(new VerticalPanel());",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(new VerticalPanel())/}",
        "  {new: com.google.gwt.user.client.ui.VerticalPanel} {empty} {/initWidget(new VerticalPanel())/}");
  }

  public void test_preFilled() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initWidget(new Button());",
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
    assertNull(composite.getWidget());
  }

  public void test_empty() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Composite {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: com.google.gwt.user.client.ui.Composite} {this} {}");
    // do refresh()
    composite.refresh();
    assertTrue(composite.isEmpty());
    assertNull(composite.getWidget());
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
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      rootPanel.add(composite);",
            "    }",
            "  }",
            "}");
    frame.refresh();
  }

  /**
   * Test for using empty <code>Composite</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47817
   */
  public void test_whenNoWidget() throws Exception {
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
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyComposite composite = new MyComposite();",
        "      rootPanel.add(composite);",
        "    }",
        "  }",
        "}");
    refresh();
    CompositeInfo composite = getJavaInfoByName("composite");
    // "MyComposite" does not set Widget, but we should do this (in reality parse with fail otherwise)
    assertTrue(composite.isEmpty());
    {
      Object widget = ReflectionUtils.invokeMethod(composite.getObject(), "getWidget()");
      assertNotNull(widget);
    }
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
            "      initWidget(button);",
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
            "      initWidget(button);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    // add setWidth()
    composite.addMethodInvocation(
        "setWidth(java.lang.String)",
        StringConverter.INSTANCE.toJavaSource(null, "150px"));
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      initWidget(button);",
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
            "    initWidget(new Button());",
            "  }",
            "}"));
    waitForAutoBuild();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      rootPanel.add(composite);",
            "    }",
            "    rootPanel.setTitle('RootPanel');",
            "  }",
            "}");
    frame.refresh();
    CompositeInfo composite = (CompositeInfo) frame.getChildrenWidgets().get(0);
    // add setWidth()
    composite.addMethodInvocation(
        "setWidth(java.lang.String)",
        StringConverter.INSTANCE.toJavaSource(null, "150px"));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyComposite composite = new MyComposite();",
        "      rootPanel.add(composite);",
        "      composite.setWidth('150px');",
        "    }",
        "    rootPanel.setTitle('RootPanel');",
        "  }",
        "}");
    // refresh
    frame.refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // shouldDrawDotsBorder()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * By default <code>Composite</code> draws dots.
   */
  public void test_shouldDrawDotsBorder_true() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initWidget(new Button());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      rootPanel.add(composite);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CompositeInfo composite = (CompositeInfo) frame.getChildrenWidgets().get(0);
    //
    assertTrue(composite.shouldDrawDotsBorder());
  }

  /**
   * We can disable dots drawing.
   */
  public void test_shouldDrawDotsBorder_false() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    initWidget(new Button());",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='shouldDrawBorder'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyComposite composite = new MyComposite();",
            "      rootPanel.add(composite);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CompositeInfo composite = (CompositeInfo) frame.getChildrenWidgets().get(0);
    //
    assertFalse(composite.shouldDrawDotsBorder());
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
            "      initWidget(button);",
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
            "      initWidget(button);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    // set external size
    composite.getTopBoundsSupport().setSize(300, 150);
    composite.refresh();
    // check Composite bounds
    Rectangle bounds = composite.getBounds();
    assertEquals(new Rectangle(0, 0, 300, 150), bounds);
  }

  /**
   * I don't know why users try to set "0" as size, but they do. :-)
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41768
   */
  public void test_badSize() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    {",
            "      AbsolutePanel absolutePanel = new AbsolutePanel();",
            "      initWidget(absolutePanel);",
            "      absolutePanel.setSize('450px', '300px');",
            "      {",
            "        Label label = new Label('New label');",
            "        absolutePanel.add(label, 10, 10);",
            "      }",
            "    }",
            "    setSize('400px', '0px');",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
    // check Composite bounds
    Rectangle bounds = composite.getBounds();
    assertEquals(new Rectangle(0, 0, 400, 0), bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Simple container
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Direct <code>Composite</code> subclass is simple container.
   */
  public void test_simpleContainer_good() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  public Test() {",
            "  }",
            "}");
    // is plain Composite, so simple container
    assertHasWidgetSimpleContainer(composite, true, true);
  }

  /**
   * While <code>TabPanel</code> is subclass of <code>Composite</code>, it is not simple container.
   */
  public void test_simpleContainer_TabPanel() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
            "  }",
            "}");
    // TabPanel is not simple container
    assertHasWidgetSimpleContainer(composite, true, false);
  }
}