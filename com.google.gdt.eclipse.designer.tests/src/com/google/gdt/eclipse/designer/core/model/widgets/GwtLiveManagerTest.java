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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveManager;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test {@link GwtLiveManager}.
 * 
 * @author scheglov_ke
 */
public class GwtLiveManagerTest extends GwtModelTest {
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
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simplest test - get live image of <code>Button</code> widget.
   * <p>
   * With "overflow: visible" in CSS, so no problem with size.
   */
  public void test_Button() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = createWidget("com.google.gwt.user.client.ui.Button");
    // ask image
    {
      Image image = button.getImage();
      assertNotNull(image);
      // bounds
      Rectangle imageBounds = image.getBounds();
      assertThat(imageBounds.width).isEqualTo(100);
      assertThat(imageBounds.height).isGreaterThan(20).isLessThan(50);
    }
    // Button has forced size
    assertTrue(button.shouldSetReasonableSize());
  }

  /**
   * There was problem with too wide <code>Button</code> in "strict" mode.
   * <p>
   * See "Weird GWT Buttons" in GMail.
   */
  @DisposeProjectAfter
  public void test_Button_tooWide() throws Exception {
    dontUseSharedGWTState();
    setFileContent("war/Module.css", "");
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = createWidget("com.google.gwt.user.client.ui.Button");
    // ask image
    {
      Image image = button.getImage();
      assertNotNull(image);
      // bounds
      Rectangle imageBounds = image.getBounds();
      assertThat(imageBounds.width).isEqualTo(100);
      assertThat(imageBounds.height).isGreaterThan(20).isLessThan(50);
    }
    // Button has forced size
    assertTrue(button.shouldSetReasonableSize());
  }

  /**
   * Access of live image should not clear main <code>RootPanel</code>, because this makes all
   * widgets detached.
   */
  public void test_dontClearRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.add(new TextBox());",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo textBox = frame.getChildrenWidgets().get(0);
    // initially "textBox" is attached
    assertEquals(Boolean.TRUE, ReflectionUtils.invokeMethod(textBox.getObject(), "isAttached()"));
    // ...and only TextBox is child of RootPanel
    assertEquals(1, ReflectionUtils.invokeMethod(frame.getObject(), "getWidgetCount()"));
    // ask image
    {
      WidgetInfo button = createWidget("com.google.gwt.user.client.ui.Button");
      Image image = button.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isGreaterThan(50).isLessThan(200);
      assertThat(image.getBounds().height).isGreaterThan(20).isLessThan(50);
    }
    // "textBox" still should be attached
    assertEquals(Boolean.TRUE, ReflectionUtils.invokeMethod(textBox.getObject(), "isAttached()"));
    // there was problem that RootPanel was not able to clear after using live images
    {
      frame.refresh();
      // still only TextBox is child of RootPanel
      assertEquals(1, ReflectionUtils.invokeMethod(frame.getObject(), "getWidgetCount()"));
    }
  }

  /**
   * <code>Hidden</code> widget does not have visual presentation, so its {@link Image} should have
   * size 0x0, but such {@link Image} can not be created, so "forced" size should be used.
   */
  public void test_Hidden() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = createWidget("com.google.gwt.user.client.ui.Hidden");
    // ask image
    {
      Image image = button.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(20);
      assertThat(image.getBounds().height).isEqualTo(20);
    }
  }

  /**
   * Test for using <code>liveComponent.no</code> parameter.
   */
  public void test_noLive() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public Element getElement() {",
            "    return null;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.no'>true</parameter>",
            "    <parameter name='liveComponent.forcedSize.width'>100</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>50</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo myButton = createWidget("test.client.MyButton");
    // ask image
    {
      Image image = myButton.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(100);
      assertThat(image.getBounds().height).isEqualTo(50);
    }
    // MyButton has no live, so no size
    assertFalse(myButton.shouldSetReasonableSize());
    // dispose
    do_projectDispose();
  }

  /**
   * If exception happens, we still should return some image.
   */
  public void test_whenException_getElement() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  private int m_getElementCount;",
            "  public com.google.gwt.user.client.Element getElement() {",
            "    m_getElementCount++;",
            "    if (m_getElementCount < 5) {",
            "      return super.getElement();",
            "    }",
            "    throw new IllegalStateException('Bad getElement()');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // add log listener for exception validation
    ILog log = DesignerPlugin.getDefault().getLog();
    ILogListener logListener = new ILogListener() {
      public void logging(IStatus status, String plugin) {
        assertEquals(IStatus.ERROR, status.getSeverity());
        Throwable exception = status.getException();
        assertThat(exception).isExactlyInstanceOf(IllegalStateException.class);
        assertEquals("Bad getElement()", exception.getMessage());
      }
    };
    // temporary intercept logging
    try {
      log.addLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(false);
      // prepare new component
      WidgetInfo myButton = createWidget("test.client.MyButton");
      // ask image
      {
        Image image = myButton.getImage();
        assertNotNull(image);
        assertThat(image.getBounds().width).isEqualTo(200);
        assertThat(image.getBounds().height).isEqualTo(50);
      }
    } finally {
      log.removeLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(true);
    }
    // dispose
    do_projectDispose();
  }

  /**
   * We should handle correctly exceptions in <code>onAttach()</code> - when widget is not
   * configured correctly and refuses to render itself; and later in <code>onDetach()</code> - when
   * widget complains that it was not attached.
   */
  public void test_whenException_inCleanup() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  protected void onAttach() {",
            "    throw new IllegalStateException('Bad onAttach');",
            "  }",
            "  protected void onDetach() {",
            "    throw new IllegalStateException('Bad onDetach');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    String originalSource = m_lastEditor.getSource();
    // add log listener for exception validation
    final AtomicInteger exceptionCount = new AtomicInteger();
    ILog log = DesignerPlugin.getDefault().getLog();
    ILogListener logListener = new ILogListener() {
      public void logging(IStatus status, String plugin) {
        exceptionCount.getAndIncrement();
      }
    };
    // temporary intercept logging
    try {
      log.addLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(false);
      // prepare new component
      WidgetInfo myButton = createWidget("test.client.MyButton");
      // ask image
      {
        Image image = myButton.getImage();
        assertNotNull(image);
      }
    } finally {
      log.removeLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(true);
    }
    // no exceptions
    assertEquals(0, exceptionCount.get());
    // no changes in editor
    assertEquals(originalSource, m_lastEditor.getSource());
    // dispose
    do_projectDispose();
  }

  /**
   * Test for using <code>liveComponent.forcedSize.width</code> and
   * <b>liveComponent.forcedSize.height</b> parameters.
   */
  public void test_forcedSize() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.width'>100</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>50</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo myButton = createWidget("test.client.MyButton");
    // ask image
    {
      Image image = myButton.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(100);
      assertThat(image.getBounds().height).isEqualTo(50);
    }
    // MyButton has forced size, so use forced
    assertTrue(myButton.shouldSetReasonableSize());
  }

  /**
   * Test for using <code>liveComponent.forcedSize.use</code> to disable forced size.
   */
  public void test_forcedSize_disabled() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyButton extends Button {",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.use'>"
                + "isStrict &amp;&amp; isExplorer &amp;&amp; false"
                + "</parameter>",
            "    <parameter name='liveComponent.forcedSize.width'>300</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>150</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo myButton = createWidget("test.client.MyButton");
    // ask image
    {
      Image image = myButton.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isLessThan(150);
      assertThat(image.getBounds().height).isLessThan(50);
    }
  }

  /**
   * For user components we can not expect <code>liveComponent.forcedSize.width</code> and
   * <b>liveComponent.forcedSize.height</b> parameters, so some defaults should be used. This is
   * useful for example for <code>Composite</code> with <code>AbsoluteLayout</code>.
   */
  public void test_defaultSize() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite() {",
            "    AbsolutePanel panel = new AbsolutePanel();",
            "    initWidget(panel);",
            "    panel.add(new Button(), 10, 10);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo myComposite = createWidget("test.client.MyComposite");
    // ask image
    {
      Image image = myComposite.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(100);
      assertThat(image.getBounds().height).isEqualTo(100);
    }
    // MyComposite has no preferred size, so use forced
    assertTrue(myComposite.shouldSetReasonableSize());
  }

  /**
   * GWT <code>Label</code> should be added with location, in other case it will have width of
   * <code>RootPanel</code>.
   */
  public void test_Label() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo label = createWidget("com.google.gwt.user.client.ui.Label");
    // ask image
    {
      Image image = label.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isGreaterThan(10).isLessThan(200);
      assertThat(image.getBounds().height).isGreaterThan(10);
    }
  }

  public void test_StaticFactory() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static Button createButton() {",
            "    return new Button('New Button');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // prepare Widget
    WidgetInfo widget;
    {
      FactoryMethodDescription description =
          FactoryDescriptionHelper.getDescription(
              m_lastEditor,
              m_lastLoader.loadClass("test.client.StaticFactory"),
              "createButton()",
              true);
      widget =
          (WidgetInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              "com.google.gwt.user.client.ui.Button",
              new StaticFactoryCreationSupport(description));
      widget.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    }
    // ask image
    {
      Image image = widget.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isGreaterThan(50).isLessThan(200);
      assertThat(image.getBounds().height).isGreaterThan(20).isLessThan(50);
    }
  }

  /**
   * User may request live image second time (because during first we may run messages loop).
   */
  public void test_secondInAsync() throws Exception {
    dontUseSharedGWTState();
    createModelType(
        "test.client",
        "MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public MyButton() {",
            "    super('New Button');",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='GWT.requiresMessagesLoop'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    final WidgetInfo button = createWidget("test.client.MyButton");
    // schedule ask in async
    final Image[] asyncImage = new Image[1];
    ExecutionUtils.runLogLater(new RunnableEx() {
      public void run() throws Exception {
        asyncImage[0] = button.getImage();
      }
    });
    // ask image in this (event) thread
    {
      Image mainImage = button.getImage();
      assertThat(mainImage.getBounds().width).isGreaterThan(50).isLessThan(200);
      assertThat(mainImage.getBounds().height).isGreaterThan(20).isLessThan(50);
    }
    // run messages loop just to make sure that "async" is executed (but it should be already)
    waitEventLoop(0);
    assertNotNull("No live image from async.", asyncImage[0]);
    assertEquals(1, asyncImage[0].getBounds().width);
    assertEquals(1, asyncImage[0].getBounds().height);
    // request in main thread still returns "normal" live image
    {
      Image mainImage = button.getImage();
      assertThat(mainImage.getBounds().width).isGreaterThan(50).isLessThan(200);
      assertThat(mainImage.getBounds().height).isGreaterThan(20).isLessThan(50);
    }
  }

  /**
   * If <code>GWTState</code> disposed, this should not cause live image problems.
   */
  public void test_noImage_afterSessionDispose() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // do dispose
    disposeLastModel();
    assertNull(frame.getImage());
  }

  /**
   * Even if (temporary) components hierarchy is disposed, so image is <code>null</code>, we should
   * not try to create live image (just is just silly), because we need live images only manual
   * components, that user drops from palette.
   */
  public void test_noImage_afterRefreshDispose() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // do dispose
    frame.refresh_dispose();
    assertNull(frame.getImage());
  }
}