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
package com.google.gdt.eclipse.designer.util.ui.log;

/**
 * The implementations of {@link IToolBarEntriesContributor} which adds "Open GWT log" item onto
 * Designer's toolbar.
 * 
 * @author mitin_aa
 * @coverage gwt.util.ui
 */
public class LoggerToolbarEntry /*implements IToolBarEntriesContributor*/{
  /*private ToolBar m_parentToolbar;
  public void contributeEntries(ToolBar parent) {
  	m_parentToolbar = parent;
  }
  public void updateEntries(DesignerEditor editor) {
  	final DesignerEditor designerEditor = DesignerPlugin.getActiveDesignerEditor();
  	ASTEditor astEditor = designerEditor.getEditor();
  	if (astEditor == null) {
  		return;
  	}
  	final GWTState state = (GWTState) astEditor.getGlobalValue(GWTState.GWT_STATE_KEY);
  	if (state == null) {
  		return;
  	}
  	// contribute item; do nothing if already contributed
  	state.getLogSupport().contributeItem(m_parentToolbar);
  }*/
}
