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
package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.dev.util.collect.Maps;

import java.util.LinkedList;
import java.util.Map;

/**
 * Handles lookup of type parameters, using a scope stack.
 */
public class TypeParameterLookup {

  private LinkedList<Map<String, JTypeParameter>> scopeStack = new LinkedList<Map<String, JTypeParameter>>();

  public JTypeParameter lookup(String name) {
    for (Map<String, JTypeParameter> scope : scopeStack) {
      JTypeParameter result = scope.get(name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public void popScope() {
    scopeStack.remove();
  }

  public void pushEnclosingScopes(JClassType type) {
    if (type == null) {
      return;
    }
    pushEnclosingScopes(type.getEnclosingType());
    JGenericType genericType = type.isGenericType();
    if (genericType != null) {
      pushScope(genericType.getTypeParameters());
    }
  }

  public void pushScope(JTypeParameter[] typeParams) {
    // push empty scopes to keep pops in sync
    scopeStack.addFirst(buildScope(typeParams));
  }

  private Map<String, JTypeParameter> buildScope(JTypeParameter[] typeParams) {
    switch (typeParams.length) {
      case 0:
        return Maps.create();
      case 1:
        return Maps.create(typeParams[0].getName(), typeParams[0]);
      default:
        Map<String, JTypeParameter> scope = new HashMap<String, JTypeParameter>();
        for (JTypeParameter typeParam : typeParams) {
          scope.put(typeParam.getName(), typeParam);
        }
        return scope;
    }
  }
}
