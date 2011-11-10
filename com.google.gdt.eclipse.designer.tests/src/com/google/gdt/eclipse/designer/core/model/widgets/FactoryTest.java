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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Test for static/instance factory in GWT.
 * 
 * @author scheglov_ke
 */
public class FactoryTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
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
  // Static factory
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_staticFactory_parse() throws Exception {
    setFileContentSrc(
        "test/client/StaticFactory.java",
        getTestSource(
            "public class StaticFactory {",
            "  public static Button createButton() {",
            "    Button button = new Button();",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(StaticFactory.createButton());",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(StaticFactory.createButton())/}",
        "  {static factory: test.client.StaticFactory createButton()} {empty} {/rootPanel.add(StaticFactory.createButton())/}");
    frame.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance factory
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_instanceFactory_parse() throws Exception {
    setFileContentSrc(
        "test/client/InstanceFactory.java",
        getTestSource(
            "public class InstanceFactory {",
            "  public Button createButton() {",
            "    Button button = new Button();",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private InstanceFactory factory = new InstanceFactory();",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(factory.createButton());",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(factory.createButton())/}",
        "  {instance factory: {field-initializer: factory} createButton()} {empty} {/rootPanel.add(factory.createButton())/}",
        "  {instance factory container}",
        "    {new: test.client.InstanceFactory} {field-initializer: factory} {/new InstanceFactory()/ /factory.createButton()/}");
    frame.refresh();
  }
}