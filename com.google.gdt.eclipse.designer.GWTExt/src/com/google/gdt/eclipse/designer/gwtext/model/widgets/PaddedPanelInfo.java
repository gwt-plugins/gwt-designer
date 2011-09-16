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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Model for <code>PaddedPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public final class PaddedPanelInfo extends ContainerInfo {
  private final PaddedPanelInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PaddedPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addContentPanelDuringCreate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the content {@link PanelInfo}, may be <code>null</code>.
   */
  public PanelInfo getContent() {
    return GenericsUtils.getFirstOrNull(getChildren(PanelInfo.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>PaddedPanel</code> requires content <code>Panel</code> to be passed as constructor
   * argument.
   */
  private void addContentPanelDuringCreate() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this) {
          addContentPanel();
        }
      }

      private void addContentPanel() throws Exception {
        AstEditor editor = getEditor();
        JavaInfo content =
            JavaInfoUtils.createJavaInfo(
                editor,
                "com.gwtext.client.widgets.Panel",
                new ConstructorCreationSupport());
        // set CreationSupport
        Expression expression;
        {
          ClassInstanceCreation creation =
              ((ConstructorCreationSupport) getCreationSupport()).getCreation();
          expression = DomGenerics.arguments(creation).get(0);
          String contentSource = content.getCreationSupport().add_getSource(null);
          expression = editor.replaceExpression(expression, contentSource);
          content.getCreationSupport().add_setSourceExpression(expression);
          content.addRelatedNode(expression);
        }
        // set Association
        content.setAssociation(new ConstructorChildAssociation());
        // set VariableSupport
        VariableSupport variableSupport = new EmptyVariableSupport(content, expression);
        content.setVariableSupport(variableSupport);
        // add content Panel as child
        addChild(content);
      }
    });
  }
}
