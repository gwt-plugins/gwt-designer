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
package com.google.gwt.dev.util.xml;

import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Converts from a string to the type needed.
 */
public abstract class AttributeConverter {
  /**
   * Converts from a string to the type needed.
   * Does not throw conversion-related exceptions.
   * @param value
   *    the value to convert
   * @param schema
   *    used to report conversion problems
   * @return
   *    the argument converted to a form that is expected to compatible with
   *    the associated parameter and will work for a reflection "invoke()" call;
   *    <code>null</code> if the conversion failed.
   */
  public abstract Object convertToArg(Schema schema, int line,
      String elem, String attr, String value) throws UnableToCompleteException;
}
