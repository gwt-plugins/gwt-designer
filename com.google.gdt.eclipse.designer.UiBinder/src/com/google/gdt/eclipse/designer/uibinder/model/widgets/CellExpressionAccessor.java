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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;

/**
 * Implementation of {@link ExpressionAccessor} for <code>setCellXXX(Widget,value)</code>
 * invocations.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class CellExpressionAccessor extends ExpressionAccessor {
  private final String m_namespace;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellExpressionAccessor(String namespace, String attribute) {
    super(attribute);
    m_namespace = namespace;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(XmlObjectInfo object) throws Exception {
    DocumentElement cellElement = getExistingCellElement(object);
    if (cellElement != null) {
      UiBinderContext context = (UiBinderContext) object.getContext();
      return context.getAttributeValue(cellElement, m_attribute);
    }
    return Property.UNKNOWN_VALUE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getExpression(XmlObjectInfo object) {
    DocumentElement cellElement = getExistingCellElement(object);
    if (cellElement == null) {
      return null;
    }
    return cellElement.getAttribute(m_attribute);
  }

  @Override
  public void setExpression(XmlObjectInfo object, String expression) throws Exception {
    DocumentElement objectElement = object.getCreationSupport().getElement();
    DocumentElement cellElement = getExistingCellElement(object);
    // wrap into "Cell" element
    if (cellElement == null) {
      DocumentElement parentElement = objectElement.getParent();
      int index = parentElement.indexOf(objectElement);
      // prepare "Cell" element
      {
        cellElement = new DocumentElement();
        cellElement.setTag(m_namespace + "Cell");
        parentElement.addChild(cellElement, index);
      }
      // move "widget" into "Cell"
      cellElement.moveChild(objectElement, 0);
    }
    // set attribute
    cellElement.setAttribute(m_attribute, expression);
    // remove "Cell" if no attributes
    if (cellElement.getDocumentAttributes().isEmpty()) {
      DocumentElement parentElement = cellElement.getParent();
      int index = parentElement.indexOf(cellElement);
      // move "widget" at place of "Cell"
      parentElement.moveChild(objectElement, index);
      // remove "Cell"
      cellElement.remove();
    }
    // finish edit operation
    ExecutionUtils.refresh(object);
  }

  private DocumentElement getExistingCellElement(XmlObjectInfo object) {
    DocumentElement objectElement = object.getCreationSupport().getElement();
    DocumentElement cellElement = objectElement.getParent();
    if (cellElement != object.getParentXML().getElement()) {
      return cellElement;
    }
    return null;
  }
}
