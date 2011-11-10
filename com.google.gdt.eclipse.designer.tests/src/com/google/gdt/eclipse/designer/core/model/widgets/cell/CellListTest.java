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
package com.google.gdt.eclipse.designer.core.model.widgets.cell;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellListInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>com.google.gwt.user.cellview.client.CellList</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class CellListTest extends GwtModelTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    super.configureModule(moduleDescription);
    // prepare classes
    setFileContentSrc(
        "test/client/User.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "public class User {",
            "}"));
    forgetCreatedResources();
  }

  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "import java.util.ArrayList;",
            "import java.util.Collections;",
            "import com.google.gwt.safehtml.shared.SafeHtmlBuilder;",
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.cell.client.Cell.Context;",
            "import com.google.gwt.user.cellview.client.*;",}, lines);
    return super.getTestSource_decorate(lines);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty <code>CellTable</code> should have reasonable size, to ensure this we fill it with
   * artificial items.
   */
  public void test_parseEmpty() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellList<User> cellList = new CellList<User>(",
        "      new AbstractCell<User>() {",
        "        @Override",
        "        public void render(Context context, User value, SafeHtmlBuilder sb) {",
        "        }",
        "      });",
        "    add(cellList);",
        "  }",
        "}");
    refresh();
    CellListInfo cellList = getJavaInfoByName("cellList");
    // has reasonable size
    {
      Rectangle bounds = cellList.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(100);
    }
  }

  /**
   * Sometimes "Cell" arguments can not be evaluated, so we replace it with "null". But
   * <code>CellList</code> does not like "null", so we should fix it.
   */
  public void test_nullCell() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellList<User> cellList = new CellList<User>(null);",
        "    add(cellList);",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  public void test_create() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // do create
    {
      CellListInfo cellList = createJavaInfo("com.google.gwt.user.cellview.client.CellList");
      cellList.putTemplateArgument("rowType", "test.client.User");
      flowContainer_CREATE(panel, cellList, null);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellList<User> cellList = new CellList<User>(new AbstractCell<User>(){",
        "        @Override",
        "        public void render(Context context, User value, SafeHtmlBuilder sb) {",
        "          // TODO",
        "        }",
        "      });",
        "      add(cellList);",
        "    }",
        "  }",
        "}");
  }
}