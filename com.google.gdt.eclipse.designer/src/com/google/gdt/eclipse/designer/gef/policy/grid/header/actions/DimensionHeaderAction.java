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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.ColumnHeaderEditPart;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.DimensionHeaderEditPart;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.DimensionInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Abstract action for manipulating selected {@link DimensionInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public abstract class DimensionHeaderAction<T extends DimensionInfo> extends ObjectInfoAction {
  private final boolean m_horizontal;
  private final IEditPartViewer m_viewer;
  private final HTMLTableInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart, String text) {
    this(editPart, text, null);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
      String text,
      ImageDescriptor imageDescriptor) {
    this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
      String text,
      ImageDescriptor imageDescriptor,
      int style) {
    super(editPart.getPanel(), text, style);
    m_horizontal = editPart instanceof ColumnHeaderEditPart;
    m_viewer = editPart.getViewer();
    m_panel = editPart.getPanel();
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
    List<T> dimensions = Lists.newArrayList();
    {
      List<EditPart> editParts = m_viewer.getSelectedEditParts();
      for (EditPart editPart : editParts) {
        if (editPart instanceof DimensionHeaderEditPart) {
          @SuppressWarnings("unchecked")
          DimensionHeaderEditPart<T> headerEditPart = (DimensionHeaderEditPart<T>) editPart;
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
  protected void run(List<T> dimensions) throws Exception {
    List<?> allDimensions = m_horizontal ? m_panel.getColumns() : m_panel.getRows();
    for (T dimension : dimensions) {
      int index = allDimensions.indexOf(dimension);
      run(dimension, index);
    }
  }

  /**
   * Does some operation on selected {@link DimensionInfo}'s.
   */
  protected void run(T dimension, int index) throws Exception {
  }
}
