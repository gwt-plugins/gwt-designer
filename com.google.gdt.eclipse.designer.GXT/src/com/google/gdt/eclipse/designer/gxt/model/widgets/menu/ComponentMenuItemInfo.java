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
package com.google.gdt.eclipse.designer.gxt.model.widgets.menu;

import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link IMenuItemInfo} for any {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
final class ComponentMenuItemInfo extends AbstractMenuObject implements IMenuItemInfo {
  private final ComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentMenuItemInfo(ComponentInfo component) {
    super(component);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getModel() {
    return m_component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    return null;
  }

  public Rectangle getBounds() {
    return m_component.getBounds();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMenuInfo getMenu() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMenuPolicy getPolicy() {
    return IMenuPolicy.NOOP;
  }
}
