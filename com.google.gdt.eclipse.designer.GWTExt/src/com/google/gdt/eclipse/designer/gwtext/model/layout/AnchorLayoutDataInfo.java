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
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.StringUtils;

/**
 * Model for <code>com.gwtext.client.widgets.layout.AnchorLayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class AnchorLayoutDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorLayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getAnchor() throws Exception {
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      Expression anchorExpression = DomGenerics.arguments(creation).get(0);
      return JavaInfoEvaluationHelper.getValue(anchorExpression);
    }
    return Property.UNKNOWN_VALUE;
  }

  public void setAnchor(Object anchor) throws Exception {
    materialize();
    PropertyUtils.getByPath(this, "Constructor/anchor").setValue(anchor);
  }

  public void setAnchorWidth(String width) throws Exception {
    Object anchorObject = getAnchor();
    if (anchorObject == Property.UNKNOWN_VALUE) {
      setAnchor(width);
    } else {
      String anchor = (String) anchorObject;
      String[] anchorParts = StringUtils.split(anchor);
      if (anchorParts.length == 1) {
        if (anchor.startsWith(" ")) {
          setAnchor(width + " " + anchor.trim());
        } else {
          setAnchor(width);
        }
      } else if (anchorParts.length == 2) {
        setAnchor(width + " " + anchorParts[1]);
      }
    }
  }

  public void setAnchorHeight(String height) throws Exception {
    Object anchorObject = getAnchor();
    if (anchorObject == Property.UNKNOWN_VALUE) {
      setAnchor(" " + height);
    } else {
      String anchor = (String) anchorObject;
      String[] anchorParts = StringUtils.split(anchor);
      if (anchorParts.length == 1 && anchor.startsWith(" ")) {
        setAnchor(" " + height);
      } else if (anchorParts.length != 0) {
        setAnchor(anchorParts[0] + " " + height);
      }
    }
  }
}