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
package com.google.gdt.eclipse.designer.gwtext.gefTree;

import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * Implementation of {@link LayoutEditPolicy} for dropping {@link LayoutInfo} on
 * {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gefTree
 */
public final class DropLayoutEditPolicy extends LayoutEditPolicy {
  private final ContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DropLayoutEditPolicy(ContainerInfo container) {
    m_container = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isRequestCondition(Request request) {
    // we understand only LayoutInfo drop
    if (request.getType() == Request.REQ_CREATE) {
      CreateRequest createRequest = (CreateRequest) request;
      return createRequest.getNewObject() instanceof LayoutInfo;
    }
    return false;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    final LayoutInfo newLayout = (LayoutInfo) newObject;
    return new EditCommand(m_container) {
      @Override
      protected void executeEdit() throws Exception {
        m_container.setLayout(newLayout);
      }
    };
  }
}