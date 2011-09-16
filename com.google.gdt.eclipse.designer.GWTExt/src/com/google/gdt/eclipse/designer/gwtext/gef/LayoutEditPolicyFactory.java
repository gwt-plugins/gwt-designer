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
package com.google.gdt.eclipse.designer.gwtext.gef;

import com.google.gdt.eclipse.designer.gwtext.gef.policy.AbsoluteLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.gef.policy.BorderLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.gef.policy.DefaultLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.gef.policy.table.TableLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AbsoluteLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.BorderLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link ILayoutEditPolicyFactory} for GWT-Ext.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof DefaultLayoutInfo) {
      return new DefaultLayoutEditPolicy(((DefaultLayoutInfo) model));
    }
    if (model instanceof BorderLayoutInfo) {
      return new BorderLayoutEditPolicy(((BorderLayoutInfo) model));
    }
    if (model instanceof AbsoluteLayoutInfo) {
      return new AbsoluteLayoutEditPolicy(((AbsoluteLayoutInfo) model));
    }
    if (model instanceof TableLayoutInfo) {
      return new TableLayoutEditPolicy(((TableLayoutInfo) model));
    }
    // not found
    return null;
  }
}
