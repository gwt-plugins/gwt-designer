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

import org.eclipse.wb.internal.css.semantics.SimpleSidedProperty;

/**
 * Property for {@link SimpleSidedProperty} with color values.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
final class StyleColorValueProperty extends StyleSimpleValueProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleColorValueProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title) {
    super(accessor, styleName, valueObjectPath, title, ColorPropertyEditor.INSTANCE);
  }
}
