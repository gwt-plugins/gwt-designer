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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.LayoutAssistant;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.gwtext.client.widgets.layout.AbsoluteLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class AbsoluteLayoutInfo extends AnchorLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initializeLayoutAssistant() {
    new LayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    super.onSet();
    for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
      Rectangle bounds = widget.getModelBounds();
      command_BOUNDS(widget, bounds.getLocation(), bounds.getSize());
    }
  }

  @Override
  protected void onDelete() throws Exception {
    super.onDelete();
    for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
      widget.getSizeSupport().setSize(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components/constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> dataClass =
        classLoader.loadClass("com.gwtext.client.widgets.layout.AbsoluteLayoutData");
    return ReflectionUtils.getConstructor(dataClass, int.class, int.class).newInstance(10, 10);
  }

  /**
   * @return {@link AbsoluteLayoutDataInfo} association with given {@link WidgetInfo}.
   */
  public static AbsoluteLayoutDataInfo getAbsoluteData(WidgetInfo widget) {
    return (AbsoluteLayoutDataInfo) getLayoutData(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs "move" or "resize" operation.
   * 
   * @param widget
   *          the {@link WidgetInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of widget. May be <code>null</code>.
   * @param size
   *          the {@link Dimension} of new size of widget. May be <code>null</code>.
   */
  public void command_BOUNDS(WidgetInfo widget, Point location, Dimension size) throws Exception {
    ContainerInfo container = getContainer();
    Assert.isTrue(container.getChildren().contains(widget), "%s is not child of %s.", widget, this);
    if (location != null) {
      AbsoluteLayoutDataInfo absoluteData = getAbsoluteData(widget);
      absoluteData.materialize();
      absoluteData.getPropertyByTitle("x").setValue(location.x);
      absoluteData.getPropertyByTitle("y").setValue(location.y);
    }
    if (size != null) {
      widget.getSizeSupport().setSize(size);
    }
    // check creation flow
    if (location != null && useCreationFlow()) {
      // force set new translated bounds
      Insets insets = container.getClientAreaInsets();
      Point location_ = location.getCopy();
      location_.translate(insets);
      AbsoluteLayoutCreationFlowSupport.checkBounds(widget, location_, size);
      // apply creation flow
      AbsoluteLayoutCreationFlowSupport.apply(
          container,
          container.getChildrenWidgets(),
          widget,
          location,
          size);
    }
  }

  private boolean useCreationFlow() {
    return getToolkit().getPreferences().getBoolean(IPreferenceConstants.P_CREATION_FLOW);
  }

  private ToolkitDescription getToolkit() {
    return getDescription().getToolkit();
  }
}