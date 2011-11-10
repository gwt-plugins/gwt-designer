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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutTest extends GxtModelTest {
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
   * We don't show {@link LayoutInfo} in tree or on canvas.
   */
  public void test_presentation() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "  }",
            "}");
    LayoutInfo layout = container.getLayout();
    assertVisible(layout, false);
  }

  /**
   * Test for {@link LayoutInfo#isActive()}.
   */
  public void test_isActive() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    container.refresh();
    // "default" is active
    LayoutInfo defaultLayout = container.getLayout();
    assertTrue(defaultLayout.isActive());
    assertSame(container, defaultLayout.getContainer());
    // set FlowLayout
    LayoutInfo flowLayout;
    {
      flowLayout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FlowLayout");
      container.setLayout(flowLayout);
      assertHierarchy(
          "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout(5))/}",
          "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}");
      assertSame(flowLayout, container.getLayout());
    }
    // RowLayout is active now
    assertTrue(flowLayout.isActive());
    assertFalse(defaultLayout.isActive());
    // both layouts bounds to same container
    assertSame(container, flowLayout.getContainer());
    assertSame(container, defaultLayout.getContainer());
  }

  /**
   * Test that {@link LayoutInfo} contributes property "Layout" to its parent
   * {@link LayoutContainerInfo}.
   */
  public void test_propertyLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}");
    // check "Layout" property
    Property layoutProperty = container.getPropertyByTitle("Layout");
    assertNotNull(layoutProperty);
    assertEquals(
        "(com.extjs.gxt.ui.client.widget.layout.FlowLayout)",
        getPropertyText(layoutProperty));
    assertTrue(layoutProperty.isModified());
    assertThat(getSubProperties(layoutProperty).length).isGreaterThan(5);
    // "Layout" property is cached
    assertSame(layoutProperty, container.getPropertyByTitle("Layout"));
    // delete Layout using property
    layoutProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor("public class Test extends LayoutContainer {", "  public Test() {", "  }", "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getWidgets()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#getWidgets()}.
   * <p>
   * Usual situation - container is empty by default and widget added.
   */
  public void test_getWidgets_emptyContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(5));",
            "    }",
            "  }",
            "}");
    LayoutInfo layout = container.getLayout();
    assertThat(layout.getWidgets()).isEqualTo(container.getWidgets());
  }

  /**
   * Test for {@link LayoutInfo#getWidgets()}.
   * <p>
   * Container has directly exposed widget, i.e. widget object is really on container object.
   */
  public void test_getWidgets_directlyExposed() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyContainer.java",
        getTestSource(
            "public class MyContainer extends LayoutContainer {",
            "  private Button button = new Button();",
            "  public MyContainer() {",
            "    setLayout(new FlowLayout());",
            "    add(button);",
            "  }",
            "  public Button getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyContainer {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.client.MyContainer} {this} {}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {implicit-layout} {}",
        "  {method: public com.extjs.gxt.ui.client.widget.button.Button test.client.MyContainer.getButton()} {property} {}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    LayoutInfo layout = container.getLayout();
    WidgetInfo widget = container.getWidgets().get(0);
    // getWidgets()
    assertThat(layout.getWidgets()).containsExactly(widget);
    // isManagedObject()
    assertThat(layout.isManagedObject(widget)).isTrue();
    assertThat(layout.isManagedObject(layout)).isFalse();
  }

  /**
   * Test for {@link LayoutInfo#getWidgets()}.
   * <p>
   * Container has indirectly exposed widget, i.e. widget object is really on sub-container object.
   */
  public void test_getWidgets_indirectlyExposed() throws Exception {
    dontUseSharedGWTState();
    String[] lines =
        {
            "public class MyContainer extends LayoutContainer {",
            "  private Button button = new Button();",
            "  public MyContainer() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      LayoutContainer inner = new LayoutContainer();",
            "      inner.add(button);",
            "      add(inner);",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return button;",
            "  }",
            "}"};
    setFileContentSrc("test/client/MyContainer.java", getTestSource(lines));
    waitForAutoBuild();
    // parse
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyContainer {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.client.MyContainer} {this} {}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {implicit-layout} {}",
        "  {method: public com.extjs.gxt.ui.client.widget.button.Button test.client.MyContainer.getButton()} {property} {}");
    LayoutInfo layout = container.getLayout();
    WidgetInfo widget = container.getWidgets().get(0);
    // getWidgets()
    assertThat(layout.getWidgets()).isEmpty();
    // isManagedObject()
    assertThat(layout.isManagedObject(widget)).isFalse();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If explicit {@link LayoutDataInfo} exists for {@link WidgetInfo}, it should be used, not
   * virtual.
   */
  public void test_explicitLayoutData() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(5));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button, new FlowData(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new FlowData(5))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowData} {empty} {/add(button, new FlowData(5))/}");
    //
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    assertNotNull(layoutData);
    assertThat(layoutData.getCreationSupport()).isInstanceOf(ConstructorCreationSupport.class);
  }

  /**
   * {@link WidgetInfo} children should have {@link LayoutDataInfo}, at least virtual.
   */
  public void test_virtualLayoutData() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    //
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    assertNotNull(layoutData);
    assertThat(layoutData.getCreationSupport()).isInstanceOf(VirtualLayoutDataCreationSupport.class);
  }

  /**
   * When we set {@link LayoutInfo}, it should add virtual {@link LayoutDataInfo} for each
   * {@link WidgetInfo}.
   */
  public void test_createLayoutData_whenSet() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}");
    // set FlowLayout
    LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FlowLayout");
    container.setLayout(layout);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout(5));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/ /setLayout(new FlowLayout(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
  }

  /**
   * When we delete {@link LayoutInfo} we should also delete {@link LayoutDataInfo} for each
   * {@link WidgetInfo}.
   */
  public void test_deleteLayoutData_whenDeleteLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(10));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button, new FlowData(10))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new FlowData(10))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowData} {empty} {/add(button, new FlowData(10))/}");
    // delete FlowLayout
    container.getLayout().delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  /**
   * When we delete {@link LayoutInfo} we should also delete {@link LayoutDataInfo} for each
   * {@link WidgetInfo}.
   */
  public void test_deleteLayoutData_includingVirtual_whenDeleteLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    // delete FlowLayout
    container.getLayout().delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  /**
   * Delete {@link WidgetInfo}. No much testing here, just improve coverage.
   */
  public void test_deleteWidget() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(10));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button, new FlowData(10))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new FlowData(10))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowData} {empty} {/add(button, new FlowData(10))/}");
    // delete Button
    container.getWidgets().get(0).delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}");
  }

  /**
   * Delete {@link LayoutDataInfo}, virtual one should be created.
   */
  public void test_deleteLayoutData() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(10));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button, new FlowData(10))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new FlowData(10))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowData} {empty} {/add(button, new FlowData(10))/}");
    // delete LayoutData
    WidgetInfo button = container.getWidgets().get(0);
    LayoutInfo.getLayoutData(button).delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
  }

  /**
   * When we create new {@link WidgetInfo} should set virtual {@link LayoutDataInfo} for it.
   */
  public void test_createLayoutData_whenCreate() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}");
    // add new Button
    ComponentInfo newButton = createButton();
    ((FlowLayoutInfo) container.getLayout()).command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
  }

  /**
   * When we move {@link WidgetInfo} we should remove existing {@link LayoutDataInfo} and add new
   * virtual one.
   */
  public void test_deleteExistingLayoutData_createVirtualLayoutData_whenMove() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      LayoutContainer container_1 = new LayoutContainer();",
            "      container_1.setLayout(new FlowLayout());",
            "      {",
            "        Button button = new Button();",
            "        container_1.add(button, new FlowData(10));",
            "      }",
            "      add(container_1);",
            "    }",
            "    {",
            "      LayoutContainer container_2 = new LayoutContainer();",
            "      container_2.setLayout(new RowLayout());",
            "      add(container_2);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(container_1)/ /add(container_2)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: container_1} {/new LayoutContainer()/ /container_1.setLayout(new FlowLayout())/ /container_1.add(button, new FlowData(10))/ /add(container_1)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/container_1.setLayout(new FlowLayout())/}",
        "    {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /container_1.add(button, new FlowData(10))/}",
        "      {new: com.extjs.gxt.ui.client.widget.layout.FlowData} {empty} {/container_1.add(button, new FlowData(10))/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: container_2} {/new LayoutContainer()/ /container_2.setLayout(new RowLayout())/ /add(container_2)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/container_2.setLayout(new RowLayout())/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
    LayoutContainerInfo container_1 = (LayoutContainerInfo) container.getWidgets().get(0);
    LayoutContainerInfo container_2 = (LayoutContainerInfo) container.getWidgets().get(1);
    WidgetInfo button = container_1.getWidgets().get(0);
    // move
    ((RowLayoutInfo) container_2.getLayout()).command_MOVE(button, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      LayoutContainer container_1 = new LayoutContainer();",
        "      container_1.setLayout(new FlowLayout());",
        "      add(container_1);",
        "    }",
        "    {",
        "      LayoutContainer container_2 = new LayoutContainer();",
        "      container_2.setLayout(new RowLayout());",
        "      {",
        "        Button button = new Button();",
        "        container_2.add(button);",
        "      }",
        "      add(container_2);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(container_1)/ /add(container_2)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: container_1} {/new LayoutContainer()/ /container_1.setLayout(new FlowLayout())/ /add(container_1)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/container_1.setLayout(new FlowLayout())/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "  {new: com.extjs.gxt.ui.client.widget.LayoutContainer} {local-unique: container_2} {/new LayoutContainer()/ /container_2.setLayout(new RowLayout())/ /add(container_2)/ /container_2.add(button)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/container_2.setLayout(new RowLayout())/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}",
        "    {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /container_2.add(button)/}",
        "      {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation: name, based on template
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_nameTemplate(String template, String... lines) throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      LayoutContainer container = new LayoutContainer();",
            "      container.setLayout(new FlowLayout(5));",
            "      add(container);",
            "    }",
            "  }",
            "}");
    container.refresh();
    LayoutContainerInfo innerContainer = container.getChildren(LayoutContainerInfo.class).get(0);
    LayoutInfo layout = innerContainer.getLayout();
    //
    Activator.getDefault().getPreferenceStore().setValue(
        com.google.gdt.eclipse.designer.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        template);
    //
    layout.getPropertyByTitle("removePositioning").setValue(true);
    assertEditor(lines);
  }

  /**
   * Template "${defaultName}" means that name should be based on name of type.
   */
  public void test_nameTemplate_useDefaultName() throws Exception {
    check_nameTemplate(
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT,
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      FlowLayout flowLayout = new FlowLayout(5);",
        "      flowLayout.setRemovePositioning(true);",
        "      container.setLayout(flowLayout);",
        "      add(container);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${layoutAcronym}${containerName-cap}" template.
   */
  public void test_nameTemplate_alternativeTemplate_1() throws Exception {
    check_nameTemplate(
        "${layoutAcronym}${containerName-cap}",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      FlowLayout flContainer = new FlowLayout(5);",
        "      flContainer.setRemovePositioning(true);",
        "      container.setLayout(flContainer);",
        "      add(container);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${containerName}${layoutClassName}" template.
   */
  public void test_nameTemplate_alternativeTemplate_2() throws Exception {
    check_nameTemplate(
        "${containerName}${layoutClassName}",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      FlowLayout containerFlowLayout = new FlowLayout(5);",
        "      containerFlowLayout.setRemovePositioning(true);",
        "      container.setLayout(containerFlowLayout);",
        "      add(container);",
        "    }",
        "  }",
        "}");
  }
}