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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import java.util.List;

/**
 * Model for GWT <code>ComplexPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class ComplexPanelInfo extends PanelInfo implements IComplexPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComplexPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE2(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    command_CREATE2(this, widget, nextWidget);
  }

  public void command_MOVE2(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    command_MOVE2(this, widget, nextWidget);
  }

  static void command_CREATE2(WidgetInfo container, WidgetInfo widget, WidgetInfo nextWidget)
      throws Exception {
    List<FlowContainer> flowContainers = new FlowContainerFactory(container, false).get();
    for (FlowContainer flowContainer : flowContainers) {
      if (flowContainer.validateComponent(widget)) {
        flowContainer.command_CREATE(widget, nextWidget);
        ExecutionUtils.refresh(container);
        break;
      }
    }
  }

  static void command_MOVE2(WidgetInfo container, WidgetInfo widget, WidgetInfo nextWidget)
      throws Exception {
    List<FlowContainer> flowContainers = new FlowContainerFactory(container, false).get();
    for (FlowContainer flowContainer : flowContainers) {
      if (flowContainer.validateComponent(widget)) {
        flowContainer.command_MOVE(widget, nextWidget);
        ExecutionUtils.refresh(container);
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    commands.add(new PanelClipboardCommand<ComplexPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(ComplexPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
      }
    });
  }
}
