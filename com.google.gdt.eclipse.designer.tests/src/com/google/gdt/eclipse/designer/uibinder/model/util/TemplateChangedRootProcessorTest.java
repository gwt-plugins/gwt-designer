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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link TemplateChangedRootProcessor}.
 * <p>
 * Using component in UiBinder.
 * 
 * @author scheglov_ke
 */
public class TemplateChangedRootProcessorTest extends UiBinderModelTest {
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
   * Test for case when UiBinder widget is used directly.
   */
  @DisposeProjectAfter
  public void test_directWidget() throws Exception {
    dontUseSharedGWTState();
    // prepare MyComponent
    setFileContentSrc(
        "test/client/MyComponent.java",
        getJavaSource(
            "public class MyComponent extends Composite {",
            "  interface Binder extends UiBinder<Widget, MyComponent> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public MyComponent() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel width='100px'/>",
            "</ui:UiBinder>"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <t:MyComponent wbp:name='component'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo component = getObjectByName("component");
    // initially "100px"
    assertThat(component.getBounds().width).isEqualTo(100);
    // initially no refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
    // update UiBinder template
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel width='200px'/>"));
    waitForAutoBuild();
    // now reparse required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertTrue(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
  }

  /**
   * Test for case when UiBinder widget is used used indirectly, wrapped (and not exposed) into
   * another custom widget.
   */
  @DisposeProjectAfter
  public void test_indirectWidget() throws Exception {
    dontUseSharedGWTState();
    // prepare MyComponent
    setFileContentSrc(
        "test/client/MyComponent.java",
        getJavaSource(
            "public class MyComponent extends Composite {",
            "  interface Binder extends UiBinder<Widget, MyComponent> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public MyComponent() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel width='100px'/>",
            "</ui:UiBinder>"));
    setFileContentSrc(
        "test/client/MyComponent2.java",
        getJavaSource(
            "public class MyComponent2 extends HorizontalPanel {",
            "  public MyComponent2() {",
            "    add(new MyComponent());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <t:MyComponent2 wbp:name='component'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo component = getObjectByName("component");
    // initially "100px"
    assertThat(component.getBounds().width).isEqualTo(100);
    // initially no refresh required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
    // update UiBinder template
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel width='200px'/>"));
    waitForAutoBuild();
    // now reparse required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertTrue(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
  }
}