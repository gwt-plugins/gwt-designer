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
package com.google.gdt.eclipse.designer.model.widgets.panels.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for GWT <code>Grid</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class GridInfo extends HTMLTableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    setRowColumnCount_afterMorphing();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setRowColumnCount_afterMorphing() {
    addBroadcastListener(new JavaEventListener() {
      private int m_rowCount;
      private int m_columnCount;

      @Override
      public void replaceChildBefore(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (newChild == GridInfo.this && oldChild instanceof HTMLTableInfo) {
          HTMLTableInfo oldTable = (HTMLTableInfo) oldChild;
          IGridInfo grid = oldTable.getGridInfo();
          m_rowCount = grid.getRowCount();
          m_columnCount = grid.getColumnCount();
        }
      }

      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (newChild == GridInfo.this) {
          addMethodInvocation("resize(int,int)", m_rowCount + ", " + m_columnCount);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Table status
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected HTMLTableStatus createTableStatus() throws Exception {
    return new GridStatus(this);
  }
}
