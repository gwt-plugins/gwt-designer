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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;

/**
 * Abstract command for pasting {@link WidgetInfo} on container {@link WidgetInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class PanelClipboardCommand<T extends WidgetInfo> extends ClipboardCommand {
  private static final long serialVersionUID = 0L;
  private final JavaInfoMemento m_widgetMemento;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PanelClipboardCommand(WidgetInfo widget) throws Exception {
    m_widgetMemento = JavaInfoMemento.createMemento(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClipboardCommand
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public final void execute(JavaInfo javaInfo) throws Exception {
    T panel = (T) javaInfo;
    WidgetInfo widget = (WidgetInfo) m_widgetMemento.create(javaInfo);
    add(panel, widget);
    m_widgetMemento.apply();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutClipboardCommand
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link WidgetInfo} to container using its specific way.
   */
  protected abstract void add(T panel, WidgetInfo widget) throws Exception;
}
