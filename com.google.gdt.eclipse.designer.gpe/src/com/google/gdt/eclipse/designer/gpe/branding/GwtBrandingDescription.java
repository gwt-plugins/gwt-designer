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
package com.google.gdt.eclipse.designer.gpe.branding;

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
    super("" /*Activator.getResourceString("%pluginName")*/, SUPPORT_INFO);
  }
}
