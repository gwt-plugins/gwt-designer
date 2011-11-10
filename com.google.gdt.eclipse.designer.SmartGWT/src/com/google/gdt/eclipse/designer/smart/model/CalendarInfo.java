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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.IObjectPropertyProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.smartgwt.client.widgets.calendar.Calendar</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class CalendarInfo extends CanvasInfo implements IObjectPropertyProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CalendarInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // tweaks for correct render
    if (!isPlaceholder()) {
      ReflectionUtils.invokeMethod(object, "setAutoFetchData(java.lang.Boolean)", false);
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
