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
package com.google.gdt.eclipse.designer.gxt.model.layout.table;

/**
 * Information about single row in {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class RowInfo extends DimensionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowInfo(TableLayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getIndex() {
    return m_layout.getRows().indexOf(this);
  }

  @Override
  public boolean isEmpty() {
    return m_layout.isEmptyRow(getIndex());
  }

  @Override
  public String getTitle() {
    return "row: " + getIndex();
  }

  @Override
  public void delete() throws Exception {
    m_layout.command_deleteRow(getIndex(), true);
  }
}
