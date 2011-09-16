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
package com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.actions;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.edit.DimensionHeaderEditPart;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.DimensionInfo;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Abstract action for manipulating selected {@link DimensionInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.TableLayout
 */
public abstract class DimensionHeaderAction extends ObjectInfoAction {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderAction(DimensionHeaderEditPart editPart, String text) {
    this(editPart, text, null);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart editPart,
      String text,
      ImageDescriptor imageDescriptor) {
    this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart editPart,
      String text,
      ImageDescriptor imageDescriptor,
      int style) {
    super(editPart.getLayout(), text, style);
    m_viewer = editPart.getViewer();
    setImageDescriptor(imageDescriptor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return getClass() == obj.getClass();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void runEx() throws Exception {
    // prepare selection
    List<DimensionInfo> dimensions = Lists.newArrayList();
    {
      for (EditPart editPart : m_viewer.getSelectedEditParts()) {
        if (editPart instanceof DimensionHeaderEditPart) {
          DimensionHeaderEditPart headerEditPart = (DimensionHeaderEditPart) editPart;
          dimensions.add(headerEditPart.getDimension());
        }
      }
    }
    // run over them
    run(dimensions);
  }

  /**
   * Does some operation on {@link List} of selected {@link DimensionInfo}'s.
   */
  protected void run(List<DimensionInfo> dimensions) throws Exception {
    for (DimensionInfo dimension : dimensions) {
      run(dimension);
    }
  }

  /**
   * Does some operation on selected {@link GridDimensionInfo}'s.
   */
  protected void run(DimensionInfo dimension) throws Exception {
  }
}
