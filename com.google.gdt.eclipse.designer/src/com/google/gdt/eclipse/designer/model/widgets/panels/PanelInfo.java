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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for GWT <code>Panel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class PanelInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all {@link WidgetInfo} children.
   */
  public final List<WidgetInfo> getChildrenWidgets() {
    List<WidgetInfo> widgets = Lists.newArrayList();
    for (ObjectInfo child : getChildren()) {
      if (child instanceof WidgetInfo) {
        WidgetInfo widget = (WidgetInfo) child;
        if (widget.getAssociation() instanceof InvocationSecondaryAssociation) {
          continue;
        }
        widgets.add(widget);
      }
    }
    return widgets;
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
        if (javaInfo == PanelInfo.this) {
          clipboardCopy_addPanelCommands(commands);
        }
      }
    });
  }

  /**
   * Adds commands for {@link WidgetInfo} children.
   */
  protected void clipboardCopy_addPanelCommands(List<ClipboardCommand> commands) throws Exception {
    for (WidgetInfo widget : getChildrenWidgets()) {
      if (!JavaInfoUtils.isImplicitlyCreated(widget)) {
        clipboardCopy_addWidgetCommands(widget, commands);
      }
    }
  }

  /**
   * Adds commands for coping child {@link WidgetInfo}.
   */
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
  }
}
