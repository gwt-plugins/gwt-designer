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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.uibinder.Activator;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.TagCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.Grid</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class GridInfo extends PanelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    parseRowsCells();
    decorateCellText();
    removeEmptyCustomCell();
    contributeCellContextMenu();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses "row", "cell" and "customCell" elements into appropriate models.
   */
  private void parseRowsCells() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        Map<DocumentElement, CustomCell> customCells = Maps.newHashMap();
        DocumentElement gridElement = getElement();
        for (DocumentElement rowElement : gridElement.getChildren()) {
          if (rowElement.getTagLocal().equals("row")) {
            Row row = new Row(rowElement);
            addChild(row);
            for (DocumentElement cellElement : rowElement.getChildren()) {
              if (cellElement.getTagLocal().equals("cell")) {
                HtmlCell cell = new HtmlCell(cellElement);
                row.addChild(cell);
              }
              if (cellElement.getTagLocal().equals("customCell")) {
                CustomCell cell = new CustomCell(cellElement);
                row.addChild(cell);
                customCells.put(cellElement, cell);
              }
            }
          }
        }
        // re-bind Widget children to "customCell" models
        for (WidgetInfo widget : getChildrenWidgets()) {
          DocumentElement cellElement = widget.getElement().getParent();
          CustomCell cell = customCells.get(cellElement);
          removeChild(widget);
          cell.addChild(widget);
        }
      }
    });
  }

  /**
   * Decorates text of {@link Cell} with short excerpt of its content.
   */
  private void decorateCellText() {
    addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof HtmlCell && isParentOf(object)) {
          DocumentElement element = ((HtmlCell) object).getElement();
          int beginIndex = element.getOpenTagOffset() + element.getOpenTagLength();
          int endIndex = element.getCloseTagOffset();
          String contentText = getContext().getContent().substring(beginIndex, endIndex);
          contentText = contentText.trim();
          contentText = StringUtils.substring(contentText, 0, 15);
          text[0] += " " + contentText;
        }
      }
    });
  }

  /**
   * {@link CustomCell} should always have a {@link WidgetInfo} child, so when it looses it, we
   * should remove {@link CustomCell} too.
   */
  private void removeEmptyCustomCell() {
    addBroadcastListener(new XmlObjectMove() {
      @Override
      public void after(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        if (oldParent instanceof CustomCell && isParentOf(oldParent)) {
          oldParent.delete();
        }
      }
    });
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent instanceof CustomCell && isParentOf(parent) && !parent.isDeleting()) {
          parent.delete();
        }
      }
    });
  }

  private void contributeCellContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof Cell && isParentOf(object)) {
          final Cell cell = (Cell) object;
          final Row row = (Row) cell.getParent();
          addAction(manager, "Insert cell", "insertCell", new RunnableEx() {
            public void run() throws Exception {
              createCell(row, cell);
            }
          });
          addAction(manager, "Append cell", "appendCell", new RunnableEx() {
            public void run() throws Exception {
              Cell nextCell = GenericsUtils.getNextOrNull(row.getCells(), cell);
              createCell(row, nextCell);
            }
          });
        }
      }

      private void addAction(IMenuManager manager,
          String text,
          String iconName,
          final RunnableEx runnable) {
        Image icon = Activator.getImage("info/Grid/" + iconName + ".png");
        manager.appendToGroup(DesignContextMenuProvider.GROUP_TOP, new ObjectInfoAction(
            GridInfo.this, text, icon) {
          @Override
          protected void runEx() throws Exception {
            runnable.run();
          }
        });
      }

      private void createCell(Row row, Cell nextCell) throws Exception {
        HtmlCell newCell = new HtmlCell();
        row.command_CREATE(newCell, nextCell);
        newCell.getElement().setText("New cell", false);
        row.getBroadcastObject().select(ImmutableList.of(newCell));
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Row} children.
   */
  public List<Row> getRows() {
    return getChildren(Row.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    setElementsForParts();
    super.refresh_fetch();
  }

  /**
   * Sets DOM elements for {@link GridElement} models.
   */
  private void setElementsForParts() throws Exception {
    Object bodyElement = ReflectionUtils.invokeMethod(getObject(), "getBodyElement()");
    List<Row> rows = getRows();
    Object[] rowElements = getDOM().getChildren(bodyElement);
    for (int i = 0; i < rowElements.length; i++) {
      Object rowElement = rowElements[i];
      if (i < rows.size()) {
        Row row = rows.get(i);
        row.setElement(rowElement);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_MOVE(Row row, Row nextRow) throws Exception {
    XmlObjectUtils.move(row, Associations.direct(), this, nextRow);
  }

  public void command_ADD(Cell cell, Row nextRow) throws Exception {
    Row newRow = command_addNewRow(nextRow);
    newRow.command_MOVE(cell, null);
  }

  public void command_CREATE(WidgetInfo newWidget, Row nextRow) throws Exception {
    Row newRow = command_addNewRow(nextRow);
    newRow.command_CREATE(newWidget, null);
  }

  public void command_ADD(WidgetInfo widget, Row nextRow) throws Exception {
    Row newRow = command_addNewRow(nextRow);
    newRow.command_ADD(widget, null);
  }

  private Row command_addNewRow(Row nextRow) throws Exception {
    Row newRow = new Row();
    XmlObjectUtils.add(newRow, Associations.direct(), this, nextRow);
    return newRow;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GridElement
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, ComponentDescription> m_gridElementDescriptions = Maps.newTreeMap();

  private ComponentDescription getElementDescription(String name) throws Exception {
    ComponentDescription description = m_gridElementDescriptions.get(name);
    if (description == null) {
      description = new ComponentDescription(Object.class);
      description.setToolkit(GwtToolkitDescription.INSTANCE);
      description.setIcon(ComponentDescriptionHelper.getIcon(
          getContext(),
          getDescription().getComponentClass(),
          "_" + name));
      m_gridElementDescriptions.put(name, description);
    }
    return description;
  }

  public class GridElement extends AbstractComponentInfo {
    private Rectangle m_absoluteBounds;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public GridElement(ComponentDescription description, DocumentElement element) throws Exception {
      super(GridInfo.this.getContext(), description, new TagCreationSupport(element));
    }

    public GridElement(ComponentDescription description, String tag) throws Exception {
      super(GridInfo.this.getContext(),
          description,
          new TagCreationSupport(GridInfo.this.getElement().getTagNS() + tag));
    }

    @Override
    public Rectangle getAbsoluteBounds() {
      return m_absoluteBounds;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Refresh
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void refresh_fetch() throws Exception {
      fetchBounds();
      super.refresh_fetch();
    }

    private void fetchBounds() {
      Object element = getObject();
      // absolute bounds
      m_absoluteBounds = getState().getAbsoluteBounds(element);
      // model/parent bounds
      Rectangle bounds = m_absoluteBounds.getCopy();
      AbstractComponentInfo parent = (AbstractComponentInfo) getParent();
      bounds.translate(parent.getAbsoluteBounds().getLocation().getNegated());
      bounds.translate(parent.getClientAreaInsets().getNegated());
      setModelBounds(bounds);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Row
  //
  ////////////////////////////////////////////////////////////////////////////
  public final class Row extends GridElement {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Row() throws Exception {
      super(getElementDescription("row"), "row");
    }

    public Row(DocumentElement element) throws Exception {
      super(getElementDescription("row"), element);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the {@link Cell} children.
     */
    public List<Cell> getCells() {
      return getChildren(Cell.class);
    }

    /**
     * Sets DOM element for this {@link Row} and its {@link Cell} children.
     */
    public void setElement(Object rowElement) throws Exception {
      setObject(rowElement);
      List<Cell> cells = getCells();
      Object[] cellElements = getDOM().getChildren(rowElement);
      for (int i = 0; i < cellElements.length; i++) {
        Object cellElement = cellElements[i];
        if (i < cells.size()) {
          Cell cell = cells.get(i);
          cell.setObject(cellElement);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Commands
    //
    ////////////////////////////////////////////////////////////////////////////
    public void command_CREATE(Cell cell, Cell nextCell) throws Exception {
      XmlObjectUtils.add(cell, Associations.direct(), this, nextCell);
    }

    public void command_MOVE(Cell cell, Cell nextCell) throws Exception {
      XmlObjectUtils.move(cell, Associations.direct(), this, nextCell);
    }

    public void command_CREATE(WidgetInfo newWidget, Cell nextCell) throws Exception {
      CustomCell newCell = command_addNewCustomCell(nextCell);
      newCell.command_CREATE(newWidget);
    }

    public void command_ADD(WidgetInfo widget, Cell nextCell) throws Exception {
      CustomCell newCell = command_addNewCustomCell(nextCell);
      newCell.command_ADD(widget);
    }

    private CustomCell command_addNewCustomCell(Cell nextCell) throws Exception {
      CustomCell newCell = new CustomCell();
      command_CREATE(newCell, nextCell);
      return newCell;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Cell
  //
  ////////////////////////////////////////////////////////////////////////////
  public class Cell extends GridElement {
    public Cell(ComponentDescription description, DocumentElement element) throws Exception {
      super(description, element);
    }

    public Cell(ComponentDescription description, String tag) throws Exception {
      super(description, tag);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // HtmlCell
  //
  ////////////////////////////////////////////////////////////////////////////
  public final class HtmlCell extends Cell {
    public HtmlCell(DocumentElement element) throws Exception {
      super(getElementDescription("cell"), element);
    }

    public HtmlCell() throws Exception {
      super(getElementDescription("cell"), "cell");
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // CustomCell
  //
  ////////////////////////////////////////////////////////////////////////////
  public final class CustomCell extends Cell {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public CustomCell(DocumentElement element) throws Exception {
      super(getElementDescription("customCell"), element);
    }

    public CustomCell() throws Exception {
      super(getElementDescription("customCell"), "customCell");
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Command
    //
    ////////////////////////////////////////////////////////////////////////////
    public void command_CREATE(WidgetInfo widget) throws Exception {
      XmlObjectUtils.add(widget, Associations.direct(), this, null);
    }

    public void command_ADD(WidgetInfo widget) throws Exception {
      XmlObjectUtils.move(widget, Associations.direct(), this, null);
    }
  }
}
