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
import com.google.gdt.eclipse.designer.gef.policy.grid.header.actions.SetAlignmentColumnAction;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

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
 * {@link EditPart} for {@link ColumnInfo} header of {@link MigLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public class ColumnHeaderEditPart extends DimensionHeaderEditPart<ColumnInfo> {
  private final ColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnHeaderEditPart(HTMLTableInfo panel, ColumnInfo column, Figure containerFigure) {
    super(panel, column, containerFigure);
    m_column = column;
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
        graphics.drawLine(r.x, r.y, r.x, r.bottom());
        graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
        // draw column index
        int titleLeft;
        {
          String title = "" + getIndex();
          Dimension textExtents = graphics.getTextExtent(title);
          if (r.width < 3 + textExtents.width + 3) {
            return;
          }
          // draw title
          titleLeft = r.x + (r.width - textExtents.width) / 2;
          int y = r.y + (r.height - textExtents.height) / 2;
          graphics.setForegroundColor(IColorConstants.black);
          graphics.drawText(title, titleLeft, y);
        }
        // draw alignment indicator
        if (titleLeft - r.x > 3 + 7 + 3) {
          Image image = m_column.getAlignment().getSmallImage();
          if (image != null) {
            int x = r.x + 2;
            drawCentered(graphics, image, x);
          }
        }
      }

      private void drawCentered(Graphics graphics, Image image, int x) {
        int y = (getBounds().height - image.getBounds().height) / 2;
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
      Interval interval = m_panel.getGridInfo().getColumnIntervals()[index];
      Rectangle bounds =
          new Rectangle(interval.begin,
              0,
              interval.length + 1,
              ((GraphicalEditPart) getParent()).getFigure().getSize().height);
      bounds.translate(getOffset().x, 0);
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
      manager.add(new DimensionHeaderAction<ColumnInfo>(this, "Insert Column") {
        @Override
        protected void run(ColumnInfo dimension, int index) throws Exception {
          m_panel.insertColumn(index);
        }
      });
      if (!m_column.isLast()) {
        manager.add(new DimensionHeaderAction<ColumnInfo>(this, "Append Column") {
          @Override
          protected void run(ColumnInfo dimension, int index) throws Exception {
            m_panel.insertColumn(index + 1);
          }
        });
      }
      manager.add(new DimensionHeaderAction<ColumnInfo>(this, "Delete Column",
          HTMLTableInfo.getImageDescriptor("h/menu/delete.gif")) {
        @Override
        protected void run(ColumnInfo dimension, int index) throws Exception {
          m_panel.deleteColumn(index);
        }
      });
      manager.add(new DimensionHeaderAction<ColumnInfo>(this, "Clear Column") {
        @Override
        protected void run(ColumnInfo dimension, int index) throws Exception {
          m_panel.clearColumn(index);
        }
      });
    }
    // alignment
    {
      manager.add(new Separator());
      manager.add(new SetAlignmentColumnAction(this, "Default", ColumnInfo.Alignment.UNKNOWN));
      manager.add(new SetAlignmentColumnAction(this, "Left", ColumnInfo.Alignment.LEFT));
      manager.add(new SetAlignmentColumnAction(this, "Center", ColumnInfo.Alignment.CENTER));
      manager.add(new SetAlignmentColumnAction(this, "Right", ColumnInfo.Alignment.RIGHT));
    }
  }

  @Override
  protected void editDimension() {
  }
}
