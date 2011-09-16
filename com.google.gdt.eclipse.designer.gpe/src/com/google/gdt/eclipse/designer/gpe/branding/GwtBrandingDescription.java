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
package com.google.gdt.eclipse.designer.gpe.branding;

import com.google.gdt.eclipse.designer.Activator;

import org.eclipse.wb.core.branding.AbstractBrandingDescription;
import org.eclipse.wb.core.branding.IBrandingDescription;
import org.eclipse.wb.core.branding.IBrandingSupportInfo;

/**
 * The {@link IBrandingDescription} for the GWT product.
 * 
 * @author Jaime Wren
 * @coverage gwt.gpe
 */
public final class GwtBrandingDescription extends AbstractBrandingDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Support info
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final IBrandingSupportInfo SUPPORT_INFO = new IBrandingSupportInfo() {
    public String getBugtrackingUrl() {
      return "http://code.google.com/p/google-web-toolkit/issues/list";
    }

    public String getForumUrl() {
      return "http://groups.google.com/group/google-web-toolkit";
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static IBrandingDescription INSTANCE = new GwtBrandingDescription();

  private GwtBrandingDescription() {
    super(Activator.getResourceString("%pluginName"), SUPPORT_INFO);
  }
}
