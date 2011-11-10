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

import java.util.List;

/**
 * Default immutable implementation of ConfigurationProperty that receives its
 * values in its constructor.
 */
public class DefaultConfigurationProperty implements ConfigurationProperty {

  private final String name;
  private final List<String> values;

  /**
   * Construct a configuration property.
   *  
   * @param name the name of this property, must not be null
   * @param values the list of possible values, must not be null and
   *     will be returned to callers, so a copy should be passed into this
   *     ctor if the caller will use this set later
   */
  public DefaultConfigurationProperty(String name, List<String> values) {
    assert name != null;
    assert values != null;
    this.name = name;
    this.values = values;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DefaultConfigurationProperty other = (DefaultConfigurationProperty) obj;
    return name.equals(other.name)
        && values.equals(other.values);
  }

  public String getName() {
    return name;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + values.hashCode();
    return result;
  }
  
  @Override
  public String toString() {
    return "ConfigProp " + name + ": " + values.toString();
  }
}
