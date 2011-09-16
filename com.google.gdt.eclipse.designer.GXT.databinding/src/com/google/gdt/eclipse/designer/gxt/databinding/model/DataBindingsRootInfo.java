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
package com.google.gdt.eclipse.designer.gxt.databinding.model;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.ContainerAstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.ISubParser;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * @author lobas_av
 * 
 */
public class DataBindingsRootInfo implements ISubParser {
  private MethodDeclaration m_initDataBindings;
  private boolean m_addInitializeContext;
  private boolean m_addPostInitializeContext;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setInitDataBindings(MethodDeclaration initDataBindings) {
    m_initDataBindings = initDataBindings;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ISubParser
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    if (signature.endsWith("initializeBindings()")) {
      m_addInitializeContext = true;
    } else if (signature.endsWith("postInitializeBindings()")) {
      m_addPostInitializeContext = true;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void commit(DatabindingsProvider provider) throws Exception {
    List<BindingInfo> bindings = provider.getBindings0();
    CodeGenerationSupport generationSupport =
        new CodeGenerationSupport(false, new ContainerAstObjectInfo(bindings));
    // prepare source code
    List<String> methodLines = Lists.newArrayList();
    //
    if (m_addInitializeContext) {
      methodLines.add("initializeBindings();");
      methodLines.add("//");
    }
    //
    int size = bindings.size();
    for (int i = 0; i < size; i++) {
      BindingInfo binding = bindings.get(i);
      if (i > 0 && binding.addSourceCodeSeparator()) {
        methodLines.add("//");
      }
      binding.addSourceCode(methodLines, generationSupport);
    }
    //
    if (m_addPostInitializeContext) {
      methodLines.add("//");
      methodLines.add("postInitializeBindings();");
    }
    //
    JavaInfo javaInfoRoot = provider.getJavaInfoRoot();
    AstEditor editor = javaInfoRoot.getEditor();
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, false);
    MethodDeclaration lastInfoMethod = getLastInfoDeclaration(javaInfoRoot);
    //
    if (m_initDataBindings != null) {
      editor.removeBodyDeclaration(m_initDataBindings);
    }
    m_initDataBindings =
        editor.addMethodDeclaration(createMethodHeader(lastInfoMethod), methodLines, target);
    // check call initDataBindings() after creation all components
    ensureInvokeInitDataBindings(editor, lastInfoMethod);
  }

  private static String createMethodHeader(MethodDeclaration lastInfoMethod) throws Exception {
    // check static
    if (Modifier.isStatic(lastInfoMethod.getModifiers())) {
      return "protected static void initDataBindings()";
    }
    // normal
    return "protected void initDataBindings()";
  }

  private MethodDeclaration getLastInfoDeclaration(JavaInfo rootJavaInfo) throws Exception {
    final JavaInfo[] result = {rootJavaInfo};
    rootJavaInfo.accept0(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof UIObjectInfo) {
          JavaInfo info = (JavaInfo) objectInfo;
          if (JavaInfoUtils.getMethodDeclaration(info) != m_initDataBindings) {
            result[0] = info;
          }
        }
      }
    });
    MethodDeclaration method = JavaInfoUtils.getMethodDeclaration(result[0]);
    Assert.isNotNull(method);
    return method;
  }

  private static void ensureInvokeInitDataBindings(AstEditor editor,
      MethodDeclaration lastInfoMethod) throws Exception {
    final boolean[] invoke = new boolean[1];
    lastInfoMethod.accept(new ASTVisitor() {
      @Override
      public boolean visit(MethodInvocation node) {
        String methodName = node.getName().getIdentifier();
        if (node.arguments().isEmpty() && "initDataBindings".equals(methodName)) {
          Assert.isTrue(!invoke[0], "Double invoke initDataBindings()");
          invoke[0] = true;
        }
        return false;
      }
    });
    //
    if (invoke[0]) {
      return;
    }
    //
    List<Statement> statements = DomGenerics.statements(lastInfoMethod.getBody());
    StatementTarget methodTarget;
    if (statements.isEmpty()) {
      methodTarget = new StatementTarget(lastInfoMethod, true);
    } else {
      Statement lastStatement = statements.get(statements.size() - 1);
      methodTarget = new StatementTarget(lastStatement, lastStatement instanceof ReturnStatement);
    }
    //
    editor.addStatement("initDataBindings();", methodTarget);
  }
}