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
package com.google.gdt.eclipse.designer.util.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Implementation of {@link IResourcesProvider} for resources in GWT project.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.resources
 */
public class DefaultResourcesProvider implements IResourcesProvider {
  private final List<IResourcesLoader> m_loaders = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultResourcesProvider(ModuleDescription moduleDescription) throws Exception {
    this(moduleDescription.getJavaProject());
  }

  public DefaultResourcesProvider(IJavaProject javaProject) throws Exception {
    //long start = System.nanoTime();
    List<File> files = getFiles(javaProject);
    for (File file : files) {
      if (file.isDirectory()) {
        m_loaders.add(new DirResourcesLoader(file));
      } else if (isJarFile(file)) {
        m_loaders.add(new JarResourcesLoader(file));
      }
    }
    //System.out.println("DefaultResourcesProvider: " + (System.nanoTime() - start) / 1000000.0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResourceListProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    for (IResourcesLoader loader : m_loaders) {
      loader.dispose();
    }
  }

  public InputStream getResourceAsStream(final String path) throws Exception {
    return ExecutionUtils.runObject(new RunnableObjectEx<InputStream>() {
      public InputStream runObject() throws Exception {
        for (IResourcesLoader loader : m_loaders) {
          InputStream stream = loader.getResourceAsStream(path);
          if (stream != null) {
            return stream;
          }
        }
        // not found
        return null;
      }
    }, "Exception during loading resource %s", path);
  }

  public List<String> listFiles(String path) throws Exception {
    List<String> resources = Lists.newArrayList();
    for (IResourcesLoader loader : m_loaders) {
      loader.appendFiles(resources, path);
    }
    return resources;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Entries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link File}'s for each jar/directory entry of classpath for {@link IJavaProject}
   *         that contains given GWT module.
   */
  private static List<File> getFiles(IJavaProject javaProject) throws Exception {
    List<File> files = Lists.newArrayList();
    // prepare source/classpath entries
    List<String> entries;
    {
      entries = Lists.newArrayList();
      addSourceFolders(Sets.<IProject>newHashSet(), entries, javaProject.getProject());
      {
        String[] classpathEntries = ProjectClassLoader.getClasspath(javaProject);
        for (String classpathEntry : classpathEntries) {
          if (classpathEntry.endsWith(".jar")) {
            entries.add(classpathEntry);
          }
        }
      }
    }
    // create File for each entry
    for (String entry : entries) {
      // not very good optimization, but it should be done - filter out JRE jar's
      if (entry.indexOf("/jre/") != -1) {
        continue;
      }
      // OK, add File
      files.add(new File(entry));
    }
    return files;
  }

  /**
   * Appends source {@link IPackageFragmentRoot}'s, because {@link DefaultResourcesProvider} should
   * provide access not only to the binary resources (i.e. just resources in classpath), but also to
   * the source resources.
   */
  private static void addSourceFolders(Set<IProject> visitedProjects,
      List<String> entries,
      IProject project) throws Exception {
    // may be not exists
    if (!project.exists()) {
      return;
    }
    // may be already visited
    if (visitedProjects.contains(project)) {
      return;
    }
    visitedProjects.add(project);
    // add source folders for IJavaProject
    {
      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject.exists()) {
        for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
          if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
            entries.add(packageFragmentRoot.getResource().getLocation().toPortableString());
          }
        }
      }
    }
    // process required projects
    for (IProject referencedProject : project.getReferencedProjects()) {
      addSourceFolders(visitedProjects, entries, referencedProject);
    }
  }

  /**
   * @return <code>true</code> if given {@link File} is JAR file.
   */
  private static boolean isJarFile(File file) {
    if (!file.exists()) {
      return false;
    }
    try {
      JarFile jarFile = new JarFile(file);
      jarFile.close();
      return true;
    } catch (Throwable e) {
      return false;
    }
  }
}
