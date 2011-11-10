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
package com.google.gwt.dev.jjs;

import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.StatementRanges;

import java.io.Serializable;

/**
 * An extensible return type for the results of compiling a single permutation.
 */
public interface PermutationResult extends Serializable {
  /**
   * Returns any Artifacts that may have been created as a result of compiling
   * the permutation.
   */
  ArtifactSet getArtifacts();

  /**
   * The compiled JavaScript code as UTF8 bytes.
   */
  byte[][] getJs();

  /**
   * The ID of the permutation.
   */
  int getPermutationId();
  
  /**
   * The symbol map for the permutation.
   */
  byte[] getSerializedSymbolMap();

  /**
   * The statement ranges for the code returned by {@link #getJs()}.
   */
  StatementRanges[] getStatementRanges();
}
