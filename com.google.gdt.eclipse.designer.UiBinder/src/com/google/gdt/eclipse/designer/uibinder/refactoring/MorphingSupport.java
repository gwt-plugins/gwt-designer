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
package com.google.gdt.eclipse.designer.uibinder.refactoring;

import com.google.gdt.eclipse.designer.uibinder.model.util.NameSupport;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.action.IContributionManager;

import org.apache.commons.lang.StringUtils;

/**
 * Helper for morphing {@link WidgetInfo} for one component class to another.
 * 
 * @author sablin_aa
 * @coverage GWT.UiBinder.refactoring
 */
public class MorphingSupport<T extends WidgetInfo>
    extends
      org.eclipse.wb.internal.core.xml.model.utils.MorphingSupport<T> {
  public static final String TOOLKIT_CLASS_NAME = "com.google.gwt.user.client.ui.Widget";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected MorphingSupport(String toolkitClassName, T component) {
    super(toolkitClassName, component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void contribute(WidgetInfo widget, IContributionManager manager) throws Exception {
    contribute(new MorphingSupport<WidgetInfo>(TOOLKIT_CLASS_NAME, widget), manager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  private String uiFieldName;

  @Override
  protected void morph_replace(T newComponent) throws Exception {
    // remember name
    uiFieldName = NameSupport.getName(m_component);
    //
    super.morph_replace(newComponent);
  }

  @Override
  protected void morph_source(T newComponent) throws Exception {
    super.morph_source(newComponent);
    // replace ui-field type
    AstEditor formEditor = newComponent.getContext().getFormEditor();
    if (!StringUtils.isEmpty(uiFieldName) && formEditor != null) {
      //NameSupport.setName(newComponent, uiFieldName);
      TypeDeclaration typeDeclaration = formEditor.getPrimaryType();
      VariableDeclaration variableDeclaration =
          NameSupport.getBinderField(typeDeclaration, uiFieldName);
      if (variableDeclaration != null) {
        formEditor.replaceVariableType(
            variableDeclaration,
            ReflectionUtils.getCanonicalName(newComponent.getDescription().getComponentClass()));
        formEditor.saveChanges(true);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utility access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String validate(WidgetInfo widget, MorphingTargetDescription target)
      throws Exception {
    MorphingSupport<WidgetInfo> morphingSupport =
        new MorphingSupport<WidgetInfo>(TOOLKIT_CLASS_NAME, widget) {
        };
    return morphingSupport.validate(target);
  }

  public static void morph(WidgetInfo widget, MorphingTargetDescription target) throws Exception {
    MorphingSupport<WidgetInfo> morphingSupport =
        new MorphingSupport<WidgetInfo>(TOOLKIT_CLASS_NAME, widget) {
        };
    morphingSupport.morph(target);
  }
}
