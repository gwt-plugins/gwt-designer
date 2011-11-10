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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.button.ButtonGroup</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class ButtonGroupInfo extends ContentPanelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ButtonGroupInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureChildBeforeAssociation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureChildBeforeAssociation() {
    new ComponentConfiguratorBeforeAssociation(this) {
      @Override
      protected void configure() throws Exception {
        JavaInfoUtils.executeScript(ButtonGroupInfo.this, CodeUtils.getSource(
            "import com.extjs.gxt.ui.client.widget.*;",
            "if (object.getItemCount() == 0) {",
            "  item = new Html('Empty ButtonGroup');",
            "  item.setTagName('span');",
            "  object.add(item);",
            "  ReflectionUtils.invokeMethod(object, 'layout()', {});",
            "}"));
      }
    };
  }
}
