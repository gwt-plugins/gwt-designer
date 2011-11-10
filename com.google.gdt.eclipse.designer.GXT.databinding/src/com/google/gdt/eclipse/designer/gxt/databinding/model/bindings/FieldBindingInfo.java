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
package com.google.gdt.eclipse.designer.gxt.databinding.model.bindings;

import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanPropertyObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeansObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders.ChooseClassAndPropertiesUiContentProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders.ConverterUiContentProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.EmptyPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class FieldBindingInfo extends BindingInfo {
  private static final String BIND =
      "com.extjs.gxt.ui.client.binding.FieldBinding.bind(com.extjs.gxt.ui.client.data.ModelData)";
  private static final String SET_STORE_START =
      "com.extjs.gxt.ui.client.binding.FieldBinding.setStore(com.extjs.gxt.ui.client.store.Store";
  private static final String SET_CONVERTER =
      "com.extjs.gxt.ui.client.binding.FieldBinding.setConverter(com.extjs.gxt.ui.client.binding.Converter)";
  //
  private static ChooseClassConfiguration m_configuration;
  //
  private final String m_parsedProperty;
  private ConverterInfo m_converter;
  private String m_storeReference;
  private BindingsInfo m_parentBinding;
  private boolean m_autobind;
  protected String m_baseClassName = "com.extjs.gxt.ui.client.binding.FieldBinding";
  private BeanObserveInfo m_gridSelectionModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldBindingInfo(ObserveInfo target, ObserveInfo targetProperty, String parsedProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
    m_parsedProperty = parsedProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingsInfo getParentBinding() {
    return m_parentBinding;
  }

  public void setParentBinding(BindingsInfo parentBinding) {
    m_parentBinding = parentBinding;
  }

  public boolean isAutobind() {
    return m_autobind;
  }

  public void setAutobind(boolean autobind) {
    m_autobind = autobind;
  }

  public String getParsedProperty() {
    return m_parsedProperty;
  }

  public ConverterInfo getConverter() {
    return m_converter;
  }

  public void setConverter(ConverterInfo converter) {
    m_converter = converter;
  }

  public void setGridSelectionModel(BeanObserveInfo model) {
    m_gridSelectionModel = model;
  }

  public void updateGridSelectionModel() throws Exception {
    if (m_autobind && m_gridSelectionModel != null) {
      boolean create = m_modelProperty == null;
      m_modelProperty = m_gridSelectionModel.resolvePropertyReference(m_parsedProperty, null);
      if (!create) {
        m_modelProperty.createBinding(FieldBindingInfo.this);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void move(List<BindingInfo> bindings) {
    if (m_parentBinding != null) {
      m_parentBinding.getFieldBindings().remove(this);
      int index = bindings.indexOf(this) - bindings.indexOf(m_parentBinding) - 1;
      m_parentBinding.getFieldBindings().add(index, this);
    }
  }

  @Override
  public void delete(List<BindingInfo> bindings, boolean deleteAll) throws Exception {
    super.delete(bindings, deleteAll);
    //
    if (m_parentBinding != null) {
      m_parentBinding.getFieldBindings().remove(this);
      //
      if (m_autobind && m_parentBinding instanceof FormBindingInfo) {
        FormBindingInfo parentBinding = (FormBindingInfo) m_parentBinding;
        if (parentBinding.isAutobind() && !deleteAll) {
          WidgetObserveInfo field = (WidgetObserveInfo) m_target;
          Property property = field.getJavaInfo().getPropertyByTitle("name");
          if (property != null) {
            property.setValue(Property.UNKNOWN_VALUE);
          }
        }
      }
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
    if (BIND.equals(signature)) {
      BeansObserveTypeContainer container = DatabindingsProvider.cast(provider).getBeansContainer();
      BeanObserveInfo beanObserveObject = container.getBeanObserveObject(arguments[0]);
      setModel(
          beanObserveObject,
          beanObserveObject.resolvePropertyReference(m_parsedProperty, null));
      //
      provider.getBindings().add(this);
    } else if (signature.startsWith(SET_STORE_START)) {
      m_storeReference = CoreUtils.getNodeReference(arguments[0]);
    } else if (SET_CONVERTER.equals(signature)) {
      m_converter = (ConverterInfo) resolver.getModel(arguments[0]);
      if (m_converter == null) {
        AbstractParser.addError(
            editor,
            "FieldBinding converter '" + arguments[0] + "' not found",
            new Throwable());
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(providers, listener, provider);
    if (!m_autobind) {
      if (m_configuration == null) {
        m_configuration = new ChooseClassConfiguration();
        m_configuration.setDialogFieldLabel("Converter:");
        m_configuration.setValueScope("com.extjs.gxt.ui.client.binding.Converter");
        m_configuration.setClearValue("N/S");
        m_configuration.setBaseClassName("com.extjs.gxt.ui.client.binding.Converter");
        m_configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
        m_configuration.setEmptyClassErrorMessage("Converter class is empty.");
        m_configuration.setErrorMessagePrefix("Converter");
      }
      //
      m_configuration.clearDefaultStrings();
      if (m_converter != null && m_converter.getClassName().indexOf('(') != -1) {
        m_configuration.addDefaultStart(m_converter.getClassName());
      }
      //
      providers.add(new ConverterUiContentProvider(m_configuration, this));
      //
      if (m_parentBinding != null && listener == EmptyPageListener.INSTANCE) {
        ChooseClassAndPropertiesConfiguration configuration =
            new ChooseClassAndPropertiesConfiguration();
        configuration.setBaseClassName("com.extjs.gxt.ui.client.data.ModelData");
        configuration.setValueScope("beans");
        configuration.setDialogFieldLabel("Model:");
        configuration.setDialogFieldEnabled(false);
        configuration.setPropertiesLabel("Model property:");
        configuration.setLoadedPropertiesCheckedStrategy(LoadedPropertiesCheckedStrategy.None);
        configuration.setPropertiesErrorMessage("Choose model property.");
        //
        providers.add(new ChooseClassAndPropertiesUiContentProvider(configuration,
            provider.getBeansContainer().getBeanSupport()) {
          public void updateFromObject() throws Exception {
            if (m_modelProperty == null) {
              BeanObserveInfo model = getLocalModel();
              if (model == null) {
                calculateFinish();
              } else {
                setClassName(model.getObjectType().getName());
              }
            } else {
              BeanPropertyObserveInfo modelProperty = (BeanPropertyObserveInfo) m_modelProperty;
              setClassNameAndProperty(
                  getLocalModel().getObjectType().getName(),
                  new PropertyAdapter(modelProperty.getPresentation().getText(),
                      modelProperty.getObjectType()),
                  true);
            }
          }

          @Override
          protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
              throws Exception {
            boolean create = m_modelProperty == null;
            m_modelProperty =
                getLocalModel().resolvePropertyReference(
                    "\"" + choosenProperties.get(0).getName() + "\"",
                    null);
            if (!create) {
              m_modelProperty.createBinding(FieldBindingInfo.this);
            }
          }
        });
      }
    }
  }

  private BeanObserveInfo getLocalModel() {
    if (m_gridSelectionModel == null) {
      if (m_model instanceof BeanObserveInfo) {
        return (BeanObserveInfo) m_model;
      }
    }
    return m_gridSelectionModel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean addSourceCodeSeparator() {
    return m_parentBinding == null;
  }

  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    if (m_parentBinding == null) {
      addSourceCode0(lines, generationSupport);
    }
  }

  public void addSourceCode0(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    if (m_autobind) {
      return;
    }
    //
    String sourceCode =
        "new "
            + m_baseClassName
            + "("
            + m_target.getReference()
            + ", "
            + m_modelProperty.getReference()
            + ")";
    String variable = getVariableIdentifier();
    if (variable == null && (m_parentBinding == null || m_converter != null)) {
      variable = generationSupport.generateLocalName("fieldBinding");
      setVariableIdentifier(variable);
    }
    if (variable != null) {
      String startCode = isField() ? "" : m_baseClassName + " ";
      sourceCode = startCode + variable + " = " + sourceCode;
    }
    //
    if (m_parentBinding == null || variable != null) {
      lines.add(sourceCode + ";");
      sourceCode = variable;
    }
    //
    if (m_converter != null) {
      lines.add(variable
          + ".setConverter("
          + m_converter.getSourceCode(lines, generationSupport)
          + ");");
    }
    //
    if (m_parentBinding == null) {
      if (m_storeReference != null) {
        lines.add(variable + ".setStore(" + m_storeReference + ");");
      }
      lines.add(variable + ".bind(" + m_model.getReference() + ");");
    } else {
      lines.add(m_parentBinding.getVariableIdentifier() + ".addFieldBinding(" + sourceCode + ");");
    }
  }

  @Override
  public String getDefinitionSource() throws Exception {
    if (!m_autobind) {
      String sourceCode =
          "new "
              + ClassUtils.getShortClassName(m_baseClassName)
              + "("
              + m_target.getReference()
              + ", "
              + m_modelProperty.getReference()
              + ")";
      String variable = getVariableIdentifier();
      if (m_parentBinding != null && variable == null) {
        return m_parentBinding.getVariableIdentifier() + ".addFieldBinding(" + sourceCode + ")";
      }
      if (variable == null) {
        return sourceCode;
      }
      return ClassUtils.getShortClassName(m_baseClassName) + " " + variable + " = " + sourceCode;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
    setVariableIdentifier(javaInfoRoot, m_baseClassName, variable, field);
  }
}