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
package com.google.gdt.eclipse.designer.gxt.databinding.parser;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class DatabindingParser extends AbstractParser {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void parse(DatabindingsProvider provider) throws Exception {
    new DatabindingParser(provider.getJavaInfoRoot(), provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DatabindingParser(JavaInfo javaInfoRoot, DatabindingsProvider provider) throws Exception {
    super(javaInfoRoot.getEditor(), provider);
    //
    TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
    //
    m_subParsers.add(provider.getRootInfo());
    for (ObserveTypeContainer container : provider.getContainers()) {
      container.createObservables(javaInfoRoot, this, m_editor, rootNode);
      m_subParsers.add(container);
    }
    //
    if (rootNode != null) {
      // find method initDataBindings()
      MethodDeclaration initDataBindings =
          AstNodeUtils.getMethodBySignature(rootNode, "initDataBindings()");
      // parse method initDataBindings()
      if (initDataBindings != null) {
        provider.getRootInfo().setInitDataBindings(initDataBindings);
        parseMethod(initDataBindings);
      }
    }
    //
    List<BindingInfo> bindings = provider.getBindings0();
    for (BindingInfo binding : Lists.newArrayList(bindings)) {
      binding.create(bindings);
    }
  }
}