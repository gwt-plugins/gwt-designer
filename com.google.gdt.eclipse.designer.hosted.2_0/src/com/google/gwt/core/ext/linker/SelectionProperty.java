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
package com.google.gwt.core.ext.linker;

import java.util.SortedSet;

/**
 * Represents a deferred binding property. The deferred binding property may or
 * may not have a single value applied across all permutations.
 * 
 * SelectionProperty implementations must support object identity comparisons.
 * 
 * @see com.google.gwt.core.ext.SelectionProperty A similarly-named interface
 *      used in generators.
 */
public interface SelectionProperty {
  /**
   * Returns the name of the deferred binding property.
   */
  String getName();

  /**
   * Returns all possible values for this deferred binding property.
   */
  SortedSet<String> getPossibleValues();

  /**
   * Returns a raw function body that provides the runtime value to be used for
   * a deferred binding property.
   */
  String getPropertyProvider();

  /**
   * Returns <code>true</code> if the value of the SelectionProperty is always
   * derived from other SelectionProperties and, as a consequence, the property
   * provider never needs to be evaluated.
   */
  boolean isDerived();

  /**
   * Returns the defined value for the deferred binding property or
   * <code>null</code> if the value of the property is not constant.
   * 
   * @see CompilationResult#getPropertyMap()
   */
  String tryGetValue();
}