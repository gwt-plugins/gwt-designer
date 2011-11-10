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
package com.google.gdt.eclipse.designer.wizards.model.mvp;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractCreateOperation;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation for creating new GWT MVP view, for GWT 2.1+
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.operation
 */
public class CreateViewOperation extends AbstractCreateOperation {
  private final IPackageFragmentRoot root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateViewOperation(IPackageFragmentRoot root) {
    this.root = root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class ViewConfiguration {
    public abstract String getViewPackageName();

    public abstract String getViewName();

    public abstract boolean isUseJavaTemplate();

    public abstract String getPlacePackageName();

    public abstract String getPlaceName();

    public abstract String getActivityPackageName();

    public abstract String getActivityName();

    public abstract String getClientFactoryPackageName();

    public abstract String getClientFactoryName();

    public Map<String, String> getVariables() {
      Map<String, String> variables = new HashMap<String, String>();
      // names
      variables.put("viewName", getViewName());
      variables.put("viewPackageName", getViewPackageName());
      variables.put("placeName", getPlaceName());
      variables.put("placePackageName", getPlacePackageName());
      variables.put("activityName", getActivityName());
      variables.put("activityPackageName", getActivityPackageName());
      variables.put("clientFactoryName", getClientFactoryName());
      variables.put("clientFactoryPackageName", getClientFactoryPackageName());
      // templates
      variables.put("activityTemplatePath", "mvp/ViewActivity");
      return variables;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void create(ViewConfiguration configuration) throws Exception {
    createViewInterface(configuration);
    createViewImplementation(configuration);
    if (configuration.getPlaceName() != null) {
      createPlace(configuration);
    }
    if (configuration.getActivityName() != null) {
      createActivity(configuration);
    }
  }

  /**
   * Create View interface.
   */
  private IFile createViewInterface(ViewConfiguration configuration) throws Exception {
    String className = configuration.getViewName();
    String packageName = configuration.getViewPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    variables.put("packageName", packageName);
    // create files from templates
    return createFileFromTemplate(packageFragment, className + ".java", "mvp/View.java", variables);
  }

  /**
   * Create View implementation.
   */
  private IFile createViewImplementation(ViewConfiguration configuration) throws Exception {
    String className = configuration.getViewName() + "Impl";
    String packageName = configuration.getViewPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    variables.put("packageName", packageName);
    // create files from templates
    IFile file;
    if (configuration.isUseJavaTemplate()) {
      // Java template
      file =
          createFileFromTemplate(
              packageFragment,
              className + ".java",
              "mvp/ViewImpl.java",
              variables);
    } else {
      // UiBinder template
      createFileFromTemplate(
          packageFragment,
          className + ".ui.xml",
          "mvp/ViewImpl.ui.xml",
          variables);
      file =
          createFileFromTemplate(
              packageFragment,
              className + ".java",
              "mvp/ViewImpl.ui.java",
              variables);
    }
    return file;
  }

  /**
   * Create Place.
   */
  private IFile createPlace(ViewConfiguration configuration) throws Exception {
    String className = configuration.getPlaceName();
    String packageName = configuration.getPlacePackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // ensure module inherits
    {
      ModuleDescription module = Utils.getSingleModule(packageFragment);
      ConfigureMvpOperation.ensureInheritsPlace(module);
    }
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    variables.put("packageName", packageName);
    // create files from templates
    return createFileFromTemplate(
        packageFragment,
        className + ".java",
        "mvp/ViewPlace.java",
        variables);
  }

  /**
   * Create Activity.
   */
  private IFile createActivity(ViewConfiguration configuration) throws Exception {
    String className = configuration.getActivityName();
    String packageName = configuration.getActivityPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // ensure module inherits
    {
      ModuleDescription module = Utils.getSingleModule(packageFragment);
      ConfigureMvpOperation.ensureInheritsActivity(module);
    }
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    variables.put("packageName", packageName);
    String clientFactoryClassName = configuration.getClientFactoryName();
    boolean useClientFactory = !StringUtils.isEmpty(clientFactoryClassName);
    if (useClientFactory) {
      // modify ClientFactory
      String clientFactoryPackageName = configuration.getClientFactoryPackageName();
      String viewName = configuration.getViewName();
      String viewPackageName = configuration.getViewPackageName();
      String viewClassName = viewPackageName + "." + viewName;
      String methodSignature = viewClassName + " get" + viewName + "()";
      // prepare AstEditor
      IPackageFragment factoryPackageFragment = getPackage(root, clientFactoryPackageName);
      ICompilationUnit factoryUnit =
          factoryPackageFragment.getCompilationUnit(clientFactoryClassName + ".java");
      AstEditor editor = new AstEditor(factoryUnit);
      TypeDeclaration factoryPrimaryType = editor.getPrimaryType();
      BodyDeclarationTarget bodyDeclarationTarget =
          new BodyDeclarationTarget(factoryPrimaryType, false);
      // modifying ...
      MethodDeclaration methodDeclaration;
      if (factoryPrimaryType.isInterface()) {
        // interface
        String methodHeader = "public " + methodSignature + ";";
        methodDeclaration = editor.addMethodDeclaration(methodHeader, null, bodyDeclarationTarget);
      } else if (AstNodeUtils.isAbstract(factoryPrimaryType)) {
        // abstract class
        String methodHeader = "public abstract " + methodSignature + ";";
        methodDeclaration = editor.addMethodDeclaration(methodHeader, null, bodyDeclarationTarget);
      } else {
        // regular class
        String methodHeader = "public " + methodSignature;
        final List<String> lines = Lists.newArrayList("return null; // FIXME");
        methodDeclaration = editor.addMethodDeclaration(methodHeader, lines, bodyDeclarationTarget);
      }
      editor.resolveImports(methodDeclaration);
      editor.saveChanges(true);
      // create file from templates
      IFile file =
          createFileFromTemplate(
              packageFragment,
              className + ".java",
              "mvp/ViewActivity.java",
              variables);
      // store PersistentProperty for ClientFactory
      ViewComposite.setClientFactoryName(
          getPackage(root, viewPackageName),
          clientFactoryPackageName + "." + clientFactoryClassName);
      return file;
    } else {
      // create file from templates
      return createFileFromTemplate(
          packageFragment,
          className + ".java",
          "mvp/ViewActivity0.java",
          variables);
    }
  }
}
