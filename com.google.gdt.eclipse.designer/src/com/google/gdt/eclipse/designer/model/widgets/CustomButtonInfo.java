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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>CustomButton</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class CustomButtonInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CustomButtonInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.addAll(m_faceProperties);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Faces
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<JavaInfo> m_faces = Lists.newArrayList();
  private final List<Property> m_faceProperties = Lists.newArrayList();

  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    addFace("UpFace");
    addFace("UpDisabledFace");
    addFace("UpHoveringFace");
    addFace("DownFace");
    addFace("DownDisabledFace");
    addFace("DownHoveringFace");
  }

  /**
   * Exposes <code>Face</code> with given name, so that its properties can be modified.
   */
  private void addFace(String faceName) throws Exception {
    final JavaInfo face = JavaInfoUtils.addChildExposedByMethod(this, "get" + faceName);
    m_faces.add(face);
    // create ComplexProperty for each face
    {
      ComplexProperty faceProperty = new ComplexProperty(faceName, "(Face properties)") {
        @Override
        public void setValue(Object value) throws Exception {
          if (value == Property.UNKNOWN_VALUE) {
            face.delete();
          }
        }
      };
      faceProperty.setProperties(face.getProperties());
      faceProperty.setCategory(PropertyCategory.system(100 + m_faces.size()));
      m_faceProperties.add(faceProperty);
    }
    // when any face property is about to set, reset other properties
    face.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (property.getJavaInfo() == face) {
          face.delete();
        }
      }
    });
  }
}
