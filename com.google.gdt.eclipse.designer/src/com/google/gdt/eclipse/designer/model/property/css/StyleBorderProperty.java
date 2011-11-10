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
import org.eclipse.wb.internal.css.semantics.BorderProperty;

/**
 * Property for {@link BorderProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
final class StyleBorderProperty extends StyleComplexProperty {
  private final RuleAccessor m_accessor;
  private final String m_styleName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleBorderProperty(RuleAccessor accessor, String styleName) {
    super(accessor, "border", new Property[]{
        new StyleColorSidedProperty(accessor, styleName, "border.color", "color"),
        new StyleLengthSidedProperty(accessor, styleName, "border.width", "width"),
        new StyleComboSidedProperty(accessor,
            styleName,
            "border.style",
            "style",
            BorderProperty.STYLES),});
    m_accessor = accessor;
    m_styleName = styleName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
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
  }
}
