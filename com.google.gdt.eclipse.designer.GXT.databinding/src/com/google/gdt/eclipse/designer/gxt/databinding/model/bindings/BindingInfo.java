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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IASTObjectInfo2;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.BindingContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public abstract class BindingInfo extends AstObjectInfo implements IBindingInfo, IASTObjectInfo2 {
  protected ObserveInfo m_target;
  protected ObserveInfo m_targetProperty;
  protected ObserveInfo m_model;
  protected ObserveInfo m_modelProperty;
  private boolean m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingInfo() {
  }

  public BindingInfo(ObserveInfo target,
      ObserveInfo targetProperty,
      ObserveInfo model,
      ObserveInfo modelProperty) {
    m_target = target;
    m_targetProperty = targetProperty;
    m_model = model;
    m_modelProperty = modelProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setModel(ObserveInfo model, ObserveInfo modelProperty) {
    m_model = model;
    m_modelProperty = modelProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IBindingInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getTarget() {
    return m_target;
  }

  public IObserveInfo getTargetProperty() {
    return m_targetProperty;
  }

  public IObserveInfo getModel() {
    return m_model;
  }

  public IObserveInfo getModelProperty() {
    return m_modelProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void create(List<BindingInfo> bindings) throws Exception {
    m_target.createBinding(this);
    m_targetProperty.createBinding(this);
    m_model.createBinding(this);
    m_modelProperty.createBinding(this);
  }

  public void edit(List<BindingInfo> bindings) throws Exception {
  }

  public void delete(List<BindingInfo> bindings, boolean deleteAll) throws Exception {
    m_target.deleteBinding(this);
    m_targetProperty.deleteBinding(this);
    m_model.deleteBinding(this);
    m_modelProperty.deleteBinding(this);
  }

  public void move(List<BindingInfo> bindings) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    // configure page
    listener.setTitle("Properties");
    listener.setMessage("Choose properties for the target and the model.");
    // add target editors
    providers.add(new LabelUiContentProvider("Target:", getTargetPresentationText()));
    // add model editors
    providers.add(new LabelUiContentProvider("Model:", getModelPresentationText()));
    //
    providers.add(new BindingContentProvider(this, provider.getJavaInfoRoot()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public abstract void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception;

  public abstract String getDefinitionSource() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getTargetPresentationText() throws Exception {
    return m_target.getPresentation().getTextForBinding();
  }

  public String getModelPresentationText() throws Exception {
    String property =
        m_modelProperty == null ? "???" : m_modelProperty.getPresentation().getTextForBinding();
    return m_model.getPresentation().getTextForBinding() + "." + property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IASTObjectInfo2
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isField() {
    return m_field;
  }

  public final void setField() {
    m_field = true;
  }

  protected final void setVariableIdentifier(final JavaInfo javaInfoRoot,
      final String type,
      final String newVariable,
      boolean newFieldState) {
    try {
      boolean oldFieldState = m_field;
      m_field = newFieldState;
      //
      final String oldVariable = getVariableIdentifier();
      setVariableIdentifier(newVariable);
      final TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
      //
      if (!oldFieldState && newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            BodyDeclarationTarget fieldTarget = new BodyDeclarationTarget(rootNode, null, true);
            javaInfoRoot.getEditor().addFieldDeclaration(
                "private " + type + " " + newVariable + ";",
                fieldTarget);
          }
        });
      } else if (oldFieldState && !newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().removeBodyDeclaration(field);
                return;
              }
            }
            Assert.fail("Undefine binding field: " + oldVariable);
          }
        });
      } else if (oldFieldState && newFieldState) {
        ExecutionUtils.run(javaInfoRoot, new RunnableEx() {
          public void run() throws Exception {
            for (FieldDeclaration field : rootNode.getFields()) {
              VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
              if (fragment.getName().getIdentifier().equals(oldVariable)) {
                javaInfoRoot.getEditor().setIdentifier(fragment.getName(), newVariable);
                return;
              }
            }
            Assert.fail("Undefine binding field: " + oldVariable);
          }
        });
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }
}