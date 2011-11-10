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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfosetObjectBefore;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link CreationSupport} for <code>RootPanel.get()</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class RootPanelCreationSupport extends CreationSupport {
  private final MethodInvocation m_invocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootPanelCreationSupport(MethodInvocation invocation) {
    m_invocation = invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "RootPanel.get()";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_invocation;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return AstNodeUtils.isMethodInvocation(
        node,
        "com.google.gwt.user.client.ui.RootPanel",
        new String[]{"get()", "get(java.lang.String)"});
  }

  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // evaluation
    m_javaInfo.addBroadcastListener(new JavaInfosetObjectBefore() {
      public void invoke(JavaInfo target, Object[] objectRef) throws Exception {
        if (target == m_javaInfo) {
          GwtState state = ((RootPanelInfo) m_javaInfo).getState();
          objectRef[0] = state.getUIObjectUtils().getRootPanel();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeEvaluated() {
    return false;
  }

  @Override
  public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
      throws Exception {
    return ((RootPanelInfo) m_javaInfo).getUIObjectUtils().getRootPanel();
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
  }
}
