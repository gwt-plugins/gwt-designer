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
package com.google.gdt.eclipse.designer.core.model.widgets.generic;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.util.TemplateChangedRootProcessor;

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
public class TemplateChangedRootProcessorTest extends GwtModelTest {
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
        getTestSource(
            "import com.google.gwt.uibinder.client.*;",
            "public class MyComponent extends Composite {",
            "  interface Binder extends UiBinder<Widget, MyComponent> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public MyComponent() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getSourceDQ(
            "<ui:UiBinder"
                + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
                + " xmlns:g='urn:import:com.google.gwt.user.client.ui'>",
            "<g:FlowPanel width='100px'/>",
            "</ui:UiBinder>"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    MyComponent component = new MyComponent();",
            "    add(component);",
            "  }",
            "}");
    refresh();
    WidgetInfo component = getJavaInfoByName("component");
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
        getSourceDQ(
            "<ui:UiBinder"
                + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
                + " xmlns:g='urn:import:com.google.gwt.user.client.ui'>",
            "<g:FlowPanel width='200px'/>",
            "</ui:UiBinder>"));
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
        getTestSource(
            "import com.google.gwt.uibinder.client.*;",
            "public class MyComponent extends Composite {",
            "  interface Binder extends UiBinder<Widget, MyComponent> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public MyComponent() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyComponent.ui.xml",
        getSourceDQ(
            "<ui:UiBinder"
                + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
                + " xmlns:g='urn:import:com.google.gwt.user.client.ui'>",
            "<g:FlowPanel width='100px'/>",
            "</ui:UiBinder>"));
    setFileContentSrc(
        "test/client/MyComponent2.java",
        getTestSource(
            "public class MyComponent2 extends HorizontalPanel {",
            "  public MyComponent2() {",
            "    add(new MyComponent());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    MyComponent2 component = new MyComponent2();",
            "    add(component);",
            "  }",
            "}");
    refresh();
    WidgetInfo component = getJavaInfoByName("component");
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
        getSourceDQ(
            "<ui:UiBinder"
                + " xmlns:ui='urn:ui:com.google.gwt.uibinder'"
                + " xmlns:g='urn:import:com.google.gwt.user.client.ui'>",
            "<g:FlowPanel width='200px'/>",
            "</ui:UiBinder>"));
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