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
package com.google.gdt.eclipse.designer.smart.model.support;

import com.google.gdt.eclipse.designer.support.http.IModuleInitializer;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import java.util.List;

/**
 * Design-time initializer for SmartGWT toolkit (support 2.2 & later).
 * 
 * @author sablin_aa
 * @coverage SmartGWT.support
 */
public final class ModuleInitializer implements IModuleInitializer {
  public static final String ISOMORPHIC_SCRIPT =
      "<script>if(!isomorphicDir){ var isomorphicDir = \"%MODULE_NAME%/sc/\"; }</script>";

  public void configure(ModuleDescription moduleDescription, List<String> declarations)
      throws Exception {
    int position = -1;
    // locate position
    for (String declaration : declarations) {
      if (declaration.startsWith("<script")) {
        position = declarations.indexOf(declaration);
        break;
      }
    }
    // add script
    declarations.add(position >= 0 ? position : 0, ISOMORPHIC_SCRIPT);
  }
}
