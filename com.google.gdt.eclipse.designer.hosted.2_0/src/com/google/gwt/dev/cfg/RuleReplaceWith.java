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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.StandardGeneratorContext;

/**
 * A rule to replace the type being rebound with an explicitly named class.
 */
public class RuleReplaceWith extends Rule {

  private final String replacementTypeName;

  public RuleReplaceWith(String typeName) {
    this.replacementTypeName = typeName;
  }

  public String getReplacementTypeName() {
    return replacementTypeName;
  }

  public String realize(TreeLogger logger, StandardGeneratorContext context,
      String typeName) throws UnableToCompleteException {
    return replacementTypeName;
  }

  public String toString() {
    return "<replace-with class='" + replacementTypeName + "'/>";
  }
}
