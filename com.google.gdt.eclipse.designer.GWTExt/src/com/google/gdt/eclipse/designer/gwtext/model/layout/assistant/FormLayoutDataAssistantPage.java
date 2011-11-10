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
package com.google.gdt.eclipse.designer.gwtext.model.layout.assistant;

import com.google.gdt.eclipse.designer.gwtext.model.layout.AnchorLayoutDataInfo;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Assistant for GWT-Ext {@link AnchorLayoutDataInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public final class FormLayoutDataAssistantPage extends AnchorLayoutDataAssistantPage {
  public FormLayoutDataAssistantPage(Composite parent, List<ObjectInfo> selection) {
    super(parent, selection);
    addStringProperty(this, "fieldLabel");
    addStringProperty(this, "labelSeparator");
    addStringProperty(this, "itemCls");
    addStringProperty(this, "clearCls");
    addStringProperty(this, "labelStyle");
    addFiller(this);
    addBooleanProperty(this, "hideLabel");
  }
}