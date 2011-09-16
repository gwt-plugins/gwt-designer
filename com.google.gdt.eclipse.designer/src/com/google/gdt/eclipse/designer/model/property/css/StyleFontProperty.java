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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.css.semantics.BorderProperty;
import org.eclipse.wb.internal.css.semantics.FontProperty;

/**
 * Property for {@link BorderProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
final class StyleFontProperty extends StyleComplexProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleFontProperty(RuleAccessor accessor, String styleName) {
    super(accessor, "font", new Property[]{
        new StyleSimpleValueProperty(accessor,
            styleName,
            "font.family",
            "family",
            FontFamilyPropertyEditor.INSTANCE),
        new StyleLengthValueProperty(accessor,
            styleName,
            "font.size",
            "size",
            new StringComboBoxPropertyEditor(FontProperty.SIZE_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "font.style",
            "style",
            new StringComboPropertyEditor(FontProperty.STYLES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "font.stretch",
            "stretch",
            new StringComboPropertyEditor(FontProperty.STRETCH_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "font.variant",
            "variant",
            new StringComboPropertyEditor(FontProperty.VARIANTS)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "font.weight",
            "weight",
            new StringComboPropertyEditor(FontProperty.WEIGHT_VALUES)),});
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /*@Override
  public String getText() throws Exception {
    BorderProperty border = m_accessor.getSemantics(m_styleName).border;
    String width = border.getWidth().get();
    String style = border.getStyle().get();
    String color = border.getColor().get();
    // prepare text
    String text = "";
    if (width != null) {
      width = width.contains(" ") ? "(" + width + ")" : width;
      text += width;
    }
    if (style != null) {
      style = style.contains(" ") ? "(" + style + ")" : style;
      text += (text.length() != 0 ? " " : "") + style;
    }
    if (color != null) {
      color = color.contains(" ") ? "(" + color + ")" : color;
      text += (text.length() != 0 ? " " : "") + color;
    }
    // done
    return text;
  }*/
}
