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
package com.google.gdt.eclipse.designer.gwtext.parser;

import com.google.gdt.eclipse.designer.gwtext.Activator;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link IParseFactory} for GWT-Ext.
 * 
 * @author sablin_aa
 * @coverage GWTExt
 */
public final class ParseFactory extends AbstractParseFactory {
  @Override
  public JavaInfo create(AstEditor editor,
      ClassInstanceCreation creation,
      IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression[] arguments,
      JavaInfo[] argumentInfos) throws Exception {
    // support for: new Viewport(panel)
    if (AstNodeUtils.isCreation(
        creation,
        "com.gwtext.client.widgets.Viewport",
        "<init>(com.gwtext.client.widgets.Panel)") && argumentInfos[0] instanceof PanelInfo) {
      ComponentInfo.markAsViewportRoot((PanelInfo) argumentInfos[0]);
    }
    return null;
  }

  @Override
  protected String getToolkitId() {
    return Activator.PLUGIN_ID;
  }
}