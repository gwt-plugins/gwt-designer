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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;

import java.util.List;

/**
 * {@link StackContainerSupport} for Ext-GWT {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class LayoutStackContainerSupport extends StackContainerSupport<WidgetInfo> {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Container
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutStackContainerSupport(LayoutInfo layout) throws Exception {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActive() {
    return m_layout.isActive();
  }

  @Override
  protected ObjectInfo getContainer() {
    return m_layout.getContainer();
  }

  @Override
  protected List<WidgetInfo> getChildren() {
    return m_layout.getWidgets();
  }
}