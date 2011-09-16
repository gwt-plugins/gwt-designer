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
