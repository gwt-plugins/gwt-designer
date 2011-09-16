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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.Dialog</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class DialogInfo extends ContentPanelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public List<DialogButton_Info> getDialogButtons() {
    List<DialogButton_Info> buttons = Lists.newArrayList();
    Object buttonBar = ReflectionUtils.invokeMethodEx(getObject(), "getButtonBar()");
    List<Object> items = (List<Object>) ReflectionUtils.invokeMethodEx(buttonBar, "getItems()");
    for (Object item : items) {
      String id = (String) ReflectionUtils.invokeMethodEx(item, "getItemId()");
      Field idField = getDialogButtonField(id);
      if (idField == null) {
        continue;
      }
      Rectangle bounds;
      {
        Object itemElement = getUIObjectUtils().getElement(item);
        bounds = getState().getAbsoluteBounds(itemElement);
        absoluteToRelative(bounds);
      }
      buttons.add(new DialogButton_Info(idField, bounds));
    }
    return buttons;
  }

  private Field getDialogButtonField(String id) {
    try {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> classDialog = classLoader.loadClass("com.extjs.gxt.ui.client.widget.Dialog");
      for (Field field : classDialog.getDeclaredFields()) {
        if (field.getType() == String.class
            && ReflectionUtils.isPublic(field)
            && ReflectionUtils.isStatic(field)) {
          if (field.get(null).equals(id)) {
            return field;
          }
        }
      }
    } catch (Throwable e) {
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button
  //
  ////////////////////////////////////////////////////////////////////////////
  public class DialogButton_Info {
    private final Field m_id;
    private final Rectangle m_bounds;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DialogButton_Info(Field id, Rectangle bounds) {
      m_id = id;
      m_bounds = bounds;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
      return m_id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof DialogButton_Info) {
        DialogButton_Info dialogButton = (DialogButton_Info) obj;
        return m_id.equals(dialogButton.m_id);
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Field getId() {
      return m_id;
    }

    public Rectangle getBounds() {
      return m_bounds;
    }

    public void open() {
      ExecutionUtils.run(DialogInfo.this, new RunnableEx() {
        public void run() throws Exception {
          openEx();
        }
      });
    }

    private void openEx() throws Exception {
      IfStatement ifStatement = getThisStatement();
      JavaInfoUtils.scheduleOpenNode(DialogInfo.this, ifStatement);
    }

    private IfStatement getThisStatement() throws Exception {
      MethodDeclaration method;
      {
        TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(DialogInfo.this);
        method =
            AstNodeUtils.getMethodBySignature(
                typeDeclaration,
                "onButtonPressed(com.extjs.gxt.ui.client.widget.button.Button)");
        if (method == null) {
          method =
              getEditor().addMethodDeclaration(
                  "protected void onButtonPressed(com.extjs.gxt.ui.client.widget.button.Button button)",
                  ImmutableList.of("super.onButtonPressed(button);"),
                  new BodyDeclarationTarget(typeDeclaration, false));
        }
      }
      // try to find exiting statement
      List<Statement> statements = DomGenerics.statements(method);
      for (Statement statement : statements) {
        if (statement instanceof IfStatement) {
          IfStatement ifStatement = (IfStatement) statement;
          Expression condition = ifStatement.getExpression();
          if (isThisCondition(condition)) {
            return ifStatement;
          }
        }
      }
      // create new
      return (IfStatement) getEditor().addStatement(
          ImmutableList.of("if (button == getButtonBar().getItemByItemId("
              + m_id.getName()
              + ")) {", "\t// TODO", "}"),
          new StatementTarget(method, true));
    }

    private boolean isThisCondition(Expression condition) {
      if (condition instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) condition;
        Expression rightOperand = infixExpression.getRightOperand();
        if (infixExpression.getOperator() == InfixExpression.Operator.EQUALS
            && AstNodeUtils.isMethodInvocation(rightOperand, "getItemByItemId(java.lang.String)")) {
          Expression idExpression = DomGenerics.arguments(rightOperand).get(0);
          String idSource = getEditor().getSource(idExpression);
          if (m_id.getName().equals(idSource)) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
