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
package com.google.gdt.eclipse.designer.core.model.widgets.cell;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Test for <code>com.google.gwt.user.cellview.client.CellTree</code>.
 * 
 * @author sablin_aa
 */
public class CellTreeTest extends GwtModelTest {
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
  private void test_parse(String... lines) throws Exception {
    RootPanelInfo frame = parseJavaInfo(lines);
    frame.refresh();
    CompositeInfo cellTree = (CompositeInfo) frame.getChildrenWidgets().get(0);
    assertNoErrors(cellTree);
    {
      // check class
      Class<?> cellTreeClass = cellTree.getDescription().getComponentClass();
      assertTrue(ReflectionUtils.isSuccessorOf(
          cellTreeClass,
          "com.google.gwt.user.cellview.client.CellTree"));
    }
    {
      // check properties
      Property[] properties = cellTree.getProperties();
      assertTrue(properties.length > 0);
      {
        ComplexProperty constructorProperty =
            (ComplexProperty) cellTree.getPropertyByTitle("Constructor");
        assertNull(constructorProperty);
        // check if <tag name="property.no" value="true"/> removed
        //Property[] constructorProperties = constructorProperty.getProperties();
        //assertNotNull(constructorProperties[1].getTitle());
      }
    }
  }

  public void test_parse_1() throws Exception {
    test_parse(
        "import com.google.gwt.user.cellview.client.CellTree;",
        "import com.google.gwt.view.client.TreeViewModel;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTree cellTree = new<String> CellTree(new TreeViewModel() {",
        "          public <T> NodeInfo<?> getNodeInfo(T value) { return null; }",
        "          public boolean isLeaf(Object value) { return true; }",
        "        }, null);",
        "      rootPanel.add(cellTree);",
        "    }",
        "  }",
        "}");
  }

  public void test_parse_2() throws Exception {
    test_parse(
        "import com.google.gwt.user.cellview.client.CellTree;",
        "import com.google.gwt.view.client.TreeViewModel;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTree cellTree = new CellTree(new TreeViewModel() {",
        "          public <T> NodeInfo<?> getNodeInfo(T value) { return null; }",
        "          public boolean isLeaf(Object value) { return true; }",
        "        }, 'string');",
        "      rootPanel.add(cellTree);",
        "    }",
        "  }",
        "}");
  }

  public void test_create() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    CompositeInfo cellTree = createJavaInfo("com.google.gwt.user.cellview.client.CellTree");
    frame.command_CREATE2(cellTree, null);
    frame.command_BOUNDS(cellTree, new Point(10, 10), null);
    //
    assertEditor(
        "import com.google.gwt.user.cellview.client.CellTree;",
        "import com.google.gwt.view.client.TreeViewModel;",
        "import com.google.gwt.view.client.AbstractDataProvider;",
        "import com.google.gwt.view.client.ListDataProvider;",
        "import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;",
        "import com.google.gwt.view.client.NoSelectionModel;",
        "import com.google.gwt.view.client.TreeViewModel.NodeInfo;",
        "import com.google.gwt.view.client.TreeViewModel.DefaultNodeInfo;",
        "import com.google.gwt.cell.client.TextCell;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTree cellTree = new CellTree(",
        "        new TreeViewModel() {",
        "          final AbstractDataProvider<String> dataProvider = new ListDataProvider<String>();",
        "          final AbstractSelectionModel<String> selectionModel = new NoSelectionModel<String>();",
        "          @Override",
        "          public <T> NodeInfo<?> getNodeInfo(T value) {",
        "            return new DefaultNodeInfo<String>(dataProvider, new TextCell(), selectionModel, null);",
        "          }",
        "          @Override",
        "          public boolean isLeaf(Object value) {",
        "            return true;",
        "          }",
        "        }, null);",
        "      rootPanel.add(cellTree, 10, 10);",
        "    }",
        "  }",
        "}");
  }
}