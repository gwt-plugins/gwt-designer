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
package com.google.gdt.eclipse.designer.gxt.databinding.model.widgets;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingsInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.ComboBoxFieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FormBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.TimeFieldBindingInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class WidgetsObserveTypeContainer extends ObserveTypeContainer {
  private static final String FIELD_BINDINGS =
      "com.extjs.gxt.ui.client.binding.FieldBinding.<init>(com.extjs.gxt.ui.client.widget.form.Field,java.lang.String)";
  private static final String COMBOBOX_FIELD_BINDINGS =
      "com.extjs.gxt.ui.client.binding.SimpleComboBoxFieldBinding.<init>(com.extjs.gxt.ui.client.widget.form.SimpleComboBox,java.lang.String)";
  private static final String TIME_FIELD_BINDINGS =
      "com.extjs.gxt.ui.client.binding.TimeFieldBinding.<init>(com.extjs.gxt.ui.client.widget.form.TimeField,java.lang.String)";
  private static final String BINDINGS = "com.extjs.gxt.ui.client.binding.Bindings.<init>()";
  private static final String FORM_BINDING_1 =
      "com.extjs.gxt.ui.client.binding.FormBinding.<init>(com.extjs.gxt.ui.client.widget.form.FormPanel)";
  private static final String FORM_BINDING_2 =
      "com.extjs.gxt.ui.client.binding.FormBinding.<init>(com.extjs.gxt.ui.client.widget.form.FormPanel,boolean)";
  private List<WidgetObserveInfo> m_observables = Collections.emptyList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetsObserveTypeContainer() {
    super(ObserveType.WIDGETS, true, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveTypeContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getObservables() {
    return CoreUtils.cast(m_observables);
  }

  @Override
  public void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    PropertiesSupport propertiesSupport = new PropertiesSupport(CoreUtils.classLoader(root));
    //
    m_observables = Lists.newArrayList();
    m_observables.add(new WidgetObserveInfo(root, null, propertiesSupport));
    m_observables.add(new BindingsWidgetObserveInfo(propertiesSupport));
  }

  @Override
  public void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
      throws Exception {
    for (WidgetObserveInfo widget : m_observables) {
      widget.update();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    if (BINDINGS.equals(signature)) {
      BindingsInfo binding = new BindingsInfo();
      binding.setTarget(m_observables.get(1), m_observables.get(1).getSelfProperty());
      provider.getBindings().add(binding);
      return binding;
    }
    //
    if (FIELD_BINDINGS.equals(signature)
        || COMBOBOX_FIELD_BINDINGS.equals(signature)
        || TIME_FIELD_BINDINGS.equals(signature)
        || FORM_BINDING_1.equals(signature)
        || FORM_BINDING_2.equals(signature)) {
      WidgetObserveInfo target = getBindableWidget(arguments[0]);
      if (target == null) {
        AbstractParser.addError(
            editor,
            "Widget argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      WidgetPropertyObserveInfo targetProperty = target.getSelfProperty();
      Assert.isNotNull(targetProperty);
      //
      if (FORM_BINDING_1.equals(signature) || FORM_BINDING_2.equals(signature)) {
        FormBindingInfo binding = new FormBindingInfo(target, targetProperty);
        //
        if (arguments.length == 2) {
          boolean autobind = CoreUtils.evaluate(Boolean.class, editor, arguments[1]);
          binding.setAutobind(autobind);
        }
        //
        provider.getBindings().add(binding);
        return binding;
      }
      //
      if (FIELD_BINDINGS.equals(signature)
          || COMBOBOX_FIELD_BINDINGS.equals(signature)
          || TIME_FIELD_BINDINGS.equals(signature)) {
        String property = CoreUtils.evaluate(String.class, editor, arguments[1]);
        Assert.isNotNull(property);
        property = "\"" + property + "\"";
        //
        if (FIELD_BINDINGS.equals(signature)) {
          return new FieldBindingInfo(target, targetProperty, property);
        }
        if (COMBOBOX_FIELD_BINDINGS.equals(signature)) {
          return new ComboBoxFieldBindingInfo(target, targetProperty, property);
        }
        if (TIME_FIELD_BINDINGS.equals(signature)) {
          return new TimeFieldBindingInfo(target, targetProperty, property);
        }
      }
    }
    //
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link WidgetObserveInfo} association with given {@link Expression}.
   */
  public WidgetObserveInfo getBindableWidget(Expression expression) throws Exception {
    return m_observables.get(0).resolveReference(expression);
  }

  public WidgetObserveInfo resolve(JavaInfo javaInfo) {
    return m_observables.get(0).resolve(javaInfo);
  }
}