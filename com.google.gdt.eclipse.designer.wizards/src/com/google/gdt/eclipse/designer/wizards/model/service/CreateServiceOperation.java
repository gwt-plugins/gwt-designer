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
package com.google.gdt.eclipse.designer.wizards.model.service;

import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.web.WebAppElement;
import com.google.gdt.eclipse.designer.model.web.WebDocumentEditContext;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractCreateOperation;

import org.eclipse.wb.internal.core.model.util.WorkspaceUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Operation for creating new GWT RemoteService.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.operation
 */
public class CreateServiceOperation extends AbstractCreateOperation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void create(IPackageFragment packageFragment, String serviceName) throws Exception {
    // prepare packages names
    String servicePackageName = packageFragment.getElementName();
    String serverPackageName = getServerPackageName(packageFragment);
    // prepare variables
    Map<String, String> variables = new HashMap<String, String>();
    variables.put("servicePackage", servicePackageName);
    variables.put("serviceName", serviceName);
    // client
    {
      // create RemoteService interface, "async" interface will be done by builder
      createFileFromTemplate(
          packageFragment,
          serviceName + ".java",
          "RemoteService.Service.java",
          variables);
      // open RemoteService in editor
      {
        String qualifiedServiceName = packageFragment.getElementName() + "." + serviceName;
        IType type =
            WorkspaceUtils.waitForType(packageFragment.getJavaProject(), qualifiedServiceName);
        JavaUI.openInEditor(type);
      }
    }
    // server: create implementation stub
    {
      // prepare server package
      IPackageFragment serverPackage;
      {
        IPackageFragmentRoot packageFragmentRoot =
            CodeUtils.getPackageFragmentRoot(packageFragment);
        serverPackage = packageFragmentRoot.createPackageFragment(serverPackageName, false, null);
      }
      // create implementation stub
      variables.put("serverPackage", serverPackageName);
      createFileFromTemplate(
          serverPackage,
          serviceName + "Impl.java",
          "RemoteService.ServiceImpl.java",
          variables);
    }
    // declare servlet
    addServlet_intoWebXML(packageFragment, serviceName, serverPackageName);
  }

  private void addServlet_intoWebXML(IPackageFragment packageFragment,
      String serviceName,
      String serverPackageName) throws CoreException, Exception {
    IProject project = packageFragment.getJavaProject().getProject();
    ModuleDescription moduleDescription = Utils.getSingleModule(packageFragment);
    ModuleElement module = Utils.readModule(moduleDescription);
    // update web.xml
    String webFolderName = WebUtils.getWebFolderName(project);
    IFile webFile = project.getFile(new Path(webFolderName + "/WEB-INF/web.xml"));
    WebDocumentEditContext context = new WebDocumentEditContext(webFile);
    try {
      WebAppElement moduleElement = context.getWebAppElement();
      // add new servlet definition
      {
        String servletClassName = serverPackageName + "." + serviceName + "Impl";
        String pattern = "/" + module.getName() + "/" + serviceName;
        // servlet
        com.google.gdt.eclipse.designer.model.web.ServletElement servletElement =
            new com.google.gdt.eclipse.designer.model.web.ServletElement();
        moduleElement.addChild(servletElement);
        servletElement.setName(serviceName);
        servletElement.setClassName(servletClassName);
        // servlet-mapping
        com.google.gdt.eclipse.designer.model.web.ServletMappingElement servletMappingElement =
            new com.google.gdt.eclipse.designer.model.web.ServletMappingElement();
        moduleElement.addChild(servletMappingElement);
        servletMappingElement.setName(serviceName);
        servletMappingElement.setPattern(pattern);
      }
      // commit modifications
      context.commit();
    } finally {
      context.disconnect();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Packages names
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * For given {@link IPackageFragment} in "source" package, returns {@link IPackageFragment} in
   * "server" package.
   */
  private String getServerPackageName(IPackageFragment sourcePackageFragment) throws Exception {
    // prepare information about module
    ModuleDescription moduleDescription = Utils.getSingleModule(sourcePackageFragment);
    String basePackageName = moduleDescription.getModulePackage().getElementName();
    String sourcePackageName = Utils.getRootSourcePackage(sourcePackageFragment).getElementName();
    // use same sub-package in "server" as sub-package in "client"
    String servicePackageName = sourcePackageFragment.getElementName();
    String serviceSubPackageName = servicePackageName.substring(sourcePackageName.length());
    return basePackageName + ".server" + serviceSubPackageName;
  }
}
