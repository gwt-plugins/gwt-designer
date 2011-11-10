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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.css.semantics.SimpleSidedProperty;

/**
 * Property for {@link SimpleSidedProperty} with color values.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
class StyleColorSidedProperty extends StyleAbstractSidedProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleColorSidedProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title) {
    super(ColorSidedPropertyEditor.INSTANCE,
        accessor,
        styleName,
        valueObjectPath,
        title,
        new Property[]{
            new StyleColorValueProperty(accessor, styleName, valueObjectPath + ".top", "top"),
            new StyleColorValueProperty(accessor, styleName, valueObjectPath + ".right", "right"),
            new StyleColorValueProperty(accessor, styleName, valueObjectPath + ".bottom", "bottom"),
            new StyleColorValueProperty(accessor, styleName, valueObjectPath + ".left", "left")});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ColorSidedPropertyEditor extends ColorPropertyEditor
      implements
        IComplexPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Instance
    //
    ////////////////////////////////////////////////////////////////////////////
    public static final PropertyEditor INSTANCE = new ColorSidedPropertyEditor();

    private ColorSidedPropertyEditor() {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IComplexPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Property[] getProperties(Property property) throws Exception {
      return ((StyleColorSidedProperty) property).m_properties;
    }
  }
}
