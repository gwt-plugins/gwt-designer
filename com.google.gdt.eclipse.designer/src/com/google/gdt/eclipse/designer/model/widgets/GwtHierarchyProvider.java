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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.cell.CellTableInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Implementation of {@link HierarchyProvider} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class GwtHierarchyProvider extends HierarchyProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final HierarchyProvider INSTANCE = new GwtHierarchyProvider();

  private GwtHierarchyProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // HierarchyProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object[] getAliases(Object object) throws Exception {
    if (isUserClass(object, "com.smartgwt.client.widgets.BaseWidget")) {
      return new Object[]{object};
    }
    if (isUserClass(object, "com.google.gwt.user.client.ui.UIObject")) {
      return new Object[]{object, ReflectionUtils.invokeMethod(object, "getElement()")};
    }
    // unknown
    return null;
  }

  @Override
  public Object getParentObject(Object object) throws Exception {
    // com.google.gwt.user.client.ui.Widget
    if (isUserClass(object, "com.google.gwt.user.client.ui.Widget")) {
      return ReflectionUtils.invokeMethod(object, "getParent()");
    }
    // com.google.gwt.user.client.Element
    if (isUserClass(object, "com.google.gwt.user.client.Element")) {
      Class<?> domClass = getEditorLoader().loadClass("com.google.gwt.user.client.DOM");
      return ReflectionUtils.invokeMethod(
          domClass,
          "getParent(com.google.gwt.user.client.Element)",
          object);
    }
    // unknown
    return null;
  }

  @Override
  public Object[] getChildrenObjects(Object object) throws Exception {
    // com.google.gwt.user.cellview.client.CellTable
    if (isUserClass(object, "com.google.gwt.user.cellview.client.CellTable")) {
      List<?> columnList = CellTableInfo.getColumnObjects(object);
      return columnList.toArray(new Object[columnList.size()]);
    }
    // com.google.gwt.user.client.ui.IndexedPanel
    if (isUserClass(object, "com.google.gwt.user.client.ui.IndexedPanel")) {
      int widgetCount = (Integer) ReflectionUtils.invokeMethod(object, "getWidgetCount()");
      Object widgets[] = new Object[widgetCount];
      for (int i = 0; i < widgetCount; i++) {
        widgets[i] = ReflectionUtils.invokeMethod(object, "getWidget(int)", i);
      }
      return widgets;
    }
    // com.google.gwt.user.client.ui.IndexedPanel
    if (isUserClass(object, "com.google.gwt.user.client.ui.Composite")) {
      Object widget = ReflectionUtils.invokeMethod(object, "getWidget()");
      if (widget != null) {
        return new Object[]{widget};
      }
    }
    // com.google.gwt.user.client.Element
    if (isUserClass(object, "com.google.gwt.user.client.Element")) {
      Class<?> domClass = getEditorLoader().loadClass("com.google.gwt.user.client.DOM");
      int childCount =
          (Integer) ReflectionUtils.invokeMethod(
              domClass,
              "getChildCount(com.google.gwt.user.client.Element)",
              object);
      Object children[] = new Object[childCount];
      for (int i = 0; i < childCount; i++) {
        children[i] =
            ReflectionUtils.invokeMethod(
                domClass,
                "getChild(com.google.gwt.user.client.Element,int)",
                object,
                i);
      }
      return children;
    }
    // unknown
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ClassLoader} of active {@link JavaInfo} hierarchy.
   */
  private static ClassLoader getEditorLoader() {
    AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
    return EditorState.get(editor).getEditorLoader();
  }

  /**
   * @return <code>true</code> if given object is successor of given class.
   */
  private static boolean isUserClass(Object o, String className) {
    return o != null && ReflectionUtils.isSuccessorOf(o.getClass(), className);
  }
}
