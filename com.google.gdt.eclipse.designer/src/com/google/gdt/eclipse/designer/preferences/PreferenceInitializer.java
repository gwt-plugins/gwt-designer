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
package com.google.gdt.eclipse.designer.preferences;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Initializer for GWT preferences.
 * 
 * @author scheglov_ke
 * @coverage gwt.preferences
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferences = Activator.getStore();
    // general
    preferences.setDefault(IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_TEXT_SUFFIX, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD, false);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH, 450);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT, 300);
    // variable names
    {
      preferences.setDefault(
          IPreferenceConstants.P_VARIABLE_TEXT_MODE,
          IPreferenceConstants.V_VARIABLE_TEXT_MODE_DEFAULT);
      preferences.setDefault(
          IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE,
          "${class_acronym}${text}");
      preferences.setDefault(IPreferenceConstants.P_VARIABLE_TEXT_WORDS_LIMIT, 3);
    }
    // NLS
    {
      preferences.setDefault(IPreferenceConstants.P_NLS_AUTO_EXTERNALIZE, true);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_QUALIFIED_TYPE_NAME, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_RENAME_WITH_VARIABLE, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_AS_VALUE_PREFIX, "*");
    }
    // GWT specific
    {
      preferences.setDefault(Constants.P_BUILDER_GENERATE_ASYNC, true);
      preferences.setDefault(Constants.P_BUILDER_COMPOSITE_DEFAULT_CONSTRUCTOR, true);
      preferences.setDefault(Constants.P_BUILDER_CHECK_CLIENT_CLASSPATH, false);
      preferences.setDefault(Constants.P_GWT_TESTS_SOURCE_FOLDER, "src-test");
      preferences.setDefault(Constants.P_WEB_FOLDER, "war");
      preferences.setDefault(Constants.P_GWT_HOSTED_INIT_TIME, 60);
      // disabled by default on Linux
      preferences.setDefault(Constants.P_ENABLE_CSS_UNITS_CONVERSION, !EnvironmentUtils.IS_LINUX);
      // CSS
      preferences.setDefault(Constants.P_CSS_USE_NAMED_COLORS, true);
      // add update listener
      addClasspathVariableUpdateListener(preferences);
    }
    // layout
    preferences.setDefault(
        com.google.gdt.eclipse.designer.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        "${layoutAcronym}_${containerName}");
    preferences.setDefault(
        com.google.gdt.eclipse.designer.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
        "${dataAcronym}_${widgetName}");
    // WebKit
    preferences.setDefault(Constants.P_GWT_USE_WEBKIT, true);
  }

  /**
   * Adds listener that updates "GWT_HOME" class path variable.
   */
  private void addClasspathVariableUpdateListener(IPreferenceStore store) {
    store.addPropertyChangeListener(new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        if (Constants.P_GWT_LOCATION.equals(event.getProperty())) {
          String newGwtHome = (String) event.getNewValue();
          try {
            JavaCore.setClasspathVariable(
                Constants.GWT_HOME_CPE,
                new Path(newGwtHome),
                new NullProgressMonitor());
          } catch (JavaModelException e) {
            DesignerPlugin.log(e);
          }
        }
      }
    });
  }
}
