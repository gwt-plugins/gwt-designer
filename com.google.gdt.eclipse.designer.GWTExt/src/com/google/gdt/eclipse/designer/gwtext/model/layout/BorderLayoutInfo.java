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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.BorderLayoutAssistant;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetAttachAfterConstructor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetAssociationBefore;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Model for <code>com.gwtext.client.widgets.layout.BorderLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class BorderLayoutInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureCenterWidgetListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initializeLayoutAssistant() {
    new BorderLayoutAssistant(this);
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
      getBorderData(widget).materialize();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components/constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
    return new Object();
  }

  /**
   * @return {@link BorderLayoutDataInfo} association with given {@link WidgetInfo}.
   */
  public static BorderLayoutDataInfo getBorderData(WidgetInfo widget) {
    return (BorderLayoutDataInfo) getLayoutData(widget);
  }

  /**
   * @return the {@link WidgetInfo} that has given position, may be <code>null</code>.
   */
  public Object getWidget(String position) {
    for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
      String widgetPosition = getBorderData(widget).getPosition();
      if (position.equals(widgetPosition)) {
        return widget;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate0() throws Exception {
    ensureCenterWidget();
    super.refresh_afterCreate0();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Center widget
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureCenterWidgetListeners() {
    addBroadcastListener(new WidgetAttachAfterConstructor() {
      public void invoke() throws Exception {
        ensureCenterWidget();
      }
    });
    // when association of container is going to be executed during "parse"
    addBroadcastListener(new JavaInfoSetAssociationBefore() {
      public void invoke(JavaInfo javaInfo, Association association) throws Exception {
        ContainerInfo container = getContainer();
        if (isActive() && GlobalState.isParsing() && javaInfo == container) {
          ensureCenterWidget();
        }
      }
    });
    // when association of container is going to be executed during "refresh"
    addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isActive() && node instanceof Statement) {
          Association association = getContainer().getAssociation();
          if (association != null && association.getStatement() == node) {
            ensureCenterWidget();
          }
        }
      }
    });
  }

  private void ensureCenterWidget() throws Exception {
    if (getWidget("center") == null) {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      // prepare Panel
      Object panel;
      {
        Class<?> panelClass = classLoader.loadClass("com.gwtext.client.widgets.Panel");
        panel =
            ReflectionUtils.getConstructor(panelClass, String.class, String.class).newInstance(
                "Center",
                "No widget with CENTER position.");
      }
      // prepare RowLayoutData
      Object data;
      {
        Class<?> dataClass =
            classLoader.loadClass("com.gwtext.client.widgets.layout.BorderLayoutData");
        Class<?> regionClass = classLoader.loadClass("com.gwtext.client.core.RegionPosition");
        Object centerRegion = ReflectionUtils.getFieldObject(regionClass, "CENTER");
        data = ReflectionUtils.getConstructor(dataClass, regionClass).newInstance(centerRegion);
      }
      // add Panel to CENTER
      ReflectionUtils.invokeMethod(
          getContainer().getObject(),
          "add(com.gwtext.client.widgets.Component,com.gwtext.client.widgets.layout.LayoutData)",
          panel,
          data);
    }
  }
}