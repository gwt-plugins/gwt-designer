/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.mobile.device.model;

/**
 * Abstract description for element of mobile devices.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public abstract class AbstractDeviceInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // id
  //
  ////////////////////////////////////////////////////////////////////////////
  protected String m_id;

  /**
   * @return the id of this {@link AbstractDeviceInfo}.
   */
  public final String getId() {
    return m_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  protected String m_name;

  /**
   * @return the display name of this {@link AbstractDeviceInfo}.
   */
  public final String getName() {
    return m_name;
  }

  /**
   * Sets the display name for this {@link AbstractDeviceInfo}.
   */
  public final void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visible
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_visible = true;

  /**
   * @return <code>true</code> if this {@link AbstractDeviceInfo} should be displayed for user.
   */
  public final boolean isVisible() {
    return m_visible;
  }

  /**
   * Specifies if this {@link AbstractDeviceInfo} should be displayed for user.
   */
  public final void setVisible(boolean visible) {
    m_visible = visible;
  }
}
