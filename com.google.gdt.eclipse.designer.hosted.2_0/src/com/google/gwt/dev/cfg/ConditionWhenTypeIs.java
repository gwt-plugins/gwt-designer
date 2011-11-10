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

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * A deferred binding condition to determine whether the type being rebound is
 * exactly a particular type.
 */
public class ConditionWhenTypeIs extends Condition {

  private final String exactTypeName;

  public ConditionWhenTypeIs(String exactTypeName) {
    this.exactTypeName = exactTypeName;
  }

  public String toString() {
    return "<when-type-is class='" + exactTypeName + "'/>";
  }

  protected boolean doEval(TreeLogger logger, PropertyOracle propertyOracle,
      TypeOracle typeOracle, String testType) {
    return exactTypeName.equals(testType);
  }

  protected String getEvalAfterMessage(String testType, boolean result) {
    if (result) {
      return "Yes, the requested type was an exact match";
    } else {
      return "Not an exact match";
    }
  }

  protected String getEvalBeforeMessage(String testType) {
    return toString();
  }
}
