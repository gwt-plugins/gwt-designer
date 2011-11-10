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
package com.google.gdt.eclipse.designer.model.widgets.live;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;

import org.eclipse.swt.graphics.Image;

/**
 * Live components cache entry for GWT toolkit.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class GwtLiveCacheEntry implements ILiveCacheEntry {
  private Image m_image;
  private boolean m_shouldSetSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDisposable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    if (m_image != null && !m_image.isDisposed()) {
      m_image.dispose();
      m_image = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Store {@link Image} into cache.
   */
  public void setImage(Image image) {
    m_image = image;
  }

  /**
   * @return the cached {@link Image}.
   */
  public Image getImage() {
    return m_image;
  }

  /**
   * Setter for {@link #shouldSetSize()}.
   */
  public void shouldSetSize(boolean value) {
    m_shouldSetSize = value;
  }

  /**
   * See {@link WidgetInfo#shouldSetReasonableSize()}.
   */
  public boolean shouldSetSize() {
    return m_shouldSetSize;
  }
}
