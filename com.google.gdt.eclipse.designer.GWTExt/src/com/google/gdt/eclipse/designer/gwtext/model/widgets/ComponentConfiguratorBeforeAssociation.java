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

import com.google.gdt.eclipse.designer.model.widgets.WidgetAttachAfterConstructor;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Helper for configuring {@link ComponentInfo} object directly before association.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public abstract class ComponentConfiguratorBeforeAssociation {
  private final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentConfiguratorBeforeAssociation(JavaInfo javaInfo) {
    m_javaInfo = javaInfo;
    ensureConfigured_attachAfterConstructor();
    ensureConfigured_directlyBeforeAssociation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Required models
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object getObject() {
    return m_javaInfo.getObject();
  }

  /**
   * For "this", attaching, so rendering is going to be executed.
   */
  private void ensureConfigured_attachAfterConstructor() {
    m_javaInfo.addBroadcastListener(new WidgetAttachAfterConstructor() {
      public void invoke() throws Exception {
        if (!isRendered()) {
          configure();
        }
      }
    });
  }

  /**
   * Ensure that each time when we try to associate this component with some parent (during parsing
   * and refresh), we ensure that it is configured.
   */
  private void ensureConfigured_directlyBeforeAssociation() {
    m_javaInfo.addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isPossibleAssociation(node)) {
          configure();
        }
      }

      /**
       * Rough approximation for association checking, may fail sometimes. But I think that GWT-Ext
       * deserve hardly better implementation.
       */
      private boolean isPossibleAssociation(ASTNode node) {
        if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;
          for (Expression argument : DomGenerics.arguments(invocation)) {
            if (m_javaInfo.isRepresentedBy(argument)) {
              return true;
            }
          }
        }
        return false;
      }
    });
  }

  private boolean isRendered() throws Exception {
    return (Boolean) ReflectionUtils.invokeMethod(getObject(), "isRendered()");
  }

  protected abstract void configure() throws Exception;
}
