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

import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderJavaParseValidator;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.editor.errors.WarningComposite;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * Test for {@link UiBinderJavaParseValidator}.
 * 
 * @author scheglov_ke
 */
public class UiBinderJavaParseValidatorTest extends UiBinderGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    DesignerPlugin.setDisplayExceptionOnConsole(true);
    EnvironmentUtils.setTestingTime(true);
    super.tearDown();
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If Java source works with UiBinder, then ui.xml should be designed instead.
   */
  public void test_showWarning() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    //
    IFile javaFile = getFileSrc("test/client/Test.java");
    assertTrue(javaFile.exists());
    // open Java
    IEditorPart javaEditor = openJavaDesignEditor(javaFile);
    switchToDesign(javaEditor);
    // prepare UIContext
    UiContext context = new UiContext();
    // WarningComposite is visible
    Composite warningComposite = context.findFirstWidget(WarningComposite.class);
    assertNotNull(warningComposite);
    assertTrue(warningComposite.isVisible());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IEditorPart openJavaDesignEditor(IFile javaFile) throws PartInitException {
    return IDE.openEditor(DesignerPlugin.getActivePage(), javaFile, IDesignerEditor.ID);
  }

  private static void switchToDesign(IEditorPart javaEditor) {
    SwitchAction switchAction = new SwitchAction();
    switchAction.setActiveEditor(null, javaEditor);
    switchAction.run();
  }
}
