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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.GwtDocumentHandler;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.module.ScriptElement;
import com.google.gdt.eclipse.designer.model.module.SetPropertyFallbackElement;
import com.google.gdt.eclipse.designer.model.module.StylesheetElement;
import com.google.gdt.eclipse.designer.model.web.WebAppElement;
import com.google.gdt.eclipse.designer.model.web.WebDocumentEditContext;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.model.web.WelcomeFileElement;
import com.google.gdt.eclipse.designer.model.web.WelcomeFileListElement;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.Version;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.visitors.TagFindingVisitor;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various non UI utilities for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.util
 */
public final class Utils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Environment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if Eclipse has GPE - Google Plugin for Eclipse, installed.
   */
  public static boolean hasGPE() {
    if (System.getProperty("wbp.noGPE") != null) {
      return false;
    }
    return Platform.getBundle("com.google.gwt.eclipse.core") != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Libraries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get absolute path to the <code>GWT_HOME</code>.
   * 
   * @param project
   *          optional GWT {@link IProject}, if not <code>null</code>, then project-specific
   *          gwt-user.jar may be returned; if <code>null</code>, then workspace-global one.
   */
  public static String getGWTLocation(IProject project) {
    IPath userLibPath = getUserLibPath(project);
    if (userLibPath != null) {
      return userLibPath.removeLastSegments(1).toPortableString();
    } else {
      return null;
    }
  }

  /**
   * Get absolute path to the gwt-user.jar.
   * 
   * @param project
   *          optional GWT {@link IProject}, if not <code>null</code>, then project-specific
   *          gwt-user.jar may be returned; if <code>null</code>, then workspace-global one.
   */
  public static IPath getUserLibPath(final IProject project) {
    // when no project, use workspace-global GWT_HOME
    if (project == null) {
      return new Path(Activator.getGWTLocation()).append("gwt-user.jar");
    }
    // try to find  project-specific GWT location
    return ExecutionUtils.runObject(new RunnableObjectEx<IPath>() {
      public IPath runObject() throws Exception {
        IJavaProject javaProject = JavaCore.create(project);
        String[] entries = ProjectClassLoader.getClasspath(javaProject);
        // try to find gwt-user.jar by name
        String userJarEntry = getUserJarEntry(entries);
        if (userJarEntry != null) {
          return new Path(userJarEntry);
        }
        // try to find gwt-user.jar by contents
        for (String entry : entries) {
          if (entry.endsWith(".jar")) {
            JarFile jarFile = new JarFile(entry);
            try {
              if (jarFile.getEntry("com/google/gwt/core/Core.gwt.xml") != null) {
                return new Path(entry);
              }
            } finally {
              jarFile.close();
            }
          }
        }
        // not found
        return null;
      }
    });
  }

  static String getUserJarEntry(String[] entries) {
    for (String entry : entries) {
      // lookup for gwt-user.jar entry; it can be 'gwt-user-1.5.3.jar', so use regexp
      Pattern p = Pattern.compile(".*gwt-user.*\\.jar");
      Matcher m = p.matcher(entry);
      if (m.matches()) {
        return entry;
      }
    }
    return null;
  }

  /**
   * Get absolute path to the gwt-dev-windows.jar or gwt-dev-linux.jar
   * 
   * @param project
   *          optional GWT {@link IProject}, if not <code>null</code>, then project-specific
   *          gwt-user.jar may be returned; if <code>null</code>, then workspace-global one.
   */
  public static IPath getDevLibPath(IProject project) {
    // try to use location of gwt-user.jar
    {
      String gwtLocation = getGWTLocation(project);
      // Maven
      if (gwtLocation.contains("/gwt/gwt-user/")) {
        String gwtFolder = StringUtils.substringBefore(gwtLocation, "/gwt-user/");
        String versionString = StringUtils.substringAfter(gwtLocation, "/gwt/gwt-user/");
        String devFolder = gwtFolder + "/gwt-dev/" + versionString;
        String devFileName = "gwt-dev-" + versionString + ".jar";
        Path path = new Path(devFolder + "/" + devFileName);
        if (path.toFile().exists()) {
          return path;
        }
      }
      // gwt-dev in same folder as gwt-user.jar
      {
        IPath path = getDevLibPath(gwtLocation);
        if (path.toFile().exists()) {
          return path;
        }
      }
    }
    // use gwt-dev.jar from default GWT location
    {
      String gwtLocation = Activator.getGWTLocation();
      return getDevLibPath(gwtLocation);
    }
  }

  private static IPath getDevLibPath(String gwtLocation) {
    String devLibName = "gwt-dev.jar";
    return new Path(gwtLocation).append(devLibName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Version
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Version GWT_2_0 = new Version(2, 0);
  public static final Version GWT_2_1 = new Version(2, 1);
  public static final Version GWT_2_1_1 = new Version(2, 1, 1);
  public static final Version GWT_2_2 = new Version(2, 2);

  /**
   * @return the default version of GWT, configured in preferences.
   */
  public static Version getDefaultVersion() {
    String userLocation = Activator.getGWTLocation() + "/gwt-user.jar";
    File userFile = new File(userLocation);
    if (userFile.exists()) {
      try {
        JarFile jarFile = new JarFile(userFile);
        try {
          if (hasClassEntry(jarFile, "com.google.gwt.canvas.client.Canvas")) {
            return GWT_2_2;
          }
          if (hasClassEntry(jarFile, "com.google.gwt.user.client.ui.DirectionalTextHelper")) {
            return GWT_2_1_1;
          }
          if (hasClassEntry(jarFile, "com.google.gwt.cell.client.Cell")) {
            return GWT_2_1;
          }
          if (hasClassEntry(jarFile, "com.google.gwt.user.client.ui.LayoutPanel")) {
            return GWT_2_0;
          }
        } finally {
          jarFile.close();
        }
      } catch (Throwable e) {
      }
    }
    // default version
    return GWT_2_2;
  }

  private static boolean hasClassEntry(JarFile jarFile, String className) {
    String path = className.replace('.', '/') + ".class";
    return jarFile.getEntry(path) != null;
  }

  /**
   * Returns the version of GWT using in given {@link IProject}.
   * 
   * @param javaProject
   *          the GWT {@link IJavaProject}.
   */
  public static Version getVersion(IProject project) {
    IJavaProject javaProject = JavaCore.create(project);
    return getVersion(javaProject);
  }

  /**
   * Returns the version of GWT using in given {@link IJavaProject}.
   * 
   * @param javaProject
   *          the GWT {@link IJavaProject}.
   */
  public static Version getVersion(IJavaProject javaProject) {
    if (ProjectUtils.hasType(javaProject, "com.google.gwt.canvas.client.Canvas")) {
      return GWT_2_2;
    }
    if (ProjectUtils.hasType(javaProject, "com.google.gwt.user.client.ui.DirectionalTextHelper")) {
      return GWT_2_1_1;
    }
    if (ProjectUtils.hasType(javaProject, "com.google.gwt.cell.client.Cell")) {
      return GWT_2_1;
    }
    if (ProjectUtils.hasType(javaProject, "com.google.gwt.user.client.ui.LayoutPanel")) {
      return GWT_2_0;
    }
    // default
    return GWT_2_2;
  }

  /**
   * @return <code>true</code> if given version of GWT contains MVP framework.
   */
  public static boolean supportMvp(Version gwtVersion) {
    return gwtVersion.isHigherOrSame(GWT_2_1);
  }

  /**
   * @return <code>true</code> if given {@link IJavaProject} support MVP framework.
   */
  public static boolean supportMvp(IJavaProject javaProject) {
    Version version = getVersion(javaProject);
    return supportMvp(version);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ModuleDescription utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given module inherits required, directly or indirectly.
   */
  public static boolean inheritsModule(ModuleDescription moduleDescription,
      final String requiredModule) throws Exception {
    final AtomicBoolean result = new AtomicBoolean();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void endVisitModule(ModuleElement module) {
        if (requiredModule.equals(module.getId())) {
          result.set(true);
        }
      }
    });
    return result.get();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Single module access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ModuleDescription} to which belongs given {@link ICompilationUnit}.
   */
  public static ModuleDescription getSingleModule(ICompilationUnit unit) throws Exception {
    IPackageFragment packageFragment = (IPackageFragment) unit.getParent();
    return getSingleModule(packageFragment);
  }

  /**
   * @return the {@link ModuleDescription} to which belongs given {@link IType}.
   */
  public static ModuleDescription getSingleModule(IType type) throws Exception {
    IPackageFragment packageFragment = type.getPackageFragment();
    return getSingleModule(packageFragment);
  }

  /**
   * @return the {@link ModuleDescription} to which belongs given {@link IPackageFragment}.
   */
  public static ModuleDescription getSingleModule(IPackageFragment pkg) throws Exception {
    IResource packageResource = pkg.getUnderlyingResource();
    return getSingleModule(packageResource);
  }

  /**
   * @return the {@link ModuleDescription} to which belongs given {@link IResource}, may be
   *         <code>null</code> if no module found.
   */
  public static ModuleDescription getSingleModule(IResource resource) throws Exception {
    List<ModuleDescription> modules = getModules(resource);
    // apply filters
    for (IModuleFilter filter : getModuleFilters()) {
      modules = filter.filter(modules);
    }
    // use first
    return modules.isEmpty() ? null : modules.get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module files searching
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ModuleDescription} that exactly corresponds to the given object, may be
   *         <code>null</code> there are no corresponding module or more than one module exists.
   */
  public static ModuleDescription getExactModule(Object object) {
    for (IModuleProvider moduleProvider : getModuleProviders()) {
      ModuleDescription module = moduleProvider.getExactModule(object);
      if (module != null) {
        return module;
      }
    }
    return null;
  }

  /**
   * @return the {@link ModuleDescription} with given id, may be <code>null</code>
   */
  public static ModuleDescription getModule(IJavaProject javaProject, String id) throws Exception {
    for (IModuleProvider moduleProvider : getModuleProviders()) {
      ModuleDescription module = moduleProvider.getModuleDescription(javaProject, id);
      if (module != null) {
        return module;
      }
    }
    return null;
  }

  /**
   * @return all {@link ModuleDescription} in given {@link IJavaProject}.
   */
  public static List<ModuleDescription> getModules(IJavaProject javaProject) throws Exception {
    List<ModuleDescription> modules = Lists.newArrayList();
    for (IModuleProvider moduleProvider : getModuleProviders()) {
      modules.addAll(moduleProvider.getModules(javaProject));
    }
    return modules;
  }

  /**
   * @return the {@link ModuleDescription}s to which belongs given {@link IResource}, may be empty
   *         list if no module found.
   */
  public static List<ModuleDescription> getModules(IResource resource) throws Exception {
    List<ModuleDescription> modules = Lists.newArrayList();
    for (IModuleProvider moduleProvider : getModuleProviders()) {
      modules.addAll(moduleProvider.getModules(resource));
    }
    return modules;
  }

  private static List<IModuleProvider> getModuleProviders() {
    return ExternalFactoriesHelper.getElementsInstances(
        IModuleProvider.class,
        "com.google.gdt.eclipse.designer.moduleProviders",
        "provider");
  }

  private static List<IModuleFilter> getModuleFilters() {
    return ExternalFactoriesHelper.getElementsInstances(
        IModuleFilter.class,
        "com.google.gdt.eclipse.designer.moduleProviders",
        "filter");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module packages/folders access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Accepts some {@link IPackageFragment} in "source" package and returns root "source" package.
   */
  public static IPackageFragment getRootSourcePackage(IPackageFragment pkg) throws Exception {
    IPackageFragmentRoot sourceRoot = (IPackageFragmentRoot) pkg.getParent();
    IPackageFragment clientRoot = pkg;
    while (isModuleSourcePackage(pkg)) {
      clientRoot = pkg;
      String pkgName = pkg.getElementName();
      String pkgParentName = CodeUtils.getPackage(pkgName);
      pkg = sourceRoot.getPackageFragment(pkgParentName);
    }
    return clientRoot;
  }

  /**
   * @return <code>true</code> if given {@link IPackageFragment} is "source" package of some GWT
   *         module.
   */
  public static boolean isModuleSourcePackage(IPackageFragment packageFragment) throws Exception {
    final String packageName = packageFragment.getElementName();
    // check enclosing module
    ModuleDescription module = getSingleModule(packageFragment);
    if (module != null) {
      final AtomicBoolean result = new AtomicBoolean();
      ModuleVisitor.accept(module, new ModuleVisitor() {
        @Override
        public boolean visitModule(ModuleElement moduleElement) {
          String modulePackage = CodeUtils.getPackage(moduleElement.getId()) + ".";
          if (packageName.startsWith(modulePackage)) {
            String folderInModule = packageName.substring(modulePackage.length()).replace('.', '/');
            if (moduleElement.isInSourceFolder(folderInModule)) {
              result.set(true);
              return false;
            }
          }
          return true;
        }
      });
      return result.get();
    }
    // no enclosing module
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module reading
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<String, ModuleElement> m_readModule = Maps.newTreeMap();

  /**
   * Reads module definition from given {@link ModuleDescription}.
   */
  public static ModuleElement readModule(ModuleDescription moduleDescription) throws Exception {
    String id = moduleDescription.getId();
    InputStream contents = moduleDescription.getContents();
    return readModule(id, contents);
  }

  /**
   * Reads module definition from given stream.
   */
  public static ModuleElement readModule(String id, InputStream inputStream) throws Exception {
    String contents = IOUtils2.readString(inputStream);
    // check cache
    String key = id + "|" + contents;
    {
      ModuleElement moduleElement = m_readModule.get(key);
      if (moduleElement != null) {
        return moduleElement;
      }
    }
    // parse using GWTDocumentHandler
    GwtDocumentHandler documentHandler = new GwtDocumentHandler();
    try {
      QParser.parse(new StringReader(contents), documentHandler);
    } catch (Throwable e) {
      throw new DesignerException(IExceptionConstants.INVALID_MODULE_FILE, id);
    }
    // prepare module element
    ModuleElement moduleElement = documentHandler.getModuleElement();
    moduleElement.setId(id);
    moduleElement.finalizeLoading();
    // fill cache
    m_readModule.put(key, moduleElement);
    // done
    return moduleElement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param moduleFile
   *          the module *.gwt.xml file.
   * @param resource
   *          the path of "public" resource.
   * 
   * @return the existing resource {@link IFile}, may be <code>null</code>.
   */
  public static IFile getFileForResource(IFile moduleFile, String resource) throws Exception {
    List<IFile> files = getFilesForResources(moduleFile, ImmutableList.of(resource));
    return !files.isEmpty() ? files.get(0) : null;
  }

  /**
   * Returns the {@link IFile}'s for "public" resources in given module. An {@link IFile} for
   * resource is returned only if this resource located in some IProject in workspace, not just in
   * jar.
   * 
   * @param moduleFile
   *          the module *.gwt.xml file.
   * @param resources
   *          the paths of "public" resources, relative to the one of the "public" package of some
   *          module.
   */
  public static List<IFile> getFilesForResources(IFile moduleFile, Collection<String> resources)
      throws Exception {
    // prepare IContainer's for each "src" folder
    List<IContainer> sourceFolders;
    {
      IJavaProject javaProject = JavaCore.create(moduleFile.getProject());
      sourceFolders = CodeUtils.getSourceContainers(javaProject, true);
    }
    // fill list of IFile's
    List<IFile> files = Lists.newArrayList();
    for (String resourcePath : resources) {
      IFile file = getFileForResource(moduleFile, sourceFolders, resourcePath);
      if (file != null) {
        files.add(file);
      }
    }
    // return IFile's
    return files;
  }

  private static IFile getFileForResource(IFile moduleFile,
      final List<IContainer> sourceFolders,
      final String resourcePath) throws Exception {
    final IFile files[] = new IFile[1];
    // check public resources
    ModuleVisitor.accept(new DefaultModuleDescription(moduleFile), new ModuleVisitor() {
      @Override
      public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
        for (IContainer sourceContainer : sourceFolders) {
          checkContainer(packageName, sourceContainer);
        }
      }

      private void checkContainer(String packageName, IContainer sourceContainer) {
        if (files[0] == null) {
          String filePath = packageName.replace('.', '/') + "/" + resourcePath;
          IFile file = sourceContainer.getFile(new Path(filePath));
          if (file.exists()) {
            files[0] = file;
          }
        }
      }
    });
    // check "web" folder
    if (files[0] == null) {
      files[0] = getFileForResource_webFolder(moduleFile, resourcePath);
    }
    // done
    return files[0];
  }

  /**
   * @return the existing resource in "web" folder, may be <code>null</code>.
   */
  private static IFile getFileForResource_webFolder(IFile moduleFile, String pathInWebFolder) {
    IProject project = moduleFile.getProject();
    String webFolderName = WebUtils.getWebFolderName(project);
    IFolder webFolder = project.getFolder(webFolderName);
    if (webFolder != null && webFolder.exists()) {
      IFile file = webFolder.getFile(new Path(pathInWebFolder));
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if resource with given path exists in module.
   */
  public static boolean isExistingResource(ModuleDescription moduleDescription, String path)
      throws Exception {
    InputStream stream = getResource(moduleDescription, path);
    IOUtils.closeQuietly(stream);
    return stream != null;
  }

  /**
   * 1. GWT 1.5 under 1.5. Resource path is path in "public", for example just "home.gif".
   * <p>
   * 2. GWT 1.5 under 1.6. In GWT 1.6 all resources should be in "web", including resources from
   * "public", which copied into sub-folder with "logical" module name (fully qualified name or
   * value of "rename-to"). So, to access resource from "web" itself, use just "home.gif". To access
   * resource from "my.long.module.Name" with "rename-to=module", use "module/home.gif". Note, that
   * copying "public" resources happens only when you run hosted mode, but not when you just use
   * design time, i.e. it should be considered as "virtual" copying. So, we should first check
   * "public" folders and if not found, check "web".
   * <p>
   * 3. GWT 1.6 under 1.6.
   */
  public static InputStream getResource(ModuleDescription moduleDescription, String path)
      throws Exception {
    // try "public" module resource
    {
      InputStream result = getResource_modulePublic(moduleDescription, path);
      if (result != null) {
        return result;
      }
    }
    // check "web" folder
    IProject project = moduleDescription.getProject();
    String webFolderName = WebUtils.getWebFolderName(project);
    IPath webFolderPath = new Path(webFolderName);
    IFolder webFolder = project.getFolder(webFolderPath);
    if (webFolder != null) {
      IFile file = webFolder.getFile(new Path(path));
      if (file.exists()) {
        return file.getContents(true);
      }
    }
    // no resource
    return null;
  }

  /**
   * If given path has module name as prefix, then resource may be in "public" folder.
   */
  public static InputStream getResource_modulePublic(ModuleDescription moduleDescription,
      String path) throws Exception {
    // prepare "public" path
    final String publicResourcePath;
    {
      ModuleElement module = readModule(moduleDescription);
      String prefix = module.getName() + "/";
      if (path.startsWith(prefix)) {
        publicResourcePath = path.substring(prefix.length());
      } else {
        publicResourcePath = path;
      }
    }
    // check "public" folders
    final InputStream[] result = new InputStream[1];
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
        if (result[0] == null) {
          String fullResourcePath = packageName.replace('.', '/') + "/" + publicResourcePath;
          result[0] = getResourcesProvider().getResourceAsStream(fullResourcePath);
        }
      }
    });
    return result[0];
  }

  /*private static InputStream getResource_modulePublic(IFile moduleFile, String path) throws Exception {
  	ModuleElement module = readModule(moduleFile);
  	String prefix = module.getName() + "/";
  	if (path.startsWith(prefix)) {
  		final String publicResourcePath = path.substring(prefix.length());
  		final InputStream[] result = new InputStream[1];
  		ModuleVisitor.accept(moduleFile, new ModuleVisitor() {
  			@Override
  			public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
  				if (result[0] == null) {
  					String fullResourcePath = packageName.replace('.', '/') + "/" + publicResourcePath;
  					result[0] = getResourcesProvider().getResourceAsStream(fullResourcePath);
  				}
  			}
  		});
  		return result[0];
  	}
  	// not module "public" path
  	return null;
  }*/
  /**
   * @return the default name of HTML file for given module name.
   */
  public static String getDefaultHTMLName(String moduleId) {
    return StringUtils.substringAfterLast(moduleId, ".") + ".html";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // *.css/*.js resources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return HTML of the first '!doctype' tag (if any), otherwise returns empty string. Note: return
   *         <code>null</code> if '!doctype' value couldn't be determined.
   */
  public static String getDocType(ModuleDescription moduleFile) throws Exception {
    IFile htmlFile = getHTMLFile(moduleFile);
    if (htmlFile != null) {
      List<TagNode> tags = getHTMLFileTags(htmlFile, "!doctype");
      for (TagNode tag : tags) {
        return tag.toHtml();
      }
      return "";
    }
    return null;
  }

  /**
   * Gets "public" paths of the CSS resources referenced by module. There are two sources for CSS
   * resources:
   * <ol>
   * <li><code>link</code> tags in HTML file;</li>
   * <li><code>stylesheet</code> tags in module file.</li>
   * </ol>
   * <p>
   * It expects that unit is inside of some GWT module with single module file. It also expects that
   * default HTML file is used, see {@link #getHTMLFile(IFile)}.
   */
  public static List<String> getCssResources(ModuleDescription moduleDescription) throws Exception {
    final List<String> cssResources = Lists.newArrayList();
    // add CSS resources from module file, *.gwt.xml
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void endVisitModule(ModuleElement module) {
        for (StylesheetElement stylesheetElement : module.getStylesheetElements()) {
          String src = stylesheetElement.getSrc();
          if (src.startsWith("/")) {
            src = src.substring(1);
          }
          if (src.startsWith("../")) {
            src = src.substring(3);
          }
          cssResources.add(src);
        }
      }
    });
    // add CSS resources from HTML
    {
      IFile htmlFile = getHTMLFile(moduleDescription);
      if (htmlFile != null) {
        String resourcePrefix = getHTMLResourcePrefix(htmlFile);
        List<TagNode> tags = getHTMLFileTags(htmlFile, "link");
        for (TagNode tag : tags) {
          if ("stylesheet".equals(tag.getAttribute("rel"))) {
            String href = tag.getAttribute("href");
            String resourcePath = resourcePrefix + href;
            cssResources.add(resourcePath);
          }
        }
      }
    }
    // OK, we have all CSS resources for module
    return cssResources;
  }

  /**
   * Resources referenced from HTML file are located relative to folder of HTML file.
   */
  private static String getHTMLResourcePrefix(IFile htmlFile) throws CoreException {
    String htmlPath = (String) htmlFile.getSessionProperty(KEY_RESOURCE_PATH);
    int index = htmlPath.indexOf("/");
    return index == -1 ? "" : htmlPath.substring(0, index + 1);
  }

  /**
   * Gets "public" paths of the CSS resources referenced by module. There are two sources for CSS
   * resources:
   * <ol>
   * <li><code>script</code> tags in HTML file;</li>
   * <li><code>script</code> tags in module file.</li>
   * </ol>
   * <p>
   * It expects that unit is inside of some GWT module with single module file. It also expects that
   * default HTML file is used, see {@link #getHTMLFile(IFile)}.
   */
  public static List<String> getScriptResources(ModuleDescription moduleDescription)
      throws Exception {
    final List<String> scriptResources = Lists.newArrayList();
    // add <script> resources from HTML
    {
      IFile htmlFile = getHTMLFile(moduleDescription);
      if (htmlFile != null) {
        List<TagNode> tags = getHTMLFileTags(htmlFile, "script");
        for (TagNode tag : tags) {
          String src = tag.getAttribute("src");
          if (src != null) {
            scriptResources.add(src);
          }
        }
      }
    }
    // add CSS resources from module file, *.gwt.xml
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void endVisitModule(ModuleElement module) {
        for (ScriptElement scriptElement : module.getScriptElements()) {
          String src = scriptElement.getSrc();
          if (src != null) {
            //scriptResources.add(mainModuleName + "/" + src);
            scriptResources.add(src);
          }
        }
      }
    });
    // exclude some scripts
    for (Iterator<String> I = scriptResources.iterator(); I.hasNext();) {
      String src = I.next();
      // GWT 1.4+ system script
      if (src.indexOf(".nocache.js") != -1) {
        I.remove();
      }
      // Google Maps
      if (src.indexOf("maps.google.com/maps?") != -1) {
        I.remove();
      }
    }
    // OK, we have all CSS resources for module
    return scriptResources;
  }

  private static final QualifiedName KEY_RESOURCE_PATH =
      new QualifiedName("com.google.gdt.eclipse.designer", "resourcePath");

  /**
   * @return the {@link IFile} for module HTML file, may be <code>null</code>.
   */
  public static IFile getHTMLFile(ModuleDescription moduleDescription) throws Exception {
    // IFile can only be returned if this ModuleDescription is a DefaultModuleDescription,
    // aka, if it wraps an IFile itself
    IFile moduleFile;
    if (moduleDescription instanceof DefaultModuleDescription) {
      moduleFile = ((DefaultModuleDescription) moduleDescription).getFile();
    } else {
      return null;
    }
    // try welcome-file
    {
      IFile file = getHTMLFile_web(moduleFile);
      if (file != null) {
        return file;
      }
    }
    // try default HTML
    String moduleId = moduleDescription.getId();
    String htmlFileName = getDefaultHTMLName(moduleId);
    IFile file = getFileForResource(moduleFile, htmlFileName);
    if (file != null) {
      file.setSessionProperty(KEY_RESOURCE_PATH, htmlFileName);
    }
    return file;
  }

  /**
   * Tries to find HTML file specified in <code>web.xml</code>, as "welcome-file".
   * 
   * @return the {@link IFile} for HTML file, may be <code>null</code>.
   */
  private static IFile getHTMLFile_web(IFile moduleFile) throws Exception {
    IFile webFile = getFileForResource_webFolder(moduleFile, "WEB-INF/web.xml");
    if (webFile == null) {
      return null;
    }
    // ignore if empty
    String webFileContent = IOUtils2.readString(webFile);
    if (StringUtils.isBlank(webFileContent)) {
      return null;
    }
    // search for "welcome-file"
    List<String> welcomeFileNames = Lists.newArrayList();
    try {
      WebDocumentEditContext context = new WebDocumentEditContext(webFile);
      try {
        WebAppElement webApp = context.getWebAppElement();
        for (WelcomeFileListElement welcomeFileList : webApp.getWelcomeFileListElements()) {
          for (WelcomeFileElement welcomeFile : welcomeFileList.getWelcomeFiles()) {
            String welcomeFileName = welcomeFile.getName();
            welcomeFileNames.add(welcomeFileName);
          }
        }
      } finally {
        context.disconnect();
      }
    } catch (Throwable e) {
      throw new DesignerException(IExceptionConstants.INVALID_WEB_XML,
          e,
          webFile.getFullPath().toPortableString(),
          webFileContent);
    }
    // try to find resource for "welcome-file"
    for (String welcomeFileName : welcomeFileNames) {
      IFile file = getFileForResource(moduleFile, welcomeFileName);
      if (file != null) {
        file.setSessionProperty(KEY_RESOURCE_PATH, welcomeFileName);
        return file;
      }
    }
    // not found
    return null;
  }

  /**
   * Uses "htmlParser" library to parse given HTML and extract tags with given name.
   * 
   * @return the {@link List} of found {@link TagNode}'s.
   */
  private static List<TagNode> getHTMLFileTags(IFile htmlFile, String tagName) throws Exception {
    // find nodes
    Node[] tags;
    {
      String htmlContents = IOUtils2.readString(htmlFile);
      Lexer lexer = new Lexer(new Page(htmlContents));
      Parser parser = new Parser(lexer, new DefaultParserFeedback(DefaultParserFeedback.QUIET));
      TagFindingVisitor visitor = new TagFindingVisitor(new String[]{tagName});
      parser.visitAllNodesWith(visitor);
      tags = visitor.getTags(0);
    }
    // convert into List<TagNode>
    List<TagNode> tagNodes = Lists.newArrayList();
    CollectionUtils.addAll(tagNodes, tags);
    return tagNodes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Locale
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the default locale from "set-property-fallback" property in module file.
   */
  public static String getDefaultLocale(ModuleDescription moduleDescription) throws Exception {
    final AtomicReference<String> defaultLocale = new AtomicReference<String>("default");
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void endVisitModule(ModuleElement module) {
        List<SetPropertyFallbackElement> elements = module.getSetPropertyFallbackElements();
        for (SetPropertyFallbackElement element : elements) {
          if (element.getName().equals("locale")) {
            defaultLocale.set(element.getValue());
          }
        }
      }
    });
    return defaultLocale.get();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RemoteService interface/impl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that given resource is Java file with remote service.
   */
  public static boolean isRemoteService(IResource resource) throws CoreException {
    if (resource != null && resource instanceof IFile && resource.getName().endsWith(".java")) {
      ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
      return isRemoteService(cu);
    }
    return false;
  }

  /**
   * Checks that given Java element is part of unit that has RemoteService.
   */
  public static boolean isRemoteService(IJavaElement element) throws CoreException {
    IType type = CodeUtils.getType(element);
    if (type != null && type.exists() && type.isInterface()) {
      return CodeUtils.isSuccessorOf(type, Constants.CLASS_REMOTE_SERVICE);
    }
    return false;
  }

  /**
   * Checks that given Java element is part of unit that has RemoteService.
   */
  public static boolean isRemoteServiceImpl(IJavaElement element) throws Exception {
    IType type = CodeUtils.getType(element);
    if (type != null && type.exists()) {
      return CodeUtils.isSuccessorOf(type, Constants.CLASS_REMOTE_SERVICE_IMPL);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryPoint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that given Java element is part part of unit that has EntryPoint.
   */
  public static boolean isEntryPoint(IJavaElement element) throws Exception {
    IType type = CodeUtils.getType(element);
    if (type != null && type.exists()) {
      return CodeUtils.isSuccessorOf(type, Constants.CLASS_ENTRY_POINT);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Progress
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

  /**
   * @return the given {@link IProgressMonitor} is it is not <code>null</code> or shared
   *         {@link NullProgressMonitor} instance.
   */
  public static IProgressMonitor getNonNullMonitor(IProgressMonitor monitor) {
    if (monitor == null) {
      monitor = NULL_PROGRESS_MONITOR;
    }
    return monitor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Projects
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IJavaProject} with given name.
   */
  public static IJavaProject getJavaProject(String name) {
    IProject project = getProject(name);
    return JavaCore.create(project);
  }

  /**
   * @return the {@link IProject} with given name.
   */
  public static IProject getProject(String name) {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
  }

  /**
   * @return <code>true</code> if given project is GWT project.
   */
  public static boolean isGWTProject(final IJavaProject javaProject) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return ProjectUtils.hasType(javaProject, "com.google.gwt.user.client.ui.Widget");
      }
    }, false);
  }

  /**
   * @return <code>true</code> if given project is GWT project.
   */
  public static boolean isGWTProject(final IProject project) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        IJavaProject javaProject = JavaCore.create(project);
        return isGWTProject(javaProject);
      }
    }, false);
  }

  /**
   * @return <code>true</code> if given project is GPE GWT project.
   */
  public static boolean isGpeGwtProject(IProject project) {
    return ProjectUtils.hasNature(project, Constants.GPE_NATURE_ID) && isGWTProject(project);
  }

  /**
   * Convenience method to get access to the {@link IJavaModel}.
   */
  public static IJavaModel getJavaModel() {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    return JavaCore.create(workspaceRoot);
  }

  /**
   * @return the GWT Java projects.
   */
  public static List<IJavaProject> getGWTProjects() throws CoreException {
    List<IJavaProject> gwtProjects = Lists.newArrayList();
    for (IJavaProject javaProject : getJavaModel().getJavaProjects()) {
      if (isGWTProject(javaProject.getProject())) {
        gwtProjects.add(javaProject);
      }
    }
    return gwtProjects;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse given model {@link ICompilationUnit} into AST {@link CompilationUnit}.
   */
  public static CompilationUnit parseUnit(ICompilationUnit compilationUnit) {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(compilationUnit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }
}
