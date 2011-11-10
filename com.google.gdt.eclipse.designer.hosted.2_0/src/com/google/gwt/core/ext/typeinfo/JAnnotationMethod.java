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
import java.util.Map;

/**
 * Method declared on an annotation type.
 */
public class JAnnotationMethod extends JMethod {
  /**
   * Default value for this annotation element. <code>null</code> is not a valid
   * default value for an annotation element.
   */
  private final Object defaultValue;

  JAnnotationMethod(JClassType enclosingType, String name,
      Map<Class<? extends Annotation>, Annotation> declaredAnnotations,
      JTypeParameter[] jtypeParameters, Object defaultValue) {
    super(enclosingType, name, declaredAnnotations, jtypeParameters);
    this.defaultValue = defaultValue;
  }

  /**
   * Returns the default value for this annotation method or <code>null</code>
   * if there is not one.
   * 
   * @return default value for this annotation method or <code>null</code> if
   *         there is not one
   */
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public JAnnotationMethod isAnnotationMethod() {
    return this;
  }
}
