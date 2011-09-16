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
package com.google.gdt.eclipse.designer.hosted;

import org.eclipse.jdt.core.IJavaProject;

import java.net.URL;
import java.util.List;

/**
 * Interface of GWT module for which we create hosted mode.
 * 
 * @author scheglov_ke
 * @coverage gwtHosted
 */
public interface IModuleDescription {
  /**
   * @return the {@link IJavaProject} which contains this module.
   */
  IJavaProject getJavaProject();

  /**
   * @return the locations of jars and directories which are classpath of this module.
   */
  List<String> getLocations() throws Exception;

  /**
   * @return the {@link URL}s which are classpath of this module.
   */
  URL[] getURLs() throws Exception;

  /**
   * @return the {@link ClassLoader} for classpath of this module.
   */
  ClassLoader getClassLoader() throws Exception;
}
