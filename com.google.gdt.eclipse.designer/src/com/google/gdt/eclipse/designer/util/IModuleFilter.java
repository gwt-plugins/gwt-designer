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

import java.util.List;

/**
 * Filter of {@link ModuleDescription}s.
 * <p>
 * Sometimes we have several {@link ModuleDescription}s and we not sure which one to choose (and use
 * just first in the list). But GPE (if present) can provide information about preferred modules,
 * which user configured.
 * 
 * @author scheglov
 * @coverage gwt.util
 */
public interface IModuleFilter {
  /**
   * @return new or same list of {@link ModuleDescription}s.
   */
  List<ModuleDescription> filter(List<ModuleDescription> modules) throws Exception;
}
