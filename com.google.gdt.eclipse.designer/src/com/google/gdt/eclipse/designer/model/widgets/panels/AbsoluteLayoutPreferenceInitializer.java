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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.ToolkitProvider;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for GWT absolute-based panels preferences.
 * 
 * @author mitin_aa
 * @coverage gwt.preferences
 */
public final class AbsoluteLayoutPreferenceInitializer extends AbstractPreferenceInitializer
    implements
      AbsolutePanelPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  //	Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ToolkitDescription} of the toolkit this policy applies to.
   */
  private ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferenceStore = getToolkit().getPreferences();
    // editing mode
    preferenceStore.setDefault(P_USE_FREE_MODE, true);
    preferenceStore.setDefault(P_USE_GRID, false);
    preferenceStore.setDefault(P_DISPLAY_GRID, false);
    preferenceStore.setDefault(P_GRID_STEP_X, 5);
    preferenceStore.setDefault(P_GRID_STEP_Y, 5);
    preferenceStore.setDefault(P_CREATION_FLOW, false);
    // gaps
    preferenceStore.setDefault(P_COMPONENT_GAP_LEFT, 6);
    preferenceStore.setDefault(P_COMPONENT_GAP_RIGHT, 6);
    preferenceStore.setDefault(P_COMPONENT_GAP_TOP, 6);
    preferenceStore.setDefault(P_COMPONENT_GAP_BOTTOM, 6);
    preferenceStore.setDefault(P_CONTAINER_GAP_LEFT, 10);
    preferenceStore.setDefault(P_CONTAINER_GAP_RIGHT, 10);
    preferenceStore.setDefault(P_CONTAINER_GAP_TOP, 10);
    preferenceStore.setDefault(P_CONTAINER_GAP_BOTTOM, 10);
    // misc
    preferenceStore.setDefault(P_DISPLAY_LOCATION_SIZE_HINTS, true);
  }
}
