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
package com.google.gdt.eclipse.designer.smart.model;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.tree.TreeGrid</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class TreeGridInfo extends ListGridInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeGridInfo(AstEditor editor,
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
   * @return the list of children {@link TreeGridFieldInfo}.
   */
  @Override
  public List<? extends TreeGridFieldInfo> getFields() {
    return getChildren(TreeGridFieldInfo.class);
  }

  /**
   * @return the {@link AbstractArrayObjectInfo} for "setFields" invocation.
   */
  @Override
  public AbstractArrayObjectInfo getFieldsArrayInfo() throws Exception {
    return ArrayChildrenContainerUtils.getMethodParameterArrayInfo(
        this,
        "setFields",
        "com.smartgwt.client.widgets.tree.TreeGridField");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(TreeGridFieldInfo newField, TreeGridFieldInfo referenceField)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getFieldsArrayInfo();
    arrayInfo.command_CREATE(newField, referenceField);
  }

  public void command_MOVE(TreeGridFieldInfo moveField, TreeGridFieldInfo referenceField)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getFieldsArrayInfo();
    arrayInfo.command_MOVE(moveField, referenceField);
  }
}
