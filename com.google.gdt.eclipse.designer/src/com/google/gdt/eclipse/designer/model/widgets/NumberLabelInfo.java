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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

/**
 * Model for <code>com.google.gwt.user.client.ui.NumberLabel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class NumberLabelInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NumberLabelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      Image typeIcon = getTypeIcon();
      if (typeIcon != null) {
        return typeIcon;
      }
      return super.getIcon();
    };
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  /**
   * @return the icon {@link Image} which corresponds to the type argument of this
   *         <code>NumberLabel</code> instance.
   */
  private Image getTypeIcon() {
    Expression creationExpression = (Expression) getCreationSupport().getNode();
    ITypeBinding creationBinding = AstNodeUtils.getTypeBinding(creationExpression);
    ITypeBinding typeBinding = AstNodeUtils.getTypeBindingArgument(creationBinding, 0);
    String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
    for (CreationDescription creation : getDescription().getCreations()) {
      if (StringUtils.endsWith(typeName, "." + creation.getId())) {
        return creation.getIcon();
      }
    }
    return null;
  }
}
