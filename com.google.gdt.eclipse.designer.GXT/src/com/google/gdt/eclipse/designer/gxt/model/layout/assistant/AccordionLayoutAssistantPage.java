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

import com.google.gdt.eclipse.designer.gxt.model.layout.AccordionLayoutInfo;

import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Assistant for GXT <code>AccordionLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public final class AccordionLayoutAssistantPage extends AbstractExtGwtAssistantPage {
  public AccordionLayoutAssistantPage(Composite parent, AccordionLayoutInfo accordionLayoutInfo) {
    super(parent, accordionLayoutInfo);
    GridLayoutFactory.create(this);
    // options
    addBooleanProperty(this, "activeOnTop", "activeOnTop");
    addBooleanProperty(this, "autoWidth", "autoWidth");
    addBooleanProperty(this, "fill", "fill");
    addBooleanProperty(this, "firesEvents", "firesEvents");
    addBooleanProperty(this, "hideCollapseTool", "hideCollapseTool");
    addBooleanProperty(this, "renderHidden", "renderHidden");
    {
      Composite composite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(composite).columns(2);
      addIntegerProperty(composite, "resizeDelay", "resizeDelay");
    }
    addBooleanProperty(this, "sequence", "sequence");
    addBooleanProperty(this, "titleCollapse", "titleCollapse");
  }
}