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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.SliderInfo;

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
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.form.SliderField</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class SliderFieldInfo extends FieldInfo {
  private final SliderFieldInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SliderFieldInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    createChildSlider_forNewSliderField();
    dontShowSliderInGraphical();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createChildSlider_forNewSliderField() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this) {
          createChildSlider(child);
        }
      }

      private void createChildSlider(JavaInfo child) throws Exception {
        // prepare "null" expression to replace with Slider
        Expression sliderExpression;
        {
          ClassInstanceCreation fieldCreation =
              ((ConstructorCreationSupport) child.getCreationSupport()).getCreation();
          sliderExpression = DomGenerics.arguments(fieldCreation).get(0);
        }
        // create new Slider
        SliderInfo slider =
            (SliderInfo) JavaInfoUtils.createJavaInfo(
                getEditor(),
                "com.extjs.gxt.ui.client.widget.Slider",
                new ConstructorCreationSupport());
        // prepare "new Slider()" source
        String sliderSource;
        {
          StatementTarget statementTarget =
              new StatementTarget(AstNodeUtils.getEnclosingStatement(sliderExpression), true);
          NodeTarget nodeTarget = new NodeTarget(statementTarget);
          sliderSource = slider.getCreationSupport().add_getSource(nodeTarget);
        }
        // replace "null" with "new Slider()"
        sliderExpression = getEditor().replaceExpression(sliderExpression, sliderSource);
        slider.setVariableSupport(new EmptyVariableSupport(slider, sliderExpression));
        slider.getCreationSupport().add_setSourceExpression(sliderExpression);
        // add Slider as child
        slider.setAssociation(new ConstructorChildAssociation());
        addChild(slider);
      }
    });
  }

  /**
   * Slider should not be visible on design canvas, because we want that user click and move field.
   */
  private void dontShowSliderInGraphical() {
    addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        if (object instanceof SliderInfo && object.getParent() == m_this) {
          visible[0] = false;
        }
      }
    });
  }
}
