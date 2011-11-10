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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Assistant for GXT <code>BoxLayout</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public abstract class BoxLayoutAssistantPage extends AbstractExtGwtAssistantPage {
  public BoxLayoutAssistantPage(Composite parent, BoxLayoutInfo selection) {
    super(parent, selection);
  }

  protected void createContents_options(Composite parent) {
    {
      Group optionsGroup =
          addBooleanProperties(parent, "Options", new String[][]{
              new String[]{"adjustForFlex", "adjustForFlex"},
              new String[]{"firesEvents", "firesEvents"},
              new String[]{"renderHidden", "renderHidden"}});
      GridDataFactory.create(optionsGroup).spanH(2).fillH();
    }
    addIntegerProperty(parent, "resizeDelay", "resizeDelay");
  }
}