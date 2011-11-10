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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.List;

/**
 * Assistant for GXT <code>BorderLayoutData</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class BorderLayoutDataAssistantPage extends LayoutDataAssistantPage {
  public BorderLayoutDataAssistantPage(Composite parent, List<ObjectInfo> selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // region
    addEnumProperty(this, "region", "Region", "com.extjs.gxt.ui.client.Style$LayoutRegion");
    // margins
    {
      Group group = addMarginProperty(this, "margins", "Margins for sides");
      GridDataFactory.create(group);
    }
    // size
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addDoubleProperty(composite, "size", "sizeValue");
      {
        Group group =
            addIntegerProperties(composite, "Size", new String[][]{
                new String[]{"maxSize", "max"},
                new String[]{"minSize", "min"}});
        GridDataFactory.create(group).spanH(2).fillH();
      }
    }
    // options
    {
      Group optionsGroup =
          addBooleanProperties(this, "Options", new String[][]{
              new String[]{"collapsible", "collapsible"},
              new String[]{"floatable", "floatable"},
              new String[]{"hidden", "hidden"},
              new String[]{"hideCollapseTool", "hideCollapseTool"},
              new String[]{"split", "split"}});
      GridDataFactory.create(optionsGroup);
    }
  }
}