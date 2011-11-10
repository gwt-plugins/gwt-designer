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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.smart.model.data.DataSourceInfo;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.IObjectPropertyProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Model for <code>com.smartgwt.client.widgets.form.FilterBuilder</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class FilterBuilderInfo extends LayoutInfo implements IObjectPropertyProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FilterBuilderInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public DataSourceInfo getDataSource() {
    MethodInvocation invocation =
        getMethodInvocation("setDataSource(com.smartgwt.client.data.DataSource)");
    if (invocation != null) {
      Expression expression = DomGenerics.arguments(invocation).get(0);
      return (DataSourceInfo) getRootJava().getChildRepresentedBy(expression);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // set default DataSource
    if (getDataSource() == null) {
      applyFakeDataSource();
    }
  }

  private Object fakeDataSource = null;

  private void applyFakeDataSource() throws Exception {
    fakeDataSource = JavaInfoUtils.executeScript(this, CodeUtils.getSource(// filler 
        "import com.smartgwt.client.data.DataSource;",
        "import com.smartgwt.client.data.fields.DataSourceIntegerField;",
        "DataSource ds = new DataSource();",
        "ds.addField(new com.smartgwt.client.data.fields.DataSourceIntegerField());",
        "object.setDataSource(ds);",
        "return ds;"));
  }

  @Override
  public void refresh_dispose() throws Exception {
    super.refresh_dispose();
    // destroy fake DataSource
    if (fakeDataSource != null) {
      SmartClientUtils.destroyDataSource(fakeDataSource);
      fakeDataSource = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObjectPropertyProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StatementTarget getObjectPropertyStatementTarget(GenericProperty property,
      JavaInfo componentValue) throws Exception {
    if ("dataSource".equals(property.getTitle()) && componentValue instanceof DataSourceInfo) {
      DataSourceInfo dataSource = (DataSourceInfo) componentValue;
      return dataSource.calculateStatementTarget(this);
    }
    return null;
  }
}
