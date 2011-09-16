/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.smart.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Utility for any container accessing by setter with array parameter.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.gef.policy
 */
public abstract class ArrayChildrenContainerUtils {
  /**
   * @return the {@link ArrayObjectInfo} for "setter method" invocation.
   */
  public static AbstractArrayObjectInfo getMethodParameterArrayInfo(JavaInfo containerInfo,
      String methodName,
      String itemClassName) throws Exception {
    // try find existing "arrayInfo"
    List<AbstractArrayObjectInfo> childrenArrays =
        containerInfo.getChildren(AbstractArrayObjectInfo.class);
    for (AbstractArrayObjectInfo arrayObjectInfo : childrenArrays) {
      if (methodName.equals(arrayObjectInfo.getCaption())) {
        return arrayObjectInfo;
      }
    }
    // prepare for create "arrayInfo" 
    AstEditor editor = containerInfo.getEditor();
    MethodInvocation methodInvocation =
        containerInfo.addMethodInvocation(methodName + "(" + itemClassName + "[])", "new "
            + itemClassName
            + "[] { }");
    ArrayCreation arrayCreation = (ArrayCreation) DomGenerics.arguments(methodInvocation).get(0);
    Class<?> itemClass =
        ReflectionUtils.getClassByName(EditorState.get(editor).getEditorLoader(), itemClassName);
    // create "arrayInfo"
    ArrayObjectInfo arrayInfo = new ArrayObjectInfo(editor, methodName, itemClass, arrayCreation);
    containerInfo.addChild(arrayInfo);
    // configure "arrayInfo"
    {
      ParameterDescription parameterDescription =
          containerInfo.getDescription().getMethod(methodName + "(" + itemClassName + "[])").getParameter(
              0);
      arrayInfo.setRemoveOnEmpty(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.REMOVE_ON_EMPTY_TAG));
      arrayInfo.setHideInTree(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.HIDE_IN_TREE_TAG));
    }
    return arrayInfo;
  }
}
