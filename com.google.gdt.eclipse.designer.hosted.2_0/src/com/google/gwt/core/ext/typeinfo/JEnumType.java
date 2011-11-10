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

import java.util.ArrayList;
import java.util.List;

/**
 * Type representing a Java enumerated type.
 */
public class JEnumType extends JRealClassType {
  private JEnumConstant[] lazyEnumConstants;

  JEnumType(TypeOracle oracle, JPackage declaringPackage,
      String enclosingTypeName, String name) {
    super(oracle, declaringPackage, enclosingTypeName, name, false);
  }

  /**
   * Returns the enumeration constants declared by this enumeration.
   * 
   * @return enumeration constants declared by this enumeration
   */
  public JEnumConstant[] getEnumConstants() {
    if (lazyEnumConstants == null) {
      List<JEnumConstant> enumConstants = new ArrayList<JEnumConstant>();
      for (JField field : getFields()) {
        if (field.isEnumConstant() != null) {
          enumConstants.add(field.isEnumConstant());
        }
      }

      lazyEnumConstants = enumConstants.toArray(
          new JEnumConstant[enumConstants.size()]);
    }

    return lazyEnumConstants;
  }

  @Override
  public JEnumType isEnum() {
    return this;
  }
}
