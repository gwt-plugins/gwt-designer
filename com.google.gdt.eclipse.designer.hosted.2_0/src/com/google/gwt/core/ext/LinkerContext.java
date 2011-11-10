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
package com.google.gwt.core.ext;

import com.google.gwt.core.ext.linker.SelectionProperty;

import java.util.SortedSet;

/**
 * Provides access to data about the linking process. Methods that return a
 * {@link SortedSet} are guaranteed to have stable iteration order between runs
 * of the compiler over identical input. Unless otherwise specified, the exact
 * iteration order is left as an implementation detail.
 */
public interface LinkerContext {
  /**
   * Returns all configuration properties defined in the module. Configuration
   * properties do not have any impact on deferred-binding decisions, but may
   * affect the behaviors of Generators and Linkers.
   */
  SortedSet<ConfigurationProperty> getConfigurationProperties();

  /**
   * Returns the name of the module's bootstrap function.
   */
  String getModuleFunctionName();

  /**
   * Returns the time at which the module being compiled was last modified. Can
   * be used to set an appropriate timestamp on artifacts which depend solely on
   * the module definition.
   */
  long getModuleLastModified();

  /**
   * Returns the name of the module being compiled.
   */
  String getModuleName();

  /**
   * Returns all deferred binding properties defined in the module. The
   * SelectionProperties will be sorted by the standard string comparison
   * function on the name of the property.
   */
  SortedSet<SelectionProperty> getProperties();

  /**
   * Returns <code>true</code> if the output should be as compact is possible
   * and <code>false</code> if the output should be human-readable.
   */
  boolean isOutputCompact();

  /**
   * Applies optimizations to a JavaScript program. This method is intended to
   * be applied to bootstrap scripts in order to apply context-specific
   * transformations to the program, based on the compiler's configuration. The
   * return value will be functionally-equivalent JavaScript, although the exact
   * transformations and structure of the output should be considered opaque.
   * 
   * While this function can be safely applied multiple times, the best results
   * will be obtained by performing all JavaScript assembly and calling the
   * function just before writing the selection script to disk.
   */
  String optimizeJavaScript(TreeLogger logger, String jsProgram)
      throws UnableToCompleteException;
}
