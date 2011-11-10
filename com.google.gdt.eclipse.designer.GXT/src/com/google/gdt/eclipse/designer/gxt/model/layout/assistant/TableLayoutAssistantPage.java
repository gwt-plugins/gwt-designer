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

import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Assistant for GXT <code>TableLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class TableLayoutAssistantPage extends AbstractExtGwtAssistantPage {
  public TableLayoutAssistantPage(Composite parent, TableLayoutInfo selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // align
    GridDataFactory.create(
        addEnumProperty(
            this,
            "cellHorizontalAlign",
            "Cell horizontal align",
            "com.extjs.gxt.ui.client.Style$HorizontalAlignment")).fillH();
    GridDataFactory.create(
        addEnumProperty(
            this,
            "cellVerticalAlign",
            "Cell vertical align",
            "com.extjs.gxt.ui.client.Style$VerticalAlignment")).fillH();
    // others
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addIntegerProperty(composite, "columns", "columns");
      addIntegerProperty(composite, "border", "border");
      addIntegerProperty(composite, "cellPadding", "cellPadding");
      addIntegerProperty(composite, "cellSpacing", "cellSpacing");
      addStringProperty(composite, "height", "height");
      addStringProperty(composite, "width", "width");
    }
    // options
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      Group optionsGroup =
          addBooleanProperties(composite, "Options", new String[][]{
              new String[]{"firesEvents", "firesEvents"},
              new String[]{"insertSpacer", "insertSpacer"},
              new String[]{"renderHidden", "renderHidden"}});
      GridDataFactory.create(optionsGroup).spanH(2).fillH();
      addIntegerProperty(composite, "resizeDelay", "resizeDelay");
    }
  }
}