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
package com.google.gdt.eclipse.designer.util.resources;

import java.io.InputStream;
import java.util.List;

/**
 * Abstract accessor for classes and source in {@link GwtResourcesProvider}.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.resources
 */
interface IResourcesLoader {
  /**
   * Frees any resources used by this {@link IResourcesLoader}.
   */
  void dispose();

  /**
   * @return the {@link InputStream} for resource on given path.
   */
  InputStream getResourceAsStream(String path) throws Exception;

  /**
   * Appends files located in subtree of given path, using given file as root.
   */
  void appendFiles(List<String> files, String path) throws Exception;
}