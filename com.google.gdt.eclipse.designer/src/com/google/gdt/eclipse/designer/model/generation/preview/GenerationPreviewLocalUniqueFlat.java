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
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link LocalUniqueVariableSupport} and
 * {@link FlatStatementGenerator}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.generation.ui
 */
public final class GenerationPreviewLocalUniqueFlat extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewLocalUniqueFlat();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewLocalUniqueFlat() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    boolean v_useFinal = variableComposite.getBoolean(LocalUniqueVariableSupport.P_DECLARE_FINAL);
    boolean s_usePrefix = statementComposite.getBoolean(FlatStatementGenerator.P_USE_PREFIX);
    String s_thePrefix = statementComposite.getString(FlatStatementGenerator.P_PREFIX_TEXT);
    String source = "";
    // begin
    source += "\t...\n";
    // parent
    {
      source += "\t";
      if (v_useFinal) {
        source += "final ";
      }
      source += "FlowPanel panel = new FlowPanel();\n";
      // properties
      source += "\tpanel.setEnabled(true);\n";
    }
    // child
    {
      // optional prefix
      if (s_usePrefix) {
        source += "\t" + s_thePrefix + "\n";
      }
      // variable
      source += "\t";
      if (v_useFinal) {
        source += "final ";
      }
      source += "Button button = new Button();\n";
      // properties
      source += "\tbutton.setText(\"Add customer...\");\n";
      source += "\tpanel.add(button);\n";
    }
    // end
    source += "\t...\n";
    // final result
    return source;
  }
}
