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

package com.google.gwt.core.ext;

import java.util.SortedSet;

/**
 * A named deferred binding (property, value) pair for use in generators.
 * 
 * @see com.google.gwt.core.ext.linker.SelectionProperty A similarly-named
 * analog for linkers.
 */
public interface SelectionProperty {
  
  /**
   * The name of the property.
   * 
   * @return the property name as a String.
   * */
  String getName();

  /**
   * The value for the permutation currently being considered.
   * 
   * @return the property value as a String.
   */
  String getCurrentValue();

  /**
   * Gets the fallback value for the property.
   * @return the fallback, or ""
   */
  String getFallbackValue(); 

  /**
   * Returns the possible values for the property in sorted order.
   * 
   * @return a SortedSet of Strings containing the possible property values.
   */
  SortedSet<String> getPossibleValues();
}
