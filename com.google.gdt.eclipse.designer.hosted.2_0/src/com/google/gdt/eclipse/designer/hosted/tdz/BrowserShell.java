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
package com.google.gdt.eclipse.designer.hosted.tdz;

import java.util.HashMap;
import java.util.Map;

import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.shell.ModuleSpace;

/**
 * Represents an individual browser window and all of its controls.
 */
public abstract class BrowserShell implements IBrowserShell {
	private IBrowserShellHost m_host;
	private TreeLogger m_logger;
	private final Map<Object, ModuleSpace> loadedModules = new HashMap<Object, ModuleSpace>();
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public final void setHost(IBrowserShellHost host) {
		m_host = host;
		m_logger = m_host.getLogger();
	}
	public final IBrowserShellHost getHost() {
		return m_host;
	}
	public void dispose() {
		loadedModules.clear();
		m_host = null;
	}
	/**
	 * Initializes and attaches module space to this browser widget. Called by subclasses in response to calls
	 * from JavaScript.
	 * 
	 * @param space
	 *            ModuleSpace instance to initialize
	 */
	protected final void attachModuleSpace(ModuleSpace space) throws UnableToCompleteException {
		space.setDevClassLoader(m_host.getDevClassLoader());
		loadedModules.put(null, space);
		m_logger.log(TreeLogger.SPAM, "Loading module "
			+ space.getModuleName(), null);
		// Let the space do its thing.
		//
		space.onLoad(m_logger);
	}
	/**
	 * Unload one or more modules. If key is null, emulate old behavior by unloading all loaded modules.
	 * 
	 * @param key
	 *            unique key to identify module to unload or null for all
	 */
	protected void doUnload(Object key) {
		if (key == null) {
			// BEGIN BACKWARD COMPATIBILITY
			// remove all modules
			for (Map.Entry<?, ModuleSpace> entry : loadedModules.entrySet()) {
				unloadModule(entry.getValue());
			}
			loadedModules.clear();
			// END BACKWARD COMPATIBILITY
		} else {
			ModuleSpace moduleSpace = loadedModules.get(key);
			if (moduleSpace != null) {
				// If the module failed to load at all, it may not be in the map.
				unloadModule(moduleSpace);
				loadedModules.remove(key);
			}
		}
	}
	/**
	 * Unload the specified module.
	 * 
	 * @param moduleSpace
	 *            a ModuleSpace instance to unload.
	 */
	protected void unloadModule(ModuleSpace moduleSpace) {
		String moduleName = moduleSpace.getModuleName();
		moduleSpace.dispose();
		m_logger.log(TreeLogger.SPAM, "Unloading module " + moduleName, null);
	}
}
