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
package com.google.gwt.dev.resource.impl;

import com.google.gwt.core.ext.TreeLogger;

import java.util.Map;

/**
 * A location that acts as a starting point for finding resources
 * {@link ResourceOracleImpl}.
 */
public abstract class ClassPathEntry {

  /**
   * Finds every resource at abstract path P within this classpath such that P
   * begins with a prefix X from the path prefix set and P is allowed by the
   * filter associated with X.
   * 
   * @return a map with key as an allowed resource and value as the PathPrefix
   *         that allows the resource; note no guarantees are made regarding the
   *         identities of the returned resource objects, and the same object
   *         may be returned across multiple calls
   */
  public abstract Map<AbstractResource, PathPrefix> findApplicableResources(
      TreeLogger logger, PathPrefixSet pathPrefixSet);

  /**
   * Gets a URL string that describes this class path entry.
   */
  public abstract String getLocation();

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + getLocation();
  }
}
