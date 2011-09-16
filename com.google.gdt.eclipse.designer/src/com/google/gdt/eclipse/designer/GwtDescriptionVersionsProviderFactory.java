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
package com.google.gdt.eclipse.designer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.model.description.resource.FromListDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;

import org.eclipse.jdt.core.IJavaProject;

import java.util.List;
import java.util.Map;

/**
 * {@link IDescriptionVersionsProviderFactory} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public final class GwtDescriptionVersionsProviderFactory
    implements
      IDescriptionVersionsProviderFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IDescriptionVersionsProviderFactory INSTANCE =
      new GwtDescriptionVersionsProviderFactory();

  private GwtDescriptionVersionsProviderFactory() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProviderFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader) {
    return ImmutableMap.of();
  }

  public IDescriptionVersionsProvider getProvider(IJavaProject javaProject, ClassLoader classLoader) {
    if (!Utils.isGWTProject(javaProject)) {
      return null;
    }
    // OK, GWT project
    String version = Utils.getVersion(javaProject).getStringMajorMinor();
    List<String> allVersions = ImmutableList.of("2.0", "2.1", "2.2");
    return new FromListDescriptionVersionsProvider(allVersions, version) {
      @Override
      protected boolean validate(Class<?> componentClass) throws Exception {
        String className = componentClass.getName();
        return className.startsWith("com.google.gwt.user.");
      }
    };
  }
}
