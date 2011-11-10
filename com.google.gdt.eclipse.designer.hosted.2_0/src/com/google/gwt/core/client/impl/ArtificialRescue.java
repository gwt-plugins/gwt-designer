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
package com.google.gwt.core.client.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An annotation to specify that if a class is rescued, other types, methods, or
 * fields should be rescued as well. This annotation is an implementation detail
 * of the deRPC code and its use by third parties is not supported.
 */
@Target(ElementType.TYPE)
public @interface ArtificialRescue {
  /**
   * Specifies the elements of a single type to rescue.
   */
  @Target(value = {})
  public @interface Rescue {
    /**
     * The class to be retained. Primitive array types should be referenced
     * using their JSNI type name.
     */
    String className();

    boolean instantiable() default false;

    /**
     * Fields are specified as raw names. That is, <code>fieldName</code>.
     */
    String[] fields() default {};

    /**
     * Methods are specified as unqualified JSNI signatures. That is,
     * <code>methodName(Lsome/Type;...)</code>.
     */
    String[] methods() default {};
  }

  Rescue[] value();
}
