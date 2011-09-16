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
package com.google.gdt.eclipse.designer.smart.model.support;

import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link HierarchyProvider} for GWT.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public final class SmartGwtHierarchyProvider extends HierarchyProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final HierarchyProvider INSTANCE = new SmartGwtHierarchyProvider();

  private SmartGwtHierarchyProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // HierarchyProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object[] getChildrenObjects(Object object) throws Exception {
    // com.smartgwt.client.widgets.layout.Layout
    if (isUserClass(object, "com.smartgwt.client.widgets.layout.Layout")) {
      return (Object[]) ReflectionUtils.invokeMethod(object, "getMembers()");
    }
    // com.smartgwt.client.widgets.Canvas
    /* failed for TabSet & probably some others
    if (isUserClass(object, "com.smartgwt.client.widgets.Canvas")) {
    	return (Object[]) ReflectionUtils.invokeMethod(object, "getChildren()");
    }*/
    // unknown
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given object is successor of given class.
   */
  private static boolean isUserClass(Object o, String className) {
    return o != null && ReflectionUtils.isSuccessorOf(o.getClass(), className);
  }
}
