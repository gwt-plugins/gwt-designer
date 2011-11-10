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
package com.google.gdt.eclipse.designer.wizards.model.common;

import com.google.gdt.eclipse.designer.wizards.Activator;
import com.google.gdt.eclipse.designer.wizards.WizardUtils;

import org.eclipse.wb.internal.core.utils.IOUtils2;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Abstract operation for creating any GWT object using templates.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.operation
 */
public abstract class AbstractCreateOperation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates sub package with given name in target {@link IPackageFragment}.
   */
  protected IPackageFragment getPackage(IPackageFragmentRoot root, String packageName)
      throws JavaModelException {
    IPackageFragment packageFragment = root.getPackageFragment(packageName);
    if (!packageFragment.exists()) {
      packageFragment = root.createPackageFragment(packageName, false, new NullProgressMonitor());
    }
    return packageFragment;
  }

  /**
   * Creates file with name 'targetName' in package using given template name and variables map.
   */
  protected static IFile createFileFromTemplate(IPackageFragment targetPackage,
      String targetName,
      String templateName,
      Map<String, String> variables) throws Exception {
    return createFileFromTemplate(
        (IFolder) targetPackage.getResource(),
        targetName,
        templateName,
        variables);
  }

  /**
   * Creates file with name 'targetName' in 'targetFolder' using given template name and variables
   * map.
   */
  protected static IFile createFileFromTemplate(IFolder targetFolder,
      String targetName,
      String templateName,
      Map<String, String> variables) throws Exception {
    IProject project = targetFolder.getProject();
    // prepare template
    String template = getTemplateContent(project, templateName);
    template = toSystemEOL(template);
    // replace %variable% with variable values
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      template = StringUtils.replace(template, "%" + name + "%", value);
    }
    // create file
    return createFile(targetFolder, targetName, template.getBytes());
  }

  /**
   * Copy template files from given source folder name to given target folder. We use it for example
   * for coping images.
   */
  protected static void copyTemplateFiles(IFolder targetFolder, String sourceFolderName)
      throws Exception {
    IProject project = targetFolder.getProject();
    String templatePath = getModelTemplatePath(project) + sourceFolderName;
    String[] paths = Activator.getEntriesPaths(templatePath);
    for (String path : paths) {
      // prepare file bytes
      byte[] bytes;
      {
        InputStream is = Activator.getFile(path);
        bytes = IOUtils2.readBytes(is);
      }
      // create file using bytes
      {
        String targetFileName = new Path(path).lastSegment();
        createFile(targetFolder, targetFileName, bytes);
      }
    }
  }

  /**
   * @return the text converted to use system line separator.
   */
  private static String toSystemEOL(String text) throws Exception {
    StringWriter stringWriter = new StringWriter();
    BufferedReader br = new BufferedReader(new StringReader(text));
    while (true) {
      String line = br.readLine();
      if (line == null) {
        break;
      }
      stringWriter.write(line);
      stringWriter.write(SystemUtils.LINE_SEPARATOR);
    }
    return stringWriter.toString();
  }

  /**
   * Creates file with name 'targetName' in package using given source.
   */
  protected static IFile createFile(IPackageFragment targetPackage, String targetName, String source)
      throws Exception {
    return createFile((IFolder) targetPackage.getResource(), targetName, source.getBytes());
  }

  /**
   * Create file with given name, folder and source.
   */
  private static IFile createFile(IFolder targetFolder, String targetName, byte[] buffer)
      throws Exception {
    IFile targetFile = targetFolder.getFile(targetName);
    IOUtils2.setFileContents(targetFile, new ByteArrayInputStream(buffer));
    return targetFile;
  }

  /**
   * Returns content of model template with given name.
   */
  private static String getTemplateContent(IProject project, String templateName)
      throws IOException {
    String path = getModelTemplatePath(project) + templateName;
    InputStream is = Activator.getFile(path);
    return IOUtils2.readString(is);
  }

  /**
   * @return the path of model templates.
   */
  protected static String getModelTemplatePath(IProject project) {
    return WizardUtils.getTemplatePath(project) + "model/";
  }
}
