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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementation of {@link TopBoundsSupport} for {@link RootLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class RootLayoutPanelTopBoundsSupport extends TopBoundsSupport {
  private final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootLayoutPanelTopBoundsSupport(WidgetInfo rootPanel) {
    super(rootPanel);
    m_widget = rootPanel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    GwtState state = m_widget.getState();
    Dimension size = getResourceSize();
    // IE in strict mode always has border 2px
    if (state.isStrictMode() && state.isBrowserExplorer()) {
      size.expand(2 + 2, 2 + 2);
    }
    // prepare Shell
    IBrowserShell shell = state.getShell();
    shell.prepare();
    // set Shell size
    {
      Rectangle shellBounds = shell.computeTrim(0, 0, size.width, size.height);
      shell.setSize(shellBounds.width, shellBounds.height);
    }
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // remember size in resource properties
    setResourceSize(width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    m_widget.getState().showShell();
    return true;
  }
}
