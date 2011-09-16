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
package com.google.gdt.eclipse.designer.uibinder.model;

import com.google.gdt.eclipse.designer.uibinder.model.util.UiBinderStaticFieldSupport;

import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditorGetExpression;

/**
 * Test for {@link UiBinderStaticFieldSupport}.
 * 
 * @author scheglov_ke
 */
public class UiBinderStaticFieldSupportTest extends UiBinderModelTest {
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
  public void test_HasHorizontalAlignment() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    refresh();
    //
    String[] expression = {null};
    m_lastObject.getBroadcast(StaticFieldPropertyEditorGetExpression.class).invoke(
        m_lastLoader.loadClass("com.google.gwt.user.client.ui.HasHorizontalAlignment"),
        "ALIGN_CENTER",
        expression);
    assertEquals("ALIGN_CENTER", expression[0]);
  }
}