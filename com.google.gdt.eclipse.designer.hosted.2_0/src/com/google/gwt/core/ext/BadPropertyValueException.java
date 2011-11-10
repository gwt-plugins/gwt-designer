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

/**
 * Thrown when a deferred binding property contains an invalid value.
 */
public class BadPropertyValueException extends Exception {

  private final String badValue;

  private final String propName;
  
  public BadPropertyValueException(String propName) {
    super("Missing property '" + propName + "' was not specified");

    this.propName = propName;
    this.badValue = "<null>";
  }

  public BadPropertyValueException(String propName, String badValue) {
    super("Property '" + propName + "' cannot be set to unexpected value '"
      + badValue + "'");

    this.propName = propName;
    this.badValue = badValue;
  }

  String getBadValue() {
    return badValue;
  }
  String getPropName() {
    return propName;
  }
}
