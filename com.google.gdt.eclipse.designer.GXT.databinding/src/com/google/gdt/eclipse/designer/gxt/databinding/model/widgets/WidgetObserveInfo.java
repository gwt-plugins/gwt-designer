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
package com.google.gdt.eclipse.designer.gxt.databinding.model.widgets;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.ComboBoxFieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.TimeFieldBindingInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author lobas_av
 * 
 */
public class WidgetObserveInfo extends ObserveInfo {
  private JavaInfo m_javaInfo;
  private final WidgetObserveInfo m_parent;
  private final List<WidgetObserveInfo> m_children = Lists.newArrayList();
  protected List<WidgetPropertyObserveInfo> m_properties;
  private final JavaInfoObservePresentation m_presentation;
  protected final PropertiesSupport m_propertiesSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetObserveInfo(JavaInfo javaInfo,
      WidgetObserveInfo parent,
      PropertiesSupport propertiesSupport) throws Exception {
    this(javaInfo.getDescription().getComponentClass(),
        new JavaInfoReferenceProvider(javaInfo),
        new JavaInfoObservePresentation(javaInfo),
        parent,
        propertiesSupport);
    m_javaInfo = javaInfo;
    // prepare children
    List<JavaInfo> childrenInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
    for (JavaInfo childInfo : childrenInfos) {
      m_children.add(new WidgetObserveInfo(childInfo, this, m_propertiesSupport));
    }
    // prepare properties
    m_properties = m_propertiesSupport.getProperties(getObjectType());
  }

  protected WidgetObserveInfo(Class<?> objectType,
      IReferenceProvider referenceProvider,
      JavaInfoObservePresentation presentation,
      WidgetObserveInfo parent,
      PropertiesSupport propertiesSupport) {
    super(objectType, referenceProvider);
    m_presentation = presentation;
    m_parent = parent;
    m_propertiesSupport = propertiesSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetPropertyObserveInfo getSelfProperty() {
    return m_properties.isEmpty() ? null : m_properties.get(0);
  }

  public boolean isFormPanel() {
    return m_propertiesSupport.getFormPanelClass().isAssignableFrom(getObjectType());
  }

  public boolean isGrid() {
    return m_propertiesSupport.getGridClass().isAssignableFrom(getObjectType());
  }

  public boolean isField() {
    return m_propertiesSupport.getFieldClass().isAssignableFrom(getObjectType());
  }

  public FieldBindingInfo createFieldBinding(ObserveInfo targetProperty, String parsedProperty) {
    if (m_propertiesSupport.getSimpleComboBoxClass().isAssignableFrom(getObjectType())) {
      return new ComboBoxFieldBindingInfo(this, targetProperty, parsedProperty);
    }
    if (m_propertiesSupport.getTimeFieldClass().isAssignableFrom(getObjectType())) {
      return new TimeFieldBindingInfo(this, targetProperty, parsedProperty);
    }
    return new FieldBindingInfo(this, targetProperty, parsedProperty);
  }

  public List<WidgetObserveInfo> getChildren() {
    return m_children;
  }

  public JavaInfo getJavaInfo() {
    return m_javaInfo;
  }

  private void setJavaInfo(JavaInfo javaInfo) throws Exception {
    // update info
    m_javaInfo = javaInfo;
    // update type
    setObjectType(m_javaInfo.getDescription().getComponentClass());
    // update reference
    JavaInfoReferenceProvider referenceProvider =
        (JavaInfoReferenceProvider) getReferenceProvider();
    referenceProvider.setJavaInfo(javaInfo);
    // update presentation
    m_presentation.setJavaInfo(m_javaInfo);
    // update properties
    m_properties = m_propertiesSupport.getProperties(getObjectType());
  }

  /**
   * Update (reorder, add, remove) children {@link WidgetBindableInfo}.
   */
  public void update() throws Exception {
    // prepare new javaInfo's
    List<JavaInfo> javaInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
    // synchronize
    SynchronizeManager.synchronizeObjects(
        m_children,
        javaInfos,
        new ISynchronizeProcessor<JavaInfo, WidgetObserveInfo>() {
          public boolean handleObject(WidgetObserveInfo widget) {
            return true;
          }

          public JavaInfo getKeyObject(WidgetObserveInfo widget) {
            return widget.m_javaInfo;
          }

          public boolean equals(JavaInfo key0, JavaInfo key1) {
            return key0 == key1;
          }

          public WidgetObserveInfo findObject(Map<JavaInfo, WidgetObserveInfo> javaInfoToWidget,
              JavaInfo javaInfo) throws Exception {
            VariableSupport variableSupport = javaInfo.getVariableSupport();
            for (Map.Entry<JavaInfo, WidgetObserveInfo> entry : javaInfoToWidget.entrySet()) {
              if (entry.getKey().getVariableSupport() == variableSupport) {
                WidgetObserveInfo widget = entry.getValue();
                widget.setJavaInfo(javaInfo);
                return widget;
              }
            }
            return null;
          }

          public WidgetObserveInfo createObject(JavaInfo javaInfo) throws Exception {
            return new WidgetObserveInfo(javaInfo, WidgetObserveInfo.this, m_propertiesSupport);
          }

          public void update(WidgetObserveInfo widget) throws Exception {
            widget.update();
          }
        });
  }

  /**
   * @return {@link WidgetObserveInfo} children that association with given {@link ASTNode} or
   *         <code>null</code>.
   */
  public WidgetObserveInfo resolveReference(ASTNode node) throws Exception {
    if (AstNodeUtils.isVariable(node)) {
      if (AstNodeUtils.getVariableName(node).equals(
          JavaInfoReferenceProvider.getReference(m_javaInfo))) {
        return this;
      }
    } else if (m_javaInfo.isRepresentedBy(node)) {
      return this;
    }
    for (WidgetObserveInfo child : m_children) {
      WidgetObserveInfo result = child.resolveReference(node);
      if (result != null) {
        return result;
      }
    }
    if (m_javaInfo.isRoot()) {
      JavaInfo javaInfo = m_javaInfo.getChildRepresentedBy(node);
      if (javaInfo != null) {
        return resolve(javaInfo);
      }
    }
    return null;
  }

  public WidgetObserveInfo resolve(JavaInfo javaInfo) {
    if (m_javaInfo == javaInfo) {
      return this;
    }
    for (WidgetObserveInfo child : m_children) {
      WidgetObserveInfo result = child.resolve(javaInfo);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createBinding(BindingInfo binding) throws Exception {
    super.createBinding(binding);
    // ensure convert local variable to field
    ensureConvertToField();
  }

  public void ensureConvertToField() throws Exception {
    VariableSupport variableSupport = m_javaInfo.getVariableSupport();
    if (variableSupport.canConvertLocalToField()) {
      variableSupport.convertLocalToField();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return m_parent;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForMasterTable) {
      return CoreUtils.cast(m_children);
    }
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      return CoreUtils.cast(m_properties);
    }
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.WIDGETS;
  }
}