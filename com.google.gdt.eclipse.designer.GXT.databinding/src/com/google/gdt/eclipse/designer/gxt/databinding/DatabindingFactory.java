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
package com.google.gdt.eclipse.designer.gxt.databinding;

import com.google.gdt.eclipse.designer.gxt.databinding.parser.DatabindingParser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingFactory;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * @author lobas_av
 * 
 */
public class DatabindingFactory implements IDatabindingFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDatabindingFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public IDatabindingsProvider createProvider(JavaInfo javaInfoRoot) throws Exception {
    // check root
    if (isGXTRootObject(javaInfoRoot)) {
      // create provider
      DatabindingsProvider provider = new DatabindingsProvider(javaInfoRoot);
      // parse
      DatabindingParser.parse(provider);
      // events
      provider.hookJavaInfoEvents();
      return provider;
    }
    return null;
  }

  public AbstractUIPlugin getPlugin() {
    return Activator.getDefault();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isGXTRootObject(JavaInfo javaInfoRoot) throws Exception {
    if (javaInfoRoot.getDescription().getToolkit().getId() == com.google.gdt.eclipse.designer.preferences.IPreferenceConstants.TOOLKIT_ID) {
      try {
        EditorState.get(javaInfoRoot.getEditor()).getEditorLoader().loadClass(
            "com.extjs.gxt.ui.client.GXT");
        return true;
      } catch (Throwable e) {
      }
    }
    return false;
  }
}