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
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.HTMLPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link HTMLPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class HTMLPanelTest extends GwtModelTest {
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
   * Test for parsing <code>HTMLPanel.add(Widget,id)</code>.
   */
  public void test_parse_add() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HTMLPanel panel = new HTMLPanel('<div id=\\'one\\'></div>');",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button, 'one');",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.HTMLPanel} {local-unique: panel} {/new HTMLPanel('<div id=\\'one\\'></div>')/ /rootPanel.add(panel)/ /panel.add(button, 'one')/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /panel.add(button, 'one')/}");
    frame.refresh();
    assertInstanceOf(HTMLPanelInfo.class, frame.getChildrenWidgets().get(0));
  }

  /**
   * Test for parsing <code>HTMLPanel.addAndReplaceElement(Widget,id)</code>.
   */
  public void test_parse_addAndReplaceElement() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HTMLPanel panel = new HTMLPanel('<div id=\\'one\\'></div>');",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.addAndReplaceElement(button, 'one');",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.HTMLPanel} {local-unique: panel} {/new HTMLPanel('<div id=\\'one\\'></div>')/ /rootPanel.add(panel)/ /panel.addAndReplaceElement(button, 'one')/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /panel.addAndReplaceElement(button, 'one')/}");
    frame.refresh();
    assertInstanceOf(HTMLPanelInfo.class, frame.getChildrenWidgets().get(0));
  }

  /**
   * Test for <code>HTMLPanel</code> on <code>Composite</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41059
   */
  public void test_parse_onComposite() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    HTMLPanel panel = new HTMLPanel('<div id=\\'one\\'></div>');",
            "    initWidget(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button, 'one');",
            "    }",
            "  }",
            "}");
    assertNoErrors(composite);
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(panel)/}",
        "  {new: com.google.gwt.user.client.ui.HTMLPanel} {local-unique: panel} {/new HTMLPanel('<div id=\\'one\\'></div>')/ /initWidget(panel)/ /panel.add(button, 'one')/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /panel.add(button, 'one')/}");
    // refresh
    composite.refresh();
    assertNoErrors(composite);
  }

  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends HTMLPanel {",
            "  public Test() {",
            "    super('<div/>');",
            "  }",
            "}");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  /**
   * There was bug with not clearing "UiBinderUtil.hiddenDiv" when we don't use
   * {@link HTMLPanelInfo} directly.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47810
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48085
   */
  @DisposeProjectAfter
  public void test_useComposite_withHTMLPanel() throws Exception {
    dontUseSharedGWTState();
    {
      setFileContentSrc(
          "test/client/MyComposite.java",
          getTestSource(
              "import com.google.gwt.uibinder.client.UiBinder;",
              "public class MyComposite extends Composite {",
              "  interface Binder extends UiBinder<Widget, MyComposite> {}",
              "  private static final Binder binder = GWT.create(Binder.class);",
              "  public MyComposite() {",
              "    initWidget(binder.createAndBindUi(this));",
              "  }",
              "}"));
      String namespaces =
          " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
              + " xmlns:g='urn:import:com.google.gwt.user.client.ui'";
      setFileContentSrc(
          "test/client/MyComposite.ui.xml",
          getSourceDQ(
              "<ui:UiBinder" + namespaces + ">",
              "  <g:HTMLPanel>",
              "    Some text <g:Button text='New Button'/>",
              "  </g:HTMLPanel>",
              "</ui:UiBinder>"));
      waitForAutoBuild();
    }
    //
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    MyComposite composite = new MyComposite();",
        "    add(composite);",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    WidgetInfo composite = getJavaInfoByName("composite");
    // assert that Button is in "composite"
    Object compositeElement = composite.getElement();
    String compositeString = compositeElement.toString();
    assertThat(compositeString).containsIgnoringCase("BUTTON");
  }
}