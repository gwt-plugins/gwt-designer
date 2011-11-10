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
package com.google.gdt.eclipse.designer.hosted;


/**
 * Factory class for {@link IHostedModeSupport}.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public interface IHostedModeSupportFactory {
  /**
   * Creates the {@link IHostedModeSupport} instance for given version of GWT.
   * 
   * @param parentClassLoader
   *          optional (may be <code>null</code>) {@link ClassLoader} to use as parent for project
   *          {@link ClassLoader}.
   */
  IHostedModeSupport create(String version,
      ClassLoader parentClassLoader,
      IModuleDescription moduleDescription) throws Exception;
}
