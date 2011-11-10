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
package com.google.gdt.eclipse.designer.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link FieldUniqueVariableSupport} and
 * {@link FlatStatementGenerator}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.generation.ui
 */
public final class GenerationPreviewFieldUniqueBlock extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewFieldUniqueBlock();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewFieldUniqueBlock() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    boolean v_useThis = variableComposite.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS);
    int v_modifierIndex = variableComposite.getInteger(FieldUniqueVariableSupport.P_FIELD_MODIFIER);
    String v_modifierSource = FieldVariableSupport.V_MODIFIER_CODE[v_modifierIndex];
    //
    String source = "";
    String panelRef = v_useThis ? "this.panel" : "panel";
    String buttonRef = v_useThis ? "this.button" : "button";
    // declare fields
    source += "\t" + v_modifierSource + "FlowPanel panel;\n";
    source += "\t" + v_modifierSource + "Button button;\n";
    // begin
    source += "\t...\n";
    // parent
    {
      // open block
      source += "\t{\n";
      // assign field
      source += "\t\t" + panelRef + " = new FlowPanel();\n";
      // properties
      source += "\t\t" + panelRef + ".setEnabled(true);\n";
      // child
      {
        // open block
        source += "\t\t{\n";
        // assign field
        source += "\t\t\t" + buttonRef + " = new Button();\n";
        // properties
        source += "\t\t\t" + buttonRef + ".setText(\"Add customer...\");\n";
        source += "\t\t\t" + panelRef + ".add(" + buttonRef + ");\n";
        // close block
        source += "\t\t}\n";
        source += "\t\t...\n";
      }
      // close block
      source += "\t}\n";
    }
    // end
    source += "\t...\n";
    // final result
    return source;
  }
}
