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

import java.lang.annotation.Annotation;

/**
 * Interface implemented by elements that can have annotations. This interface
 * is a departure for GWT in that it used types declared in the
 * java.lang.annotation package instead of types declared as part of this
 * typeinfo package. This reflects a compromise between a pure
 * {@link TypeOracle} model and one that is more useful to developers.
 */
public interface HasAnnotations {
  /**
   * Returns an instance of the specified annotation type if it is present on
   * this element or <code>null</code> if it is not.
   * 
   * @param annotationClass annotation type to search for
   * @return instance of the specified annotation type if it is present on this
   *         element or <code>null</code> if it is not
   */
  <T extends Annotation> T getAnnotation(Class<T> annotationClass);

  /**
   * Returns <code>true</code> if this item has an annotation of the specified
   * type.
   * 
   * @param annotationClass
   * 
   * @return <code>true</code> if this item has an annotation of the specified
   *         type
   */
  boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
}
