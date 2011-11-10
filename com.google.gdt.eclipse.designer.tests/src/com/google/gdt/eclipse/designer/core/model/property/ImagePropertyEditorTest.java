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
package com.google.gdt.eclipse.designer.core.model.property;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.tests.designer.TestUtils;

/**
 * Test for {@link Image_PropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ImagePropertyEditorTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
    createModelType(
        "test.client",
        "MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public void setImage(Image image) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

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
  public void test_noValue() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    Property imageProperty = button.getPropertyByTitle("image");
    assertFalse(imageProperty.isModified());
    assertNull(getPropertyText(imageProperty));
  }

  public void test_asURL() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "      button.setImage(new Image('1.png'));",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    Property imageProperty = button.getPropertyByTitle("image");
    // check initial value
    assertTrue(imageProperty.isModified());
    assertEquals("as URL: 1.png", getPropertyText(imageProperty));
    // test "as URL" sub-property
    {
      Property urlProperty = getPropertyByTitle(getSubProperties(imageProperty), "as URL");
      assertTrue(urlProperty.isModified());
      assertEquals("1.png", getPropertyText(urlProperty));
      // set 1.png
      urlProperty.setValue("2.png");
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      rootPanel.add(button);",
          "      button.setImage(new Image('2.png'));",
          "    }",
          "  }",
          "}");
      // remove
      urlProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_asBundle() throws Exception {
    ensureFolderExists("src/test/client/deep");
    TestUtils.createImagePNG(m_testProject, "src/test/client/first.png", 1, 1);
    TestUtils.createImagePNG(m_testProject, "src/test/client/second.png", 1, 1);
    createModelType(
        "test.client",
        "MyImageBundle.java",
        getTestSource(
            "public interface MyImageBundle extends ImageBundle {",
            "  AbstractImagePrototype first();",
            "  AbstractImagePrototype second();",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "      button.setImage(m_myBundle.first().createImage());",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    Property imageProperty = button.getPropertyByTitle("image");
    // check initial value
    assertTrue(imageProperty.isModified());
    assertEquals(
        "as Bundle: first() from test.client.MyImageBundle",
        getPropertyText(imageProperty));
    // test "as Bundle" sub-property
    {
      Property bundleProperty = getPropertyByTitle(getSubProperties(imageProperty), "as Bundle");
      assertTrue(bundleProperty.isModified());
      assertEquals("first() from test.client.MyImageBundle", getPropertyText(bundleProperty));
      // set second()
      {
        ImageBundleInfo bundle = ImageBundleContainerInfo.getBundles(frame).get(0);
        ImageBundlePrototypeDescription prototype = bundle.getPrototypes().get(1);
        assertEquals("second", prototype.getMethod().getName());
        bundleProperty.setValue(prototype);
      }
      assertEditor(
          "public class Test implements EntryPoint {",
          "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      rootPanel.add(button);",
          "      button.setImage(m_myBundle.second().createImage());",
          "    }",
          "  }",
          "}");
      // remove
      bundleProperty.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  private static final MyImageBundle m_myBundle = GWT.create(MyImageBundle.class);",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      MyButton button = new MyButton();",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
    }
  }
}