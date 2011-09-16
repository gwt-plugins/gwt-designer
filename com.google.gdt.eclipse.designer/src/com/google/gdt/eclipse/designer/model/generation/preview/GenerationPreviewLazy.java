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
package com.google.gdt.eclipse.designer.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link LazyVariableSupport} and
 * {@link LazyStatementGenerator}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.generation.ui
 */
public final class GenerationPreviewLazy extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewLazy();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewLazy() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    int v_modifierIndex = variableComposite.getInteger(LazyVariableSupport.P_METHOD_MODIFIER);
    String v_modifierSource = LazyVariableSupport.V_MODIFIER_CODE[v_modifierIndex];
    String source = "";
    // declare fields
    source += "\tprivate FlowPanel panel;\n";
    source += "\tprivate Button button;\n";
    // begin
    source += "\t...\n";
    // declare getPanel(), use getButton()
    {
      // open method
      source += "\t" + v_modifierSource + "FlowPanel getPanel() {\n";
      // code for panel
      source += "\t\tif (panel == null) {\n";
      source += "\t\t\tpanel = new FlowPanel();\n";
      source += "\t\t\tpanel.setEnabled(true);\n";
      // use getButton()
      source += "\t\t\tpanel.add(getButton());\n";
      // close method
      source += "\t\t}\n";
      source += "\t\treturn panel;\n";
      source += "\t}\n";
    }
    // separator
    source += "\t...\n";
    // declare getButton()
    {
      // open method
      source += "\t" + v_modifierSource + "Button getButton() {\n";
      // code
      source += "\t\tif (button == null) {\n";
      source += "\t\t\tbutton = new Button();\n";
      source += "\t\t\tbutton.setText(\"New button\");\n";
      source += "\t\t}\n";
      // close method
      source += "\t\treturn button;\n";
      source += "\t}\n";
    }
    // final result
    return source;
  }
}
