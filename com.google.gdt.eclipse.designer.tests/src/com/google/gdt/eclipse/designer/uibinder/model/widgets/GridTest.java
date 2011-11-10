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

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Cell;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.CustomCell;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.HtmlCell;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Row;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GridInfo} widget.
 * 
 * @author scheglov_ke
 */
public class GridTest extends UiBinderModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty <code>Grid</code> should have some reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid wbp:name='grid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    GridInfo grid = getObjectByName("grid");
    // bounds
    {
      Rectangle bounds = grid.getBounds();
      assertThat(bounds.width).isGreaterThan(70);
      assertThat(bounds.height).isGreaterThan(20);
    }
    // no rows
    List<Row> rows = grid.getRows();
    assertThat(rows).isEmpty();
  }

  public void test_cellText() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid>",
        "      <g:row>",
        "        <g:cell wbp:name='cell_00'>0-0</g:cell>",
        "        <g:cell wbp:name='cell_01'>",
        "          0-1",
        "        </g:cell>",
        "        <g:cell wbp:name='cell_02'>some <b>very long</b> text</g:cell>",
        "      </g:row>",
        "      <g:row>",
        "        <g:cell>1-0</g:cell>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell_00'>",
        "      <g:cell wbp:name='cell_01'>",
        "      <g:cell wbp:name='cell_02'>",
        "    <g:row>",
        "      <g:cell>",
        "      <g:customCell>",
        "        <g:Button wbp:name='button'>");
    refresh();
    HtmlCell cell_00 = getObjectByName("cell_00");
    HtmlCell cell_01 = getObjectByName("cell_01");
    HtmlCell cell_02 = getObjectByName("cell_02");
    // text for components tree
    assertEquals("g:cell 0-0", ObjectsLabelProvider.INSTANCE.getText(cell_00));
    assertEquals("g:cell 0-1", ObjectsLabelProvider.INSTANCE.getText(cell_01));
    assertEquals("g:cell some <b>very lo", ObjectsLabelProvider.INSTANCE.getText(cell_02));
  }

  public void test_rowCellBounds() throws Exception {
    GridInfo grid =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Grid>",
            "    <g:row>",
            "      <g:cell>0-0</g:cell>",
            "      <g:cell>0-1</g:cell>",
            "    </g:row>",
            "    <g:row>",
            "      <g:cell>1-0</g:cell>",
            "      <g:cell>1-1</g:cell>",
            "      <g:cell>1-2</g:cell>",
            "    </g:row>",
            "  </g:Grid>",
            "</ui:UiBinder>");
    refresh();
    // has 2 rows
    List<Row> rows = grid.getRows();
    assertThat(rows).hasSize(2);
    // row 0
    {
      Row row = rows.get(0);
      {
        Rectangle bounds = row.getBounds();
        assertEquals(new Rectangle(0, 2, 450, 147), bounds);
      }
      // has 2 cells
      List<Cell> cells = row.getCells();
      assertThat(cells).hasSize(2);
      {
        Cell cell = cells.get(0);
        Rectangle bounds = cell.getBounds();
        assertEquals(new Rectangle(2, 0, 147, 147), bounds);
      }
      {
        Cell cell = cells.get(1);
        Rectangle bounds = cell.getBounds();
        assertEquals(new Rectangle(151, 0, 147, 147), bounds);
      }
    }
    // row 1
    {
      Row row = rows.get(1);
      {
        Rectangle bounds = row.getBounds();
        assertEquals(new Rectangle(0, 151, 450, 147), bounds);
      }
      List<Cell> cells = row.getCells();
      assertThat(cells).hasSize(3);
      {
        Cell cell = cells.get(0);
        Rectangle bounds = cell.getBounds();
        assertEquals(new Rectangle(2, 0, 147, 147), bounds);
      }
      {
        Cell cell = cells.get(1);
        Rectangle bounds = cell.getBounds();
        assertEquals(new Rectangle(151, 0, 147, 147), bounds);
      }
      {
        Cell cell = cells.get(2);
        Rectangle bounds = cell.getBounds();
        assertEquals(new Rectangle(301, 0, 147, 147), bounds);
      }
    }
  }

  /**
   * When we delete {@link WidgetInfo} from {@link CustomCell}, the cell also should be deleted.
   */
  public void test_deleteWidget_onCustomCell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell/>",
        "      <g:customCell>",
        "        <g:Button wbp:name='button'/>",
        "      </g:customCell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    // do delete
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  /**
   * Deleting {@link CustomCell} should not cause attempt of double deleting.
   */
  public void test_deleteCustomCell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell/>",
        "      <g:customCell wbp:name='cell'>",
        "        <g:Button/>",
        "      </g:customCell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    // do delete
    cell.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridInfo#command_MOVE(Row, Row)}.
   */
  public void test_grid_MOVE_row() throws Exception {
    GridInfo grid =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Grid>",
            "    <g:row wbp:name='row_1'/>",
            "    <g:row wbp:name='row_2'/>",
            "    <g:row wbp:name='row_3'/>",
            "  </g:Grid>",
            "</ui:UiBinder>");
    Row row_1 = getObjectByName("row_1");
    Row row_3 = getObjectByName("row_3");
    //
    grid.command_MOVE(row_3, row_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row wbp:name='row_3'/>",
        "    <g:row wbp:name='row_1'/>",
        "    <g:row wbp:name='row_2'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link GridInfo#command_ADD(Cell, Row)}.
   */
  public void test_grid_ADD_cell() throws Exception {
    GridInfo grid =
        parse(
            "<ui:UiBinder>",
            "  <g:Grid>",
            "    <g:row>",
            "      <g:cell wbp:name='cell'/>",
            "      <g:cell wbp:name='otherCell'/>",
            "    </g:row>",
            "    <g:row wbp:name='refRow'/>",
            "  </g:Grid>",
            "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    Row refRow = getObjectByName("refRow");
    //
    grid.command_ADD(cell, refRow);
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='otherCell'/>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "    <g:row wbp:name='refRow'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(WidgetInfo, Row)}.
   */
  public void test_grid_CREATE_widget() throws Exception {
    GridInfo grid =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Grid>",
            "    <g:row wbp:name='refRow'/>",
            "  </g:Grid>",
            "</ui:UiBinder>");
    Row refRow = getObjectByName("refRow");
    //
    WidgetInfo newButton = createButton();
    grid.command_CREATE(newButton, refRow);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button/>",
        "      </g:customCell>",
        "    </g:row>",
        "    <g:row wbp:name='refRow'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link GridInfo#command_ADD(WidgetInfo, Row)}.
   */
  public void test_grid_ADD_widget() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:Grid wbp:name='grid'>",
        "      <g:row wbp:name='refRow'/>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    GridInfo grid = getObjectByName("grid");
    Row refRow = getObjectByName("refRow");
    //
    grid.command_ADD(button, refRow);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid wbp:name='grid'>",
        "      <g:row>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "      </g:row>",
        "      <g:row wbp:name='refRow'/>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link Row#command_ADD(WidgetInfo, Cell)}.
   */
  public void test_row_ADD_widget() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button wbp:name='button'/>",
        "      </g:customCell>",
        "      <g:cell/>",
        "    </g:row>",
        "    <g:row wbp:name='row'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    Row row = getObjectByName("row");
    //
    row.command_ADD(button, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell/>",
        "    </g:row>",
        "    <g:row wbp:name='row'>",
        "      <g:customCell>",
        "        <g:Button wbp:name='button'/>",
        "      </g:customCell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Cell} context menu.
   */
  public void test_contextMenu_insertCell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    //
    IMenuManager contextMenu = getContextMenu(cell);
    IAction action = findChildAction(contextMenu, "Insert cell");
    assertNotNull(action);
    assertNotNull(action.getImageDescriptor());
    action.run();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell>New cell</g:cell>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link Cell} context menu.
   */
  public void test_contextMenu_appendCell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    //
    IMenuManager contextMenu = getContextMenu(cell);
    IAction action = findChildAction(contextMenu, "Append cell");
    assertNotNull(action);
    assertNotNull(action.getImageDescriptor());
    action.run();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "      <g:cell>New cell</g:cell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }
}