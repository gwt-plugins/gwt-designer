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
package com.google.gdt.eclipse.designer.gxt.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * The {@link ExpressionConverter} for <code>Margins</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.property
 */
public final class MarginsConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new MarginsConverter();

  private MarginsConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(com.extjs.gxt.ui.client.util.Margins) null";
    } else {
      int top = ReflectionUtils.getFieldInt(value, "top");
      int left = ReflectionUtils.getFieldInt(value, "left");
      int bottom = ReflectionUtils.getFieldInt(value, "bottom");
      int right = ReflectionUtils.getFieldInt(value, "right");
      return "new com.extjs.gxt.ui.client.util.Margins("
          + top
          + ", "
          + right
          + ", "
          + bottom
          + ", "
          + left
          + ")";
    }
  }
}
