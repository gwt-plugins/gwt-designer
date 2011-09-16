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
package com.google.gdt.eclipse.designer.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ImplicitFactoryArgumentAssociation;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for <code>Tree</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class TreeInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TreeItemInfo} children.
   */
  public List<TreeItemInfo> getItems() {
    return getChildren(TreeItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link TreeItemInfo} based on {@link WidgetInfo} to this {@link TreeInfo}.
   */
  public void command_CREATE(WidgetInfo widget, TreeItemInfo nextItem) throws Exception {
    command_CREATE_Widget(this, widget);
  }

  static void command_CREATE_Widget(JavaInfo host, WidgetInfo widget) throws Exception {
    AstEditor editor = host.getEditor();
    // prepare CreationSupport for TreeItem
    CreationSupport creationSupport;
    {
      String signature = "addItem(com.google.gwt.user.client.ui.Widget)";
      String source = "addItem((com.google.gwt.user.client.ui.Widget) null)";
      creationSupport = new ImplicitFactoryCreationSupport(host, signature, source);
    }
    // add TreeItem
    TreeItemInfo item =
        (TreeItemInfo) JavaInfoUtils.createJavaInfo(
            editor,
            "com.google.gwt.user.client.ui.TreeItem",
            creationSupport);
    JavaInfoUtils.add(item, null, host, null);
    // prepare added invocation
    MethodInvocation invocation = (MethodInvocation) item.getCreationSupport().getNode();
    item.setAssociation(new InvocationVoidAssociation(invocation));
    // configure Widget
    {
      NodeTarget target = new NodeTarget(new StatementTarget(invocation, true));
      Expression widgetExpression =
          editor.replaceInvocationArgument(
              invocation,
              0,
              widget.getCreationSupport().add_getSource(target));
      widget.getCreationSupport().add_setSourceExpression(widgetExpression);
      widget.setVariableSupport(new EmptyVariableSupport(widget, widgetExpression));
      widget.setAssociation(new ImplicitFactoryArgumentAssociation(invocation, item));
      item.addChild(widget);
    }
  }
}
