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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import org.apache.commons.lang.ClassUtils;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class ConverterInfo extends AstObjectInfo {
  private String m_className;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConverterInfo(String className) {
    m_className = className;
  }

  public ConverterInfo(AstEditor editor, ClassInstanceCreation creation) {
    String className = AstNodeUtils.getFullyQualifiedName(creation, false);
    if (!creation.arguments().isEmpty()) {
      String source = editor.getSource(creation);
      int index = source.indexOf('(');
      className += source.substring(index);
    }
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClassName() {
    return m_className;
  }

  public void setClassName(String className) {
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return ClassUtils.getShortClassName(m_className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public final String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    String defaultCostructor = m_className.indexOf('(') == -1 ? "()" : "";
    String variable = getVariableIdentifier();
    // check variable
    if (variable == null) {
      // no variable
      return "new " + m_className + defaultCostructor;
    }
    // variable mode
    lines.add("com.extjs.gxt.ui.client.binding.Converter "
        + variable
        + " = new "
        + m_className
        + defaultCostructor
        + ";");
    return variable;
  }
}