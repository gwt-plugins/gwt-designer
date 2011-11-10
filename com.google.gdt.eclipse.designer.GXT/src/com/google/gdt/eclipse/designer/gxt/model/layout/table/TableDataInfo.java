/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gxt.model.layout.table;

import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutDataInfo;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.layout.TableData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class TableDataInfo extends LayoutDataInfo {
  int x = -1;
  int y = -1;
  int width = 1;
  int height = 1;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getColumn() {
    return x;
  }

  public void setColumn(int column) {
    x = column;
  }

  public int getRow() {
    return y;
  }

  public void setRow(int row) {
    y = row;
  }

  public int getColSpan() {
    return width;
  }

  public void setColSpan(int colSpan) throws Exception {
    if (width != colSpan) {
      width = colSpan;
      getPropertyByTitle("colspan").setValue(colSpan);
    }
  }

  public int getRowSpan() {
    return height;
  }

  public void setRowSpan(int rowSpan) throws Exception {
    if (height != rowSpan) {
      height = rowSpan;
      getPropertyByTitle("rowspan").setValue(rowSpan);
    }
  }

  /**
   * Fetches initial col/row snap values.
   */
  void initializeSpans() throws Exception {
    {
      Property property = getPropertyByTitle("colspan");
      width = property.isModified() ? (Integer) property.getValue() : 1;
    }
    {
      Property property = getPropertyByTitle("rowspan");
      height = property.isModified() ? (Integer) property.getValue() : 1;
    }
  }
}