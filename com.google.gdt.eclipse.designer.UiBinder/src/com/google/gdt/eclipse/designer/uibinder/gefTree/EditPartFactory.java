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
package com.google.gdt.eclipse.designer.uibinder.gefTree;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.uibinder.gefTree.part.UiChildPositionEditPart;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiChildSupport;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;

/**
 * {@link IEditPartFactory} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
  private final IEditPartFactory[] FACTORIES = {MATCHING_FACTORY};
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("com.google.gdt.eclipse.designer.uibinder.model.widgets"),
          ImmutableList.of("com.google.gdt.eclipse.designer.uibinder.gefTree.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    for (IEditPartFactory factory : FACTORIES) {
      EditPart editPart = factory.createEditPart(null, model);
      if (editPart != null) {
        return editPart;
      }
    }
    if (model instanceof UiChildSupport.Position) {
      return new UiChildPositionEditPart((UiChildSupport.Position) model);
    }
    return null;
  }
}