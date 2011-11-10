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

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.property.css.StylesEditDialog;

import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.actions.DesignPageAction;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.window.Window;

import java.util.List;

/**
 * Helper for adding selection actions for {@link UIObjectInfo}.
 * 
 * @author sablin_aa
 * @coverage gwt.model
 */
public final class SelectionActionsSupport extends ObjectEventListener {
  private final UIObjectInfo m_model;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionActionsSupport(UIObjectInfo object) {
    m_model = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
    if (m_model.isRoot()) {
      actions.add(new CssEditAction());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS editor action
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class CssEditAction extends DesignPageAction {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public CssEditAction() {
      setText("CSS Editor");
      setToolTipText("Editor for rules in available CSS files");
      setImageDescriptor(new ImageImageDescriptor(Activator.getImage("css_editor.png")));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // DesignPageAction
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void run(IDesignPage designPage) {
      List<IFile> cssFiles = m_model.getState().getCssSupport().getFiles();
      StylesEditDialog stylesDialog = new StylesEditDialog(DesignerPlugin.getShell(), cssFiles, "");
      // open dialog
      int result = stylesDialog.open();
      if (result == Window.CANCEL) {
        return;
      }
      // wait for auto-build - for coping CSS files from source folder to binary
      ProjectUtils.waitForAutoBuild();
      // check CSS files modification
      if (m_model.getState().isModified()) {
        // refresh 
        ExecutionUtils.refresh(m_model);
      }
    }
  }
}