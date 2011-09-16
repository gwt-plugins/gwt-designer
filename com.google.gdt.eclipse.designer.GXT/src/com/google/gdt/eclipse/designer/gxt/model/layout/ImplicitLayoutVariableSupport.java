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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.AbstractImplicitVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

/**
 * {@link VariableSupport} for implicit {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class ImplicitLayoutVariableSupport extends AbstractImplicitVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitLayoutVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "implicit-layout";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String getTitle() throws Exception {
    return "(implicit layout)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Materializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected JavaInfo getParent() {
    return m_javaInfo.getParentJava();
  }
}
