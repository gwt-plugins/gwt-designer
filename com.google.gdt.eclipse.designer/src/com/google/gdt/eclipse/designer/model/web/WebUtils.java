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
package com.google.gdt.eclipse.designer.model.web;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import java.io.StringReader;

/**
 * Utils for web.xml file.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.web
 */
public class WebUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Web folder
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of "web" folder.
   */
  public static String getWebFolderName(IJavaProject javaProject) {
    IProject project = javaProject.getProject();
    return getWebFolderName(project);
  }

  /**
   * @return the name of "web" folder.
   */
  public static String getWebFolderName(IProject project) {
    // WTP
    {
      String name = "WebContent";
      if (project.getFolder(name).exists()) {
        return name;
      }
    }
    // Maven
    {
      String name = "src/main/webapp";
      if (project.getFolder(new Path(name)).exists()) {
        return name;
      }
    }
    // default
    {
      String name = "war";
      if (project.getFolder(name).exists()) {
        return name;
      }
    }
    // configured
    return Activator.getStore().getString(Constants.P_WEB_FOLDER);
  }

  /**
   * @return the "web" folder.
   */
  public static IFolder getWebFolder(IJavaProject javaProject) {
    IProject project = javaProject.getProject();
    return getWebFolder(project);
  }

  /**
   * @return the "web" folder.
   */
  public static IFolder getWebFolder(IProject project) {
    // WTP
    {
      IFolder folder = project.getFolder("WebContent");
      if (folder.exists()) {
        return folder;
      }
    }
    // Maven
    {
      IFolder folder = project.getFolder(new Path("src/main/webapp"));
      if (folder.exists()) {
        return folder;
      }
    }
    // default
    {
      IFolder folder = project.getFolder("war");
      if (folder.exists()) {
        return folder;
      }
    }
    // configured
    {
      String defaultName = Activator.getStore().getString(Constants.P_WEB_FOLDER);
      return project.getFolder(defaultName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // web.xml
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Reads web.xml file.
   */
  public static WebAppElement readModule(IFile file) throws Exception {
    String contents = IOUtils2.readString(file);
    WebDocumentHandler documentHandler = new WebDocumentHandler();
    QParser.parse(new StringReader(contents), documentHandler);
    return documentHandler.getWebAppElement();
  }
}
