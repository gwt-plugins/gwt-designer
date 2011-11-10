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

import com.google.gwt.dev.util.StringInterner;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Represents a parameter in a declaration.
 */
@SuppressWarnings("deprecation")
public class JParameter implements HasAnnotations, HasMetaData {

  private final Annotations annotations;

  private boolean argNameIsReal;

  private final JAbstractMethod enclosingMethod;

  private String name;

  private JType type;
  
  JParameter(JAbstractMethod enclosingMethod, JParameter srcParam) {
    this.enclosingMethod = enclosingMethod;
    this.type = srcParam.type;
    this.name = StringInterner.get().intern(srcParam.name);
    this.annotations = new Annotations(srcParam.annotations);
  }

  JParameter(JAbstractMethod enclosingMethod, JType type,
      String name) {
    this(enclosingMethod, type, name, null);
  }

  JParameter(JAbstractMethod enclosingMethod, JType type, String name,
      Map<Class<? extends Annotation>, Annotation> declaredAnnotations) {
    this(enclosingMethod, type, name, declaredAnnotations, true);
  }

  JParameter(JAbstractMethod enclosingMethod, JType type, String name,
      Map<Class<? extends Annotation>, Annotation> declaredAnnotations,
      boolean argNameIsReal) {
    this.enclosingMethod = enclosingMethod;
    this.type = type;
    this.name = StringInterner.get().intern(name);
    this.argNameIsReal = argNameIsReal;

    enclosingMethod.addParameter(this);

    annotations = new Annotations(declaredAnnotations);
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return annotations.getAnnotation(annotationClass);
  }

  public JAbstractMethod getEnclosingMethod() {
    return enclosingMethod;
  }

  @Deprecated
  public final String[][] getMetaData(String tagName) {
    return TypeOracle.NO_STRING_ARR_ARR;
  }

  @Deprecated
  public final String[] getMetaDataTags() {
    return TypeOracle.NO_STRINGS;
  }

  public String getName() {
    if (!argNameIsReal) {
      name = enclosingMethod.getRealParameterName(this);
      argNameIsReal = true;
    }
    return name;
  }

  public JType getType() {
    return type;
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return annotations.isAnnotationPresent(annotationClass);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(type.getParameterizedQualifiedSourceName());
    sb.append(" ");
    sb.append(getName());
    return sb.toString();
  }

  /**
   * NOTE: This method is for testing purposes only.
   */
  Annotation[] getAnnotations() {
    return annotations.getAnnotations();
  }

  /**
   * NOTE: This method is for testing purposes only.
   */
  Annotation[] getDeclaredAnnotations() {
    return annotations.getDeclaredAnnotations();
  }

  // Only called by JAbstractMethod after real parameter names are fetched.
  void setName(String name) {
    this.name = StringInterner.get().intern(name);
  }

  // Called when parameter types are found to be parameterized
  void setType(JType type) {
    this.type = type;
  }
}
