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
package com.google.gdt.eclipse.designer.util;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.IExceptionConstants;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.DefaultMethodInterceptor;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.FatalDesignerException;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Resolves methods of <code>JavaScriptObject</code> objects.
 * 
 * @author scheglov_ke
 * @coverage gwt.util
 */
public final class GwtInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
  private static final String JSO_NAME = "com.google.gwt.core.client.JavaScriptObject";

  ////////////////////////////////////////////////////////////////////////////
  //
  // InvocationEvaluatorInterceptor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Method resolveMethod(Class<?> clazz, String signature) throws Exception {
    if (isJavaScriptObject_rewrittenInterface(clazz)) {
      String implClassName = clazz.getName() + "$";
      Class<?> implClass = clazz.getClassLoader().loadClass(implClassName);
      return ReflectionUtils.getMethodBySignature(implClass, signature);
    }
    return null;
  }

  @Override
  public Object evaluateAnonymous(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      ITypeBinding typeBindingConcrete,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.user.client.Command")) {
      return null;
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.text.shared.Renderer")) {
      return null;
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.cell.client.Cell")) {
      return getCellFake(context, typeBinding, methodBinding, arguments);
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.view.client.TreeViewModel")) {
      return getTreeViewModelFake(context, typeBinding, methodBinding, arguments);
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.user.cellview.client.Column")) {
      return getColumnFake(context, typeBinding);
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.user.cellview.client.Header")) {
      return AstEvaluationEngine.createAnonymousInstance(context, methodBinding, arguments);
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * Support evaluating <code>com.google.gwt.cell.client.Cell</code> instances.
   * 
   * @return instance with overridden "renderer" method.
   */
  private Object getCellFake(EvaluationContext context,
      ITypeBinding typeBinding,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    final String renderSignature =
        MessageFormat.format(
            "render({0},{1},{2})",
            "com.google.gwt.cell.client.Cell.Context",
            "java.lang.Object",
            "com.google.gwt.safehtml.shared.SafeHtmlBuilder");
    return AstEvaluationEngine.createAnonymousInstance(
        context,
        methodBinding,
        arguments,
        new DefaultMethodInterceptor() {
          @Override
          public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
              throws Throwable {
            String signature = ReflectionUtils.getMethodSignature(method);
            if (renderSignature.equals(signature)) {
              Object safeHtmlBuilder = args[2];
              String toAppend = "<rendered Cell value>";
              ReflectionUtils.invokeMethod(
                  safeHtmlBuilder,
                  "appendEscaped(java.lang.String)",
                  toAppend);
              return null;
            }
            return super.intercept(obj, method, args, proxy);
          }
        });
  }

  /**
   * Support evaluating <code>com.google.gwt.view.client.TreeViewModel</code> instances.
   * 
   * @return Fake <code>com.google.gwt.view.client.TreeViewModel</code> instance.
   */
  private Object getTreeViewModelFake(EvaluationContext context,
      ITypeBinding typeBinding,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    ClassLoader classLoader = context.getClassLoader();
    final Object nodeInfoFake = TreeViewModelSupport.getNodeInfoFake(classLoader);
    return AstEvaluationEngine.createAnonymousInstance(
        context,
        methodBinding,
        arguments,
        new DefaultMethodInterceptor() {
          @Override
          public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
              throws Throwable {
            // intercept com.google.gwt.view.client.TreeViewModel.getNodeInfo(T)
            if ("getNodeInfo".equals(method.getName())) {
              Class<?>[] parameterTypes = method.getParameterTypes();
              if (parameterTypes.length == 1) {
                return nodeInfoFake;
              }
            }
            // intercept com.google.gwt.view.client.TreeViewModel.isLeaf(Object)
            if ("isLeaf".equals(method.getName())) {
              Class<?>[] parameterTypes = method.getParameterTypes();
              if (parameterTypes.length == 1
                  && "java.lang.Object".equals(ReflectionUtils.getFullyQualifiedName(
                      parameterTypes[0],
                      false))) {
                if (args[0] == nodeInfoFake) {
                  return Boolean.TRUE;
                } else {
                  return Boolean.FALSE;
                }
              }
            }
            return super.intercept(obj, method, args, proxy);
          }
        });
  }

  /**
   * Support for evaluating <code>com.google.gwt.view.client.Column</code> instances.
   * 
   * @return the instance of <code>TextColumn</code>.
   */
  private Object getColumnFake(EvaluationContext context, ITypeBinding typeBinding)
      throws Exception {
    String columnText;
    {
      String columnValueTypeName = getColumnValueTypeName(typeBinding);
      String shortTypeName = CodeUtils.getShortClass(columnValueTypeName);
      columnText = "<" + StringUtils.substring(shortTypeName, 0, 20) + ">";
    }
    // create TextColumn
    ClassLoader classLoader = context.getClassLoader();
    return createTextColumn(classLoader, columnText);
  }

  /**
   * @return the instance of <code>TextColumn</code> for displaying static text;
   */
  public static Object createTextColumn(ClassLoader classLoader, final String columnText)
      throws ClassNotFoundException {
    Class<?> classTextColumn =
        classLoader.loadClass("com.google.gwt.user.cellview.client.TextColumn");
    // prepare Enhancer
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(classTextColumn);
    enhancer.setCallback(new MethodInterceptor() {
      public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
          throws Throwable {
        if (ReflectionUtils.getMethodSignature(method).equals("getValue(java.lang.Object)")) {
          return columnText;
        }
        return proxy.invokeSuper(obj, args);
      }
    });
    // create instance
    return enhancer.create();
  }

  /**
   * @return the name of <code>getValue()</code> return type.
   */
  private String getColumnValueTypeName(ITypeBinding columnTypeBinding) {
    ITypeBinding cellArgumentTypeBinding =
        AstNodeUtils.getTypeBindingArgument(
            columnTypeBinding,
            "com.google.gwt.user.cellview.client.Column",
            1);
    return AstNodeUtils.getFullyQualifiedName(cellArgumentTypeBinding, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invocation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object evaluate(EvaluationContext context,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Class<?> clazz,
      Method method,
      Object[] argumentValues) {
    // Panel.add(Widget) throws exception, so make it fatal
    if (ReflectionUtils.getMethodSignature(method).equals(
        "add(com.google.gwt.user.client.ui.Widget)")) {
      if (method.getDeclaringClass().getName().equals("com.google.gwt.user.client.ui.Panel")) {
        FatalDesignerException e =
            new FatalDesignerException(IExceptionConstants.PANEL_ADD_INVOCATION,
                invocation.toString());
        e.setSourcePosition(invocation.getStartPosition());
        throw e;
      }
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  @Override
  public Object evaluate(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    if (isWidget(clazz)) {
      return evaluateGWT(context, expression, clazz, actualConstructor, arguments);
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  private Object evaluateGWT(EvaluationContext context,
      ClassInstanceCreation expression,
      Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    PlaceholderUtils.clear(expression);
    // ignore Google Map
    if (clazz.getName().equals("com.google.gwt.maps.client.MapWidget")) {
      PlaceholderUtils.markPlaceholder(expression);
      return createPlaceholder(clazz);
    }
    // fix arguments
    fixArguments(clazz, actualConstructor, arguments);
    // ValueLabel++
    if (clazz.getName().equals("com.google.gwt.user.client.ui.ValueLabel")) {
      return createValueLabel(actualConstructor, arguments);
    }
    if (ReflectionUtils.isSuccessorOf(clazz, "com.google.gwt.user.client.ui.DateLabel")) {
      return createDateLabel(expression, actualConstructor, arguments);
    }
    if (ReflectionUtils.isSuccessorOf(clazz, "com.google.gwt.user.client.ui.NumberLabel")) {
      return createNumberLabel(expression, actualConstructor, arguments);
    }
    // try actual constructor
    try {
      return actualConstructor.newInstance(arguments);
    } catch (Throwable e) {
      context.addException(expression, e);
      PlaceholderUtils.addException(expression, e);
    }
    // some exception happened, try default constructor (if actual was not default)
    try {
      Constructor<?> defaultConstructor =
          ReflectionUtils.getConstructorBySignature(clazz, "<init>()");
      if (defaultConstructor != null
          && !ReflectionUtils.equals(actualConstructor, defaultConstructor)) {
        return defaultConstructor.newInstance();
      }
    } catch (Throwable e) {
      context.addException(expression, e);
      PlaceholderUtils.addException(expression, e);
    }
    // still no success, use placeholder
    PlaceholderUtils.markPlaceholder(expression);
    return createPlaceholder(clazz);
  }

  /**
   * Tweaks arguments to fix know problem cases.
   */
  private void fixArguments(Class<?> clazz, Constructor<?> constructor, Object[] arguments)
      throws Exception {
    if (clazz.getName().equals("com.google.gwt.user.client.ui.Tree")) {
      String signature = ReflectionUtils.getConstructorSignature(constructor);
      if (signature.equals("<init>(com.google.gwt.user.client.ui.TreeImages)")) {
        if (arguments[0] == null) {
          arguments[0] =
              ScriptUtils.evaluate(clazz.getClassLoader(), CodeUtils.getSource(
                  "import com.google.gwt.core.client.GWT;",
                  "import com.google.gwt.user.client.ui.*;",
                  "return GWT.create(TreeImages);"));
        }
      }
    }
    // prevent "null" as "cell" in CellList
    if (clazz.getName().equals("com.google.gwt.user.cellview.client.CellList")) {
      String signature = ReflectionUtils.getConstructorSignature(constructor);
      if (signature.equals("<init>(com.google.gwt.cell.client.Cell)")) {
        if (arguments[0] == null) {
          ClassLoader classLoader = clazz.getClassLoader();
          Class<?> classTextCell = classLoader.loadClass("com.google.gwt.cell.client.TextCell");
          arguments[0] = classTextCell.newInstance();
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ValueLabel
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object createValueLabel(Constructor<?> actualConstructor, Object[] arguments)
      throws Exception {
    Object valueLabel = actualConstructor.newInstance(arguments);
    // set text 
    {
      String rendererName;
      if (arguments[0] == null) {
        rendererName = "null";
      } else {
        rendererName = CodeUtils.getShortClass(arguments[0].getClass().getName());
      }
      String text = "ValueLabel(" + rendererName + ")";
      setValueLabelText(valueLabel, text);
    }
    // done
    return valueLabel;
  }

  private static Object createDateLabel(ClassInstanceCreation expression,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    Object valueLabel = actualConstructor.newInstance(arguments);
    setValueLabelText(valueLabel, "12/31/2010");
    return valueLabel;
  }

  private static Object createNumberLabel(ClassInstanceCreation expression,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    Object valueLabel = actualConstructor.newInstance(arguments);
    // prepare text to show
    String text;
    {
      ITypeBinding creationBinding = AstNodeUtils.getTypeBinding(expression);
      ITypeBinding typeBinding =
          AstNodeUtils.getTypeBindingArgument(
              creationBinding,
              "com.google.gwt.user.client.ui.NumberLabel",
              0);
      String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
      text = MessageFormat.format("NumberLabel<{0}>", CodeUtils.getShortClass(typeName));
    }
    // set text
    setValueLabelText(valueLabel, text);
    return valueLabel;
  }

  /**
   * Sets text for given <code>ValueLabel</code> widget.
   */
  public static void setValueLabelText(Object valueLabel, String text) throws Exception {
    Object directionalTextHelper =
        ReflectionUtils.getFieldObject(valueLabel, "directionalTextHelper");
    ReflectionUtils.invokeMethod(
        directionalTextHelper,
        "setTextOrHtml(java.lang.String,boolean)",
        text,
        false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exception
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Throwable rewriteException(Throwable e) {
    if (isMailExampleException(e)) {
      String userStackTrace = AstEvaluationEngine.getUserStackTrace(e);
      return new DesignerException(IExceptionConstants.MAIL_SAMPLE_GET, e, userStackTrace);
    }
    return null;
  }

  /**
   * @return <code>true</code> if given {@link Throwable} is caused by GWT Mail example.
   */
  private static boolean isMailExampleException(Throwable e) {
    StackTraceElement[] stackTrace = e.getStackTrace();
    StackTraceElement element = stackTrace[0];
    return "com.google.gwt.sample.mail.client.MailList".equals(element.getClassName())
        && "selectRow".equals(element.getMethodName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isWidget(Class<?> clazz) {
    return ReflectionUtils.isSuccessorOf(clazz, "com.google.gwt.user.client.ui.Widget");
  }

  private static boolean isJavaScriptObject_rewrittenInterface(Class<?> clazz) {
    return clazz.isInterface() && ReflectionUtils.isSuccessorOf(clazz, JSO_NAME);
  }

  /**
   * @return the <code>Widget</code> to use as placeholder instead of real component that can not be
   *         created because of some exception.
   */
  private static Object createPlaceholder(Class<?> clazz) throws Exception {
    String message =
        MessageFormat.format(
            "Exception during creation of: {0}. See \"Open error log\" for details.",
            CodeUtils.getShortClass(clazz.getName()));
    // script
    String script;
    {
      AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
      ComponentDescription description = ComponentDescriptionHelper.getDescription(editor, clazz);
      script = description.getParameter("placeholderScript");
    }
    // variables
    Map<String, Object> variables = Maps.newTreeMap();
    variables.put("clazz", clazz);
    variables.put("message", message);
    // execute
    ClassLoader classLoader = clazz.getClassLoader();
    return ScriptUtils.evaluate(classLoader, script, variables);
  }
}
