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

import com.google.gdt.eclipse.designer.gwtext.model.layout.AccordionLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Assistant for GWT-Ext {@link AccordionLayoutInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public final class AccordionLayoutAssistantPage extends AbstractGwtExtAssistantPage {
  public AccordionLayoutAssistantPage(Composite parent, AccordionLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // options
    {
      Group optionsGroup =
          addBooleanProperties(this, "Options", new String[][]{
              new String[]{"activeOnTop", "activeOnTop"},
              new String[]{"animate", "animate"},
              new String[]{"autoWidth", "autoWidth"},
              new String[]{"collapseFirst", "collapseFirst"},
              new String[]{"fill", "fill"},
              new String[]{"hideCollapseTool", "hideCollapseTool"},
              new String[]{"titleCollapse", "titleCollapse"},
              new String[]{"sequence", "sequence"},
              new String[]{"renderHidden", "renderHidden"}});
      GridLayoutFactory.create(optionsGroup).columns(2);
      GridDataFactory.create(optionsGroup).spanH(2);
    }
    addStringProperty(this, "extraCls");
    addStringProperty(this, "spacing");
  }
}