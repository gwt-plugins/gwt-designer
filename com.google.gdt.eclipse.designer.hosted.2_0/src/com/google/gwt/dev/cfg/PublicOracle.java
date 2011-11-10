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
package com.google.gwt.dev.cfg;

import com.google.gwt.dev.resource.Resource;

/**
 * Abstracts the process of querying for public files.
 * 
 * @deprecated with no replacement, just use {@link ModuleDef} directly
 */
@Deprecated
public interface PublicOracle {

  /**
   * Finds a file on the public path.
   * 
   * @param partialPath a file path relative to the root of any public package
   * @return the url of the file, or <code>null</code> if no such file exists
   */
  Resource findPublicFile(String partialPath);

  /**
   * Returns all available public files.
   * 
   * @return an array containing the partial path to each available public file
   */
  String[] getAllPublicFiles();
}
