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
package com.google.gdt.eclipse.designer.model.widgets.panels.grid;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Information about single column/row in {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class DimensionInfo {
  protected final HTMLTableInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionInfo(HTMLTableInfo panel) {
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link DimensionInfo} in array of all dimensions.
   */
  public abstract int getIndex();

  /**
   * @return <code>true</code> is this {@link DimensionInfo} is last in array.
   */
  public abstract boolean isLast();

  /**
   * @return <code>true</code> if this {@link DimensionInfo} contains no {@link WidgetInfo}'s.
   */
  public abstract boolean isEmpty();
}
