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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * {@link CreationSupport} for virtual {@link LayoutDataInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class VirtualLayoutDataCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final WidgetInfo m_widget;
  private final Object m_dataObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualLayoutDataCreationSupport(WidgetInfo widget, Object dataObject) {
    m_widget = widget;
    m_dataObject = dataObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    Class<?> layoutDataClass = getComponentClass();
    return "virtual-layout_data: " + layoutDataClass.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_javaInfo.setObject(m_dataObject);
    m_widget.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object object) throws Exception {
        // check, may be this creation support is not active
        if (m_javaInfo.getCreationSupport() != VirtualLayoutDataCreationSupport.this) {
          m_widget.removeBroadcastListener(this);
          return;
        }
        // OK, check for widget
        if (target == m_widget) {
          m_javaInfo.setObject(m_dataObject);
        }
      }
    });
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return false;
  }

  @Override
  public ASTNode getNode() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardImplicitCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public IClipboardImplicitCreationSupport getImplicitClipboard() {
    return null;
  }
}