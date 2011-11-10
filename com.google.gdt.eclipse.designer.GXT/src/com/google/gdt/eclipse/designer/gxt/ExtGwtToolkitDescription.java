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
package com.google.gdt.eclipse.designer.gxt;

import com.google.gdt.eclipse.designer.GwtToolkitDescription;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

/**
 * {@link ToolkitDescription} for GXT.
 * 
 * @author sablin_aa
 * @coverage ExtGWT
 */
public final class ExtGwtToolkitDescription extends ToolkitDescription {
  public static final ToolkitDescription INSTANCE = new ExtGwtToolkitDescription();

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
    return "ExtGWT (GXT) toolkit";
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
