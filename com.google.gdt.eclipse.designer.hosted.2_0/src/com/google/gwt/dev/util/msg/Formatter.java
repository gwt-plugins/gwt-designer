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
package com.google.gwt.dev.util.msg;

/**
 * Abstract formatter used by {@link Message}.
 */
public abstract class Formatter {

  /**
   * Transforms the specified object into a string format.
   * 
   * @param toFormat the object to format; the caller is responsible for 
   * ensuring that the type of the passed-in object is the type expected 
   * by the subclass
   * 
   * @throws ClassCastException if <code>toFormat</code> is not of the type expected by the subclass
   */
  public abstract String format(Object toFormat);
}
