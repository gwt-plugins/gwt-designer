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
package com.google.gwt.dev.jdt;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Abstracts the implementation of making deferred binding decisions.
 */
public interface RebindOracle {

  /**
   * Determines which type should be substituted for the requested type. The
   * caller must ensure that the result type is instantiable.
   * 
   * @return the substitute type name, which may be the requested type itself;
   *         this method must not return <code>null</code> if sourceTypeName
   *         is not <code>null</code>
   */
  String rebind(TreeLogger logger, String sourceTypeName)
      throws UnableToCompleteException;
}
