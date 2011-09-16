/*
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 */
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.refactoring.MorphingSupport;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link MorphingSupport} and UiBinder.
 * 
 * @author scheglov_ke
 */
public class MorphingSupportTest extends UiBinderModelTest {
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
  public void test_morph() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField Button button;",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button ui:field='button' text='AAA'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:Button ui:field='button' text='AAA'>");
    refresh();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // morph button
    MorphingTargetDescription morphingTarget = null;
    {
      for (MorphingTargetDescription target : button.getDescription().getMorphingTargets()) {
        if ("com.google.gwt.user.client.ui.Label".equals(target.getComponentClass().getName())) {
          morphingTarget = target;
          break;
        }
      }
    }
    assertNotNull(morphingTarget);
    morph(button, morphingTarget);
    // check
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:Label ui:field='button' text='AAA'>");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Label ui:field='button' text='AAA'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  @UiField Label button;",
        "}");
  }

  public void test_morphRoot() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField FlowPanel panel;",
            "}"));
    waitForAutoBuild();
    // parse
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='panel'>",
            "    <g:Button text='AAA'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel ui:field='panel'>",
        "  <g:Button text='AAA'>");
    refresh();
    // prepare target
    MorphingTargetDescription morphingTarget = null;
    {
      for (MorphingTargetDescription target : panel.getDescription().getMorphingTargets()) {
        if ("com.google.gwt.user.client.ui.HorizontalPanel".equals(target.getComponentClass().getName())) {
          morphingTarget = target;
          break;
        }
      }
    }
    // use DesignPageSite
    final AtomicBoolean reparseRequested = new AtomicBoolean();
    DesignPageSite.Helper.setSite(panel, new DesignPageSite() {
      @Override
      public void reparse() {
        reparseRequested.set(true);
      }
    });
    // morph "panel"
    assertNotNull(morphingTarget);
    morph(panel, morphingTarget);
    // check
    assertEquals(true, reparseRequested.get());
    assertEquals(
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:HorizontalPanel ui:field='panel'>",
            "    <g:Button text='AAA'/>",
            "  </g:HorizontalPanel>",
            "</ui:UiBinder>"),
        m_lastContext.getContent());
    assertJava(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  @UiField HorizontalPanel panel;",
        "}");
  }

  /**
   * Performs morphing of {@link JavaInfo} into given target.
   */
  private static void morph(WidgetInfo widgetInfo, MorphingTargetDescription target)
      throws Exception {
    MorphingSupport.validate(widgetInfo, target);
    MorphingSupport.morph(widgetInfo, target);
  }
}
