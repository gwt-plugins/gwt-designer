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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Model for <code>AnchorData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public class AnchorDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getAnchor() {
    return (String) ReflectionUtils.invokeMethodEx(getObject(), "getAnchorSpec()");
  }

  public void setAnchor(Object anchor) throws Exception {
    getPropertyByTitle("anchorSpec").setValue(anchor);
  }

  public String getAnchorWidth() throws Exception {
    String anchor = getAnchor();
    if (anchor != null) {
      String[] anchorParts = StringUtils.split(anchor);
      if (anchorParts.length >= 1) {
        return anchorParts[0];
      }
    }
    return null;
  }

  public void setAnchorWidth(Object width) throws Exception {
    String height = getAnchorHeight();
    if (width == Property.UNKNOWN_VALUE) {
      String anchor;
      if (height == null) {
        anchor = null;
      } else {
        anchor = "0% " + height;
      }
      setAnchor(anchor);
    } else if (width instanceof String) {
      String anchor = (String) width;
      if (height != null) {
        anchor += " " + height;
      }
      setAnchor(anchor);
    }
  }

  public String getAnchorHeight() throws Exception {
    String anchor = getAnchor();
    if (anchor != null) {
      String[] anchorParts = StringUtils.split(anchor);
      if (anchorParts.length >= 2) {
        return anchorParts[1];
      }
    }
    return null;
  }

  public void setAnchorHeight(Object height) throws Exception {
    String width = getAnchorWidth();
    if (height == Property.UNKNOWN_VALUE) {
      setAnchor(width);
    } else if (height instanceof String) {
      if (width == null) {
        width = "0%";
      }
      String anchor = width + " " + height;
      setAnchor(anchor);
    }
  }
}
