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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.SimpleAlignmentActionsSupport;

/**
 * Alignment actions for {@link CanvasInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public final class CanvasAlignmentSupport extends SimpleAlignmentActionsSupport<WidgetInfo> {
  private final CanvasInfo m_canvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasAlignmentSupport(CanvasInfo canvas) {
    m_canvas = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isComponentInfo(ObjectInfo object) {
    return object instanceof WidgetInfo;
  }

  @Override
  protected boolean isValidObjectOnRootPath(IAbstractComponentInfo parent) {
    return parent instanceof CanvasInfo;
  }

  @Override
  protected IAbstractComponentInfo getLayoutContainer() {
    return m_canvas;
  }

  @Override
  protected void commandChangeBounds(WidgetInfo component, Point location, Dimension size)
      throws Exception {
    CanvasInfo canvas =
        component instanceof CanvasInfo
            ? (CanvasInfo) component
            : (CanvasInfo) component.getParentJava();
    m_canvas.command_BOUNDS(canvas, location, size);
  }
}