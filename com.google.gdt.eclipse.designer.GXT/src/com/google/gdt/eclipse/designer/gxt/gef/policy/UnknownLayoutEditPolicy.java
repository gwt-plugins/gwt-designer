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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for unknown (or unsupported on canvas) {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class UnknownLayoutEditPolicy extends LayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    PolicyUtils.showBorderTargetFeedback(this);
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    PolicyUtils.eraseBorderTargetFeedback(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    return null;
  }
}
