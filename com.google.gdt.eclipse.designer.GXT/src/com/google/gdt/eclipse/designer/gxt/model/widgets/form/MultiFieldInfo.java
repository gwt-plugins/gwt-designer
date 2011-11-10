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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.common.collect.ImmutableMap;
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.form.MultiField</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class MultiFieldInfo extends FieldInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiFieldInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
    ensureAtLeastOneChildField_directlyBeforeAssociation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureAtLeastOneChildField_directlyBeforeAssociation() {
    addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isPossibleAssociation(node)) {
          ensureAtLeastOneChildField();
        }
      }

      /**
       * Rough approximation for association checking, may fail sometimes.
       */
      private boolean isPossibleAssociation(ASTNode node) {
        if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;
          List<Expression> arguments = DomGenerics.arguments(invocation);
          return !arguments.isEmpty() && isRepresentedBy(arguments.get(0));
        }
        return false;
      }
    });
  }

  private void ensureAtLeastOneChildField() throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Map<String, Object> variables = ImmutableMap.of("multiField", getObject());
    ScriptUtils.evaluate(classLoader, CodeUtils.getSource(
        "import com.extjs.gxt.ui.client.widget.form.*;",
        "",
        "if (multiField.getAll().isEmpty()) {",
        "  multiField.add(new LabelField('Empty MultiField'));",
        "}",
        ""), variables);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isHorizontal() {
    Object orientation = ReflectionUtils.invokeMethodEx(getObject(), "getOrientation()");
    return ((Enum<?>) orientation).name().equals("HORIZONTAL");
  }
}
