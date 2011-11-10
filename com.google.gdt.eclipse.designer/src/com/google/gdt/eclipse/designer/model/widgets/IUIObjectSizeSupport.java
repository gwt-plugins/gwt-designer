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
package com.google.gdt.eclipse.designer.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;

/**
 * Interface for changing size of {@link IUIObjectInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public interface IUIObjectSizeSupport {
  /**
   * When this {@link String} is used as argument of {@link #setSize(String, String)}, corresponding
   * size component is reset to default.
   */
  String NO_SIZE = "__wbp_noSize";

  /**
   * Sets the size of this {@link UIObjectInfo}.
   * 
   * @param size
   *          new size, if <code>null</code> then default size should be set.
   */
  void setSize(Dimension size) throws Exception;

  /**
   * Sets the size of this {@link UIObjectInfo}, in pixels.
   */
  void setSize(int width, int height) throws Exception;

  /**
   * Sets the size of this {@link IUIObjectSizeSupport}.
   * 
   * @param width
   *          the object's new width, in CSS units (e.g. "10px", "1em"). If <code>null</code>, then
   *          width is not modified. If {@link #NO_SIZE} then width is removed.
   * @param height
   *          the object's new height, in CSS units (e.g. "10px", "1em"). If <code>null</code>, then
   *          height is not modified. If {@link #NO_SIZE} then height is removed.
   */
  public void setSize(String width, String height) throws Exception;
}
