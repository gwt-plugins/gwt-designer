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
import com.google.gdt.eclipse.designer.model.widgets.UIObjectSizeSupport;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.smart.model.live.SmartGwtLiveManager;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.BoundsProperty;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.Canvas</code>.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public class CanvasInfo extends BaseWidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    installListiners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void installListiners() {
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
    // add bounds property for child widget
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof CanvasInfo && javaInfo.getParent() == CanvasInfo.this) {
          CanvasInfo canvas = (CanvasInfo) javaInfo;
          properties.add(getBoundsProperty(canvas));
        }
      }
    });
    // add root attach listener
    addBroadcastListener(new CanvasAfterAttach() {
      public void invoke() throws Exception {
        processObjectReady();
      }
    });
    removeLocation_whenMoveOut();
  }

  /**
   * {@link CanvasInfo} is "absolute container", but when we move {@link WidgetInfo} out, target
   * container may be not "absolute". So, we should remove information related to "location".
   */
  private void removeLocation_whenMoveOut() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == CanvasInfo.this && child instanceof CanvasInfo) {
          CanvasInfo canvas = (CanvasInfo) child;
          removeLocation(canvas);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean shouldEvaluateInvocation(MethodInvocation invocation) {
    // disable set location for root canvas
    if (isRoot()) {
      final List<String> signatures =
          Lists.newArrayList(
              "setLeft(",
              "setTop(",
              "setPageLeft(",
              "setPageTop(",
              "setPosition(",
              "setBottom(",
              "setRight(",
              "setRect(");
      String invocationSignature = AstNodeUtils.getMethodSignature(invocation);
      for (String signature : signatures) {
        if (invocationSignature.startsWith(signature)) {
          return false;
        }
      }
    }
    return super.shouldEvaluateInvocation(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new CanvasTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected SmartGwtLiveManager getLiveComponentsManager() {
    return new SmartGwtLiveManager(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} children.
   */
  public final List<WidgetInfo> getWidgets() {
    return getChildren(WidgetInfo.class);
  }

  /**
   * @return <code>true</code> if this {@link CanvasInfo} is exactly <code>Canvas</code>, so
   *         absolute layout should be used for it.
   */
  public final boolean isExactlyCanvas() {
    return getDescription().getComponentClass().getName().equals(
        "com.smartgwt.client.widgets.Canvas");
  }

  /**
   * @return <code>true</code> if object created in JS.
   */
  public boolean isCreated() {
    return !isPlaceholder() && SmartClientUtils.isCanvasCreated(getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // detach children
    if (isCreated()) {
      refresh_dispose_detach();
    }
    //
    super.refresh_dispose();
  }

  /**
   * Detach children from canvas.
   */
  protected void refresh_dispose_detach() throws Exception {
    for (WidgetInfo child : getWidgets()) {
      refresh_dispose_detach(child);
    }
  }

  /**
   * Detach child from canvas.
   */
  protected void refresh_dispose_detach(WidgetInfo child) throws Exception {
    if (child instanceof CanvasInfo) {
      CanvasInfo childCanvas = (CanvasInfo) child;
      if (childCanvas.isCreated()) {
        ReflectionUtils.invokeMethod(
            getObject(),
            "removeChild(com.smartgwt.client.widgets.Canvas)",
            childCanvas.getObject());
      }
    } else {
      // TODO
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Rectangle fetchAbsoluteBounds(Object element) {
    Object object = getObject();
    return SmartClientUtils.getAbsoluteBounds(object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected UIObjectSizeSupport createSizeSupport() {
    return new CanvasSizeSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds Property
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property getBoundsProperty(CanvasInfo canvas) {
    ComplexProperty boundsProperty = (ComplexProperty) canvas.getArbitraryValue(this);
    if (boundsProperty == null) {
      boundsProperty = new ComplexProperty("Bounds", null);
      boundsProperty.setCategory(PropertyCategory.system(5));
      boundsProperty.setModified(true);
      canvas.putArbitraryValue(this, boundsProperty);
      // x
      BoundsProperty<?> xProperty = new BoundsProperty<CanvasInfo>(canvas, "x") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          command_BOUNDS(m_component, new Point(value, modelBounds.y), null);
        }
      };
      // y
      BoundsProperty<?> yProperty = new BoundsProperty<CanvasInfo>(canvas, "y") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          command_BOUNDS(m_component, new Point(modelBounds.x, value), null);
        }
      };
      // width
      BoundsProperty<?> widthProperty = new BoundsProperty<CanvasInfo>(canvas, "width") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          command_BOUNDS(m_component, null, new Dimension(value, modelBounds.height));
        }
      };
      // height
      BoundsProperty<?> heightProperty = new BoundsProperty<CanvasInfo>(canvas, "height") {
        @Override
        public void setValue2(int value, Rectangle modelBounds) throws Exception {
          command_BOUNDS(m_component, null, new Dimension(modelBounds.width, value));
        }
      };
      boundsProperty.setProperties(new Property[]{
          xProperty,
          yProperty,
          widthProperty,
          heightProperty});
    }
    // apply current bounds
    Rectangle modelBounds = canvas.getModelBounds();
    if (modelBounds != null) {
      boundsProperty.setText("("
          + modelBounds.x
          + ", "
          + modelBounds.y
          + ", "
          + modelBounds.width
          + ", "
          + modelBounds.height
          + ")");
    }
    // done
    return boundsProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_absolute_CREATE(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    if (widget instanceof CanvasInfo) {
      JavaInfoUtils.add(widget, createAssociation(), this, nextWidget);
    } else {
      WidgetCanvasInfo.command_CREATE_widget(this, widget, nextWidget);
    }
  }

  public void command_absolute_MOVE(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    if (widget instanceof CanvasInfo) {
      JavaInfoUtils.move(widget, createAssociation(), this, nextWidget);
    } else {
      if (widget.getParentJava() instanceof WidgetCanvasInfo) {
        JavaInfoUtils.move(widget.getParentJava(), createAssociation(), this, nextWidget);
      } else {
        // nothing TODO
      }
    }
  }

  /**
   * Performs "move" or "resize" operation. Modifies location/size values by modifying appropriate
   * "add", "setWidgetPosition", "setSize" arguments.
   * 
   * @param canvas
   *          the {@link WidgetInfo} which modifications applies to.
   * @param location
   *          the {@link Point} of new location of widget. May be <code>null</code>.
   * @param size
   *          the {@link Dimension} of new size of widget. May be <code>null</code>.
   */
  public void command_BOUNDS(CanvasInfo canvas, Point location, Dimension size) throws Exception {
    Assert.isTrue(getChildren().contains(canvas), "%s is not child of %s.", canvas, this);
    if (location != null && size != null) {
      canvas.removeMethodInvocations("setRect(int,int,int,int)");
      removeLocationInvocations(canvas);
      canvas.removeMethodInvocations("resizeTo(int,int)");
      canvas.removeMethodInvocations("setSize(java.lang.String,java.lang.String)");
      canvas.removeMethodInvocations("setWidth(int)");
      canvas.removeMethodInvocations("setWidth(java.lang.String)");
      canvas.removeMethodInvocations("setHeight(int)");
      canvas.removeMethodInvocations("setHeight(java.lang.String)");
      {
        String xString = getRectString(location.x);
        String yString = getRectString(location.y);
        String wString = getRectString(size.width);
        String hString = getRectString(size.height);
        String arguments = xString + ", " + yString + ", " + wString + ", " + hString;
        canvas.addMethodInvocation("setRect(int,int,int,int)", arguments);
      }
      return;
    }
    if (size != null) {
      command_BOUNDS_setSize(canvas, size);
    }
    if (location != null) {
      command_BOUNDS_setLocation(canvas, location);
    }
  }

  private void command_BOUNDS_setLocation(CanvasInfo canvas, Point location) throws Exception {
    Assert.isNotNull(location);
    AstEditor editor = canvas.getEditor();
    String xString = getRectString(location.x);
    String yString = getRectString(location.y);
    // setRect(int,int,int,int)
    {
      MethodInvocation invocation = canvas.getMethodInvocation("setRect(int,int,int,int)");
      if (invocation != null) {
        editor.replaceInvocationArgument(invocation, 0, xString);
        editor.replaceInvocationArgument(invocation, 1, yString);
        return;
      }
    }
    // moveTo(int,int)
    {
      MethodInvocation invocation = canvas.getMethodInvocation("moveTo(int,int)");
      if (invocation != null) {
        editor.replaceInvocationArgument(invocation, 0, xString);
        editor.replaceInvocationArgument(invocation, 1, yString);
        return;
      }
    }
    // no existing location invocations, generate moveTo(int,int)
    removeLocationInvocations(canvas);
    canvas.addMethodInvocation("moveTo(int,int)", xString + ", " + yString);
  }

  private void removeLocation(CanvasInfo canvas) throws Exception {
    removeLocationInvocations(canvas);
    // setRect(int,int,int,int)
    {
      MethodInvocation invocation = canvas.getMethodInvocation("setRect(int,int,int,int)");
      if (invocation != null) {
        List<Expression> invocationArguments = DomGenerics.arguments(invocation);
        AstEditor editor = canvas.getEditor();
        // add setSize()
        {
          String wString = editor.getSource(invocationArguments.get(2));
          String hString = editor.getSource(invocationArguments.get(3));
          String arguments = wString + ", " + hString;
          canvas.addMethodInvocation("resizeTo(int,int)", arguments);
        }
        // remove setRect()
        editor.removeEnclosingStatement(invocation);
      }
    }
  }

  private void removeLocationInvocations(CanvasInfo canvas) throws Exception {
    canvas.removeMethodInvocations("moveTo(int,int)");
    canvas.removeMethodInvocations("setLeft(int)");
    canvas.removeMethodInvocations("setLeft(java.lang.String)");
    canvas.removeMethodInvocations("setTop(int)");
    canvas.removeMethodInvocations("setTop(java.lang.String)");
  }

  private void command_BOUNDS_setSize(CanvasInfo canvas, Dimension size) throws Exception {
    Assert.isNotNull(size);
    AstEditor editor = canvas.getEditor();
    String wString = getRectString(size.width);
    String hString = getRectString(size.height);
    // moveTo(int,int)
    {
      MethodInvocation invocation = canvas.getMethodInvocation("moveTo(int,int)");
      if (invocation != null) {
        StatementTarget target;
        {
          Statement statement = AstNodeUtils.getEnclosingStatement(invocation);
          target = new StatementTarget(statement, true);
        }
        {
          String xString = editor.getSource(DomGenerics.arguments(invocation).get(0));
          String yString = editor.getSource(DomGenerics.arguments(invocation).get(1));
          String arguments = xString + ", " + yString + ", " + wString + ", " + hString;
          canvas.addMethodInvocation(target, "setRect(int,int,int,int)", arguments);
        }
        editor.removeEnclosingStatement(invocation);
        return;
      }
    }
    // no special case, just set size
    canvas.getSizeSupport().setSize(size);
  }

  /**
   * @return the {@link AssociationObject} for standard association with <code>Canvas</code>.
   */
  private static AssociationObject createAssociation() throws Exception {
    return AssociationObjects.invocationChild("%parent%.addChild(%child%)", false);
  }

  /**
   * @return the {@link String} presentation of <code>int</code> for location/size.
   */
  protected String getRectString(int value) throws Exception {
    return IntegerConverter.INSTANCE.toJavaSource(this, value);
  }
}
