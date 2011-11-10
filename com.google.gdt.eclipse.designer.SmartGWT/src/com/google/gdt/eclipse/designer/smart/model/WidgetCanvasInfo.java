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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.WidgetCanvas</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class WidgetCanvasInfo extends CanvasInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetCanvasInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    installListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void installListeners() {
    // properties
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo == WidgetCanvasInfo.this) {
          // remove properties
          List<Property> propertiesToMove = Lists.newArrayList();
          for (Property property : properties) {
            if (!property.getTitle().equalsIgnoreCase("Bounds")) {
              propertiesToMove.add(property);
            }
          }
          properties.removeAll(propertiesToMove);
          // add to "canvas" property
          ComplexProperty canvasProperty = getCanvasProperty();
          canvasProperty.setProperties(propertiesToMove);
          canvasProperty.setText(getVariableSupport().getTitle());
          properties.add(canvasProperty);
          // add underlying widget properties
          Property[] widgetProperties = getWidget().getProperties();
          properties.addAll(Lists.newArrayList(widgetProperties));
        }
      }
    });
  }

  private ComplexProperty getCanvasProperty() {
    final String CANVAS_PROPERTY = "CANVAS_PROPERTY";
    ComplexProperty canvasProperty = (ComplexProperty) getArbitraryValue(CANVAS_PROPERTY);
    if (canvasProperty == null) {
      canvasProperty = new ComplexProperty("Canvas", null);
      canvasProperty.setCategory(PropertyCategory.system(5));
      canvasProperty.setModified(true);
      putArbitraryValue(CANVAS_PROPERTY, canvasProperty);
    }
    return canvasProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  protected Dimension getLivePreferredSize() {
    Dimension size = super.getLivePreferredSize();
    WidgetInfo widget = getWidget();
    if (size == null && widget != null) {
      size = widget.getPreferredSize();
    }
    return size;
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  DefaultJavaInfoPresentation m_presintation = new DefaultJavaInfoPresentation(this) {
    @Override
    public String getText() throws Exception {
      return getWidget().getPresentation().getText() + " \\ " + super.getText();
    };

    @Override
    public org.eclipse.swt.graphics.Image getIcon() throws Exception {
      return getWidget().getPresentation().getIcon();
    };

    @Override
    public java.util.List<ObjectInfo> getChildrenTree() throws Exception {
      List<ObjectInfo> childrenTree = super.getChildrenTree();
      childrenTree.remove(getWidget());
      return childrenTree;
    };

    @Override
    public java.util.List<org.eclipse.wb.core.model.ObjectInfo> getChildrenGraphical()
        throws Exception {
      List<ObjectInfo> childrenGraphical = super.getChildrenGraphical();
      childrenGraphical.remove(getWidget());
      return childrenGraphical;
    };
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presintation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Property getSizeProperty() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo getWidget() {
    return getWidgets().get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static WidgetCanvasInfo command_CREATE_widget(CanvasInfo container,
      WidgetInfo widget,
      WidgetInfo nextWidget) throws Exception {
    // create empty WidgetCanvas
    ConstructorCreationSupport canvasCreationSupport = new ConstructorCreationSupport();
    WidgetCanvasInfo widgetCanvas =
        (WidgetCanvasInfo) JavaInfoUtils.createJavaInfo(
            container.getEditor(),
            "com.smartgwt.client.widgets.WidgetCanvas",
            canvasCreationSupport);
    container.command_absolute_CREATE(widgetCanvas, nextWidget);
    // attach widget at WidgetCanvas
    Expression expression = DomGenerics.arguments(canvasCreationSupport.getCreation()).get(0);
    // add source
    String widgetSource = widget.getCreationSupport().add_getSource(null);
    expression = container.getEditor().replaceExpression(expression, widgetSource);
    widget.addRelatedNode(expression);
    // set supports
    widget.setVariableSupport(new EmptyVariableSupport(widget, expression));
    widget.getCreationSupport().add_setSourceExpression(expression);
    widget.setAssociation(new ConstructorChildAssociation());
    // set hierarchy
    widgetCanvas.addChild(widget);
    // ready
    return widgetCanvas;
  }
}
