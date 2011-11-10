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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Model for <code>MarginData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public class MarginDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarginDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Object getMarginAll() {
    int all = getMarginTop();
    if (getMarginTop() == all
        && getMarginRight() == all
        && getMarginBottom() == all
        && getMarginLeft() == all) {
      return all;
    }
    return Property.UNKNOWN_VALUE;
  }

  public void setMarginAll(Object value) throws Exception {
    // margin LayoutData usually have no other constructor arguments than margin
    if (value == Property.UNKNOWN_VALUE) {
      delete();
      return;
    }
    // set some value
    materialize();
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      String valueSource = getMarginValueSource(value);
      replaceConstructorArguments(valueSource);
      ExecutionUtils.refresh(this);
    }
  }

  public final int getMarginTop() {
    return getMarginsField("top");
  }

  public final int getMarginRight() {
    return getMarginsField("right");
  }

  public final int getMarginBottom() {
    return getMarginsField("bottom");
  }

  public final int getMarginLeft() {
    return getMarginsField("left");
  }

  public final void setMarginTop(Object value) throws Exception {
    setMargin_constructor(0, value);
  }

  public final void setMarginRight(Object value) throws Exception {
    setMargin_constructor(1, value);
  }

  public final void setMarginBottom(Object value) throws Exception {
    setMargin_constructor(2, value);
  }

  public final void setMarginLeft(Object value) throws Exception {
    setMargin_constructor(3, value);
  }

  private int getMarginsField(String fieldName) {
    Object margins = ReflectionUtils.invokeMethodEx(getObject(), "getMargins()");
    return ReflectionUtils.getFieldInt(margins, fieldName);
  }

  private void setMargin_constructor(int index, Object value) throws Exception {
    materialize();
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      setMargin_constructor4();
      // new arguments
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      String signature = creationSupport.getDescription().getSignature();
      List<Expression> arguments = DomGenerics.arguments(creationSupport.getCreation());
      // update one of 4 arguments
      if ("<init>(int,int,int,int)".equals(signature)) {
        String valueSource = getMarginValueSource(value);
        getEditor().replaceExpression(arguments.get(index), valueSource);
        ExecutionUtils.refresh(this);
      }
    }
  }

  /**
   * Attempts to convert {@link ConstructorCreationSupport} into using 4 arguments.
   */
  private void setMargin_constructor4() throws Exception {
    String signature;
    {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      signature = creationSupport.getDescription().getSignature();
    }
    // if no arguments, expand to 4 arguments
    if ("<init>()".equals(signature)) {
      setMargin_constructor4(0);
    }
    // if one argument, expand to 4 arguments
    if ("<init>(int)".equals(signature)) {
      setMargin_constructor4(getMarginTop());
    }
  }

  private void setMargin_constructor4(int value) throws Exception {
    String source_1 = IntegerConverter.INSTANCE.toJavaSource(null, value);
    String source_4 = source_1 + ", " + source_1 + ", " + source_1 + ", " + source_1;
    replaceConstructorArguments(source_4);
  }

  /**
   * Replaces arguments of {@link ClassInstanceCreation} and updates
   * {@link ConstructorCreationSupport}.
   */
  private void replaceConstructorArguments(String args) throws Exception {
    ConstructorCreationSupport creationSupport = (ConstructorCreationSupport) getCreationSupport();
    ClassInstanceCreation creation = creationSupport.getCreation();
    getEditor().replaceCreationArguments(creation, ImmutableList.of(args));
    setCreationSupport(new ConstructorCreationSupport(creation));
  }

  private static String getMarginValueSource(Object value) throws Exception {
    if (value == Property.UNKNOWN_VALUE) {
      return "0";
    }
    return IntegerConverter.INSTANCE.toJavaSource(null, value);
  }
}
