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
package com.google.gdt.eclipse.designer.hosted.tdt;

import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * Helper interface intended to delegate module creating to {@link HostedModeSupport} instance.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public interface IBrowserShellHost {
  /**
   * @return the {@link ClassLoader} for loading from project.
   */
  ClassLoader getDevClassLoader();

  /**
   * Creates the {@link ModuleSpaceHost} instance for given moduleName.
   */
  Object /*ModuleSpaceHost*/createModuleSpaceHost(String moduleName) throws Exception;

  Object createModuleSpace(String moduleName,
      Object msHost,
      ModuleSpace delegateModuleSpace) throws Exception;

  DispatchIdOracle getDispatchIdOracle(Object delegate) throws Exception;
}