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
package com.google.gdt.eclipse.designer.gxt.gefTree;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;

/**
 * {@link IEditPartFactory} for Ext-GWT widgets.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("com.google.gdt.eclipse.designer.gxt.model.widgets"),
          ImmutableList.of("com.google.gdt.eclipse.designer.gxt.gefTree.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    return MATCHING_FACTORY.createEditPart(context, model);
  }
}