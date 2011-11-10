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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.editor.DesignPageSite;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import static org.easymock.EasyMock.capture;

import org.easymock.Capture;
import org.easymock.EasyMock;

/**
 * Test for GXT untyped events support.
 * 
 * @author scheglov_ke
 */
public class UntypedEventsTest extends GxtModelTest {
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
  public void test_eventsInContextMenu() throws Exception {
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
    ComponentInfo button = getJavaInfoByName("button");
    //
    IMenuManager contextMenu = getContextMenu(button);
    {
      IMenuManager eventManager = findChildMenuManager(contextMenu, "BoxComponentEvent");
      assertNotNull(eventManager);
      assertNotNull(findChildAction(eventManager, "Move"));
      assertNotNull(findChildAction(eventManager, "Resize"));
    }
    {
      IMenuManager eventManager = findChildMenuManager(contextMenu, "ButtonEvent");
      assertNotNull(eventManager);
      assertNotNull(findChildAction(eventManager, "Select"));
      assertNotNull(findChildAction(eventManager, "MenuShow"));
    }
    {
      IMenuManager eventManager = findChildMenuManager(contextMenu, "ComponentEvent");
      assertNotNull(eventManager);
      assertNotNull(findChildAction(eventManager, "Attach"));
      assertNotNull(findChildAction(eventManager, "Render"));
    }
  }

  public void test_openNewListener() throws Exception {
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
    ComponentInfo button = getJavaInfoByName("button");
    // prepare action
    IAction action;
    {
      IMenuManager contextMenu = getContextMenu(button);
      IMenuManager eventManager = findChildMenuManager(contextMenu, "ButtonEvent");
      assertNotNull(eventManager);
      action = findChildAction(eventManager, "Select");
      assertNotNull(action);
    }
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      pageSite.openSourcePosition(capture(openSourcePosition));
      EasyMock.replay(pageSite);
      // do set
      DesignPageSite.Helper.setSite(button, pageSite);
    }
    // run action
    action.runWithEvent(new Event());
    waitEventLoop(0);
    // verify
    EasyMock.verify(pageSite);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      button.addListener(Events.Select, new Listener<ButtonEvent>() {",
        "        public void handleEvent(ButtonEvent e) {",
        "        }",
        "      });",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    // check captured position
    {
      assertTrue(openSourcePosition.hasCaptured());
      int position = openSourcePosition.getValue().intValue();
      assertEquals(getNode("button.addListener").getStartPosition(), position);
    }
  }

  public void test_openExistingListener() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.addListener(Events.Select, new Listener<ButtonEvent>() {",
        "        public void handleEvent(ButtonEvent e) {",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    // prepare action
    IAction action;
    {
      IMenuManager contextMenu = getContextMenu(button);
      IMenuManager eventManager = findChildMenuManager(contextMenu, "ButtonEvent");
      assertNotNull(eventManager);
      action = findChildAction(eventManager, "Select");
      assertNotNull(action);
    }
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      pageSite.openSourcePosition(capture(openSourcePosition));
      EasyMock.replay(pageSite);
      // do set
      DesignPageSite.Helper.setSite(button, pageSite);
    }
    // run action
    action.runWithEvent(new Event());
    waitEventLoop(0);
    // verify
    EasyMock.verify(pageSite);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.addListener(Events.Select, new Listener<ButtonEvent>() {",
        "        public void handleEvent(ButtonEvent e) {",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
    // check captured position
    {
      assertTrue(openSourcePosition.hasCaptured());
      int position = openSourcePosition.getValue().intValue();
      assertEquals(getNode("button.addListener").getStartPosition(), position);
    }
  }

  public void test_removeListener() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.addListener(Events.Select, new Listener<ButtonEvent>() {",
        "        public void handleEvent(ButtonEvent e) {",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    // prepare action
    IAction action;
    {
      IMenuManager contextMenu = getContextMenu(button);
      IMenuManager eventManager = findChildMenuManager(contextMenu, "ButtonEvent");
      assertNotNull(eventManager);
      action = findChildAction(eventManager, "Select");
      assertNotNull(action);
    }
    // run action
    {
      Event event = new Event();
      event.stateMask = SWT.CTRL;
      action.runWithEvent(event);
    }
    assertEditor(
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
}