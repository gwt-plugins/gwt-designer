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
package com.google.gdt.eclipse.designer.util.resources;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for resources provider. It supports two main operations:
 * <ul>
 * <li>Reading resource as {@link InputStream}.</li>
 * <li>Listing of resources. We need this for image/CSS/HTML selection.</li>
 * <ul>
 * 
 * @author scheglov_ke
 * @coverage gwt.util.resources
 */
public interface IResourcesProvider {
  /**
   * Frees any allocated objects.
   */
  void dispose();

  /**
   * Returns an {@link InputStream} for reading the specified resource.
   * 
   * @param path
   *          the '/' separated path.
   */
  InputStream getResourceAsStream(String path) throws Exception;

  /**
   * Returns list of files in subtree of given path.
   * 
   * @param path
   *          the '/' separated path.
   */
  List<String> listFiles(String path) throws Exception;
}
