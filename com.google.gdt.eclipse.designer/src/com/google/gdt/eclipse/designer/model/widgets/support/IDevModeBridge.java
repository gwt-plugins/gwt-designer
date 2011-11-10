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
package com.google.gdt.eclipse.designer.model.widgets.support;

/**
 * Interface exposing some dev mode features.
 * 
 * @author mitin_aa
 */
public interface IDevModeBridge {
  /**
   * Finds a class or interface given its fully-qualified name.
   * 
   * @param name
   *          fully-qualified class/interface name - for nested classes, use its source name rather
   *          than its binary name (that is, use a "." rather than a "$")
   * 
   * @return <code>null</code> if the type is not found
   */
  Object findJType(String name);

  /**
   * @return the {@link ClassLoader} which is used for generators. It provides "dev" classes (such
   *         as <code>JType</code>) and some "user" classes (such as generators).
   */
  ClassLoader getDevClassLoader();

  /**
   * Invalidates the given type name, so the next rebind request will generate type again.
   */
  void invalidateRebind(String binderClassName);
}
