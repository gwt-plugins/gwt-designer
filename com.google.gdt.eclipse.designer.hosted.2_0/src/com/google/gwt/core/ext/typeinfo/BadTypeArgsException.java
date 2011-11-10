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
package com.google.gwt.core.ext.typeinfo;

/**
 * Thrown when an invalid type is specified as part of a
 * <code>gwt.typeArgs</code> tag within a doc comment.
 */
public class BadTypeArgsException extends TypeOracleException {

  public BadTypeArgsException(String message) {
    super(message);
  }

  public BadTypeArgsException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadTypeArgsException(Throwable cause) {
    super(cause);
  }

}
