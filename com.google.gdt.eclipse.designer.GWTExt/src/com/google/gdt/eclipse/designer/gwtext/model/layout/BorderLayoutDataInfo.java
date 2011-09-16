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

import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Model for <code>com.gwtext.client.widgets.layout.BorderLayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class BorderLayoutDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the position from <code>com.gwtext.client.core.RegionPosition</code>.
   */
  public String getPosition() {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        ConstructorCreationSupport creationSupport =
            (ConstructorCreationSupport) getCreationSupport();
        Expression regionExpression = DomGenerics.arguments(creationSupport.getCreation()).get(0);
        Object region = JavaInfoEvaluationHelper.getValue(regionExpression);
        return (String) ReflectionUtils.invokeMethod(region, "getPosition()");
      }
    }, "north");
  }

  /**
   * Sets the position from <code>com.gwtext.client.core.RegionPosition</code>.
   */
  public void setPosition(String position) throws Exception {
    materialize();
    // prepare RegionPosition
    Object region;
    {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> regionClass = classLoader.loadClass("com.gwtext.client.core.RegionPosition");
      region = ReflectionUtils.invokeMethod(regionClass, "getPosition(java.lang.String)", position);
    }
    // set "region" value
    Property property = PropertyUtils.getByPath(this, "region");
    property.setValue(region);
  }
}