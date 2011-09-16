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
package com.google.gdt.eclipse.designer.gxt.databinding.model.bindings;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeansObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders.BindingsUiContentProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class BindingsInfo extends BindingInfo {
  private static final String ADD_FIELD_BINDING =
      "com.extjs.gxt.ui.client.binding.Bindings.addFieldBinding(com.extjs.gxt.ui.client.binding.FieldBinding)";
  private static final String BIND =
      "com.extjs.gxt.ui.client.binding.Bindings.bind(com.extjs.gxt.ui.client.data.ModelData)";
  //
  protected final List<FieldBindingInfo> m_fieldBindings = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<FieldBindingInfo> getFieldBindings() {
    return m_fieldBindings;
  }

  public void setTarget(ObserveInfo target, ObserveInfo targetProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldBindingInfo createFieldBinding(WidgetObserveInfo field) {
    FieldBindingInfo binding = field.createFieldBinding(field.getSelfProperty(), null);
    binding.setModel(m_model, null);
    binding.setParentBinding(this);
    return binding;
  }

  public void setFieldBindings(List<FieldBindingInfo> newBindings, DatabindingsProvider provider)
      throws Exception {
    List<FieldBindingInfo> fieldBindings = Lists.newArrayList(m_fieldBindings);
    List<BindingInfo> bindings = provider.getBindings0();
    for (FieldBindingInfo binding : fieldBindings) {
      if (!newBindings.contains(binding)) {
        binding.delete(bindings, false);
      }
    }
    for (FieldBindingInfo binding : newBindings) {
      if (!m_fieldBindings.contains(binding)) {
        binding.create(bindings);
      }
    }
    //
    bindings.removeAll(fieldBindings);
    m_fieldBindings.clear();
    m_fieldBindings.addAll(newBindings);
    //
    int index = bindings.indexOf(this);
    if (index != -1) {
      bindings.addAll(index + 1, m_fieldBindings);
    }
  }

  @Override
  public void create(List<BindingInfo> bindings) throws Exception {
    super.create(bindings);
    //
    int index = bindings.indexOf(this) + 1;
    int size = m_fieldBindings.size();
    for (int i = 0; i < size; i++) {
      FieldBindingInfo binding = m_fieldBindings.get(i);
      if (bindings.indexOf(binding) == -1) {
        bindings.add(index + i, binding);
      }
    }
  }

  @Override
  public void move(List<BindingInfo> bindings) {
    if (!m_fieldBindings.isEmpty()) {
      bindings.removeAll(m_fieldBindings);
      int index = bindings.indexOf(this) + 1;
      bindings.addAll(index, m_fieldBindings);
    }
  }

  @Override
  public void delete(List<BindingInfo> bindings, boolean deleteAll) throws Exception {
    super.delete(bindings, deleteAll);
    bindings.removeAll(m_fieldBindings);
    for (FieldBindingInfo binding : Lists.newArrayList(m_fieldBindings)) {
      binding.delete(bindings, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    if (ADD_FIELD_BINDING.equals(signature)) {
      FieldBindingInfo binding = (FieldBindingInfo) resolver.getModel(arguments[0]);
      if (binding == null) {
        AbstractParser.addError(
            editor,
            "FieldBinding '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      //
      m_fieldBindings.add(binding);
      binding.setParentBinding(this);
    } else if (BIND.equals(signature)) {
      finishBinding(arguments, provider);
    }
    return null;
  }

  protected void finishBinding(Expression[] arguments, IDatabindingsProvider provider)
      throws Exception {
    BeansObserveTypeContainer container = DatabindingsProvider.cast(provider).getBeansContainer();
    BeanObserveInfo beanObserveObject = container.getBeanObserveObject(arguments[0]);
    m_model = beanObserveObject;
    m_modelProperty = beanObserveObject.getSelfProperty();
    //
    for (FieldBindingInfo binding : m_fieldBindings) {
      binding.setModel(
          beanObserveObject,
          beanObserveObject.resolvePropertyReference(binding.getParsedProperty(), null));
    }
    //
    int index = provider.getBindings().indexOf(this);
    provider.getBindings().addAll(index + 1, m_fieldBindings);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(providers, listener, provider);
    //
    TabContainerConfiguration configuration = new TabContainerConfiguration();
    configuration.setUseMultiAddButton(true);
    configuration.setCreateEmptyPage("Bindings", "Add field bindings for this form.");
    //
    providers.add(new BindingsUiContentProvider(provider, configuration, this));
  }

  protected final void super_createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(providers, listener, provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    String variable = getVariableIdentifier();
    if (variable == null) {
      variable = generationSupport.generateLocalName("bindings");
      setVariableIdentifier(variable);
    }
    String startCode = isField() ? "" : "com.extjs.gxt.ui.client.binding.Bindings ";
    lines.add(startCode + variable + " = new com.extjs.gxt.ui.client.binding.Bindings();");
    //
    for (FieldBindingInfo binding : m_fieldBindings) {
      lines.add("//");
      binding.addSourceCode0(lines, generationSupport);
    }
    //
    lines.add("//");
    lines.add(variable + ".bind(" + m_model.getReference() + ");");
  }

  @Override
  public String getDefinitionSource() throws Exception {
    String variable = getVariableIdentifier();
    if (variable != null) {
      return "Bindings " + variable + " = new Bindings();";
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getModelPresentationText() throws Exception {
    return m_model.getPresentation().getTextForBinding();
  }

  protected final String super_getModelPresentationText() throws Exception {
    return super.getModelPresentationText();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
    setVariableIdentifier(javaInfoRoot, "com.extjs.gxt.ui.client.binding.Bindings", variable, field);
  }
}