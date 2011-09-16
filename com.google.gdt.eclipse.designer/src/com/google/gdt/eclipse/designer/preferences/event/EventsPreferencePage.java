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
package com.google.gdt.eclipse.designer.preferences.event;

import com.google.gdt.eclipse.designer.Activator;

import org.eclipse.wb.internal.core.model.property.event.EventsProperty;

import org.eclipse.jface.preference.PreferencePage;

/**
 * {@link PreferencePage} for {@link EventsProperty}.
 * 
 * @author scheglov_ke
 * @coverage gwt.preferences.ui
 */
public final class EventsPreferencePage
    extends
      org.eclipse.wb.internal.core.preferences.event.EventsPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventsPreferencePage() {
    super(Activator.getDefault().getPreferenceStore());
  }
}
