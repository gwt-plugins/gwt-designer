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
package com.google.gdt.eclipse.designer.smart.model.support;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link InvocationEvaluatorInterceptor} for SmartGWT.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public final class SmartGwtInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
  @Override
  public Object evaluateAnonymous(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      ITypeBinding typeBindingConcrete,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    if (AstNodeUtils.isSuccessorOf(typeBindingConcrete, "com.smartgwt.client.data.DataSource")) {
      return AstEvaluationEngine.createAnonymousInstance(context, methodBinding, arguments);
    }
    return AstEvaluationEngine.UNKNOWN;
  }
}
