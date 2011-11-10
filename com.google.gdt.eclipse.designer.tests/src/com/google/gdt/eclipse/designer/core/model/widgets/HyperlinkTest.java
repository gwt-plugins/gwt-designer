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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for <code>com.google.gwt.user.client.Hyperlink</code>.
 * 
 * @author scheglov_ke
 */
public class HyperlinkTest extends GwtModelTest {
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
   * Test that constructor parameters are bound to properties, so they are modified and may be
   * externalized.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=2649
   */
  public void test_constructorParametersBinding_1() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Hyperlink('text', 'token'));",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo hyperlink = frame.getChildrenWidgets().get(0);
    // check "text" property
    {
      Property textProperty = hyperlink.getPropertyByTitle("text");
      assertTrue(textProperty.isModified());
      assertEquals("text", textProperty.getValue());
    }
    // check "targetHistoryToken" property
    {
      Property tokenProperty = hyperlink.getPropertyByTitle("targetHistoryToken");
      assertTrue(tokenProperty.isModified());
      assertEquals("token", tokenProperty.getValue());
    }
  }

  /**
   * Test that constructor parameters are bound to properties, so they are modified and may be
   * externalized.
   * <p>
   * http://www.instantiations.com/forum/viewtopic.php?f=11&t=2649
   */
  public void test_constructorPropertiesBinding_2() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new Hyperlink('text', false, 'token'));",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo hyperlink = frame.getChildrenWidgets().get(0);
    // check "text" property
    {
      Property textProperty = hyperlink.getPropertyByTitle("text");
      assertTrue(textProperty.isModified());
      assertEquals("text", textProperty.getValue());
    }
    // check "targetHistoryToken" property
    {
      Property tokenProperty = hyperlink.getPropertyByTitle("targetHistoryToken");
      assertTrue(tokenProperty.isModified());
      assertEquals("token", tokenProperty.getValue());
    }
  }
}