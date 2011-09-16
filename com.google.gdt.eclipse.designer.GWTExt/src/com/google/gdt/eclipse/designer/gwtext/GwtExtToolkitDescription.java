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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.GwtToolkitDescription;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

/**
 * {@link ToolkitDescription} for GWT-Ext.
 * 
 * @author sablin_aa
 * @coverage gwt
 */
public final class GwtExtToolkitDescription extends ToolkitDescription {
  public static final ToolkitDescription INSTANCE = new GwtExtToolkitDescription();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    return Activator.PLUGIN_ID;
  }

  @Override
  public String getName() {
    return "GWT-Ext toolkit";
  }

  @Override
  public String getProductName() {
    return BrandingUtils.getBranding().getProductName();
  }

  @Override
  public Bundle getBundle() {
    return Activator.getDefault().getBundle();
  }

  @Override
  public IPreferenceStore getPreferences() {
    return GwtToolkitDescription.INSTANCE.getPreferences();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public GenerationSettings getGenerationSettings() {
    return GwtToolkitDescription.INSTANCE.getGenerationSettings();
  }
}
