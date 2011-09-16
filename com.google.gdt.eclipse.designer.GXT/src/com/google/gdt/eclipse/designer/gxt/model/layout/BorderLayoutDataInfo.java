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

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.util.List;

/**
 * Model for <code>BorderLayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
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
  // Region
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of <code>LayoutRegion</code> constant, may be <code>null</code> if virtual.
   */
  public String getRegion() {
    Object object = getObject();
    if (object == null) {
      return null;
    }
    Object region = ReflectionUtils.invokeMethodEx(object, "getRegion()");
    return (String) ReflectionUtils.invokeMethodEx(region, "name()");
  }

  /**
   * Sets <code>LayoutRegion</code>.
   */
  public void setRegion(String region) throws Exception {
    materialize();
    GenericProperty property = (GenericProperty) getPropertyByTitle("region");
    property.setExpression("com.extjs.gxt.ui.client.Style.LayoutRegion." + region, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  public float getSize() {
    Object object = getObject();
    if (object == null) {
      return 200.0f;
    }
    return (Float) ReflectionUtils.invokeMethodEx(object, "getSize()");
  }

  public void setSize(float size) throws Exception {
    getPropertyByTitle("size").setValue(size);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation optimization
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void on_setPropertyExpression(GenericPropertyImpl property) throws Exception {
    super.on_setPropertyExpression(property);
    if (getCreationSupport() instanceof ConstructorCreationSupport
        && property.getTitle().equals("size")) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      String signature = creationSupport.getDescription().getSignature();
      if ("<init>(com.extjs.gxt.ui.client.Style.LayoutRegion)".equals(signature)) {
        getEditor().addCreationArgument(creation, 1, "0.0f");
        setCreationSupport(new ConstructorCreationSupport(creation));
      }
    }
  }

  @Override
  protected boolean postProcessConstructorCreation(String signature,
      ClassInstanceCreation creation,
      List<Object> argumentValues) throws Exception {
    if ("<init>(com.extjs.gxt.ui.client.Style.LayoutRegion,float)".equals(signature)) {
      float size = ((Number) argumentValues.get(1)).floatValue();
      if (Math.abs(size - 200.0f) < 1E-10) {
        getEditor().removeCreationArgument(creation, 1);
        return true;
      }
    }
    return false;
  }
}
