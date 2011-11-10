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
package com.google.gdt.eclipse.designer.gxt.actions;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import java.text.MessageFormat;

/**
 * {@link PropertyTester} for GXT specific properties.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.actions
 */
public class GxtPropertyTester extends PropertyTester {
  public static final String PROPERTY_IS_CONFIGURED = "isConfigured";

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
    // prepare IJavaElement
    final IJavaElement element;
    if (receiver instanceof IAdaptable) {
      element = (IJavaElement) ((IAdaptable) receiver).getAdapter(IJavaElement.class);
    } else {
      element = null;
    }
    // Java tests
    {
      // prepare java element
      if (element == null || !element.exists()) {
        return false;
      }
      // do tests
      if (PROPERTY_IS_CONFIGURED.equals(property)) {
        IJavaProject javaProject = element.getJavaProject();
        boolean hasJar = javaProject.findType("com.extjs.gxt.ui.client.widget.Component") != null;
        if (hasJar) {
          IJavaElement pkg = element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
          if (pkg != null) {
            IResource resource = pkg.getUnderlyingResource();
            ModuleDescription module = Utils.getSingleModule(resource);
            return module != null && Utils.inheritsModule(module, "com.extjs.gxt.ui.GXT");
          }
        }
        return false;
      }
    }
    // unknown property
    throw new IllegalArgumentException(MessageFormat.format(
        "Illegal property '{0}' for '{1}'.",
        property,
        receiver));
  }
}
