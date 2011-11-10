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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Statement;

/**
 * Model for <code>Editor</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model
 */
public class EditorInfo extends ComponentInfo {
  private final EditorInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    initStateSuccessors();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Ensure checked CheckItem
  //
  ////////////////////////////////////////////////////////////////////////////
  private void initStateSuccessors() {
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == m_this) {
          if (/*getField() == null && */!isDeleting()) {
            // Editor must have Field
            addDefaultField();
          }
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this) {
          // Editor must have Field
          addDefaultField();
        }
      }
    });
  }

  private void addDefaultField() throws Exception {
    WidgetInfo item =
        (WidgetInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.gwtext.client.widgets.form.TextField",
            new ConstructorCreationSupport());
    command_CREATE(item, null);
    //item.getPropertyByTitle("text").setValue("Default");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(WidgetInfo item, WidgetInfo nextItem) throws Exception {
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.setField(%child%)", true);
    StatementTarget target = getTargetForNewItem(item, nextItem);
    JavaInfoUtils.addTarget(item, associationObject, this, target);
  }

  private StatementTarget getTargetForNewItem(WidgetInfo item, WidgetInfo nextItem)
      throws Exception {
    StatementTarget target;
    if (nextItem == null) {
      Statement thisAssociationStatement = getAssociation().getStatement();
      target = new StatementTarget(thisAssociationStatement, true);
    } else {
      target = JavaInfoUtils.getTarget(this, item, nextItem);
    }
    return target;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getField() throws Exception {
    return ReflectionUtils.invokeMethod(getObject(), "field");
  }
}
