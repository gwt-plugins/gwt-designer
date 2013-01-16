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

import com.google.gdt.eclipse.designer.hosted.IHostedModeSupport;
import com.google.gdt.eclipse.designer.hosted.IHostedModeSupportFactory;
import com.google.gdt.eclipse.designer.hosted.IModuleDescription;

/**
 * Implementation for {@link IHostedModeSupportFactory} for GWT SDK.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public class HostedModeSupportFactory implements IHostedModeSupportFactory {
  public IHostedModeSupport create(String version,
      ClassLoader parentClassLoader,
      IModuleDescription moduleDescription) throws Exception {
    if ("2.2".equals(version) || "2.4".equals(version) || "2.5".equals(version)) {
      return new HostedModeSupport(parentClassLoader, moduleDescription);
    }
    return null;
  }
}
