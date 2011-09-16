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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link CreationSupport} for implicit {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class ImplicitLayoutCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final ContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitLayoutCreationSupport(ContainerInfo container) {
    m_container = container;
    // add listeners
    m_container.addBroadcastListener(new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
          throws Exception {
        if (isAddNewLayout(parent, child) && parent.getChildren().contains(m_javaInfo)) {
          if (nextChild[0] == m_javaInfo) {
            nextChild[0] = GenericsUtils.getNextOrNull(parent.getChildren(), m_javaInfo);
          }
          parent.removeChild(m_javaInfo);
        }
      }
    });
    m_container.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (useImplicitLayout() && isAddNewLayout(parent, child)) {
          parent.addChild(m_javaInfo);
          ((LayoutInfo) m_javaInfo).onSet();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if implicit layout should be added/removed.
   */
  private boolean useImplicitLayout() {
    return m_container.getArbitraryValue(ContainerInfo.KEY_DONT_SET_IMPLICIT_LAYOUT) != Boolean.TRUE;
  }

  /**
   * @return <code>true</code> if given combination of parent/child is adding new {@link LayoutInfo}
   *         on our {@link ContainerInfo}.
   */
  private boolean isAddNewLayout(ObjectInfo parent, ObjectInfo child) {
    return parent == m_container && child instanceof LayoutInfo && child != m_javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    Class<?> layoutClass = getComponentClass();
    // check for default layout
    if (layoutClass == null) {
      return "implicit-layout: default";
    }
    // "real" layout
    return "implicit-layout: " + layoutClass.getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_container.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object o) throws Exception {
        if (target == m_container) {
          Object layout = ReflectionUtils.invokeMethod(o, "getLayout()");
          m_javaInfo.setObject(layout);
        }
      }
    });
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      return invocation.arguments().isEmpty()
          && invocation.getName().getIdentifier().equals("getLayout")
          && m_container.isRepresentedBy(invocation.getExpression());
    }
    return false;
  }

  @Override
  public ASTNode getNode() {
    return m_container.getCreationSupport().getNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    String layoutClassName = m_javaInfo.getDescription().getComponentClass().getName();
    return TemplateUtils.format("({0}) {1}.getLayout()", layoutClassName, m_container);
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
    // if implicit layout was materialized, so has real variable, restore implicit variable
    if (!(m_javaInfo.getVariableSupport() instanceof ImplicitLayoutVariableSupport)) {
      m_javaInfo.setVariableSupport(new ImplicitLayoutVariableSupport(m_javaInfo));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardImplicitCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public IClipboardImplicitCreationSupport getImplicitClipboard() {
    return null;
  }
}
