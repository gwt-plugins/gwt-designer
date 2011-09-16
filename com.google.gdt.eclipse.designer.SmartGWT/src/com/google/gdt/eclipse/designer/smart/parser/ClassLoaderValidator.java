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
package com.google.gdt.eclipse.designer.smart.parser;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.IClassLoaderValidator;
import com.google.gdt.eclipse.designer.smart.IExceptionConstants;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.lang.ArrayUtils;

/**
 * {@link IClassLoaderValidator} for SmartGWT.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT
 */
public final class ClassLoaderValidator implements IClassLoaderValidator {
  private static final String[] VALID_VERSIONS = {"2.4", "2.5"};
  private static final String VALID_VERSIONS_STRING = "2.4, 2.5";

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClassLoaderValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(IJavaProject javaProject, GwtState state) throws Exception {
    if (hasSmartGWT(javaProject, state)) {
      ClassLoader classLoader = state.getClassLoader();
      // version
      {
        String description = getWrongVersionDescription(classLoader);
        if (description != null) {
          throw new DesignerException(IExceptionConstants.INCORRECT_VERSION,
              VALID_VERSIONS_STRING,
              description);
        }
      }
    }
  }

  /**
   * @return <code>true</code> if given {@link GwtState} has access to SmartGWT.
   */
  private static boolean hasSmartGWT(IJavaProject javaProject, GwtState state) {
    if (ProjectUtils.hasType(javaProject, "com.smartgwt.client.widgets.Canvas")) {
      try {
        state.getClassLoader().loadClass("com.smartgwt.client.Version");
        return true;
      } catch (ClassNotFoundException e) {
      }
    }
    return false;
  }

  /**
   * @return the description of invalid version, or <code>null</code> if version in valid.
   */
  private static String getWrongVersionDescription(final ClassLoader classLoader) {
    return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        Class<?> classVersion = classLoader.loadClass("com.smartgwt.client.Version");
        String version = (String) ReflectionUtils.invokeMethod(classVersion, "getVersion()");
        if (ArrayUtils.contains(VALID_VERSIONS, version)) {
          initializeSmartGWT(classLoader);
          return null;
        }
        return version;
      }
    }, "See logged exception");
  }

  /**
   * Initialize SmartGWT environment.
   */
  private static void initializeSmartGWT(final ClassLoader classLoader) throws Exception {
    // try process SmartGWT initialization routine
    try {
      // SmartGwtEntryPoint for version 2.3
      Class<?> smartGWTEntryPointClass =
          classLoader.loadClass("com.smartgwt.client.SmartGwtEntryPoint");
      Object smartGWTEntryPoint = smartGWTEntryPointClass.newInstance();
      ReflectionUtils.invokeMethod2(smartGWTEntryPoint, "onModuleLoad");
    } catch (ClassNotFoundException e) {
      // do nothing
    }
  }
}