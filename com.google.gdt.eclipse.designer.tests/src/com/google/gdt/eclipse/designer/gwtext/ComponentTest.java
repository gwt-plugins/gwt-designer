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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link ComponentInfo}.
 * 
 * @author sablin_aa
 */
public class ComponentTest extends GwtExtModelTest {
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
   * Users often try to use GWT-Ext in module which is not configured correctly. So, we should check
   * this and show good message.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43409
   */
  @DisposeProjectAfter
  public void test_notConfiguredCorrectly() throws Exception {
    dontUseSharedGWTState();
    // remove Ext scripts
    {
      IFile moduleFile = getFileSrc("test/Module.gwt.xml");
      String content = getFileContent(moduleFile);
      content = StringUtils.replace(content, "<script src=\"", "<script src=\"bad/");
      setFileContent(moduleFile, content);
    }
    // try to parse, failure expected
    try {
      parseJavaInfo(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      Button button = new Button();",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
      fail();
    } catch (Throwable e) {
      DesignerException de = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(de.getCode(), IExceptionConstants.NOT_CONFIGURED);
      assertTrue(DesignerExceptionUtils.isWarning(e));
    }
  }

  /**
   * These properties can not be asked without rendering.
   * <p>
   * Not finished message to GWT-Ext list.
   * <p>
   * I'm working not on GWT-Ext in our GWT Designer product. I have problem with default values for
   * properties.
   * <p>
   * In Swing world, when we just created component, we ask values for all its properties and treat
   * these values as default. So, when user sets some value for property, we check that it is not
   * default and add new component.setFoo(value) invocation. If user sets default value, we remove
   * existing component.setFoo(value) invocation.
   * <p>
   * However in GWT-Ext...
   */
  public void test_noDefaultValues_forSomeProperties() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Button;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    // check properties
    assertSame(Property.UNKNOWN_VALUE, button.getPropertyByTitle("styleName").getValue());
    assertSame(Property.UNKNOWN_VALUE, button.getPropertyByTitle("stylePrimaryName").getValue());
    assertSame(Property.UNKNOWN_VALUE, button.getPropertyByTitle("title").getValue());
  }

  public void test_properties() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Button;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button('button');",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    // check properties
    {
      Property property = button.getPropertyByTitle("text");
      assertNotNull(property);
      assertTrue(property.isModified());
      assertEquals("button", property.getValue());
    }
    {
      Property property = button.getPropertyByTitle("disabled");
      assertNotNull(property);
      assertFalse(property.isModified());
      assertEquals(false, property.getValue());
    }
  }

  public void test_properties_defaultValueUsed() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Button;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    Property disabledProperty = button.getPropertyByTitle("disabled");
    // default value is "false"
    assertEquals(false, disabledProperty.getValue());
    // set "disabled" to "true"
    disabledProperty.setValue(true);
    assertEditor(
        "import com.gwtext.client.widgets.Button;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      button.setDisabled(true);",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    // set "disabled" to "false", i.e. default value, so invocation should be removed
    disabledProperty.setValue(false);
    assertEditor(
        "import com.gwtext.client.widgets.Button;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When GWT-Ext creates <code>Component</code>, it sets it new unique ID. This ID is used for
   * checking that component instance was rendered, etc. If component was rendered, we can not set
   * some properties, such as layout. Usually this is not problem, because for each instance of
   * Component, new ID is generated.
   * <p>
   * However when we sets some specific ID for component, GWT-Ext thinks after refresh that we still
   * mean old component, that was rendered, so prevents <code>setLayout()</code> execution.
   * <p>
   * We should somehow dispose/destroy components.
   */
  public void test_componentWithID() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Panel panel = new Panel('Title');",
            "      panel.setId('my-panel');",
            "      panel.setLayout(new RowLayout());",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel('Title')/ /panel.setId('my-panel')/ /panel.setLayout(new RowLayout())/ /rootPanel.add(panel)/}",
        "    {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/panel.setLayout(new RowLayout())/}");
    //
    frame.refresh();
    assertNoErrors(frame);
  }

  /**
   * GWT-Ext component renders itself on <code>RootPanel</code> when we call
   * <code>getElement()</code>, so it will be attached even if there are no association in source
   * code. To solve this we should remove any dangling models directly after parsing.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42199
   */
  public void test_parseDangling() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private  Button button = new Button();",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    //
    frame.refresh();
    assertNoErrors(frame);
  }

  /**
   * When <code>new Viewport(panel)</code> is used, this means that this panel should be root.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42699
   */
  public void test_parseViewport() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Panel panel = new Panel();",
            "    new Viewport(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel()/ /new Viewport(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}");
    //
    panel.refresh();
    assertNoErrors(panel);
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isEqualTo(300);
    }
  }

  /**
   * When <code>new Viewport(panel)</code> is used, this means that this panel should be root.
   * <p>
   * So, we should remove <code>RootPanel</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42699
   */
  public void test_parseViewport_withRootPanel() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Panel panel = new Panel();",
            "    new Viewport(panel);",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel()/ /new Viewport(panel)/ /rootPanel.add(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}");
    //
    panel.refresh();
    assertNoErrors(panel);
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isEqualTo(300);
    }
  }
}