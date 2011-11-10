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

import com.google.gdt.eclipse.designer.gwtext.model.layout.FormLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant for GWT-Ext {@link FormLayoutInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public final class FormLayoutAssistantPage extends AbstractGwtExtAssistantPage {
  public FormLayoutAssistantPage(Composite parent, FormLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // align
    addStaticFieldsProperty(
        this,
        "labelAlign",
        "Label align",
        "com.gwtext.client.core.Position",
        new String[]{"LEFT", "RIGHT", "CENTER", "TOP", "BOTTOM", "AUTO"});
    // label
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addIntegerProperty(composite, "labelWidth");
      addIntegerProperty(composite, "labelPad");
      addBooleanProperty(composite, "hideLabels");
      addBooleanProperty(composite, "renderHidden");
      addStringProperty(composite, "spacing");
    }
  }
}