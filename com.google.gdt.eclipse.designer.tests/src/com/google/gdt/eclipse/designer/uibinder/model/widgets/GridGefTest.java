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

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Cell;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Row;

/**
 * Test for {@link GridInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class GridGefTest extends UiBinderGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_canvas_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid, canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_grid_CREATE_widget() throws Exception {
    GridInfo grid =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Grid/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    canvas.moveTo(grid, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button/>",
        "      </g:customCell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_canvas_grid_MOVE_row() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
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
    canvas.beginDrag(row_3).dragTo(row_1, 0.5, 1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row wbp:name='row_3'/>",
        "    <g:row wbp:name='row_1'/>",
        "    <g:row wbp:name='row_2'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_canvas_grid_ADD_cell() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "      <g:customCell wbp:name='otherCell'>",
        "        <g:Button text='Button'/>",
        "      </g:customCell>",
        "    </g:row>",
        "    <g:row wbp:name='refRow'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    Row refRow = getObjectByName("refRow");
    //
    canvas.beginDrag(cell).dragTo(refRow, 0.5, 1).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell wbp:name='otherCell'>",
        "        <g:Button text='Button'/>",
        "      </g:customCell>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "    <g:row wbp:name='refRow'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_canvas_grid_ADD_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:Grid wbp:name='grid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    GridInfo grid = getObjectByName("grid");
    //
    canvas.beginDrag(button).dragTo(grid, 0.5, 0.5).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid wbp:name='grid'>",
        "      <g:row>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row, canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_row_CREATE_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    //
    loadButton();
    canvas.moveTo(cell, 1, 0.5).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button/>",
        "      </g:customCell>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_canvas_row_MOVE_cell() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell_1'/>",
        "      <g:cell wbp:name='cell_2'/>",
        "      <g:cell wbp:name='cell_3'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell_1 = getObjectByName("cell_1");
    Cell cell_3 = getObjectByName("cell_3");
    //
    canvas.beginDrag(cell_3).dragTo(cell_1, 1, 0.5).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell_3'/>",
        "      <g:cell wbp:name='cell_1'/>",
        "      <g:cell wbp:name='cell_2'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_canvas_row_ADD_cell() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'>cell</g:cell>",
        "      <g:cell wbp:name='otherCell'>otherCell</g:cell>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='refCell'>refCell</g:cell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    Cell refCell = getObjectByName("refCell");
    //
    canvas.beginDrag(cell).dragTo(refCell, 1, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='otherCell'>otherCell</g:cell>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'>cell</g:cell>",
        "      <g:cell wbp:name='refCell'>refCell</g:cell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_canvas_row_ADD_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:Grid>",
        "      <g:row>",
        "        <g:cell wbp:name='refCell'>refCell</g:cell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    Cell refCell = getObjectByName("refCell");
    //
    canvas.beginDrag(button).dragTo(refCell, 1, 0.5).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid>",
        "      <g:row>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "        <g:cell wbp:name='refCell'>refCell</g:cell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid, tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_grid_CREATE_widget() throws Exception {
    GridInfo grid =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Grid/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(grid).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button/>",
        "      </g:customCell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_grid_MOVE_row() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
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
    tree.startDrag(row_3).dragBefore(row_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row wbp:name='row_3'/>",
        "    <g:row wbp:name='row_1'/>",
        "    <g:row wbp:name='row_2'/>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_tree_grid_ADD_cell() throws Exception {
    openEditor(
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
    tree.startDrag(cell).dragBefore(refRow).endDrag();
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

  public void test_tree_grid_ADD_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:Grid wbp:name='grid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    GridInfo grid = getObjectByName("grid");
    //
    tree.startDrag(button).dragOn(grid).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid wbp:name='grid'>",
        "      <g:row>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row, tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_row_CREATE_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    //
    loadButton();
    tree.moveBefore(cell).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:customCell>",
        "        <g:Button/>",
        "      </g:customCell>",
        "      <g:cell wbp:name='cell'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_tree_row_MOVE_cell() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell_1'/>",
        "      <g:cell wbp:name='cell_2'/>",
        "      <g:cell wbp:name='cell_3'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell_1 = getObjectByName("cell_1");
    Cell cell_3 = getObjectByName("cell_3");
    //
    tree.startDrag(cell_3).dragBefore(cell_1).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell_3'/>",
        "      <g:cell wbp:name='cell_1'/>",
        "      <g:cell wbp:name='cell_2'/>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_tree_row_ADD_cell() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'>cell</g:cell>",
        "      <g:cell wbp:name='otherCell'>otherCell</g:cell>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='refCell'>refCell</g:cell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
    Cell cell = getObjectByName("cell");
    Cell refCell = getObjectByName("refCell");
    //
    tree.startDrag(cell).dragBefore(refCell).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Grid>",
        "    <g:row>",
        "      <g:cell wbp:name='otherCell'>otherCell</g:cell>",
        "    </g:row>",
        "    <g:row>",
        "      <g:cell wbp:name='cell'>cell</g:cell>",
        "      <g:cell wbp:name='refCell'>refCell</g:cell>",
        "    </g:row>",
        "  </g:Grid>",
        "</ui:UiBinder>");
  }

  public void test_tree_row_ADD_widget() throws Exception {
    openEditor(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:Grid>",
        "      <g:row>",
        "        <g:cell wbp:name='refCell'>refCell</g:cell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    Cell refCell = getObjectByName("refCell");
    //
    tree.startDrag(button).dragBefore(refCell).endDrag();
    assertXML(
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Grid>",
        "      <g:row>",
        "        <g:customCell>",
        "          <g:Button wbp:name='button'/>",
        "        </g:customCell>",
        "        <g:cell wbp:name='refCell'>refCell</g:cell>",
        "      </g:row>",
        "    </g:Grid>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}