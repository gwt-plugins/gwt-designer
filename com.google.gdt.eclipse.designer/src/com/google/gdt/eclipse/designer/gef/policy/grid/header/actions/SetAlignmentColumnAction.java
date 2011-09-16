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
package com.google.gdt.eclipse.designer.gef.policy.grid.header.actions;

import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.DimensionHeaderEditPart;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for modifying alignment of {@link ColumnInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class SetAlignmentColumnAction extends DimensionHeaderAction<ColumnInfo> {
  private final ColumnInfo.Alignment m_alignment;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetAlignmentColumnAction(DimensionHeaderEditPart<ColumnInfo> header,
      String text,
      ColumnInfo.Alignment alignment) {
    super(header, text, alignment.getMenuImage(), AS_RADIO_BUTTON);
    m_alignment = alignment;
    setChecked(header.getDimension().getAlignment() == m_alignment);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void run(ColumnInfo dimension, int index) throws Exception {
    if (isChecked()) {
      dimension.setAlignment(m_alignment);
    }
  }
}