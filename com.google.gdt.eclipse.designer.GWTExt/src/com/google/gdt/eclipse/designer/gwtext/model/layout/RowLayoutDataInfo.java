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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Model for <code>com.gwtext.client.widgets.layout.RowLayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class RowLayoutDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowLayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    trackHeightProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Height
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_changingHeight;

  public void setHeight(final int height) throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        setHeight0(height);
      }
    });
  }

  public void setHeight(final String height) throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        setHeight0(height);
      }
    });
  }

  /**
   * Attempts to generate optimal code for setting "height" properties.
   */
  private void trackHeightProperties() {
    addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (property.getJavaInfo() == RowLayoutDataInfo.this && !m_changingHeight) {
          String title = property.getTitle();
          Object val = value[0];
          if ("height(int)".equals(title) && val instanceof Integer) {
            setHeight((Integer) val);
            shouldSetValue[0] = false;
          } else if ("height(java.lang.String)".equals(title) && val instanceof String) {
            setHeight((String) val);
            shouldSetValue[0] = false;
          }
        }
      }
    });
  }

  private void setHeight0(int height) throws Exception {
    m_changingHeight = true;
    try {
      materialize();
      replaceConstructorArgument("<init>(java.lang.String)", "0");
      getPropertyByTitle("height(java.lang.String)").setValue(Property.UNKNOWN_VALUE);
      getPropertyByTitle("height(int)").setValue(height);
    } finally {
      m_changingHeight = false;
    }
  }

  private void setHeight0(String height) throws Exception {
    m_changingHeight = true;
    try {
      materialize();
      replaceConstructorArgument("<init>(int)", "(java.lang.String) null");
      getPropertyByTitle("height(int)").setValue(Property.UNKNOWN_VALUE);
      getPropertyByTitle("height(java.lang.String)").setValue(height);
    } finally {
      m_changingHeight = false;
    }
  }

  private void replaceConstructorArgument(String requiredSignature, String constructorArgument)
      throws Exception {
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      String signature = AstNodeUtils.getCreationSignature(creation);
      if (requiredSignature.equals(signature)) {
        getEditor().replaceCreationArguments(creation, ImmutableList.of(constructorArgument));
        setCreationSupport(new ConstructorCreationSupport(creation));
      }
    }
  }
}