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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

/**
 * {@link ComplexProperty} for CSS style sub-properties.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
class StyleComplexProperty extends ComplexProperty {
  private final ObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleComplexProperty(RuleAccessor accessor, String title, Property... properties) {
    super(title, "", properties);
    m_object = accessor.getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    for (Property property : getProperties()) {
      if (property.isModified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setValue(final Object value) throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        for (Property property : getProperties()) {
          property.setValue(value);
        }
      }
    });
  }
}
