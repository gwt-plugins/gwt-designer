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
package com.google.gdt.eclipse.designer.common;

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

import java.text.MessageFormat;

/**
 * {@link PropertyTester} for GWT specific properties. It is used to determine enablement for
 * actions/participants, for example for creation or launching.
 * 
 * @author scheglov_ke
 * @coverage gwt.common
 */
public class GwtPropertyTester extends PropertyTester {
  public static final String PROPERTY_IS_ENTRY_POINT = "isEntryPoint";
  public static final String PROPERTY_IS_REMOTE_SERVICE = "isRemoteService";
  public static final String PROPERTY_IS_REMOTE_SERVICE_IMPL = "isRemoteServiceImpl";
  public static final String PROPERTY_IS_CLIENT_PACKAGE = "isClientPackage";
  public static final String PROPERTY_IS_GWT_PROJECT_ELEMENT = "isGwtProjectElement";
  /**
   * Checks is given {@link Object} is adaptable to {@link IResource}.
   */
  public static final String PROPERTY_IS_RESOURCE = "isResource";
  /**
   * Checks if given {@link IResource} is part of GWT module.
   */
  public static final String PROPERTY_IS_GWT_MODULE_ELEMENT = "isGwtModuleElement";

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertyTester
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean test(final Object receiver,
      final String property,
      Object[] args,
      Object expectedValue) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return testEx(receiver, property);
      }
    }, false);
  }

  /**
   * Tests property, can throw {@link Exception}.
   */
  private static boolean testEx(Object receiver, String property) throws Exception {
    // prepare IResource
    final IResource resource;
    if (receiver instanceof IAdaptable) {
      resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
    } else {
      resource = null;
    }
    // prepare IJavaElement
    final IJavaElement element;
    if (receiver instanceof IAdaptable) {
      element = (IJavaElement) ((IAdaptable) receiver).getAdapter(IJavaElement.class);
    } else {
      element = null;
    }
    // resources tests
    {
      if (PROPERTY_IS_GWT_MODULE_ELEMENT.equals(property)) {
        if (resource != null) {
          return Utils.getSingleModule(resource) != null;
        }
        return false;
      }
      if (PROPERTY_IS_RESOURCE.equals(property)) {
        return resource != null;
      }
    }
    // project tests
    {
      if (PROPERTY_IS_GWT_PROJECT_ELEMENT.equals(property)) {
        // resource selected
        if (resource != null && resource.exists()) {
          IProject project = resource.getProject();
          IJavaProject javaProject = JavaCore.create(project);
          return Utils.isGWTProject(javaProject);
        }
        // Java element selected
        if (element != null && element.exists()) {
          IJavaProject javaProject = (IJavaProject) element.getAncestor(IJavaElement.JAVA_PROJECT);
          return Utils.isGWTProject(javaProject);
        }
        // bad selection
        return false;
      }
    }
    // Java tests
    {
      // prepare java element
      if (element == null || !element.exists()) {
        return false;
      }
      // do tests
      if (PROPERTY_IS_CLIENT_PACKAGE.equals(property)) {
        IPackageFragment packageFragment =
            (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
        if (packageFragment != null) {
          return Utils.isModuleSourcePackage(packageFragment);
        }
        return false;
      }
      if (PROPERTY_IS_ENTRY_POINT.equals(property)) {
        return Utils.isEntryPoint(element);
      }
      if (PROPERTY_IS_REMOTE_SERVICE.equals(property)) {
        return Utils.isRemoteService(element);
      }
      if (PROPERTY_IS_REMOTE_SERVICE_IMPL.equals(property)) {
        return Utils.isRemoteServiceImpl(element);
      }
    }
    // unknown property
    throw new IllegalArgumentException(MessageFormat.format(
        "Illegal property '{0}' for '{1}'.",
        property,
        receiver));
  }
}
