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

import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Test {@link UIObjectUtils}.
 * 
 * @author scheglov_ke
 */
public class UIObjectUtilsTest extends GwtModelTest {
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
   * Test for {@link UIObjectUtils#isObjectVisible(Object)} and
   * {@link UIObjectUtils#setObjectVisible(Object, boolean)}.
   */
  public void test_isVisible_setVisible() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Button());",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo buttonInfo = frame.getChildrenWidgets().get(0);
    Object buttonObject = buttonInfo.getObject();
    // initially "button" is visible
    assertTrue(UIObjectUtils.isObjectVisible(buttonObject));
    // make it invisible
    UIObjectUtils.setObjectVisible(buttonObject, false);
    assertFalse(UIObjectUtils.isObjectVisible(buttonObject));
  }

  /**
   * Test for {@link UIObjectUtils#isObjectVisible(Object)} and
   * {@link UIObjectUtils#setObjectVisible(Object, boolean)}.
   */
  public void test_isVisible_badComponent() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/BadButton.java",
        getTestSource(
            "public class BadButton extends Button {",
            "  public native boolean isVisible() /*-{",
            "    return undefined;",
            "  }-*/;",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new BadButton());",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo buttonInfo = frame.getChildrenWidgets().get(0);
    Object buttonObject = buttonInfo.getObject();
    // "BadButton" has bad implementation of isVisible()
    assertFalse(UIObjectUtils.isObjectVisible(buttonObject));
  }

  /**
   * Test that bad component does not cause GWTD hang.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45386
   */
  public void test_badComponent_withoutElement() throws Exception {
    try {
      parseJavaInfo(
          "// filler filler filler",
          "public class Test extends Widget {",
          "  public Test() {",
          "  }",
          "}");
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_DESIGN_WIDGET, e.getCode());
    }
  }
}