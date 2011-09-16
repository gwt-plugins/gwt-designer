/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link HTMLPanelInfo} widget.
 * 
 * @author scheglov_ke
 */
public class HTMLPanelTest extends UiBinderModelTest {
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
   * Even empty <code>HTMLPanel</code> should have some reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:HTMLPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    HTMLPanelInfo panel = getObjectByName("panel");
    // bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  public void test_onlyHTML() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:HTMLPanel wbp:name='panel'>",
        "      <div>first</div>",
        "      <div>second</div>",
        "    </g:HTMLPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:HTMLPanel wbp:name='panel'>");
    refresh();
    HTMLPanelInfo panel = getObjectByName("panel");
    // check that div-s are used
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(40);
    }
  }

  public void test_withWidget() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:HTMLPanel>",
        "    <g:Button wbp:name='button' text='New Button'/>",
        "  </g:HTMLPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:HTMLPanel>",
        "  <g:Button wbp:name='button' text='New Button'>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // bounds
    {
      Rectangle bounds = button.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(65);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  /**
   * There was bug with not clearing "UiBinderUtil.hiddenDiv" when we don't use
   * {@link HTMLPanelInfo} directly.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47810
   */
  @DisposeProjectAfter
  public void test_useComposite_withHTMLPanel() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getJavaSource(
            "import com.google.gwt.dom.client.Element;",
            "public class MyComposite extends Composite {",
            "  interface Binder extends UiBinder<Widget, MyComposite> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public MyComposite() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComposite.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:HTMLPanel>",
            "    Some text <g:Button text='New Button'/>",
            "  </g:HTMLPanel>",
            "</ui:UiBinder>"));
    waitForAutoBuild();
    //
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <t:MyComposite wbp:name='composite'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo composite = getObjectByName("composite");
    // assert that Button is in "composite"
    Object compositeElement = composite.getDOMElement();
    String compositeString = compositeElement.toString();
    assertThat(compositeString).contains("BUTTON");
  }
}