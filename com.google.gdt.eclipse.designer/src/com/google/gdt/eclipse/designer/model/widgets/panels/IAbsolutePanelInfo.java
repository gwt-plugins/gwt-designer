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

import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;

/**
 * Interface model for <code>com.google.gwt.user.client.ui.AbsolutePanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public interface IAbsolutePanelInfo<T extends IWidgetInfo> extends IComplexPanelInfo<T> {
  /**
   * Performs "move" or "resize" operation. Modifies location/size values by modifying appropriate
   * "add", "setWidgetPosition", "setSize" arguments.
   * 
   * @param widget
   *          the {@link IWidgetInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of widget. May be <code>null</code>.
   * @param size
   *          the {@link Dimension} of new size of widget. May be <code>null</code>.
   */
  void command_BOUNDS(T widget, Point location, Dimension size) throws Exception;
}
