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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.AbstractResizeSelectionEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo.ColumnInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link ColumnInfo} in {@link PortalInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class PortalColumnSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final ColumnInfo m_widget;
  private final PortalInfo m_portal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PortalColumnSelectionEditPolicy(ColumnInfo widget) {
    m_widget = widget;
    m_portal = m_widget.getPortal();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(new MoveHandle(getHost()));
    handles.add(createResizeHandle(IPositionConstants.EAST, 0.5, IColorConstants.green));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTooltipCommand() {
    // EAST
    if (m_resizeDirection == IPositionConstants.EAST) {
      final double newWidth = getNewWidth();
      m_command = new EditCommand(m_portal) {
        @Override
        protected void executeEdit() throws Exception {
          m_widget.setWidth(newWidth);
        }
      };
    }
  }

  private double getNewWidth() {
    if (m_ctrlPressed) {
      double width = getCurrentPercentWidth();
      width += m_sizeDelta.width / 100.0;
      width = roundPercentToFive(width);
      //
      m_tooltip = "width: " + getPercentString(width) + "\nRelease Ctrl to set width in pixels";
      return width;
    } else {
      double width = m_newSize.width;
      width = roundToFive(width);
      if (width < 0) {
        width = 0;
      }
      //
      m_tooltip = "width: " + (int) width + "\nPress Ctrl to set width in percents";
      return width;
    }
  }

  private double getCurrentPercentWidth() {
    double width = m_widget.getWidth();
    if (width < 0 || width > 1) {
      width = (double) getHostFigure().getSize().width / getParentWidth();
    }
    return width;
  }

  private static String getPercentString(double width) {
    return (int) (width * 100) + "%";
  }

  private static double roundPercentToFive(double value) {
    value = Math.max(value, 0.0);
    value = Math.min(value, 1.0);
    value *= 100;
    value = roundToFive(value);
    value /= 100;
    return value;
  }

  private static double roundToFive(double value) {
    return (int) value / 5 * 5;
  }
}