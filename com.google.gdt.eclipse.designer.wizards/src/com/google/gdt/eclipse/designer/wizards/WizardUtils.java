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
package com.google.gdt.eclipse.designer.wizards;

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.Version;

import org.eclipse.core.resources.IProject;

/**
 * Utils for wizards.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard
 */
public abstract class WizardUtils {
  /**
   * @return the path for versioned templates, with trailing "/".
   */
  public static String getTemplatePath(IProject project) {
    Version version = getTemplateVersion(project);
    return "templates/" + version.getStringMajorMinor() + "/";
  }

  /**
   * @return the version of templates, not exactly same as version of GWT.
   */
  private static Version getTemplateVersion(IProject project) {
    Version version = Utils.getVersion(project);
    if (version.isHigherOrSame(Utils.GWT_2_1)) {
      version = Utils.GWT_2_1;
    }
    return version;
  }
}
