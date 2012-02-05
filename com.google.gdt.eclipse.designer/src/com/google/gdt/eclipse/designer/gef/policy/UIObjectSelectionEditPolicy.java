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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for any {@link IUIObjectInfo}. We need it because by default
 * any {@link IUIObjectInfo} can be set some size, so by default we set policy that supports this.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class UIObjectSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final IUIObjectInfo m_object;
  private String m_tooltipWidth;
  private String m_tooltipHeight;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectSelectionEditPolicy(IUIObjectInfo object) {
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(createMoveHandle());
    handles.add(createResizeHandle(IPositionConstants.SOUTH));
    handles.add(createResizeHandle(IPositionConstants.EAST));
    handles.add(createResizeHandle(IPositionConstants.SOUTH_EAST));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTooltipCommand() {
    final String newWidth = getNewWidth();
    final String newHeight = getNewHeight();
    // tooltip
    m_tooltip = "";
    if (PolicyUtils.hasDirection(m_resizeDirection, IPositionConstants.EAST)) {
      m_tooltip += m_tooltipWidth;
    }
    if (PolicyUtils.hasDirection(m_resizeDirection, IPositionConstants.SOUTH)) {
      if (m_tooltip.length() != 0) {
        m_tooltip += "\n";
      }
      m_tooltip += m_tooltipHeight;
    }
    if (m_ctrlPressed) {
      m_tooltip += "\nRelease Ctrl to set size in pixels";
    } else {
      m_tooltip += "\nPress Ctrl to set size in percents";
    }
    // command
    m_command = new EditCommand(m_object) {
      @Override
      protected void executeEdit() throws Exception {
        IUIObjectSizeSupport sizeSupport = m_object.getSizeSupport();
        if (m_resizeDirection == IPositionConstants.EAST) {
          sizeSupport.setSize(newWidth, null);
        } else if (m_resizeDirection == IPositionConstants.SOUTH) {
          sizeSupport.setSize(null, newHeight);
        } else {
          sizeSupport.setSize(newWidth, newHeight);
        }
      }
    };
  }

  private String getNewWidth() {
    if (m_ctrlPressed) {
      int percent = 50 + m_sizeDelta.width;
      percent = Math.max(percent, 0);
      percent = Math.min(percent, 100);
      percent = roundToFive(percent);
      m_tooltipWidth = "width: " + percent + "%";
      return percent + "%";
    } else {
      int width = m_newSize.width;
      if (width > 0) {
        m_tooltipWidth = "width: " + width;
        return width + "px";
      } else {
        m_tooltipWidth = "width: ignore";
        return null;
      }
    }
  }

  private String getNewHeight() {
    if (m_ctrlPressed) {
      int percent = 50 + m_sizeDelta.height;
      percent = Math.max(percent, 0);
      percent = Math.min(percent, 100);
      percent = roundToFive(percent);
      m_tooltipHeight = "height: " + percent + "%";
      return percent + "%";
    } else {
      int height = m_newSize.height;
      if (height > 0) {
        m_tooltipHeight = "height: " + height;
        return height + "px";
      } else {
        m_tooltipHeight = "height: ignore";
        return null;
      }
    }
  }

  private static int roundToFive(int value) {
    return value / 5 * 5;
  }
}