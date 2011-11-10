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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.generic;

import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

/**
 * Some panels, such as <code>SimplePanel</code> require "technical" size <code>"100%"</code> for
 * child widget(s) to fill this panel client area. But when we move widget out, we should not keep
 * this "technical" size, because <code>"100%"</code> for widget on <code>RootPanel</code> looks
 * very bad. On same time size for widgets on <code>AbsolutePanel</code> is usually reasonable, so
 * should be kept.
 * <p>
 * Supports for following parameters:
 * <ul>
 * <li><b>onChildOut.removeSize</b> specifies when size should be removed. Following values are
 * possible:
 * <ul>
 * <li><b>always</b> - remove size always, no matter what is new target container.</li>
 * <li><b>never</b> - keep size, no matter what is new target container.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class UpdateSizeOnChildOutSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateSizeOnChildOutSupport(XmlObjectInfo root) {
    root.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
        removeSize(child);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void removeSize(ObjectInfo child) throws Exception {
    if (!child.isDeleting()
        && child instanceof WidgetInfo
        && child.getParent() instanceof WidgetInfo) {
      WidgetInfo widget = (WidgetInfo) child;
      WidgetInfo parent = (WidgetInfo) child.getParent();
      String removePolicy = XmlObjectUtils.getParameter(parent, "onChildOut.removeSize");
      if ("always".equals(removePolicy)) {
        String noSize = IUIObjectSizeSupport.NO_SIZE;
        widget.getSizeSupport().setSize(noSize, noSize);
      }
    }
  }
}