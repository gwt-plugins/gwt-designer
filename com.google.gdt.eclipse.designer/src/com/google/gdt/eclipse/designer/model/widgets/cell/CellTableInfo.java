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
package com.google.gdt.eclipse.designer.model.widgets.cell;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.util.GwtInvocationEvaluatorInterceptor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.cellview.client.CellTable</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class CellTableInfo extends AbstractHasDataInfo {
  static final String TABLE_CLASS_NAME = "com.google.gwt.user.cellview.client.CellTable";
  static final String COLUMN_CLASS_NAME = "com.google.gwt.user.cellview.client.Column";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellTableInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addClipboardSupport();
    // creation support templates
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void associationTemplate(JavaInfo component, String[] source) throws Exception {
        // Column creation
        if (component instanceof ColumnInfo && component.getParent() == CellTableInfo.this) {
          String rowTypeName;
          {
            ITypeBinding rowTypeBinding = getRowTypeBinding();
            rowTypeName = AstNodeUtils.getFullyQualifiedName(rowTypeBinding, false);
          }
          // apply "%rowType%" into creation source
          source[0] = StringUtils.replace(source[0], "%rowType%", rowTypeName);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<ColumnInfo> getColumns() {
    return getChildren(ColumnInfo.class);
  }

  /**
   * @return the height of header with columns.
   */
  public int getHeaderHeight() throws Exception {
    Object thead = ReflectionUtils.getFieldObject(getObject(), "thead");
    return getState().getAbsoluteBounds(thead).height;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    // create exposed columns
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> columnClass = editorLoader.loadClass("com.google.gwt.user.cellview.client.Column");
    JavaInfoUtils.addExposedChildren(this, new Class<?>[]{columnClass});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    replaceCustomColumns();
  }

  /**
   * We set {@link String} objects as rows. But if there is custom <code>Column</code> which expects
   * some real "row", this can cause {@link ClassCastException}. So, we replace custom
   * <code>Column</code> with fake.
   */
  @SuppressWarnings("unchecked")
  private void replaceCustomColumns() throws ClassNotFoundException, Exception {
    // prepare object -> model map
    Map<Object, ColumnInfo> columnModels = Maps.newHashMap();
    for (ColumnInfo columnModel : getColumns()) {
      columnModels.put(columnModel.getObject(), columnModel);
    }
    // replace columns
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    List<Object> columns = (List<Object>) ReflectionUtils.getFieldObject(getObject(), "columns");
    for (int i = 0; i < columns.size(); i++) {
      Object column = columns.get(i);
      String columnClassName = column.getClass().getName();
      // replace Column with "fake"
      if (!Enhancer.isEnhanced(column.getClass())
          && !columnClassName.startsWith("com.google.gwt.user.cellview.client.")) {
        ColumnInfo columnModel = columnModels.get(column);
        // create new Column instance
        {
          String text = CodeUtils.getShortClass(columnClassName);
          column = GwtInvocationEvaluatorInterceptor.createTextColumn(classLoader, text);
          columns.set(i, column);
        }
        // set new Column object into model
        if (columnModel != null) {
          columnModel.setObject(column);
        }
      }
    }
    // set rows
    {
      List<String> rows = ImmutableList.of("1", "2", "3", "4", "5");
      ReflectionUtils.invokeMethod(getObject(), "setRowData(java.util.List)", rows);
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // calculate columns bounds
    {
      List<Rectangle> columnBounds = getColumnBoundsByBody();
      List<?> columnObjects = getColumnObjects();
      List<ColumnInfo> columns = getColumns();
      for (ColumnInfo column : columns) {
        int index = columnObjects.indexOf(column.getObject());
        column.setModelBounds(columnBounds.get(index));
      }
    }
  }

  /**
   * Calculate columns bounds by first data row cells bounds.
   * 
   * @return {@link List} columns bounds, if available.
   */
  private List<Rectangle> getColumnBoundsByBody() throws Exception {
    DOMUtils dom = getDOMUtils();
    Object tbody = ReflectionUtils.getFieldObject(getObject(), "tbody");
    Object tr = dom.getChild(tbody, 0);
    List<Rectangle> rects = getRowCellsRects(tr);
    // translate bounds to header area
    int headerHeight = getHeaderHeight();
    for (Rectangle rect : rects) {
      rect.y -= headerHeight;
      rect.height = headerHeight;
    }
    // done
    return rects;
  }

  /**
   * @return the {@link List} of bounds for each <code>TD</code> element in given <code>TR</code>.
   */
  private List<Rectangle> getRowCellsRects(Object trElement) throws Exception {
    List<Rectangle> rects = Lists.newArrayList();
    DOMUtils dom = getDOMUtils();
    for (Object cell : dom.getChildren(trElement)) {
      Rectangle rect = getState().getAbsoluteBounds(cell);
      absoluteToRelative(rect);
      rects.add(rect);
    }
    return rects;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the number of columns.
   */
  public int getColumnCount() {
    return getColumnObjects().size();
  }

  public List<?> getColumnObjects() {
    return getColumnObjects(getObject());
  }

  public static List<?> getColumnObjects(Object table) {
    return (List<?>) ReflectionUtils.getFieldObject(table, "columns");
  }

  /**
   * Adds given {@link ColumnInfo} as last.
   */
  private void addColumn(ColumnInfo column) throws Exception {
    List<FlowContainer> flowContainers = new FlowContainerFactory(this, false).get();
    for (FlowContainer flowContainer : flowContainers) {
      if (flowContainer.validateComponent(column)) {
        flowContainer.command_CREATE(column, null);
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == CellTableInfo.this) {
          List<ColumnInfo> columns = getColumns();
          for (ColumnInfo column : columns) {
            final JavaInfoMemento columnMemento = JavaInfoMemento.createMemento(column);
            final Object headerValue = column.getHeaderProperty().getValue();
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(JavaInfo javaInfo) throws Exception {
                CellTableInfo cellTable = (CellTableInfo) javaInfo;
                ColumnInfo column = (ColumnInfo) columnMemento.create(cellTable);
                cellTable.addColumn(column);
                column.getHeaderProperty().setValue(headerValue);
              }
            });
          }
        }
      }
    });
  }
}
