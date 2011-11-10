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

import com.google.gwt.core.ext.typeinfo.JWildcardType.BoundType;
import com.google.gwt.dev.util.collect.Lists;

import java.util.List;

/**
 * Type declaration that has type parameters.
 */
public class JGenericType extends JRealClassType implements HasTypeParameters {

  private JRawType lazyRawType = null;

  private List<JTypeParameter> typeParams = Lists.create();

  public JGenericType(TypeOracle oracle, JPackage declaringPackage,
      String enclosingTypeName, String name, boolean isInterface,
      JTypeParameter[] jtypeParameters) {
    super(oracle, declaringPackage, enclosingTypeName, name, isInterface);

    if (jtypeParameters != null) {
      for (JTypeParameter jtypeParameter : jtypeParameters) {
        addTypeParameter(jtypeParameter);
      }
    }
  }

  public JParameterizedType asParameterizedByWildcards() {
    JClassType[] typeArgs = new JClassType[typeParams.size()];
    for (int i = 0; i < typeArgs.length; ++i) {
      typeArgs[i] = getOracle().getWildcardType(BoundType.EXTENDS,
          typeParams.get(i).getFirstBound());
    }
    return getOracle().getParameterizedType(this, typeArgs);
  }

  @Override
  public JClassType getErasedType() {
    return getRawType();
  }

  @Override
  public String getParameterizedQualifiedSourceName() {
    StringBuffer sb = new StringBuffer();

    if (getEnclosingType() != null) {
      sb.append(getEnclosingType().getParameterizedQualifiedSourceName());
      sb.append(".");
      sb.append(getSimpleSourceName());
    } else {
      sb.append(getQualifiedSourceName());
    }

    sb.append('<');
    boolean needComma = false;
    for (JClassType typeParam : typeParams) {
      if (needComma) {
        sb.append(", ");
      } else {
        needComma = true;
      }
      sb.append(typeParam.getParameterizedQualifiedSourceName());
    }
    sb.append('>');
    return sb.toString();
  }

  public JRawType getRawType() {
    if (lazyRawType == null) {
      lazyRawType = new JRawType(this);
    }

    return lazyRawType;
  }

  public JTypeParameter[] getTypeParameters() {
    return typeParams.toArray(new JTypeParameter[typeParams.size()]);
  }

  @Override
  public JGenericType isGenericType() {
    return this;
  }

  @Override
  public String toString() {
    if (isInterface() != null) {
      return "interface " + getParameterizedQualifiedSourceName();
    }

    return "class " + getParameterizedQualifiedSourceName();
  }

  private void addTypeParameter(JTypeParameter typeParameter) {
    typeParams = Lists.add(typeParams, typeParameter);
    typeParameter.setDeclaringClass(this);
  }
}
