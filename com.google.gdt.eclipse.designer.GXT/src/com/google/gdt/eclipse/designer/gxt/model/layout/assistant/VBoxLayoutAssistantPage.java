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
package com.google.gdt.eclipse.designer.gxt.model.layout.assistant;

import com.google.gdt.eclipse.designer.gxt.model.layout.BoxLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant for GXT <code>VBoxLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class VBoxLayoutAssistantPage extends BoxLayoutAssistantPage {
  public VBoxLayoutAssistantPage(Composite parent, BoxLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // align
    addEnumProperty(
        this,
        "VBoxLayoutAlign",
        "Align",
        "com.extjs.gxt.ui.client.widget.layout.VBoxLayout$VBoxLayoutAlign");
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      GridDataFactory.create(
          addEnumProperty(
              composite,
              "pack",
              "Pack",
              "com.extjs.gxt.ui.client.widget.layout.BoxLayout$BoxLayoutPack")).spanH(2).fillH();
      createContents_options(composite);
    }
  }
}