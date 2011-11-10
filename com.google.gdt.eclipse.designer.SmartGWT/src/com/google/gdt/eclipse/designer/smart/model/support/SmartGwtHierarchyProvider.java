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
