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
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.StandardGeneratorContext;

/**
 * Abstract the process of determining all of the possible deferred binding
 * answers for a given type.
 */
public interface RebindPermutationOracle {

  /**
   * Called when the compiler is done with this oracle, so memory can be freed
   * up. After calling this method, the only legal method to call is
   * {@link #getAllPossibleRebindAnswers}.
   */
  void clear();

  /**
   * Always answers with at least one name.
   */
  String[] getAllPossibleRebindAnswers(TreeLogger logger, String sourceTypeName)
      throws UnableToCompleteException;

  /**
   * Returns the CompilationState.
   */
  CompilationState getCompilationState();

  /**
   * Returns the StandardGeneratorContext.
   */
  StandardGeneratorContext getGeneratorContext();
}
