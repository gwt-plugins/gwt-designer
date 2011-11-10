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
package com.google.gdt.eclipse.designer.gef.policy.grid.header.edit;

import com.google.gdt.eclipse.designer.gef.policy.grid.header.actions.DimensionHeaderAction;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.actions.SetAlignmentRowAction;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

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
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link RowInfo} header of {@link MigLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public class RowHeaderEditPart extends DimensionHeaderEditPart<RowInfo> {
  private final RowInfo m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowHeaderEditPart(HTMLTableInfo panel, RowInfo row, Figure containerFigure) {
    super(panel, row, containerFigure);
    m_row = row;
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
          String title = "" + getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.height < textExtents.height) {
            return;
          }
          // draw title
          titleTop = r.y + (r.height - textExtents.height) / 2;
          int x = r.x + (r.width - textExtents.width) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, x, titleTop);
        }
        // draw alignment indicator
        if (titleTop - r.y > 3 + 7 + 3) {
          Image image = m_row.getAlignment().getSmallImage();
          if (image != null) {
            int y = r.y + 2;
            drawCentered(graphics, image, y);
          }
        }
      }

      private void drawCentered(Graphics graphics, Image image, int y) {
        int x = (getBounds().width - image.getBounds().width) / 2;
        graphics.drawImage(image, x, y);
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
    Figure figure = getFigure();
    // bounds
    {
      int index = getIndex();
      Interval interval = m_panel.getGridInfo().getRowIntervals()[index];
      Rectangle bounds =
          new Rectangle(0,
              interval.begin,
              ((GraphicalEditPart) getParent()).getFigure().getSize().width,
              interval.length + 1);
      bounds.translate(0, getOffset().y);
      figure.setBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    // operations
    {
      manager.add(new DimensionHeaderAction<RowInfo>(this, "Insert Row") {
        @Override
        protected void run(RowInfo dimension, int index) throws Exception {
          m_panel.insertRow(index);
        }
      });
      if (!m_row.isLast()) {
        manager.add(new DimensionHeaderAction<RowInfo>(this, "Append Row") {
          @Override
          protected void run(RowInfo dimension, int index) throws Exception {
            m_panel.insertRow(index + 1);
          }
        });
      }
      manager.add(new DimensionHeaderAction<RowInfo>(this, "Delete Row",
          HTMLTableInfo.getImageDescriptor("v/menu/delete.gif")) {
        @Override
        protected void run(RowInfo dimension, int index) throws Exception {
          m_panel.deleteRow(index);
        }
      });
      manager.add(new DimensionHeaderAction<RowInfo>(this, "Clear Row") {
        @Override
        protected void run(RowInfo dimension, int index) throws Exception {
          m_panel.clearRow(index);
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentRowAction(this, "Default", RowInfo.Alignment.UNKNOWN));
      manager.add(new SetAlignmentRowAction(this, "Top", RowInfo.Alignment.TOP));
      manager.add(new SetAlignmentRowAction(this, "Center", RowInfo.Alignment.MIDDLE));
      manager.add(new SetAlignmentRowAction(this, "Bottom", RowInfo.Alignment.BOTTOM));
    }
  }

  @Override
  protected void editDimension() {
  }
}
