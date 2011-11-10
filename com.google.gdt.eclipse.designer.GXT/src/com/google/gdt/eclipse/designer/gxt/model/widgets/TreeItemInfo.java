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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>TreeItem</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class TreeItemInfo extends ComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the children {@link TreeItemInfo}-s.
   */
  public List<TreeItemInfo> getItems() {
    return getChildren(TreeItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * After create new {@link TreeItemInfo} on this.
   */
  public void command_CREATE_after(TreeItemInfo item, TreeItemInfo nextItem) throws Exception {
    makeExpanded();
  }

  /**
   * After move {@link TreeItemInfo} on this.
   */
  public void command_MOVE_after(TreeItemInfo item, TreeItemInfo nextItem) throws Exception {
    makeExpanded();
  }

  private void makeExpanded() throws Exception {
    boolean isRootItem = getParent() instanceof TreeInfo;
    if (!isRootItem) {
      getPropertyByTitle("expanded").setValue(true);
    }
  }
}
