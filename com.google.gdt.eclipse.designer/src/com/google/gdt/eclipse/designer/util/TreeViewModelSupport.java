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

import org.eclipse.wb.internal.core.utils.reflect.ClassLoaderLocalMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Support evaluating <code>com.google.gwt.view.client.TreeViewModel</code> instances.
 * 
 * @author sablin_aa
 * @coverage gwt.util
 */
public final class TreeViewModelSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TreeViewModelSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return Fake <code>com.google.gwt.view.client.TreeViewModel</code> instance.
   */
  private static Object TREE_VIEW_MODEL_CLASS_KEY =
      "Fake com.google.gwt.view.client.TreeViewModel instance";

  public static Object getTreeViewModelFake(ClassLoader classLoader) throws Exception {
    Object fakeInstance = ClassLoaderLocalMap.get(classLoader, TREE_VIEW_MODEL_CLASS_KEY);
    if (fakeInstance == null) {
      // create fake instances
      fakeInstance = createTreeViewModelFake(classLoader);
      ClassLoaderLocalMap.put(classLoader, TREE_VIEW_MODEL_CLASS_KEY, fakeInstance);
    }
    return fakeInstance;
  }

  /**
   * @return new fake instance of <code>com.google.gwt.view.client.TreeViewModel</code>.
   */
  private static Object createTreeViewModelFake(ClassLoader classLoader) throws Exception {
    Class<?> treeViewModelClass = classLoader.loadClass("com.google.gwt.view.client.TreeViewModel");
    final Object fakeNodeInfo = getNodeInfoFake(classLoader);
    // create proxy
    return Proxy.newProxyInstance(
        classLoader,
        new Class<?>[]{treeViewModelClass},
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // intercept com.google.gwt.view.client.TreeViewModel.getNodeInfo(T)
            if (method.getName().equals("getNodeInfo")) {
              return fakeNodeInfo;
            }
            // intercept com.google.gwt.view.client.TreeViewModel.isLeaf(Object)
            if (method.getName().equals("isLeaf")) {
              return Boolean.FALSE;
            }
            // others
            return null;
          }
        });
  }

  /**
   * @return Fake instance of <code>com.google.gwt.view.client.TreeViewModel$DefaultNodeInfo</code>.
   */
  private static Object NODE_INFO_CLASS_KEY =
      "Fake com.google.gwt.view.client.TreeViewModel$DefaultNodeInfo instance";

  public static Object getNodeInfoFake(ClassLoader classLoader) throws Exception {
    Object fakeInstance = ClassLoaderLocalMap.get(classLoader, NODE_INFO_CLASS_KEY);
    if (fakeInstance == null) {
      // create fake instances
      fakeInstance = createNodeInfoFake(classLoader);
      ClassLoaderLocalMap.put(classLoader, NODE_INFO_CLASS_KEY, fakeInstance);
    }
    return fakeInstance;
  }

  /**
   * @return new fake instance of
   *         <code>com.google.gwt.view.client.TreeViewModel$DefaultNodeInfo</code>.
   */
  private static Object createNodeInfoFake(ClassLoader classLoader) throws Exception {
    Class<?> defaultNodeInfoClass =
        classLoader.loadClass("com.google.gwt.view.client.TreeViewModel$DefaultNodeInfo");
    Constructor<?> defaultNodeInfoConstructor =
        defaultNodeInfoClass.getConstructor(
            classLoader.loadClass("com.google.gwt.view.client.AbstractDataProvider"),
            classLoader.loadClass("com.google.gwt.cell.client.Cell"));
    Object fakeListDataProvider =
        classLoader.loadClass("com.google.gwt.view.client.ListDataProvider").newInstance();
    {
      Object fakeItemsList = ReflectionUtils.invokeMethod(fakeListDataProvider, "getList()");
      ReflectionUtils.invokeMethod(
          fakeItemsList,
          "add(java.lang.Object)",
          "TreeViewModel with sample content.");
    }
    Object fakeTextCell =
        classLoader.loadClass("com.google.gwt.cell.client.TextCell").newInstance();
    final Object fakeNodeInfo =
        defaultNodeInfoConstructor.newInstance(fakeListDataProvider, fakeTextCell);
    return fakeNodeInfo;
  }
}
