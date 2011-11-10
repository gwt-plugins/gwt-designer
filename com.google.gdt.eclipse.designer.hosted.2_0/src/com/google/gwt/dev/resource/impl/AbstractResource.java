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

import com.google.gwt.dev.resource.Resource;

/**
 * TODO(bruce): write me.
 */
public abstract class AbstractResource extends Resource {

  /**
   * Accesses the path root under which this resource was found. Only available
   * within this package.
   */
  public abstract ClassPathEntry getClassPathEntry();

  public abstract boolean isStale();
}

