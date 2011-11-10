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
package com.google.gdt.eclipse.designer.gpe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.util.IModuleFilter;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

import java.util.List;
import java.util.Map;

/**
 * {@link IModuleFilter} which puts {@link ModuleDescription}s with entry points first, so when you
 * open form which belongs several modules, the one with entry point will be choosen.
 * 
 * @author scheglov_ke
 * @coverage gwt.gpe
 */
public class EntryPointsModuleFilter implements IModuleFilter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IModuleFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<ModuleDescription> filter(List<ModuleDescription> modules) throws Exception {
    if (!isGpeGwtProject(modules)) {
      return modules;
    }
    List<ModuleDescription> filtered = Lists.newArrayList();
    // prepare map id -> module
    Map<String, ModuleDescription> moduleMap = Maps.newLinkedHashMap();
    for (ModuleDescription module : modules) {
      moduleMap.put(module.getId(), module);
    }
    // add modules in same order as they are in GPE
    List<String> entryPointModules = gwtEntryPointModules(modules);
    for (String id : entryPointModules) {
      ModuleDescription module = moduleMap.remove(id);
      if (module != null) {
        filtered.add(module);
      }
    }
    // add the rest modules
    filtered.addAll(moduleMap.values());
    // done
    return filtered;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given modules are from GPE GWT projects.
   */
  private boolean isGpeGwtProject(List<ModuleDescription> modules) {
    for (ModuleDescription module : modules) {
      IProject project = module.getProject();
      if (!Utils.isGpeGwtProject(project)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return the {@link List} of module ids with entry points.
   */
  private List<String> gwtEntryPointModules(List<ModuleDescription> modules) throws Exception {
    List<String> entryPointModules = Lists.newArrayList();
    for (ModuleDescription module : modules) {
      IProject project = module.getProject();
      List<String> projectModules = gwtEntryPointModules(project);
      for (String id : projectModules) {
        if (!entryPointModules.contains(id)) {
          entryPointModules.add(id);
        }
      }
    }
    return entryPointModules;
  }

  /**
   * Calls GPE's "GWTProjectProperties.getEntryPointModules()" method.
   */
  @SuppressWarnings("unchecked")
  private static List<String> gwtEntryPointModules(IProject project) throws Exception {
    Bundle bundle = Platform.getBundle("com.google.gwt.eclipse.core");
    Class<?> clazz =
        bundle.loadClass("com.google.gwt.eclipse.core.properties.GWTProjectProperties");
    return (List<String>) ReflectionUtils.invokeMethod(
        clazz,
        "getEntryPointModules(org.eclipse.core.resources.IProject)",
        project);
  }
}
