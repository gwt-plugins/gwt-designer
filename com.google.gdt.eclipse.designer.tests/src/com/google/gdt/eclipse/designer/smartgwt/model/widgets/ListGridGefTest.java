/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.ListGridInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtGefTest;

import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;

import java.util.List;

/**
 * Test for {@link ListGridInfo} in GEF.
 * 
 * @author sablin_aa
 */
public class ListGridGefTest extends SmartGwtGefTest {
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
   * <code>ListGrid</code> parsing.
   */
  public void test_parse() throws Exception {
    ListGridInfo listGrid =
        openJavaInfo(
            "public class Test extends ListGrid {",
            "  public Test() {",
            "    ListGridField field = new ListGridField('field', 'Field');",
            "    setFields(new ListGridField[] { field });",
            "  }",
            "}");
    //
    assertEquals(1, listGrid.getFields().size());
    List<ArrayObjectInfo> arrayInfos = listGrid.getChildren(ArrayObjectInfo.class);
    assertEquals(1, arrayInfos.size());
  }

  /**
   * Creation fields.
   */
  public void test_CREATE() throws Exception {
    ListGridInfo listGrid =
        openJavaInfo(
            "public class Test extends ListGrid {",
            "  public Test() {",
            "    // filler",
            "  }",
            "}");
    // drop field
    loadCreationTool("com.smartgwt.client.widgets.grid.ListGridField");
    assertFeedbacks(listGrid);
    canvas.click();
    assertEditor(
        "public class Test extends ListGrid {",
        "  public Test() {",
        "    setFields(new ListGridField[] { new ListGridField('newField', 'New Field')});",
        "    // filler",
        "  }",
        "}");
    // check other
    loadCreationTool("com.smartgwt.client.widgets.grid.ListGridField");
    assertFeedbacks(listGrid);
  }

  private void assertFeedbacks(CanvasInfo canvasInfo) throws Exception {
    // check tree
    tree.moveOn(canvasInfo);
    tree.assertFeedback_on(canvasInfo);
    tree.assertCommandNotNull();
    /* check canvas
     * flow container only for tree
    canvas.moveTo(canvasInfo, 10, 10);
    canvas.assertFeedbackFigures(1);
    canvas.assertCommandNotNull();*/
  }
}