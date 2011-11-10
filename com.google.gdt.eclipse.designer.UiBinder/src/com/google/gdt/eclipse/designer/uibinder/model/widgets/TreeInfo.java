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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.List;

/**
 * Model for <code>Tree</code>.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class TreeInfo extends WidgetInfo implements TreeItemsContainer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addClipboardSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<TreeItemInfo> getItems() {
    return getChildren(TreeItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  static void addClipboardSupport(final TreeItemsContainer container) {
    container.addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == container) {
          for (TreeItemInfo widget : container.getItems()) {
            addWidgetCommand(commands, widget);
          }
        }
      }

      private void addWidgetCommand(List<ClipboardCommand> commands, TreeItemInfo item)
          throws Exception {
        final XmlObjectMemento memento = XmlObjectMemento.createMemento(item);
        commands.add(new ClipboardCommand() {
          private static final long serialVersionUID = 0L;

          @Override
          public void execute(XmlObjectInfo tree) throws Exception {
            TreeItemInfo item = (TreeItemInfo) memento.create(tree);
            XmlObjectUtils.flowContainerCreate(tree, item, null);
            memento.apply();
          }
        });
      }
    });
  }
}
