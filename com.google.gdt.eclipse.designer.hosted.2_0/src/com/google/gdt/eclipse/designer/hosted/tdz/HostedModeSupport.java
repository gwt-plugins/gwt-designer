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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;



import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.hosted.IBrowserShellFactory;
import com.google.gdt.eclipse.designer.hosted.IHostedModeSupport;
import com.google.gdt.eclipse.designer.hosted.ILogSupport;
import com.google.gdt.eclipse.designer.hosted.IModuleDescription;
import com.google.gdt.eclipse.designer.hosted.classloader.GWTSharedClassLoader;
import com.google.gdt.eclipse.designer.hosted.tdz.log.LogSupport;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.cfg.Properties;
import com.google.gwt.dev.cfg.Property;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.shell.ArtifactAcceptor;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;

import org.eclipse.wb.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;


/**
 * Implementation for {@link IHostedModeSupport} for GWT 2.0. Also used as {@link IBrowserShellHost} while
 * creating {@link ModuleSpace} for current platform.
 * 
 * @author mitin_aa
 */
public final class HostedModeSupport implements IHostedModeSupport, IBrowserShellHost {
	private static GWTSharedClassLoader m_gwtSharedClassLoader;
	private final ClassLoader m_parentClassLoader;
	private final IModuleDescription m_moduleDescription;
	private final BrowserShell m_browserShell;
	private final IJavaProject m_javaProject;
	private ProjectClassLoader m_devClassLoader;
	private ShellModuleSpaceHost m_moduleSpaceHost;
	private File m_shellDirectory;
	private final LogSupport m_logSupport;
	private ModuleDef m_moduleDef;
	private TypeOracle m_typeOracle;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public HostedModeSupport(ClassLoader parentClassLoader, IModuleDescription moduleDescription) throws Exception {
		m_parentClassLoader = parentClassLoader;
    m_moduleDescription = moduleDescription;
		m_javaProject = moduleDescription.getJavaProject();
		// Logger
		m_logSupport = new LogSupport(TreeLogger.TRACE, m_javaProject);
		// Class loaders
		createClassLoaders();
		// Browser shell
		m_browserShell = (BrowserShell) createBrowserShell();
		m_browserShell.setHost(this);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ClassLoaders
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates the special {@link ClassLoader}'s to work with GWT "dev" classes and "user" classes.
	 */
	private void createClassLoaders() throws Exception {
		m_devClassLoader = ProjectClassLoader.create(getSharedClassLoader(), m_javaProject);
		{
			Bundle bundle = Platform.getBundle("com.google.gdt.eclipse.designer.hosted.2_0.super");
			URL rootEntry = bundle.getEntry("");
			rootEntry = FileLocator.toFileURL(rootEntry);
			m_devClassLoader.addURL(rootEntry);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IHostedModeSupport
	//
	////////////////////////////////////////////////////////////////////////////
	public void startup(String browserStartupUrl, String moduleName, IProgressMonitor monitor, int timeout)
			throws Exception {
		CompilingClassLoader.parentClassLoader = m_parentClassLoader;
		try {
			//long l = System.currentTimeMillis();
			m_browserShell.setUrl(browserStartupUrl, moduleName, timeout, new Runnable() {
				public void run() {
					runMessagesLoop();
				}
			});
			//System.out.println("\tstartup time: " + (System.currentTimeMillis() - l));
		} finally {
			CompilingClassLoader.parentClassLoader = null;
		}
	}
	public void dispose() {
		m_browserShell.dispose();
		// dispose if initialized (may be not if Module loading failed)
		if (m_moduleSpaceHost != null) {
			m_moduleSpaceHost.getModuleSpace().dispose();
		}
		// dispose for project; if the same project used in another editor 
		// it would be added again by activating the project. 
		m_gwtSharedClassLoader.dispose(m_moduleDescription);
		m_logSupport.dispose();
		m_moduleSpaceHost = null;
		// clear static caches
		try {
			Class<?> clazz = m_gwtSharedClassLoader.loadClass("com.google.gwt.i18n.rebind.ClearStaticData");
			Method method = clazz.getDeclaredMethod("clear");
			method.setAccessible(true);
			method.invoke(null);
		} catch (Throwable e) {
		}
		try {
			Class<?> clazz =
					m_gwtSharedClassLoader.loadClass("com.google.gwt.uibinder.rebind.model.OwnerFieldClass");
			Field mapField = clazz.getDeclaredField("FIELD_CLASSES");
			mapField.setAccessible(true);
			Map<?, ?> map = (Map<?, ?>) mapField.get(null);
			map.clear();
		} catch (Throwable e) {
		}
	}
	public IBrowserShell getBrowserShell() {
		return m_browserShell;
	}
	public ClassLoader getClassLoader() {
		return m_moduleSpaceHost.getClassLoader();
	}
	public ClassLoader getDevClassLoader() {
		return m_devClassLoader;
	}
	public Object findJType(String name) {
		return m_typeOracle.findType(name);
	}
	private ClassLoader getSharedClassLoader() throws Exception {
		// shared class loader for gwt-user.jar
		if (m_gwtSharedClassLoader == null) {
			m_gwtSharedClassLoader = new GWTSharedClassLoader(ModuleDef.class.getClassLoader(), new URL[]{});
		}
		activate();
		return m_gwtSharedClassLoader;
	}
	public void invalidateRebind(String typeName) {
	}
	public void activate() throws Exception {
		m_gwtSharedClassLoader.setActiveProject(m_moduleDescription);
		ModuleSpace.setLogger(getLogger());
	}
	public byte[] getGeneratedResource(String resourceName) throws Exception {
		File resourceFile = new File(m_shellDirectory, resourceName);
		if (!resourceFile.exists()) {
			return null;
		}
		return IOUtils2.readBytes(resourceFile);
	}
	public ILogSupport getLogSupport() {
		return m_logSupport;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BrowserShell 
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates and returns the {@link IBrowserShell} instance for current platform using external factory.
	 */
	@SuppressWarnings("unchecked")
	private IBrowserShell createBrowserShell() throws Exception {
		List<IBrowserShellFactory> factories =
				ExternalFactoriesHelper.getElementsInstances(
					IBrowserShellFactory.class,
					"com.google.gdt.eclipse.designer.hosted.2_0.browserShellFactory",
					"factory");
		for (IBrowserShellFactory factory : factories) {
			IBrowserShell shell = factory.create();
			if (shell != null) {
				return shell;
			}
		}
		// no shell has been created by factories
		if (isWindows64()) {
			// special message for windows
			throw new HostedModeException(HostedModeException.WIN32_NO_WINDOWS_64);
		}
		throw new HostedModeException(HostedModeException.UNSUPPORTED_OS);
	}
	/**
	 * @return <code>true</code> while running Windows 64-bit.
	 */
	private boolean isWindows64() {
		String osName = System.getProperty("os.name");
		String archName = System.getProperty("os.arch");
		if (!StringUtils.isEmpty(osName) && !StringUtils.isEmpty(archName)) {
			return osName.startsWith("Windows") && archName.indexOf("64") != -1;
		}
		return false;
	}
	/**
	 * Forces an outstanding messages to be processed
	 */
	public void runMessagesLoop() {
		try {
			while (Display.getCurrent().readAndDispatch()) {
				// wait
			}
		} catch (Throwable e) {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IBrowserShellHost
	//
	////////////////////////////////////////////////////////////////////////////
	//private final Set<String> alreadySeenModules = new HashSet<String>();
	/**
	 * Load a module.
	 * 
	 * @param moduleName
	 *            a Name of the module to load.
	 * @param outDir
	 * @return the loaded module.
	 */
	private ModuleDef loadModule(String moduleName) throws Exception {
		//boolean assumeFresh = !alreadySeenModules.contains(moduleName);
		ModuleDef moduleDef =
				ModuleDefLoader.loadFromClassPath(getLogger(), moduleName, true/*!assumeFresh*/);
		//alreadySeenModules.add(moduleName);
		assert moduleDef != null : "Required module state is absent";
		return moduleDef;
	}
	public ModuleSpaceHost createModuleSpaceHost(final String moduleName) throws Exception {
		ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(m_devClassLoader);
		try {
			// Try to find an existing loaded version of the module def.
			//
			String outDir = getTemporaryDirectoryName(m_javaProject);
			m_moduleDef = loadModule(moduleName);
			assert m_moduleDef != null;
			fixUserAgentProperty(m_moduleDef);
			// TODO uncomment, if want to see/debug generators, like UiBinder
			//File genDir = new File(outDir);
			File genDir = null;
			// Create a sandbox for the module.
			m_shellDirectory =
					new File(outDir, ".tmp" + File.separator + "shell" + File.separator + moduleName);
			CompilationState compilationState = m_moduleDef.getCompilationState(getLogger());
			m_typeOracle = compilationState.getTypeOracle();
			m_moduleSpaceHost =
					new ShellModuleSpaceHost(getLogger(),
						compilationState,
						m_moduleDef,
						genDir,
						m_shellDirectory,
						new ArtifactAcceptor() {
							public void accept(TreeLogger logger, ArtifactSet newlyGeneratedArtifacts)
									throws UnableToCompleteException {
								// TODO: does we need this?
							}
						});
			return m_moduleSpaceHost;
		} finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}
	}
	/**
	 * Forcibly set 'user.agent' property to current platform.
	 * http://fogbugz.instantiations.com/fogbugz/default.php?41513
	 */
	private void fixUserAgentProperty(ModuleDef module) {
		Properties properties = module.getProperties();
		for (Property property : properties) {
			if ("user.agent".equals(property.getName())) {
				BindingProperty bindingProperty = (BindingProperty) property;
				bindingProperty.setAllowedValues(
					bindingProperty.getRootCondition(),
					m_browserShell.getUserAgentString());
				return;
			}
		}
	}
	public TreeLogger getLogger() {
		return (TreeLogger) m_logSupport.getLogger();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getTemporaryDirectoryName(IJavaProject javaProject) {
		String logDir = javaProject.getProject().getLocation().toOSString() + File.separator + ".gwt";
		File logDirFile = new File(logDir);
		logDirFile.mkdirs();
		return logDir;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IHostedModeSupport, invocations of native code.
	//
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("rawtypes")
	public boolean invokeNativeBoolean(String string, Class[] classes, Object[] objects) {
		try {
			return m_moduleSpaceHost.getModuleSpace().invokeNativeBoolean(string, null, classes, objects);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("rawtypes")
	public String invokeNativeString(String string, Class[] classes, Object[] objects) {
		try {
			return (String) m_moduleSpaceHost.getModuleSpace().invokeNativeObject(
				string,
				null,
				classes,
				objects);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("rawtypes")
	public void invokeNativeVoid(String string, Class[] classes, Object[] objects) {
		try {
			m_moduleSpaceHost.getModuleSpace().invokeNativeVoid(string, null, classes, objects);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
