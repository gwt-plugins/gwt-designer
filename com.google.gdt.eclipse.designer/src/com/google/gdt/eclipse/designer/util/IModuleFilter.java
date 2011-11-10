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
