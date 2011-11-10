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

import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
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

import java.util.List;

/**
 * Model for <code>FormPanel</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public class FormPanelInfo extends ContainerInfo {
  private final FormPanelInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureAtLeastOneField();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureAtLeastOneField() {
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == m_this && !m_this.isDeleting() && getFields().isEmpty()) {
          addDefaultField();
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this) {
          addDefaultField();
        }
      }
    });
  }

  private void addDefaultField() throws Exception {
    WidgetInfo field =
        (WidgetInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.gwtext.client.widgets.form.TextField",
            new ConstructorCreationSupport());
    AssociationObject association =
        AssociationObjects.invocationChild("%parent%.add(%child%)", true);
    JavaInfoUtils.add(field, association, m_this, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected LayoutInfo createDefaultImplicitLayout(CreationSupport creationSupport)
      throws Exception {
    return (LayoutInfo) JavaInfoUtils.createJavaInfo(
        getEditor(),
        "com.gwtext.client.widgets.layout.FormLayout",
        creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<FieldInfo> getFields() throws Exception {
    return getChildren(FieldInfo.class);
  }
}
