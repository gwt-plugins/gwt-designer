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
package com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.edit;

import com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.actions.DimensionHeaderAction;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.DimensionInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.RowInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

/**
 * {@link EditPart} for {@link RowInfo} header of {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.TableLayout
 */
public final class RowHeaderEditPart extends DimensionHeaderEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(TableLayoutInfo layout, RowInfo row, Figure containerFigure) {
    super(layout, row, containerFigure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure newFigure = new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle r = getClientArea();
        // draw rectangle
        graphics.setForegroundColor(IColorConstants.buttonDarker);
        graphics.drawLine(r.x, r.y, r.right(), r.y);
        graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
        // draw row index
        int titleTop;
        {
          String title = "" + (1 + m_dimension.getIndex());
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < 3 + textExtents.height + 3) {
            return;
          }
          // draw title
          titleTop = r.y + (r.height - textExtents.height) / 2;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
      }
    };
    //
    newFigure.setFont(DEFAULT_FONT);
    newFigure.setOpaque(true);
    return newFigure;
  }

  @Override
  protected void refreshVisuals() {
    super.refreshVisuals();
    // prepare column interval
    Interval interval;
    {
      int index = m_dimension.getIndex();
      IGridInfo gridInfo = m_layout.getGridInfo();
      interval = gridInfo.getRowIntervals()[index];
    }
    // prepare bounds
    Rectangle bounds;
    {
      bounds =
          new Rectangle(0,
              interval.begin,
              ((GraphicalEditPart) getParent()).getFigure().getSize().width,
              interval.length + 1);
      bounds.translate(0, getOffset().y);
    }
    // set bounds
    getFigure().setBounds(bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // operations
    {
      manager.add(new Separator());
      manager.add(new DimensionHeaderAction(this, "&Delete Row",
          TableLayoutInfo.getImageDescriptor("v/menu/delete.gif")) {
        @Override
        protected void run(DimensionInfo dimension) throws Exception {
          m_layout.command_deleteRow(dimension.getIndex(), true);
        }
      });
    }
  }
}
