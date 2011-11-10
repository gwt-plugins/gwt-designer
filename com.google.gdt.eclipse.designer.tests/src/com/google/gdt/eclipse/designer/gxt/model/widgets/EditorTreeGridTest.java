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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Test for <code>EditorTreeGrid</code>.
 * 
 * @author scheglov_ke
 */
public class EditorTreeGridTest extends GxtModelTest {
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
  public void test_CREATE() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare new Grid
    WidgetInfo newGrid = createJavaInfo("com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid");
    // check "live image"
    {
      Image liveImage = newGrid.getImage();
      assertEquals(302, liveImage.getBounds().width);
      assertEquals(202, liveImage.getBounds().height);
    }
    // do create
    panel.command_CREATE2(newGrid, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;",
        "import com.extjs.gxt.ui.client.store.TreeStore;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnModel;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      EditorTreeGrid editorTreeGrid = new EditorTreeGrid(new TreeStore(), new ColumnModel(new ArrayList<ColumnConfig>()));",
        "      add(editorTreeGrid);",
        "      editorTreeGrid.setBorders(true);",
        "    }",
        "  }",
        "}");
  }
}