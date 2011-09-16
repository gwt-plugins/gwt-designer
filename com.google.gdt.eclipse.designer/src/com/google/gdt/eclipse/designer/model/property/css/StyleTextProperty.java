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
import org.eclipse.wb.internal.css.semantics.TextProperty;

/**
 * Property for {@link TextProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
final class StyleTextProperty extends StyleComplexProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleTextProperty(RuleAccessor accessor, String styleName) {
    super(accessor, "text", new Property[]{
        new StyleLengthValueProperty(accessor,
            styleName,
            "text.indent",
            "indent",
            new StringComboBoxPropertyEditor(TextProperty.INDENT_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "text.align",
            "align",
            new StringComboPropertyEditor(TextProperty.ALIGN_H_VALUES)),
        new StyleLengthValueProperty(accessor,
            styleName,
            "text.verticalAlign",
            "vertical-align",
            new StringComboBoxPropertyEditor(TextProperty.ALIGN_V_VALUES)),
        new StyleLengthValueProperty(accessor,
            styleName,
            "text.letterSpacing",
            "letter-spacing",
            new StringComboBoxPropertyEditor(TextProperty.SPACE_VALUES)),
        new StyleLengthValueProperty(accessor,
            styleName,
            "text.wordSpacing",
            "word-spacing",
            new StringComboBoxPropertyEditor(TextProperty.SPACE_VALUES)),
        new StyleLengthValueProperty(accessor,
            styleName,
            "text.lineHeight",
            "line-height",
            new StringComboBoxPropertyEditor(TextProperty.LINE_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "text.decoration",
            "decoration",
            new StyleSetPropertyEditor(TextProperty.DECORATION_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "text.transform",
            "transform",
            new StringComboPropertyEditor(TextProperty.TRANSFORM_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "text.direction",
            "direction",
            new StringComboPropertyEditor(TextProperty.DIRECTION_VALUES)),
        new StyleSimpleValueProperty(accessor,
            styleName,
            "text.whiteSpace",
            "white-space",
            new StringComboBoxPropertyEditor(TextProperty.WHITE_VALUES)),});
  }
}
