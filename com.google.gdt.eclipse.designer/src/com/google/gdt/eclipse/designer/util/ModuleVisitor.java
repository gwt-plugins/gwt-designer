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
package com.google.gdt.eclipse.designer.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.model.module.InheritsElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.module.PublicElement;
import com.google.gdt.eclipse.designer.model.module.SourceElement;
import com.google.gdt.eclipse.designer.model.module.SuperSourceElement;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * A visitor for GWT modules structure.
 * 
 * @author scheglov_ke
 * @coverage gwt.util
 */
public abstract class ModuleVisitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  private IResourcesProvider m_resourcesProvider;

  /**
   * @return the {@link IResourcesProvider} that is used for visiting modules.
   */
  public final IResourcesProvider getResourcesProvider() {
    return m_resourcesProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitor interface
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Enters into module.
   * 
   * @return <code>true</code> if this module should be visited.
   */
  public boolean visitModule(ModuleElement module) {
    return true;
  }

  /**
   * Leaves module.
   */
  public void endVisitModule(ModuleElement module) {
  }

  /**
   * Visits "source" package of module.
   * 
   * @param module
   *          the module to which belongs "source" package.
   * @param packageName
   *          the qualified name of "source" package.
   * @param superSource
   *          is <code>true</code> if this source package comes from <code>super-source</code> tag
   *          and <code>false</code> if from usual <code>source</code> tag.
   */
  public void visitSourcePackage(ModuleElement module, String packageName, boolean superSource)
      throws Exception {
  }

  /**
   * Visits "public" package of module.
   * 
   * @param module
   *          the module to which belongs "public" package.
   * @param packageName
   *          the qualified name of "public" package.
   */
  public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits public folders of given module and inherited modules (recursively).
   */
  public static void accept(ModuleDescription moduleDescription, ModuleVisitor visitor)
      throws Exception {
    IResourcesProvider resourcesProvider = moduleDescription.getResourcesProvider();
    visitor.m_resourcesProvider = resourcesProvider;
    {
      String moduleId = moduleDescription.getId();
      accept(resourcesProvider, Sets.<String>newTreeSet(), moduleId, visitor);
    }
  }

  /**
   * Visit modules recursively.
   */
  private static void accept(IResourcesProvider resources,
      Set<String> visitedModules,
      String moduleName,
      ModuleVisitor visitor) throws Exception {
    // check that module is not from gwt-dev-xxx.jar
    if (moduleName.startsWith("com.google.gwt.dev")) {
      return;
    }
    // check, may be we already visited this module
    if (visitedModules.contains(moduleName)) {
      return;
    }
    visitedModules.add(moduleName);
    // prepare module
    ModuleElement module;
    {
      String moduleResourceName = moduleName.replace('.', '/') + ".gwt.xml";
      // prepare stream
      InputStream is = resources.getResourceAsStream(moduleResourceName);
      Assert.isTrueException(is != null, IExceptionConstants.NO_MODULE, moduleName);
      // read module
      module = Utils.readModule(moduleName, is);
    }
    // start visit module
    if (!visitor.visitModule(module)) {
      return;
    }
    String modulePackageName = CodeUtils.getPackage(moduleName);
    // visit "source" packages
    {
      List<SuperSourceElement> superSourceElements = module.getSuperSourceElements();
      List<SourceElement> sourceElements = module.getSourceElements();
      // visit explicit
      for (SuperSourceElement sourceElement : superSourceElements) {
        visitSourcePackage(visitor, module, modulePackageName, sourceElement.getPath(), true);
      }
      for (SourceElement sourceElement : sourceElements) {
        visitSourcePackage(visitor, module, modulePackageName, sourceElement.getPath(), false);
      }
      // no any source elements, use default
      if (superSourceElements.isEmpty() && sourceElements.isEmpty()) {
        visitSourcePackage(visitor, module, modulePackageName, "client", false);
      }
    }
    // visit "public" folders
    {
      // prepare folders
      List<PublicElement> publicElements = module.getPublicElements();
      if (publicElements.isEmpty()) {
        PublicElement defaultPublicElement = new PublicElement();
        defaultPublicElement.setPath("public");
        publicElements = ImmutableList.of(defaultPublicElement);
      }
      // visit folders
      for (PublicElement publicElement : publicElements) {
        String packageName = modulePackageName + "." + publicElement.getPath().replace('/', '.');
        visitor.visitPublicPackage(module, packageName);
      }
    }
    // try inherited modules
    {
      List<InheritsElement> inheritsElements = module.getInheritsElements();
      for (InheritsElement inheritsElement : inheritsElements) {
        String inheritsName = inheritsElement.getName();
        Assert.isTrueException(
            inheritsName != null,
            IExceptionConstants.INHERITS_NO_NAME,
            moduleName);
        accept(resources, visitedModules, inheritsName, visitor);
      }
    }
    // end visit modules
    visitor.endVisitModule(module);
  }

  /**
   * Visits single "source" package.
   * 
   * @param visitor
   *          the {@link ModuleVisitor} to visit.
   * @param module
   *          the module that has this "source" package.
   * @param modulePackageName
   *          the name of package in which module is located.
   * @param pathInModule
   *          the simple path to the "source" folder, as it is described in module file, may be
   *          <code>null</code> if <code>modulePackage</code> itself should be used as source folder
   *          (probably only for <code>super-source</code>).
   * @param superSource
   *          is <code>true</code> if this source package comes from <code>super-source</code> tag
   *          and <code>false</code> if from usual <code>source</code> tag.
   */
  private static void visitSourcePackage(ModuleVisitor visitor,
      ModuleElement module,
      String modulePackageName,
      String pathInModule,
      boolean superSource) throws Exception {
    if (pathInModule == null) {
      visitor.visitSourcePackage(module, modulePackageName, superSource);
    } else {
      visitor.visitSourcePackage(
          module,
          modulePackageName + "." + pathInModule.replace('/', '.'),
          superSource);
    }
  }
}
