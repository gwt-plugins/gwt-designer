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
package com.google.gdt.eclipse.designer.uibinder.gef;

import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderEditor;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Basic test for {@link UiBinderEditor}.
 * 
 * @author scheglov_ke
 */
public class UiBinderEditorTest extends UiBinderGefTest {
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
  public void test_openWith_FlowPanel() throws Exception {
    WidgetInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    canvas.assertNotNullEditPart(panel);
  }

  /**
   * This test exists mostly to get coverage of {@link EditPartFactory}.
   */
  public void test_noEditPart_forObjectInfo() throws Exception {
    WidgetInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    //
    ObjectInfo object = new TestObjectInfo();
    panel.addChild(object);
    panel.refresh();
    canvas.assertNullEditPart(object);
  }

  @DisposeProjectAfter
  public void test_reparseOnCss() throws Exception {
    dontUseSharedGWTState();
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' styleName='test'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // initially "button" is narrow
    assertThat(button.getBounds().width).isLessThan(100);
    // initially not reparse required
    {
      EditorActivatedRequest request = new EditorActivatedRequest();
      panel.getBroadcast(EditorActivatedListener.class).invoke(request);
      assertFalse(request.isReparseRequested());
      assertFalse(request.isRefreshRequested());
    }
    // update CSS in editor
    {
      IFile cssFile = getFile("war/Module.css");
      IEditorPart cssEditor = IDE.openEditor(DesignerPlugin.getActivePage(), cssFile);
      setFileContent(
          cssFile,
          getSourceDQ(
              "/* filler filler filler filler filler */",
              "/* filler filler filler filler filler */",
              "/* filler filler filler filler filler */",
              ".test {",
              "  width: 200px;",
              "}"));
      // close CSS editor...
      DesignerPlugin.getActivePage().closeEditor(cssEditor, true);
    }
    // ...so UiBinder editor activated and reparse was done
    assertThat(button.getBounds().width).isEqualTo(200);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_resize_EAST() throws Exception {
    XmlObjectInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' text='New Button'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST);
    canvas.dragTo(panel, 200, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='New Button' width='200px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_resize_SOUTH() throws Exception {
    XmlObjectInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' text='New Button'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragTo(panel, 0, 100).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='New Button' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_resize_SOUTH_EAST() throws Exception {
    XmlObjectInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:Button wbp:name='button' text='New Button'/>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH_EAST);
    canvas.dragTo(panel, 200, 100).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='New Button' width='200px' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}