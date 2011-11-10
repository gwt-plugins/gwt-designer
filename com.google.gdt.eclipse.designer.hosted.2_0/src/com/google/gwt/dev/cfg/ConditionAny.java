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
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.util.Iterator;

/**
 * A compound condition that is satisfied if any of its children are satisfied.
 */
public class ConditionAny extends CompoundCondition {

  public ConditionAny() {
  }

  protected boolean doEval(TreeLogger logger, PropertyOracle propertyOracle,
      TypeOracle typeOracle, String testType) throws UnableToCompleteException {
    for (Iterator<Condition> iter = getConditions().iterator(); iter.hasNext();) {
      Condition condition = iter.next();
      if (condition.isTrue(logger, propertyOracle, typeOracle, testType)) {
        return true;
      }
    }
    return false;
  }

  protected String getEvalAfterMessage(String testType, boolean result) {
    if (result) {
      return "Yes: One or more subconditions was true";
    } else {
      return "No: All subconditions were false";
    }
  }

  protected String getEvalBeforeMessage(String testType) {
    return "Checking if any subcondition is true (<any>)";
  }
}
