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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import com.google.gdt.eclipse.designer.gwtext.model.layout.ColumnLayoutDataInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for <code>MultiFieldPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public final class MultiFieldPanelInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiFieldPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getWidth(WidgetInfo widget) throws Exception {
    // check for ColumnLayoutData
    {
      List<ColumnLayoutDataInfo> data = widget.getChildren(ColumnLayoutDataInfo.class);
      if (!data.isEmpty()) {
        double weight = (Double) data.get(0).getPropertyByTitle("width").getValue();
        return (int) (weight * 100) + "%";
      }
    }
    // integer width expected
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      if (association.getDescription().getSignature().endsWith(",int)")) {
        Expression expression = DomGenerics.arguments(association.getInvocation()).get(1);
        return ((Integer) JavaInfoEvaluationHelper.getValue(expression)).toString();
      }
    }
    // impossible
    return Property.UNKNOWN_VALUE;
  }

  public void setWidth(final WidgetInfo widget, final Object value) throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        setWidth0(widget, value);
      }
    });
  }

  private void setWidth0(WidgetInfo widget, Object _value) throws Exception {
    String value;
    if (_value instanceof String) {
      value = (String) _value;
    } else {
      value = "100";
    }
    //
    AstEditor editor = getEditor();
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      // set percent
      if (value.endsWith("%")) {
        ColumnLayoutDataInfo columnData;
        if (association.getDescription().getSignature().endsWith(",int)")) {
          columnData = setWidth_addColumnLayoutData(widget, invocation);
        } else {
          columnData = widget.getChildren(ColumnLayoutDataInfo.class).get(0);
        }
        // set "columnWidth" property
        {
          value = StringUtils.substring(value, 0, -1);
          double weight = Integer.parseInt(value) / 100.0;
          columnData.setWidth(weight);
        }
        // done
        return;
      }
      // set absolute value
      {
        Expression expression = DomGenerics.arguments(invocation).get(1);
        editor.replaceExpression(expression, value);
        editor.replaceInvocationBinding(invocation);
        for (JavaInfo columnData : widget.getChildren(ColumnLayoutDataInfo.class)) {
          columnData.delete();
        }
      }
    }
  }

  private ColumnLayoutDataInfo setWidth_addColumnLayoutData(WidgetInfo widget,
      MethodInvocation invocation) throws Exception {
    AstEditor editor = getEditor();
    ColumnLayoutDataInfo columnData =
        (ColumnLayoutDataInfo) JavaInfoUtils.createJavaInfo(
            editor,
            "com.gwtext.client.widgets.layout.ColumnLayoutData",
            new ConstructorCreationSupport());
    // set CreationSupport
    Expression expression;
    {
      expression = DomGenerics.arguments(invocation).get(1);
      String contentSource = columnData.getCreationSupport().add_getSource(null);
      expression = editor.replaceExpression(expression, contentSource);
      columnData.getCreationSupport().add_setSourceExpression(expression);
      columnData.addRelatedNode(expression);
    }
    // set Association
    columnData.setAssociation(new InvocationSecondaryAssociation(invocation));
    editor.replaceInvocationBinding(invocation);
    // set VariableSupport
    VariableSupport variableSupport = new EmptyVariableSupport(columnData, expression);
    columnData.setVariableSupport(variableSupport);
    // add content ColumnLayoutData as child
    widget.addChild(columnData);
    return columnData;
  }
}
