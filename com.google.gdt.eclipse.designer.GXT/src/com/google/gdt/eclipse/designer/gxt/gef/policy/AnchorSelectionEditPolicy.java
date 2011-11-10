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
import com.google.gdt.eclipse.designer.gxt.model.layout.AnchorLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link AnchorLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public class AnchorSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorSelectionEditPolicy(WidgetInfo widget) {
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(createResizeHandle(IPositionConstants.EAST, 0.25, IColorConstants.green));
    handles.add(createResizeHandle(IPositionConstants.SOUTH, 0.25, IColorConstants.green));
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
      final String newWidth = getNewWidth();
      m_command = new EditCommand(m_widget) {
        @Override
        protected void executeEdit() throws Exception {
          AnchorLayoutInfo.getAnchorData(m_widget).setAnchorWidth(newWidth);
        }
      };
    }
    // SOUTH
    if (m_resizeDirection == IPositionConstants.SOUTH) {
      final String newHeight = getNewHeight();
      m_command = new EditCommand(m_widget) {
        @Override
        protected void executeEdit() throws Exception {
          AnchorLayoutInfo.getAnchorData(m_widget).setAnchorHeight(newHeight);
        }
      };
    }
  }

  private String getNewWidth() {
    if (m_ctrlPressed) {
      int width = 100 * m_newSize.width / getParentWidth();
      width = Math.max(width, 0);
      width = Math.min(width, 100);
      width = roundToFive(width);
      //
      String widthString = width + "%";
      m_tooltip = "width: " + widthString + "\nPress Ctrl to set width in percents";
      return widthString;
    } else {
      int width = m_newSize.width - getParentWidth();
      width = Math.min(width, 0);
      width = roundToFive(width);
      //
      String widthString = "parentWidth - " + Math.abs(width);
      m_tooltip = "width: " + widthString + "\nPress Ctrl to set width in percents";
      return "" + width;
    }
  }

  private String getNewHeight() {
    if (m_ctrlPressed) {
      int height = 100 * m_newSize.height / getParentHeight();
      height = Math.max(height, 0);
      height = Math.min(height, 100);
      height = roundToFive(height);
      //
      String heightString = height + "%";
      m_tooltip = "height: " + heightString + "\nPress Ctrl to set height in percents";
      return heightString;
    } else {
      int height = m_newSize.height - getParentHeight();
      height = Math.min(height, 0);
      height = roundToFive(height);
      //
      String heightString = "parentHeight - " + Math.abs(height);
      m_tooltip = "height: " + heightString + "\nPress Ctrl to set height in percents";
      return "" + height;
    }
  }

  private static int roundToFive(int value) {
    return value / 5 * 5;
  }
}