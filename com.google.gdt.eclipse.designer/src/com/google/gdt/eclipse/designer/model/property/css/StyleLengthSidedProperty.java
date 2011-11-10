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
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.css.semantics.LengthSidedProperty;

/**
 * Property for {@link LengthSidedProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
class StyleLengthSidedProperty extends StyleAbstractSidedProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleLengthSidedProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title) {
    super(LengthSidedPropertyEditor.INSTANCE,
        accessor,
        styleName,
        valueObjectPath,
        title,
        new Property[]{
            new StyleLengthValueProperty(accessor, styleName, valueObjectPath + ".top", "top"),
            new StyleLengthValueProperty(accessor, styleName, valueObjectPath + ".right", "right"),
            new StyleLengthValueProperty(accessor, styleName, valueObjectPath + ".bottom", "bottom"),
            new StyleLengthValueProperty(accessor, styleName, valueObjectPath + ".left", "left")});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class LengthSidedPropertyEditor extends AbstractTextPropertyEditor
      implements
        IComplexPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Instance
    //
    ////////////////////////////////////////////////////////////////////////////
    public static final PropertyEditor INSTANCE = new LengthSidedPropertyEditor();

    private LengthSidedPropertyEditor() {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Text
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getText(Property property) throws Exception {
      return (String) property.getValue();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Editing
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getEditorText(Property property) throws Exception {
      return getText(property);
    }

    @Override
    protected boolean setEditorText(Property property, String text) throws Exception {
      property.setValue(text);
      return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IComplexPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Property[] getProperties(Property property) throws Exception {
      return ((StyleLengthSidedProperty) property).m_properties;
    }
  }
}
