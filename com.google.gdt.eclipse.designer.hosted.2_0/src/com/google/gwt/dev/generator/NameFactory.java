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
package com.google.gwt.dev.generator;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 * Generates unqiue identifiers. Use this class to avoid generating conflicting
 * names with user code. This class isn't smart enough to know about scopes
 * (which isn't generally a problem for generators in any case).
 */
public class NameFactory {

  private final Set<String> usedNames = new HashSet<String>();

  /**
   * Creates a new <code>NameFactory</code> that knows about
   * <code>existingNames</code>.
   * 
   * @param existingNames a list of names that may be <code>null</code>.
   */
  public NameFactory(Collection<String> existingNames) {
    if (existingNames == null) {
      return;
    }
    usedNames.addAll(existingNames);
  }

  /**
   * Creates a new <code>NameFactory</code> that doesn't know about any
   * existing names.
   */
  public NameFactory() {
    this(null);
  }

  /**
   * Adds a name to the set of already known identifiers. Has no affect if the
   * name is already considered an existing identifier.
   * 
   * @param name a not <code>null</code> name
   */
  public void addName(String name) {
    usedNames.add(name);
  }

  /**
   * Creates a new unique name based off of <code>name</code> and adds it to
   * the list of known names.
   * 
   * @param name a not <code>null</code> name to base the new unique name from
   * @return a new unique, not <code>null</code> name. This name may be
   *         possibly identical to <code>name</code>.
   */
  public String createName(String name) {
    String newName = name;

    for (int count = 0; true; ++count) {
      if (usedNames.contains(newName)) {
        newName = name + count;
      } else {
        return newName;
      }
    }
  }
}
