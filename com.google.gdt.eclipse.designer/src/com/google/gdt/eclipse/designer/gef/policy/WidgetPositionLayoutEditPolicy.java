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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;

import org.eclipse.wb.core.gef.policy.layout.position.ObjectPositionLayoutEditPolicy;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;

/**
 * {@link ObjectPositionLayoutEditPolicy} for GWT toolkit.
 * 
 * @author sablin_aa
 * @coverage gwt.gef.policy
 */
public abstract class WidgetPositionLayoutEditPolicy<C, D>
    extends
      ObjectPositionLayoutEditPolicy<C, D> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetPositionLayoutEditPolicy(ObjectInfo panel) {
    super(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return WidgetsLayoutRequestValidator.INSTANCE;
  }
}
