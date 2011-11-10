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
package com.google.gdt.eclipse.designer.actions.deploy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.actions.AbstractModuleAction;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.launch.GwtLaunchConfigurationDelegate;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.module.ServletElement;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.ModuleVisitor;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.Version;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Action for deployment selected GWT module into application server.
 * 
 * @author scheglov_ke
 * @coverage gwt.deploy
 */
public class DeployModuleAction extends AbstractModuleAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Action implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void selectedModuleChanged(IAction action) {
    if (m_selectedModule != null) {
      action.setEnabled(true);
      action.setToolTipText("Deploy module '"
          + m_selectedModule.getId()
          + "' on application server");
    } else {
      action.setEnabled(false);
      action.setToolTipText("No selected GWT module to deploy");
    }
  }

  @Override
  protected void runWithSelectedModule() throws Exception {
    final DeployDialog deployDialog =
        new DeployDialog(DesignerPlugin.getShell(), Activator.getDefault(), m_selectedModule);
    if (deployDialog.open() != Window.OK) {
      return;
    }
    //
    IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        try {
          monitor.beginTask("Deployment of module '" + m_selectedModule.getId() + "'", 1);
          // create script
          {
            monitor.worked(1);
            createBuildScript(deployDialog);
          }
          // execute script
          {
            IProject project = m_selectedModule.getProject();
            IFolder targetFolder = m_selectedModule.getModuleFolder();
            File buildFile = targetFolder.getFile("build.xml").getLocation().toFile();
            AntHelper antHelper = new AntHelper(buildFile, project.getLocation().toFile());
            antHelper.execute(monitor);
          }
        } catch (Throwable e) {
          throw new InvocationTargetException(e);
        }
      }
    };
    ProgressMonitorDialog progressMonitorDialog =
        new ProgressMonitorDialog(DesignerPlugin.getShell());
    progressMonitorDialog.run(true, false, runnableWithProgress);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ANT Script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates build.xml file in same folder as module.
   */
  private void createBuildScript(DeployDialog deployDialog) throws Exception {
    String moduleId = m_selectedModule.getId();
    //
    IProject project = m_selectedModule.getProject();
    IFolder targetFolder = m_selectedModule.getModuleFolder();
    // prepare script
    String script = getScriptTemplate();
    // apply template values
    script = StringUtils.replace(script, "##GWT_HOME##", Utils.getGWTLocation(project));
    script = StringUtils.replace(script, "##PROJECT_ROOT##", project.getLocation().toOSString());
    script = StringUtils.replace(script, "##MODULE_ID##", moduleId);
    script = StringUtils.replace(script, "##WAR_NAME##", deployDialog.getWarName());
    script = StringUtils.replace(script, "##DEPLOY_DIR##", deployDialog.getServerPath());
    script = StringUtils.replace(script, "##COMPILER_STYLE##", deployDialog.getCompilerStyle());
    script =
        StringUtils.replace(script, "##COMPILER_MAX_MEMORY##", deployDialog.getCompilerMaxMemory());
    // set class path elements
    {
      String classpathElements = "";
      String[] classpath = getGWTProjectClasspath(project);
      for (int i = 0; i < classpath.length; i++) {
        String element = classpath[i].replace('\\', '/');
        classpathElements += "\t\t\t\t<pathelement location=\"" + element + "\" />";
        if (i != classpath.length - 1) {
          classpathElements += "\n";
        }
      }
      script = StringUtils.replace(script, "##CLASS_PATH_ELEMENTS##", classpathElements);
    }
    // set servlets
    {
      final StringBuffer buffer = new StringBuffer();
      ModuleVisitor.accept(m_selectedModule, new ModuleVisitor() {
        @Override
        public void endVisitModule(ModuleElement module) {
          List<ServletElement> servletElements = module.getServletElements();
          for (ServletElement servletElement : servletElements) {
            String servletName = servletElement.getClassName();
            {
              buffer.append("\t<servlet>\n");
              buffer.append("\t\t<servlet-name>" + servletName + "</servlet-name>\n");
              buffer.append("\t\t<servlet-class>"
                  + servletElement.getClassName()
                  + "</servlet-class>\n");
              buffer.append("\t</servlet>\n");
            }
            {
              buffer.append("\t<servlet-mapping>\n");
              buffer.append("\t\t<servlet-name>" + servletName + "</servlet-name>\n");
              buffer.append("\t\t<url-pattern>" + servletElement.getPath() + "</url-pattern>\n");
              buffer.append("\t</servlet-mapping>\n");
            }
          }
        }
      });
      // apply sevlets
      script = StringUtils.replace(script, "##SERVLETS##", buffer.toString());
    }
    // create/copy jar's
    {
      String targetModulePath = WebUtils.getWebFolderName(project);
      String jarsScript = prepareJars(project, targetModulePath, true);
      jarsScript = StringUtils.chomp(jarsScript, "\n");
      script = StringUtils.replace(script, "<!--##PROJECT_AND_REQUIRED_JARS##-->", jarsScript);
    }
    // create build.xml
    IFile buildFile = targetFolder.getFile("build.xml");
    ByteArrayInputStream source = new ByteArrayInputStream(script.getBytes());
    if (buildFile.exists()) {
      buildFile.setContents(source, true, true, null);
    } else {
      buildFile.create(source, true, null);
    }
  }

  private String getScriptTemplate() throws Exception {
    IProject project = m_selectedModule.getProject();
    String buildVersion = getBuildVersion(project).getStringMajorMinor();
    String scriptName = "build-" + buildVersion + ".xml";
    // try to find script near to module
    {
      IFile scriptFile = m_selectedModule.getModuleFolder().getFile(new Path(scriptName));
      if (scriptFile.exists()) {
        return IOUtils2.readString(scriptFile);
      }
    }
    // OK, load default script
    return IOUtils2.readString(DeployModuleAction.class.getResourceAsStream(scriptName));
  }

  /**
   * @return the version of build file, not exactly same as version of GWT.
   */
  private static Version getBuildVersion(IProject project) {
    Version version = Utils.getVersion(project);
    if (version.isHigherOrSame(Utils.GWT_2_0)) {
      version = Utils.GWT_2_0;
    }
    return version;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script: jars
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns ANT code for creating jar's for given project itself, coping its jar's from classpath
   * and calls itself for required projects.
   */
  private static String prepareJars(IProject project,
      String targetModulePath,
      boolean addRuntimeJars) throws Exception {
    String script = "";
    //
    IJavaProject javaProject = JavaCore.create(project);
    IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
    // add <jar> task for creating jar from project source and output folders
    {
      List<String> sourceLocations = Lists.newArrayList();
      List<String> binaryLocations = Lists.newArrayList();
      for (IPackageFragmentRoot packageFragmentRoot : roots) {
        if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          // add source location
          sourceLocations.add(packageFragmentRoot.getResource().getLocation().toPortableString());
          // add output location
          {
            // prepare output location
            IPath location;
            {
              IClasspathEntry cpEntry = packageFragmentRoot.getRawClasspathEntry();
              location = cpEntry.getOutputLocation();
              if (location == null) {
                location = javaProject.getOutputLocation();
              }
            }
            // add absolute location
            {
              // remove first segment (project)
              location = location.removeFirstSegments(1);
              // prepare absolute location
              IPath absoluteLocation = project.getLocation().append(location);
              binaryLocations.add(absoluteLocation.toPortableString());
            }
          }
        }
      }
      //
      script += "\t\t<!--=== " + project.getName() + " ===-->\n";
      script +=
          "\t\t<jar destfile='"
              + targetModulePath
              + "/WEB-INF/lib/"
              + project.getName()
              + ".jar'>\n";
      script += prepareFileSets(sourceLocations, "**");
      script += prepareFileSets(binaryLocations, "**/*.class");
      script += "\t\t</jar>\n";
    }
    // add <copy> task for coping required runtime jar's
    if (addRuntimeJars) {
      String jars = "";
      IRuntimeClasspathEntry[] classpathEntries =
          JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject);
      for (int entryIndex = 0; entryIndex < classpathEntries.length; entryIndex++) {
        IRuntimeClasspathEntry entry = classpathEntries[entryIndex];
        IRuntimeClasspathEntry[] resolvedEntries =
            JavaRuntime.resolveRuntimeClasspathEntry(entry, javaProject);
        for (int resolvedEntryIndex = 0; resolvedEntryIndex < resolvedEntries.length; resolvedEntryIndex++) {
          IRuntimeClasspathEntry resolvedEntry = resolvedEntries[resolvedEntryIndex];
          if (resolvedEntry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES
              && resolvedEntry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
            String location = resolvedEntry.getLocation();
            // exclude gwt-user.jar, it is in classpath, but it has Servlet class, so can not be in application
            if (location.endsWith("gwt-user.jar")) {
              continue;
            }
            // add jar file in fileset
            jars += "\t\t\t<fileset file=\"" + location + "\"/>\n";
          }
        }
      }
      //
      if (jars.length() != 0) {
        script += "\t\t<copy todir='" + targetModulePath + "/WEB-INF/lib'>\n";
        script += jars;
        script += "\t\t</copy>\n";
      }
    }
    // add required projects
    {
      IProject[] referencedProjects = project.getReferencedProjects();
      for (int i = 0; i < referencedProjects.length; i++) {
        IProject referencedProject = referencedProjects[i];
        script += prepareJars(referencedProject, targetModulePath, false);
      }
    }
    //
    return script;
  }

  /**
   * Prepares source of <fileset>/<include> for given list of directories and inclusion pattern.
   */
  private static String prepareFileSets(List<String> dirs, String includePattern) {
    String result = "";
    for (String dir : dirs) {
      result += "\t\t\t<fileset dir=\"" + dir + "\">\n";
      result += "\t\t\t\t<include name=\"" + includePattern + "\"/>\n";
      result += "\t\t\t</fileset>\n";
    }
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classpath
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return GWT runtime classpath for given {@link IProject}.
   */
  public static String[] getGWTProjectClasspath(IProject project) throws CoreException {
    // create new launch configuration just as container for parameters, we are not going to save it
    ILaunchConfigurationWorkingCopy launchConfiguration;
    {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType launchConfigurationType =
          launchManager.getLaunchConfigurationType(Constants.LAUNCH_TYPE_ID_SHELL);
      launchConfiguration = launchConfigurationType.newInstance(null, "__GWTDesigner_tmp");
    }
    // set project name
    launchConfiguration.setAttribute(
        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
        project.getName());
    // create delegate just for asking classpath
    GwtLaunchConfigurationDelegate delegate = new GwtLaunchConfigurationDelegate();
    return delegate.getClasspathAll(launchConfiguration);
  }
}
