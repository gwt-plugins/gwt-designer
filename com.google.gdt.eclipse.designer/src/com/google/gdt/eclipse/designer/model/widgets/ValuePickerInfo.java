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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.cell.CellListInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.ValuePicker</code>.
 * 
 * @author sablin_aa
 * @coverage gwt.model
 */
public class ValuePickerInfo extends CompositeInfo {
  private final ValuePickerInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValuePickerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    installChildCellListListiners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void installChildCellListListiners() {
    // create CellList info
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this && getCellList() == null) {
          createChildCellList();
        }
      }
    });
    // CellList should not be visible on design canvas, because we want that user click and move picker.
    addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        if (object == getCellList()) {
          visible[0] = false;
        }
      }
    });
  }

  private void createChildCellList() throws Exception {
    if (!(getCreationSupport() instanceof ConstructorCreationSupport)) {
      return;
    }
    // prepare expression
    ClassInstanceCreation creation =
        ((ConstructorCreationSupport) getCreationSupport()).getCreation();
    Expression cellListExpression = DomGenerics.arguments(creation).get(0);
    if (!AstNodeUtils.isSuccessorOf(
        AstNodeUtils.getTypeBinding(cellListExpression),
        "com.google.gwt.user.cellview.client.CellList")) {
      return;
    }
    // create info
    ConstructorCreationSupport creationSupport = new ConstructorCreationSupport();
    JavaInfo cellList =
        JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.google.gwt.user.cellview.client.CellList",
            creationSupport);
    // configure info
    cellList.setVariableSupport(new EmptyVariableSupport(cellList, cellListExpression));
    creationSupport.add_setSourceExpression(cellListExpression);
    // add info as child
    cellList.setAssociation(new ConstructorChildAssociation());
    addChild(cellList);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellListInfo getCellList() {
    CellListInfo cellList;
    List<CellListInfo> children = getChildren(CellListInfo.class);
    if (children.isEmpty()) {
      cellList = null;
    } else {
      cellList = children.get(0);
    }
    return cellList;
  }
}
