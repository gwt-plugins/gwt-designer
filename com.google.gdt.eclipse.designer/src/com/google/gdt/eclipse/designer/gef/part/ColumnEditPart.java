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
package com.google.gdt.eclipse.designer.gef.part;

import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.DirectTextPropertyEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * {@link EditPart} for {@link ColumnInfo}.
 * 
 * @author sablin_aa
 * @coverage gwt.gef.part
 */
public class ColumnEditPart extends AbstractComponentEditPart {
  private final ColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnEditPart(ColumnInfo column) {
    super(column);
    m_column = column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // install direct text edit policy for "header" property
    {
      Property headerProperty = m_column.getHeaderProperty();
      DirectTextPropertyEditPolicy policy;
      if (headerProperty != null) {
        policy = new DirectTextPropertyEditPolicy(m_column, headerProperty);
      } else {
        policy = null;
      }
      installEditPolicy(DirectTextPropertyEditPolicy.KEY, policy);
    }
  }
}
