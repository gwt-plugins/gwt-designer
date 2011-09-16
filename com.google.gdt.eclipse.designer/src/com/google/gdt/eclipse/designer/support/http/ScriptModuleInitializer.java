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
package com.google.gdt.eclipse.designer.support.http;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Design-time initializer for GWT toolkit. Add javascript's references.
 * 
 * @author sablin_aa
 * @coverage gwt.http
 */
public final class ScriptModuleInitializer implements IModuleInitializer {
  public void configure(ModuleDescription moduleDescription, List<String> declarations)
      throws Exception {
    List<String> scriptResources = Utils.getScriptResources(moduleDescription);
    for (String scriptSrc : scriptResources) {
      declarations.add(MessageFormat.format(
          "<script language=''javascript'' src=''{0}''></script>",
          scriptSrc));
    }
  }
}
