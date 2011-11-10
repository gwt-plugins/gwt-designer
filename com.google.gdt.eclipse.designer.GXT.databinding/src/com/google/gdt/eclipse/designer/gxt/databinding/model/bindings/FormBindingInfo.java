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
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeansObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetsObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders.AutobindUiContentProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders.FormBindingUiContentProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassRouter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.MultiTargetRunnable;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class FormBindingInfo extends BindingsInfo {
  private static final String SET_STORE =
      "com.extjs.gxt.ui.client.binding.FormBinding.setStore(com.extjs.gxt.ui.client.store.Store)";
  private static final String AUTO_BIND = "com.extjs.gxt.ui.client.binding.FormBinding.autoBind()";
  private static final String BIND =
      "com.extjs.gxt.ui.client.binding.FormBinding.bind(com.extjs.gxt.ui.client.data.ModelData)";
  private String m_storeReference;
  private boolean m_autobind;
  private BeanObserveInfo m_gridSelectionModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormBindingInfo(ObserveInfo target, ObserveInfo targetProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isAutobind() {
    return m_autobind;
  }

  public void setAutobind(boolean autobind) {
    m_autobind = autobind;
  }

  public BeanObserveInfo getGridSelectionModel() {
    return m_gridSelectionModel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public FieldBindingInfo createFieldBinding(WidgetObserveInfo field) {
    FieldBindingInfo binding = super.createFieldBinding(field);
    binding.setGridSelectionModel(m_gridSelectionModel);
    return binding;
  }

  @Override
  public void delete(List<BindingInfo> bindings, boolean deleteAll) throws Exception {
    if (m_model instanceof WidgetObserveInfo) {
      WidgetObserveInfo model = (WidgetObserveInfo) m_model;
      if (model.isGrid()) {
        model.getSelfProperty().setProperties(Collections.<IObserveInfo>emptyList());
      }
    }
    super.delete(bindings, deleteAll);
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
    if (SET_STORE.equals(signature)) {
      m_storeReference = CoreUtils.getNodeReference(arguments[0]);
      return null;
    }
    if (AUTO_BIND.equals(signature)) {
      setAutobind(true);
      return null;
    }
    if (BIND.equals(signature)) {
      AnonymousClassDeclaration anonymousClassDeclaration =
          AstNodeUtils.getEnclosingNode(invocation, AnonymousClassDeclaration.class);
      if (anonymousClassDeclaration != null
          && anonymousClassDeclaration.getParent() instanceof ClassInstanceCreation
          && anonymousClassDeclaration.getParent().getParent() instanceof MethodInvocation) {
        MethodInvocation addListenerInvocation =
            (MethodInvocation) anonymousClassDeclaration.getParent().getParent();
        if ("addListener".equals(addListenerInvocation.getName().getIdentifier())
            && addListenerInvocation.getExpression() instanceof MethodInvocation) {
          MethodInvocation getSelectionModelInvocation =
              (MethodInvocation) addListenerInvocation.getExpression();
          if ("getSelectionModel".equals(getSelectionModelInvocation.getName().getIdentifier())) {
            finishBinding(getSelectionModelInvocation.getExpression(), arguments[0], provider);
            return null;
          }
        }
      }
      finishBinding(arguments, provider);
      return null;
    }
    //
    return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
  }

  @Override
  protected void finishBinding(Expression[] arguments, IDatabindingsProvider provider)
      throws Exception {
    if (m_autobind) {
      createAutobindings(m_fieldBindings, false);
    }
    super.finishBinding(arguments, provider);
  }

  private void finishBinding(Expression grid,
      Expression bindingElement,
      IDatabindingsProvider iprovider) throws Exception {
    DatabindingsProvider provider = DatabindingsProvider.cast(iprovider);
    BeansObserveTypeContainer beansContainer = provider.getBeansContainer();
    WidgetsObserveTypeContainer widgetsContainer = provider.getWidgetsContainer();
    //
    WidgetObserveInfo gridWidget = widgetsContainer.getBindableWidget(grid);
    m_model = gridWidget;
    m_modelProperty = gridWidget.getSelfProperty();
    //
    ClassLoader classLoader = CoreUtils.classLoader(provider.getJavaInfoRoot());
    String bindingElementClassName = AstNodeUtils.getFullyQualifiedName(bindingElement, true);
    Class<?> bindingElementClass = classLoader.loadClass(bindingElementClassName);
    //
    m_gridSelectionModel =
        new BeanObserveInfo(beansContainer.getBeanSupport(), m_modelProperty, bindingElementClass);
    gridWidget.getSelfProperty().setProperties(
        m_gridSelectionModel.getChildren(ChildrenContext.ChildrenForPropertiesTable));
    //
    for (FieldBindingInfo binding : m_fieldBindings) {
      binding.setModel(
          m_model,
          m_gridSelectionModel.resolvePropertyReference(binding.getParsedProperty(), null));
      binding.setGridSelectionModel(m_gridSelectionModel);
    }
    //
    int index = provider.getBindings().indexOf(this);
    provider.getBindings().addAll(index + 1, m_fieldBindings);
    //
    if (m_autobind) {
      createAutobindings(m_fieldBindings, true);
    }
  }

  public void createAutobindings(List<FieldBindingInfo> bindings) throws Exception {
    createAutobindings(bindings, true);
  }

  private void createAutobindings(List<FieldBindingInfo> bindings, boolean full) throws Exception {
    BeanObserveInfo beanObserveObject = null;
    if (full) {
      if (m_gridSelectionModel == null) {
        if (m_model instanceof BeanObserveInfo) {
          beanObserveObject = (BeanObserveInfo) m_model;
        }
      } else {
        beanObserveObject = m_gridSelectionModel;
      }
    }
    WidgetObserveInfo formPanel = (WidgetObserveInfo) m_target;
    for (WidgetObserveInfo field : formPanel.getChildren()) {
      Property property = field.getJavaInfo().getPropertyByTitle("name");
      if (property != null) {
        Object nameValue = property.getValue();
        if (nameValue instanceof String && !StringUtils.isEmpty(nameValue.toString())) {
          FieldBindingInfo binding =
              field.createFieldBinding(field.getSelfProperty(), "\"" + nameValue.toString() + "\"");
          if (full) {
            if (beanObserveObject == null) {
              binding.setModel(m_model, null);
            } else {
              binding.setModel(
                  beanObserveObject,
                  beanObserveObject.resolvePropertyReference(binding.getParsedProperty(), null));
            }
          }
          binding.setAutobind(true);
          binding.setParentBinding(this);
          bindings.add(binding);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      final DatabindingsProvider provider) throws Exception {
    super_createContentProviders(providers, listener, provider);
    //
    AutobindUiContentProvider autoBindEditor = new AutobindUiContentProvider(this);
    providers.add(autoBindEditor);
    //
    MultiTargetRunnable multiTargetRunnable = null;
    //
    if (m_model instanceof WidgetObserveInfo) {
      WidgetObserveInfo model = (WidgetObserveInfo) m_model;
      if (model.isGrid()) {
        ChooseClassConfiguration configuration = new ChooseClassConfiguration();
        configuration.setDialogFieldLabel("Grid selection element:");
        configuration.setBaseClassName("com.extjs.gxt.ui.client.data.ModelData");
        configuration.setValueScope("beans");
        configuration.setChooseInterfaces(true);
        configuration.setEmptyClassErrorMessage("Choose Grid selection element");
        configuration.setErrorMessagePrefix("Grid selection element");
        //
        ChooseClassUiContentProvider gridSelectionElementEditor =
            new ChooseClassUiContentProvider(configuration) {
              public void updateFromObject() throws Exception {
                if (m_gridSelectionModel == null) {
                  calculateFinish();
                } else {
                  setClassName(m_gridSelectionModel.getObjectType().getName());
                }
              }

              public void saveToObject() throws Exception {
                m_gridSelectionModel =
                    new BeanObserveInfo(provider.getBeansContainer().getBeanSupport(),
                        m_modelProperty,
                        getChoosenClass());
                ((WidgetObserveInfo) m_model).getSelfProperty().setProperties(
                    m_gridSelectionModel.getChildren(ChildrenContext.ChildrenForPropertiesTable));
              }
            };
        providers.add(gridSelectionElementEditor);
        //
        multiTargetRunnable = new MultiTargetRunnable(gridSelectionElementEditor);
        new ChooseClassRouter(gridSelectionElementEditor, multiTargetRunnable);
      }
    }
    //
    TabContainerConfiguration configuration = new TabContainerConfiguration();
    configuration.setUseMultiAddButton(true);
    configuration.setCreateEmptyPage("Bindings", "Add field bindings for this form.");
    //
    providers.add(new FormBindingUiContentProvider(provider,
        configuration,
        (BooleanDialogField) autoBindEditor.getDialogField(),
        multiTargetRunnable,
        this));
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
      variable = generationSupport.generateLocalName("formBinding");
      setVariableIdentifier(variable);
    }
    //
    String startPrefix = m_gridSelectionModel != null ? "final " : "";
    //
    String startCode =
        isField() ? "" : startPrefix + "com.extjs.gxt.ui.client.binding.FormBinding ";
    lines.add(startCode
        + variable
        + " = new com.extjs.gxt.ui.client.binding.FormBinding("
        + m_target.getReference()
        + ");");
    //
    if (!m_fieldBindings.isEmpty()) {
      lines.add("//");
    }
    //
    for (FieldBindingInfo binding : m_fieldBindings) {
      binding.addSourceCode0(lines, generationSupport);
    }
    //
    if (!m_fieldBindings.isEmpty()) {
      lines.add("//");
    }
    //
    if (m_gridSelectionModel == null) {
      if (m_autobind) {
        lines.add(variable + ".autoBind();");
      }
      if (m_storeReference != null) {
        lines.add(variable + ".setStore(" + m_storeReference + ");");
      }
      lines.add(variable + ".bind(" + m_model.getReference() + ");");
    } else {
      String gridSelectionElementClass = m_gridSelectionModel.getObjectType().getName();
      //
      lines.add(m_model.getReference()
          + ".getSelectionModel().addListener(com.extjs.gxt.ui.client.event.Events.SelectionChange,");
      lines.add("\t\tnew com.extjs.gxt.ui.client.event.Listener<com.extjs.gxt.ui.client.event.SelectionChangedEvent<"
          + gridSelectionElementClass
          + ">>() {");
      lines.add("\t\t\tpublic void handleEvent(com.extjs.gxt.ui.client.event.SelectionChangedEvent<"
          + gridSelectionElementClass
          + "> event) {");
      lines.add("\t\t\t\t"
          + gridSelectionElementClass
          + " selectionItem = event.getSelectedItem();");
      lines.add("\t\t\t\tif (selectionItem == null) {");
      lines.add("\t\t\t\t\t" + variable + ".unbind();");
      lines.add("\t\t\t\t} else {");
      lines.add("\t\t\t\t\t" + variable + ".bind(selectionItem);");
      lines.add("\t\t\t\t}");
      lines.add("\t\t\t}");
      lines.add("\t\t});");
    }
  }

  @Override
  public String getDefinitionSource() throws Exception {
    String sourceCode = "new FormBinding(" + m_target.getReference() + ");";
    String variable = getVariableIdentifier();
    if (variable == null) {
      return sourceCode;
    }
    return "FormBinding " + variable + " = " + sourceCode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field) {
    setVariableIdentifier(
        javaInfoRoot,
        "com.extjs.gxt.ui.client.binding.FormBinding",
        variable,
        field);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getModelPresentationText() throws Exception {
    if (m_model instanceof WidgetObserveInfo && ((WidgetObserveInfo) m_model).isGrid()) {
      return super_getModelPresentationText();
    }
    return super.getModelPresentationText();
  }
}