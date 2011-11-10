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
package com.google.gdt.eclipse.designer.parser;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.internal.core.editor.EditorLifeCycleListener;

import org.eclipse.jdt.core.ICompilationUnit;

import java.util.Map;

/**
 * {@link EditorLifeCycleListener} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.parser
 */
//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class GwtEditorLifeCycleListener extends EditorLifeCycleListener {
  private static Object m_currentEditor;
  private static final Map<Object, GwtState> m_editorStates = Maps.newHashMap();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static EditorLifeCycleListener INSTANCE = new GwtEditorLifeCycleListener();

  private GwtEditorLifeCycleListener() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditorLifeCycleListener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean parseWithProgress(Object editor, ICompilationUnit unit) {
    // no ready GwtState
    if (m_editorStates.get(editor) == null) {
      return true;
    }
    // source is more than 50 kilobytes
    try {
      if (unit.getSource().length() > 50 * 1024) {
        return true;
      }
    } catch (Throwable e) {
      return true;
    }
    // OK, no progress required
    return false;
  }

  @Override
  public void parseStart(Object editor) throws Exception {
    m_currentEditor = editor;
  }

  @Override
  public void parseEnd(Object editor) throws Exception {
    m_currentEditor = null;
  }

  @Override
  public void disposeContext(Object editor, boolean force) throws Exception {
    if (force) {
      GwtState state = m_editorStates.remove(editor);
      if (state != null) {
        state.dispose();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if there is current editor, so we can attempt to get prepared
   *         {@link GwtState} and remember new one.
   */
  static boolean hasCurrentEditor() {
    return m_currentEditor != null;
  }

  /**
   * @return the {@link GwtState} for current editor, which initiated parsing and called
   *         {@link #parseStart(Object)}.
   */
  static GwtState getCurrentEditorState() {
    return m_editorStates.get(m_currentEditor);
  }

  /**
   * Remembers {@link GwtState} for current editor.
   */
  static void setCurrentEditorState(GwtState state) {
    if (m_currentEditor != null) {
      m_editorStates.put(m_currentEditor, state);
    }
  }
}
