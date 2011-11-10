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
import com.google.gdt.eclipse.designer.model.widgets.CustomButtonInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link CustomButtonInfo}.
 * 
 * @author scheglov_ke
 */
public class CustomButtonTest extends GwtModelTest {
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
  public void test_exposedFaces() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    PushButton button = new PushButton('My text');",
            "    rootPanel.add(button);",
            "  }",
            "}");
    frame.refresh();
    CustomButtonInfo button = (CustomButtonInfo) frame.getChildrenWidgets().get(0);
    // check for properties for faces
    hasFace(button, "UpFace");
    hasFace(button, "UpDisabledFace");
    hasFace(button, "UpHoveringFace");
    hasFace(button, "DownFace");
    hasFace(button, "DownDisabledFace");
    hasFace(button, "DownHoveringFace");
    // faces should not be visible as children
    assertThat(button.getPresentation().getChildrenTree()).isEmpty();
    assertThat(button.getPresentation().getChildrenGraphical()).isEmpty();
  }

  private static void hasFace(CustomButtonInfo button, String faceName) throws Exception {
    Property faceProperty = button.getPropertyByTitle(faceName);
    assertNotNull("No property for " + faceName, faceProperty);
    assertTrue(faceProperty.getCategory().isSystem());
  }

  public void test_resetFace() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    PushButton button = new PushButton('My text');",
            "    rootPanel.add(button);",
            "    button.getDownFace().setText('down text');",
            "  }",
            "}");
    frame.refresh();
    CustomButtonInfo button = (CustomButtonInfo) frame.getChildrenWidgets().get(0);
    // check for properties for faces
    button.getPropertyByTitle("DownFace").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    PushButton button = new PushButton('My text');",
        "    rootPanel.add(button);",
        "  }",
        "}");
  }

  /**
   * When set HTML/text/image property, all other properties should be reset.
   */
  public void test_setFaceProperty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      PushButton button = new PushButton();",
            "      button.getUpFace().setText('the text');",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CustomButtonInfo button = (CustomButtonInfo) frame.getChildrenWidgets().get(0);
    // check for properties for faces
    Property upProperty = button.getPropertyByTitle("UpFace");
    Property[] upProperties = getSubProperties(upProperty);
    // check existing "text" property
    {
      Property textProperty = getPropertyByTitle(upProperties, "text");
      assertTrue(textProperty.isModified());
      assertEquals("the text", textProperty.getValue());
    }
    // set "HTML" property
    {
      Property textProperty = getPropertyByTitle(upProperties, "html");
      assertFalse(textProperty.isModified());
      textProperty.setValue("the html");
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      PushButton button = new PushButton();",
          "      button.getUpFace().setHTML('the html');",
          "      rootPanel.add(button);",
          "    }",
          "  }",
          "}");
    }
  }
}