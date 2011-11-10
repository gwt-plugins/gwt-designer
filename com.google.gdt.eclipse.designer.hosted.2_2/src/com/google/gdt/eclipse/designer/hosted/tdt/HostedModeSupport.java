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

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.hosted.IBrowserShellFactory;
import com.google.gdt.eclipse.designer.hosted.IHostedModeSupport;
import com.google.gdt.eclipse.designer.hosted.ILogSupport;
import com.google.gdt.eclipse.designer.hosted.IModuleDescription;
import com.google.gdt.eclipse.designer.hosted.tdt.log.LogSupport;
import com.google.gwt.dev.shell.designtime.DispatchClassInfo;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.ModuleSpace;
import com.google.gwt.thirdparty.guava.common.collect.Maps;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.osgi.framework.Bundle;

import java.io.File;
import java.lang.reflect.Member;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

/**
 * Implementation for {@link IHostedModeSupport} for GWT. Also used as {@link IBrowserShellHost}
 * while creating {@link ModuleSpace} for current platform.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public final class HostedModeSupport implements IHostedModeSupport, IBrowserShellHost {
  private final ClassLoader parentClassLoader;
  private final IModuleDescription moduleDescription;
  private final BrowserShell browserShell;
  private final IJavaProject javaProject;
  private ClassLoader projectClassLoader;
  private Object moduleSpaceHost;
  private final LogSupport logSupport;
  private Object impl;
  private DispatchIdOracle dispatchIdOracle;
  private static Map<String, ClassLoader> devClassLoaders = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HostedModeSupport(ClassLoader parentClassLoader, IModuleDescription moduleDescription)
      throws Exception {
    this.parentClassLoader = parentClassLoader;
    this.moduleDescription = moduleDescription;
    this.javaProject = moduleDescription.getJavaProject();
    // Class loaders
    createClassLoaders();
    // impl
    loadImpl();
    // Logger
    this.logSupport = new LogSupport(3 /*TreeLogger.TRACE*/, impl, javaProject);
    // Browser shell
    this.browserShell = (BrowserShell) createBrowserShell();
    this.browserShell.setHost(this);
  }

  /**
   * Constructor to use for "warm up".
   */
  public HostedModeSupport(IModuleDescription moduleDescription) throws Exception {
    this.parentClassLoader = null;
    this.moduleDescription = moduleDescription;
    this.javaProject = moduleDescription.getJavaProject();
    // Class loaders
    createClassLoaders();
    // impl
    loadImpl();
    // Logger
    this.logSupport = new LogSupport(3 /*TreeLogger.TRACE*/, impl, javaProject);
    // Browser shell
    this.browserShell = null;
  }

  private void loadImpl() throws Exception {
    Class<?> implClass =
        getDevClassLoader().loadClass("com.google.gwt.dev.shell.designtime.HostedModeSupportImpl");
    impl = implClass.newInstance();
    //
    Class<?> moduleSpaceClass =
        getDevClassLoader().loadClass("com.google.gwt.dev.shell.designtime.DelegatingModuleSpace");
    ModuleSpace.setDelegatingModuleSpaceClass(moduleSpaceClass);
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
    if (javaProject == null) {
      projectClassLoader = ClassLoader.getSystemClassLoader();
    } else {
      ClassLoader devClassLoader = getDevClassLoader0();
      projectClassLoader = new LocalProjectClassLoader(moduleDescription.getURLs(), devClassLoader);
    }
  }
  
  private static final class LocalProjectClassLoader extends URLClassLoader {
    private LocalProjectClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    @Override
    public URL findResource(String name) {
      URL url = super.findResource(name);
      if (isWrongURL(url)) {
        url = null;
      }
      return url;
    }

    /**
     * @return <code>true</code> if given {@link URL} represents {@link File} with non-canonical
     *         path, such as using incorrect case on Windows. JDT compiler tried to detect if
     *         given name "test" is name of package or not, by searching for "test.class"
     *         resource. But on Windows file system is not case sensitive, so "Test.class"
     *         resource returned, so it is considered not as package, but as type.
     */
    private boolean isWrongURL(URL url) {
      if (EnvironmentUtils.IS_WINDOWS) {
        File file = FileUtils.toFile(url);
        if (file != null && file.exists()) {
          try {
            String absolutePath = file.getAbsolutePath();
            String canonicalPath = file.getCanonicalPath();
            return !absolutePath.equals(canonicalPath);
          } catch (Throwable e) {
          }
        }
      }
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHostedModeSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public void startup(String browserStartupUrl,
      String moduleName,
      IProgressMonitor monitor,
      int timeout) throws Exception {
    browserShell.setUrl(browserStartupUrl, moduleName, timeout, new Runnable() {
      public void run() {
        runMessagesLoop();
      }
    });
    // setup parent for CompilingClassLoader
    ClassLoader classLoader = getClassLoader();
    ReflectionUtils.setField(classLoader, "parent", parentClassLoader);
  }

  public void dispose() {
    if (moduleSpaceHost != null) {
      // clear static caches
      ClassLoader devClassLoader = getDevClassLoader();
      try {
        Class<?> clazz = devClassLoader.loadClass("com.google.gwt.i18n.rebind.ClearStaticData");
        ReflectionUtils.invokeMethod2(clazz, "clear");
      } catch (Throwable e) {
      }
      try {
        Class<?> clazz =
            devClassLoader.loadClass("com.google.gwt.uibinder.rebind.model.OwnerFieldClass");
        Map<?, ?> map = (Map<?, ?>) ReflectionUtils.getFieldObject(clazz, "FIELD_CLASSES");
        map.clear();
      } catch (Throwable e) {
      }
      // remove parent of CompilingClassLoader
      if (parentClassLoader != null) {
        ClassLoader classLoader = getClassLoader();
        ReflectionUtils.setField(classLoader, "parent", null);
      }
    }
    //
    if (browserShell != null) {
      browserShell.dispose();
    }
    logSupport.dispose();
    moduleSpaceHost = null;
    impl = null;
    projectClassLoader = null;
    dispatchIdOracle = null;
  }

  public IBrowserShell getBrowserShell() {
    return browserShell;
  }

  public ClassLoader getClassLoader() {
    // returns CompilingClassLoader
    try {
      return (ClassLoader) ReflectionUtils.invokeMethod2(moduleSpaceHost, "getClassLoader");
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  public ClassLoader getDevClassLoader() {
    return projectClassLoader;
  }

  public void invalidateRebind(String typeName) {
    try {
      browserShell.getModuleSpace().invalidateRebind(typeName);
    } catch (Throwable e) {
      ReflectionUtils.propagate(e);
    }
  }

  public Object findJType(String name) {
    try {
      return ReflectionUtils.invokeMethod(impl, "findJType(java.lang.String)", name);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the {@link ClassLoader} for accessing gwt-dev classes mixed with design-time support
   *         lib.
   */
  private ClassLoader getDevClassLoader0() throws Exception {
    // disable persistent cache
    System.setProperty("gwt.persistentunitcache", "false");
    // prepare gwt-dev.jar location
    String devLibLocation = Utils.getDevLibLocation(moduleDescription);
    if (devLibLocation == null) {
      throw new HostedModeException(HostedModeException.NO_DEV_LIB);
    }
    String gwtLocation = FilenameUtils.getFullPath(devLibLocation);
    // add 'dev' & 'dev-designtime'
    ClassLoader devClassLoader = devClassLoaders.get(gwtLocation);
    if (devClassLoader == null) {
      URL resolvedDevLibUrl = new File(devLibLocation).toURI().toURL();
      Bundle bundle = Activator.getDefault().getBundle();
      URL devDesignUrl = FileLocator.resolve(bundle.getEntry("/gwt-dev-designtime.jar"));
      devClassLoader = new URLClassLoader(new URL[]{devDesignUrl, resolvedDevLibUrl}, null);
      devClassLoaders.put(gwtLocation, devClassLoader);
    }
    return devClassLoader;
  }

  public void activate() throws Exception {
    // do nothing
  }

  public byte[] getGeneratedResource(String resourceName) throws Exception {
    return null;
  }

  public ILogSupport getLogSupport() {
    return logSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BrowserShell 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates and returns the {@link IBrowserShell} instance for current platform using external
   * factory.
   */
  private IBrowserShell createBrowserShell() throws Exception {
    List<IBrowserShellFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IBrowserShellFactory.class,
            "com.google.gdt.eclipse.designer.hosted.2_2.browserShellFactory",
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
  public Object createModuleSpaceHost(String moduleName) throws Exception {
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getDevClassLoader());
    try {
      initializePersistentUnitCache();
      // create ShellModuleSpaceHost
      moduleSpaceHost =
          ReflectionUtils.invokeMethod(
              impl,
              "createModuleSpaceHost(java.lang.String,java.io.File,java.lang.String)",
              moduleName,
              null,
              getUserAgent());
      return moduleSpaceHost;
    } finally {
      Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    }
  }

  private void initializePersistentUnitCache() throws Exception {
    // there are failures in tests, don't know why
    if (EnvironmentUtils.isTestingTime()) {
      return;
    }
    // do initialize
    try {
      File cacheDir = new File(SystemUtils.getJavaIoTmpDir(), SystemUtils.USER_NAME + "-gwtd");
      ClassLoader devClassLoader = getDevClassLoader();
      Class<?> builderClass =
          devClassLoader.loadClass("com.google.gwt.dev.javac.CompilationStateBuilder");
      ReflectionUtils.invokeMethod(
          builderClass,
          "init(com.google.gwt.core.ext.TreeLogger,java.io.File)",
          logSupport.getLogger(),
          cacheDir);
    } catch (Throwable e) {
    }
  }

  /**
   * @return the actual user agent, or "safari" if "warm up" mode.
   */
  private String getUserAgent() {
    if (browserShell == null) {
      return "safari";
    }
    return browserShell.getUserAgentString();
  }

  public Object createModuleSpace(String moduleName, Object msHost, ModuleSpace delegateModuleSpace)
      throws Exception {
    return ReflectionUtils.invokeMethod(
        impl,
        "createDelegatingModuleSpace(java.lang.Object,java.lang.String,java.lang.Object)",
        msHost,
        moduleName,
        delegateModuleSpace);
  }

  public DispatchIdOracle getDispatchIdOracle(Object delegate) throws Exception {
    if (dispatchIdOracle == null) {
      final Object dispatchIdOracleImpl =
          ReflectionUtils.invokeMethod2(delegate, "getDispatchIdOracle");
      dispatchIdOracle = new DispatchIdOracleImpl(dispatchIdOracleImpl);
    }
    return dispatchIdOracle;
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
      return browserShell.getModuleSpace().invokeNativeBoolean(string, null, classes, objects);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @SuppressWarnings("rawtypes")
  public String invokeNativeString(String string, Class[] classes, Object[] objects) {
    try {
      return (String) browserShell.getModuleSpace().invokeNativeObject(
          string,
          null,
          classes,
          objects);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @SuppressWarnings("rawtypes")
  public void invokeNativeVoid(String string, Class[] classes, Object[] objects) {
    try {
      browserShell.getModuleSpace().invokeNativeVoid(string, null, classes, objects);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }


  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class DispatchIdOracleImpl implements DispatchIdOracle {
    private final Object dispatchIdOracleImpl;

    private DispatchIdOracleImpl(Object dispatchIdOracleImpl) {
      this.dispatchIdOracleImpl = dispatchIdOracleImpl;
    }

    public int getDispId(String member) {
      try {
        return (Integer) ReflectionUtils.invokeMethod(
            dispatchIdOracleImpl,
            "getDispId(java.lang.String)",
            member);
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
    }

    public DispatchClassInfo getClassInfoByDispId(int dispId) {
      try {
        final Object dispatchClassInfo =
            ReflectionUtils.invokeMethod(dispatchIdOracleImpl, "getClassInfoByDispId(int)", dispId);
        return new DispatchClassInfo() {
          public Member getMember(int dispId) {
            try {
              return (Member) ReflectionUtils.invokeMethod(
                  dispatchClassInfo,
                  "getMember(int)",
                  dispId);
            } catch (Throwable e) {
              throw ReflectionUtils.propagate(e);
            }
          }
        };
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
    }
  }
}
