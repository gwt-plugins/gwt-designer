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

import java.util.Arrays;

/**
 * Type representing an annotation type.
 */
public class JAnnotationType extends JRealClassType {

  JAnnotationType(TypeOracle oracle, JPackage declaringPackage,
      String enclosingTypeName, String name) {
    super(oracle, declaringPackage, enclosingTypeName, name, true);
  }

  @Override
  public JAnnotationMethod getMethod(String name, JType[] paramTypes)
      throws NotFoundException {
    return (JAnnotationMethod) super.getMethod(name, paramTypes);
  }

  @Override
  public JAnnotationMethod[] getMethods() {
    JMethod[] methodArray = super.getMethods();
    return Arrays.asList(methodArray).toArray(
        new JAnnotationMethod[methodArray.length]);
  }

  @Override
  public JAnnotationMethod[] getOverridableMethods() {
    JMethod[] methodArray = super.getOverridableMethods();
    return Arrays.asList(methodArray).toArray(
        new JAnnotationMethod[methodArray.length]);
  }

  @Override
  public JAnnotationType isAnnotation() {
    return this;
  }

}
