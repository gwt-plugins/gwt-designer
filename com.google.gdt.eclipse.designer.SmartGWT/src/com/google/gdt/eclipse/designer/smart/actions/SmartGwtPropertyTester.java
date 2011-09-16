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
package com.google.gdt.eclipse.designer.smart.actions;

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
 * {@link PropertyTester} for SmartGWT specific properties.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.actions
 */
public class SmartGwtPropertyTester extends PropertyTester {
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
        boolean hasJar = javaProject.findType("com.smartgwt.client.widgets.BaseWidget") != null;
        if (hasJar) {
          IJavaElement pkg = element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
          if (pkg != null) {
            IResource resource = pkg.getUnderlyingResource();
            ModuleDescription module = Utils.getSingleModule(resource);
            return module != null && Utils.inheritsModule(module, "com.smartgwt.SmartGwt");
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
