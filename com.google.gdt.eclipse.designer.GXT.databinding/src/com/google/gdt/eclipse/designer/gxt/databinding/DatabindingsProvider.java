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
package com.google.gdt.eclipse.designer.gxt.databinding;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.model.DataBindingsRootInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.JavaInfoDecorator;
import com.google.gdt.eclipse.designer.gxt.databinding.model.JavaInfoDeleteManager;
import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeansObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingsInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FormBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.BindingsWidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetsObserveTypeContainer;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.property.JavaInfoPropertiesManager;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.BindingLabelProvider;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class DatabindingsProvider implements IDatabindingsProvider {
  private final JavaInfo m_javaInfoRoot;
  private final List<ObserveTypeContainer> m_containers = Lists.newArrayList();
  private final DataBindingsRootInfo m_rootInfo = new DataBindingsRootInfo();
  private final List<BindingInfo> m_bindings = Lists.newArrayList();
  private JavaInfoDeleteManager m_javaInfoDeleteManager;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DatabindingsProvider(JavaInfo javaInfoRoot) throws Exception {
    m_javaInfoRoot = javaInfoRoot;
    m_containers.add(new BeansObserveTypeContainer());
    m_containers.add(new WidgetsObserveTypeContainer());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  public static DatabindingsProvider cast(IDatabindingsProvider provider) {
    return (DatabindingsProvider) provider;
  }

  public BeansObserveTypeContainer getBeansContainer() {
    return (BeansObserveTypeContainer) m_containers.get(0);
  }

  public WidgetsObserveTypeContainer getWidgetsContainer() {
    return (WidgetsObserveTypeContainer) m_containers.get(1);
  }

  public JavaInfo getJavaInfoRoot() {
    return m_javaInfoRoot;
  }

  public List<ObserveTypeContainer> getContainers() {
    return m_containers;
  }

  public DataBindingsRootInfo getRootInfo() {
    return m_rootInfo;
  }

  public void hookJavaInfoEvents() throws Exception {
    // update observes
    new SynchronizeManager(this, m_javaInfoRoot);
    // handle delete info's
    m_javaInfoDeleteManager = new JavaInfoDeleteManager(this);
    // decorate info's
    new JavaInfoDecorator(this);
    // properties
    new JavaInfoPropertiesManager(this, m_javaInfoRoot);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bindings
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configureBindingViewer(IDialogSettings settings, TableViewer viewer) {
    // prepare table
    Table table = viewer.getTable();
    // binding type image column
    TableColumn bindingColumn = new TableColumn(table, SWT.NONE);
    bindingColumn.setWidth(23);
    bindingColumn.setResizable(false);
    // target column
    TableColumn targetColumn = UiUtils.createSmartColumn(table, settings, "TargetColumn", 300);
    targetColumn.setText("Target");
    // model column
    TableColumn modelColumn = UiUtils.createSmartColumn(table, settings, "ModelColumn", 300);
    modelColumn.setText("Model");
    // binding variable column
    TableColumn variableBindingColumn =
        UiUtils.createSmartColumn(table, settings, "VariableBindingColumn", 250);
    variableBindingColumn.setText("Binding");
    // label provider
    viewer.setLabelProvider(new BindingLabelProvider());
  }

  public List<IBindingInfo> getBindings() {
    return CoreUtils.cast(m_bindings);
  }

  public List<BindingInfo> getBindings0() {
    return m_bindings;
  }

  public String getBindingPresentationText(IBindingInfo binding) throws Exception {
    return null;
  }

  public void gotoDefinition(IBindingInfo ibinding) {
    try {
      BindingInfo binding = (BindingInfo) ibinding;
      String source = binding.getDefinitionSource();
      //
      if (source != null) {
        int position = m_javaInfoRoot.getEditor().getEnclosingNode(source).getStartPosition();
        IDesignPageSite site = IDesignPageSite.Helper.getSite(m_javaInfoRoot);
        site.openSourcePosition(position);
      }
    } catch (Throwable e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Types
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<ObserveType> getTypes() {
    return ObserveType.TYPES;
  }

  public ObserveType getTargetStartType() {
    return ObserveType.WIDGETS;
  }

  public ObserveType getModelStartType() {
    return ObserveType.BEANS;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Observes
  //
  ////////////////////////////////////////////////////////////////////////////
  public IBaseLabelProvider createPropertiesViewerLabelProvider(TreeViewer viewer) {
    return new ObserveDecoratingLabelProvider(viewer);
  }

  public List<PropertyFilter> getObservePropertyFilters() {
    return Collections.emptyList();
  }

  public List<IObserveInfo> getObserves(ObserveType type) {
    for (ObserveTypeContainer container : m_containers) {
      if (container.getObserveType() == type) {
        return container.getObservables();
      }
    }
    //
    return Collections.emptyList();
  }

  public void synchronizeObserves() throws Exception {
    // prepare editor
    AstEditor editor = m_javaInfoRoot.getEditor();
    // prepare node
    TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(m_javaInfoRoot);
    if (rootNode == null) {
      // use first type declaration from compilation unit 
      CompilationUnit astUnit = editor.getAstUnit();
      rootNode = (TypeDeclaration) astUnit.types().get(0);
    }
    // synchronize
    for (ObserveTypeContainer container : m_containers) {
      container.synchronizeObserves(m_javaInfoRoot, editor, rootNode);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<IUiContentProvider> getContentProviders(IBindingInfo ibinding, IPageListener listener)
      throws Exception {
    List<IUiContentProvider> providers = Lists.newArrayList();
    BindingInfo binding = (BindingInfo) ibinding;
    binding.createContentProviders(providers, listener, this);
    return providers;
  }

  public boolean validate(IObserveInfo target,
      IObserveInfo targetProperty,
      IObserveInfo model,
      IObserveInfo modelProperty) throws Exception {
    // ignore itself
    if (target == model && targetProperty == modelProperty) {
      return false;
    }
    // prepare ObserveInfo's
    ObserveInfo targetObserve = (ObserveInfo) target;
    ObserveInfo targetPropertyObserve = (ObserveInfo) targetProperty;
    ObserveInfo modelObserve = (ObserveInfo) model;
    ObserveInfo modelPropertyObserve = (ObserveInfo) modelProperty;
    // check over reference's
    if (targetObserve.getReference().equals(modelObserve.getReference())
        && targetPropertyObserve.getReference().equals(modelPropertyObserve.getReference())) {
      return false;
    }
    //
    if (model instanceof BeanObserveInfo && target instanceof BeanObserveInfo) {
      return false;
    }
    if (model instanceof WidgetObserveInfo && target instanceof WidgetObserveInfo) {
      WidgetObserveInfo widgetModel = (WidgetObserveInfo) model;
      WidgetObserveInfo widgetTarget = (WidgetObserveInfo) target;
      //
      if (widgetModel.isGrid() && widgetModel.getSelfProperty() == modelProperty) {
        return widgetTarget.isFormPanel() && widgetTarget.getSelfProperty() == targetProperty;
      }
      if (widgetTarget.isGrid() && widgetTarget.getSelfProperty() == targetProperty) {
        return widgetModel.isFormPanel() && widgetModel.getSelfProperty() == modelProperty;
      }
      //
      return false;
    }
    if (model instanceof BeanObserveInfo) {
      BeanObserveInfo beanObserve = (BeanObserveInfo) model;
      if (modelProperty == beanObserve.getSelfProperty()) {
        WidgetObserveInfo widgetObserve = (WidgetObserveInfo) target;
        if ((!widgetObserve.isFormPanel() || !(widgetObserve instanceof BindingsWidgetObserveInfo))
            && targetProperty != widgetObserve.getSelfProperty()) {
          return false;
        }
      }
    }
    if (target instanceof BeanObserveInfo) {
      BeanObserveInfo beanObserve = (BeanObserveInfo) target;
      if (targetProperty == beanObserve.getSelfProperty()) {
        WidgetObserveInfo widgetObserve = (WidgetObserveInfo) model;
        if ((!widgetObserve.isFormPanel() || !(widgetObserve instanceof BindingsWidgetObserveInfo))
            && modelProperty != widgetObserve.getSelfProperty()) {
          return false;
        }
      }
    }
    if (model instanceof WidgetObserveInfo) {
      WidgetObserveInfo widgetObserve = (WidgetObserveInfo) model;
      if (widgetObserve.isGrid()) {
        return false;
      }
      if ((widgetObserve.isFormPanel() || widgetObserve instanceof BindingsWidgetObserveInfo)
          && modelProperty == widgetObserve.getSelfProperty()) {
        BeanObserveInfo beanObserve = (BeanObserveInfo) target;
        if (targetProperty != beanObserve.getSelfProperty()) {
          return false;
        }
      }
    }
    if (target instanceof WidgetObserveInfo) {
      WidgetObserveInfo widgetObserve = (WidgetObserveInfo) target;
      if (widgetObserve.isGrid()) {
        return false;
      }
      if ((widgetObserve.isFormPanel() || widgetObserve instanceof BindingsWidgetObserveInfo)
          && targetProperty == widgetObserve.getSelfProperty()) {
        BeanObserveInfo beanObserve = (BeanObserveInfo) model;
        if (modelProperty != beanObserve.getSelfProperty()) {
          return false;
        }
      }
    }
    return true;
  }

  public IBindingInfo createBinding(IObserveInfo target,
      IObserveInfo itargetProperty,
      IObserveInfo model,
      IObserveInfo imodelProperty) throws Exception {
    BeanObserveInfo beanObserve = null;
    ObserveInfo modelProperty = null;
    WidgetObserveInfo widgetObserve = null;
    ObserveInfo targetProperty = null;
    //
    if (target instanceof WidgetObserveInfo && model instanceof WidgetObserveInfo) {
      ObserveInfo gridObserve = null;
      //
      if (((WidgetObserveInfo) target).isFormPanel()) {
        widgetObserve = (WidgetObserveInfo) target;
        targetProperty = (ObserveInfo) itargetProperty;
        //
        gridObserve = (ObserveInfo) model;
        modelProperty = (ObserveInfo) imodelProperty;
      } else {
        widgetObserve = (WidgetObserveInfo) model;
        targetProperty = (ObserveInfo) imodelProperty;
        //
        gridObserve = (ObserveInfo) target;
        modelProperty = (ObserveInfo) itargetProperty;
      }
      //
      FormBindingInfo formBinding = new FormBindingInfo(widgetObserve, targetProperty);
      formBinding.setModel(gridObserve, modelProperty);
      return formBinding;
    }
    //
    if (target instanceof WidgetObserveInfo) {
      beanObserve = (BeanObserveInfo) model;
      modelProperty = (ObserveInfo) imodelProperty;
      //
      widgetObserve = (WidgetObserveInfo) target;
      targetProperty = (ObserveInfo) itargetProperty;
    } else {
      beanObserve = (BeanObserveInfo) target;
      modelProperty = (ObserveInfo) itargetProperty;
      //
      widgetObserve = (WidgetObserveInfo) model;
      targetProperty = (ObserveInfo) imodelProperty;
    }
    //
    if (modelProperty == beanObserve.getSelfProperty()) {
      if (widgetObserve.isFormPanel()) {
        FormBindingInfo formBinding = new FormBindingInfo(widgetObserve, targetProperty);
        formBinding.setModel(beanObserve, modelProperty);
        return formBinding;
      }
      if (widgetObserve instanceof BindingsWidgetObserveInfo) {
        BindingsInfo bindings = new BindingsInfo();
        bindings.setTarget(widgetObserve, targetProperty);
        bindings.setModel(beanObserve, modelProperty);
        return bindings;
      }
    }
    //
    FieldBindingInfo fieldBinding = widgetObserve.createFieldBinding(targetProperty, null);
    fieldBinding.setModel(beanObserve, modelProperty);
    return fieldBinding;
  }

  public void addBinding(IBindingInfo ibinding) {
    // add
    final BindingInfo binding = (BindingInfo) ibinding;
    m_bindings.add(binding);
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        binding.create(m_bindings);
      }
    });
    // save
    saveEdit();
  }

  public void editBinding(final IBindingInfo ibinding) {
    // edit
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        BindingInfo binding = (BindingInfo) ibinding;
        binding.edit(m_bindings);
      }
    });
    // save
    saveEdit();
  }

  public void deleteBinding(IBindingInfo ibinding) {
    // delete
    final BindingInfo binding = (BindingInfo) ibinding;
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        binding.delete(m_bindings, false);
      }
    });
    m_bindings.remove(binding);
    // save
    saveEdit();
  }

  public void deleteAllBindings() {
    // delete all
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        while (!m_bindings.isEmpty()) {
          m_bindings.remove(0).delete(m_bindings, true);
        }
      }
    });
    // save
    saveEdit();
  }

  public void deleteBindings(JavaInfo javaInfo) throws Exception {
    if (m_javaInfoDeleteManager != null) {
      m_javaInfoDeleteManager.deleteJavaInfo(javaInfo);
    }
  }

  public boolean canMoveBinding(IBindingInfo ibinding, int targetIndex, boolean upDown) {
    if (ibinding instanceof FieldBindingInfo) {
      FieldBindingInfo binding = (FieldBindingInfo) ibinding;
      if (binding.getParentBinding() == null) {
        BindingInfo target = m_bindings.get(targetIndex);
        if (target instanceof FieldBindingInfo) {
          FieldBindingInfo fieldTarget = (FieldBindingInfo) target;
          return fieldTarget.getParentBinding() == null;
        }
      } else {
        if (binding.isAutobind()) {
          return false;
        }
        BindingInfo target = m_bindings.get(targetIndex);
        if (target instanceof FieldBindingInfo) {
          FieldBindingInfo fieldTarget = (FieldBindingInfo) target;
          return binding.getParentBinding() == fieldTarget.getParentBinding()
              && !fieldTarget.isAutobind();
        }
        return false;
      }
    }
    return true;
  }

  public void moveBinding(IBindingInfo ibinding, int sourceIndex, int targetIndex, boolean upDown) {
    // configure target index
    if (upDown) {
      targetIndex = configureMoveUpDown(ibinding, sourceIndex, targetIndex);
    }
    //
    BindingInfo binding = (BindingInfo) ibinding;
    // do reorder
    m_bindings.remove(sourceIndex);
    m_bindings.add(targetIndex, binding);
    binding.move(m_bindings);
    // save
    saveEdit();
  }

  private int configureMoveUpDown(IBindingInfo ibinding, int sourceIndex, int targetIndex) {
    if (ibinding instanceof FieldBindingInfo) {
      FieldBindingInfo binding = (FieldBindingInfo) ibinding;
      if (binding.getParentBinding() != null) {
        return targetIndex;
      }
    }
    //
    boolean up = sourceIndex > targetIndex;
    BindingInfo target = m_bindings.get(targetIndex);
    // configure target index
    if (target instanceof FieldBindingInfo) {
      FieldBindingInfo fieldTarget = (FieldBindingInfo) target;
      BindingsInfo parentBinding = fieldTarget.getParentBinding();
      //
      if (parentBinding != null) {
        if (up) {
          targetIndex = m_bindings.indexOf(parentBinding);
        } else {
          // skip all field bindings
          targetIndex += parentBinding.getFieldBindings().size();
          //
          target = m_bindings.get(targetIndex);
          if (target instanceof BindingsInfo) {
            // skip all field bindings
            BindingsInfo bindings = (BindingsInfo) target;
            targetIndex += bindings.getFieldBindings().size();
          }
        }
      }
    } else if (target instanceof BindingsInfo && !up) {
      // skip all field bindings
      BindingsInfo bindings = (BindingsInfo) target;
      targetIndex += bindings.getFieldBindings().size();
    }
    //
    return targetIndex;
  }

  public void saveEdit() {
    ExecutionUtils.run(m_javaInfoRoot, new RunnableEx() {
      public void run() throws Exception {
        m_rootInfo.commit(DatabindingsProvider.this);
      }
    });
  }

  public void setBindingPage(Object bindingPage) {
  }

  public void refreshDesigner() {
  }

  public void fillExternalBindingActions(ToolBar toolBar, Menu contextMenu) {
  }
}