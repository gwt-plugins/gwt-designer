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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link CreationSupport} for <code>Menu#addSeparator()</code> and
 * <code>Menu#addText(String)</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public final class MenuAddItemCreationSupport extends VoidInvocationCreationSupport {
  private final String m_source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuAddItemCreationSupport(JavaInfo hostJavaInfo,
      MethodDescription description,
      MethodInvocation invocation,
      JavaInfo[] argumentInfos) {
    super(hostJavaInfo, description, invocation);
    String source = hostJavaInfo.getEditor().getSource(invocation);
    Expression expression = invocation.getExpression();
    if (expression != null) {
      String expressionSource = hostJavaInfo.getEditor().getSource(expression);
      source = source.substring(expressionSource.length() + 1);
    }
    m_source = source;
  }

  private static final String DEFAULT_SOURCE = "addSeparator()";

  public MenuAddItemCreationSupport(JavaInfo hostJavaInfo) {
    super(hostJavaInfo, hostJavaInfo.getDescription().getMethod(DEFAULT_SOURCE));
    m_source = DEFAULT_SOURCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getObject(Object hostObject) throws Exception {
    Object[] items = (Object[]) ReflectionUtils.invokeMethod(hostObject, "getItems()");
    return items[items.length - 1];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReorder() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String add_getMethodSource() throws Exception {
    return m_source;
  }
}
