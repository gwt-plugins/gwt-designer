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
package com.google.gdt.eclipse.designer.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * Provider of {@link ModuleDescription}s.
 * 
 * @author scheglov
 * @coverage gwt.util
 */
public interface IModuleProvider {
  /**
   * @return the {@link ModuleDescription} with given id, may be <code>null</code>
   */
  ModuleDescription getModuleDescription(IJavaProject javaProject, String id) throws Exception;

  /**
   * @return all {@link ModuleDescription} in given {@link IJavaProject}.
   */
  List<ModuleDescription> getModules(IJavaProject javaProject) throws Exception;

  /**
   * @return the {@link ModuleDescription}s to which belongs given {@link IResource}, may be empty
   *         list if no module found.
   */
  List<ModuleDescription> getModules(IResource resource) throws Exception;

  /**
   * @return the {@link ModuleDescription} that exactly corresponds to the given object, may be
   *         <code>null</code> there are no corresponding module or more than one module exists.
   */
  ModuleDescription getExactModule(Object object);
}
