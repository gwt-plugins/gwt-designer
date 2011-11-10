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
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Util;

/**
 * A deferred binding condition to determine whether the type being rebound is
 * assignment-compatible with a particular type.
 */
public class ConditionWhenTypeAssignableTo extends Condition {

  private final String assignableToTypeName;

  public ConditionWhenTypeAssignableTo(String assignableToTypeName) {
    this.assignableToTypeName = assignableToTypeName;
  }

  public String getAssignableToTypeName() {
    return assignableToTypeName;
  }

  public String toString() {
    return "<when-assignable class='" + assignableToTypeName + "'/>";
  }

  protected boolean doEval(TreeLogger logger, PropertyOracle propertyOracle,
      TypeOracle typeOracle, String testType) throws UnableToCompleteException {
    JClassType fromType = typeOracle.findType(testType);
    if (fromType == null) {
      Util.logMissingTypeErrorWithHints(logger, testType);
      throw new UnableToCompleteException();
    }

    JClassType toType = typeOracle.findType(assignableToTypeName);
    if (toType == null) {
      // If we don't know the type, it can't be assignable to it.
      // This isn't a strict failure case because stale rules can reference
      // types that have been deleted.
      //
      logger.log(TreeLogger.WARN, "Unknown type '" + assignableToTypeName
          + "' specified in deferred binding rule", null);
      return false;
    }

    if (fromType.isAssignableTo(toType)) {
      return true;
    } else {
      return false;
    }
  }

  protected String getEvalAfterMessage(String testType, boolean result) {
    if (result) {
      return "Yes, the requested type was assignable";
    } else {
      return "No, the requested type was not assignable";
    }
  }

  protected String getEvalBeforeMessage(String testType) {
    return toString();
  }

}
