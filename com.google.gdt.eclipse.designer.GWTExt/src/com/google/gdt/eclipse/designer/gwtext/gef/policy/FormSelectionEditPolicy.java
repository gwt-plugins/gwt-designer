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
package com.google.gdt.eclipse.designer.gwtext.gef.policy;

import com.google.gdt.eclipse.designer.gwtext.model.layout.FormLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;

/**
 * {@link SelectionLayoutEditPolicy} for {@link WidgetInfo} on {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.policy
 */
public final class FormSelectionEditPolicy extends NonResizableSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormSelectionEditPolicy(WidgetInfo widget) {
  }
}