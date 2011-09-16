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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * Model for <code>Composite</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class CompositeInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_empty;

  /**
   * @return <code>true</code> if this <code>Composite</code> does not have widget.
   */
  public boolean isEmpty() {
    return m_empty;
  }

  /**
   * @return the single {@link WidgetInfo} child or <code>null</code> if widget is not set yet.
   */
  public WidgetInfo getWidget() {
    return GenericsUtils.getFirstOrNull(getChildren(WidgetInfo.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    // no content - no properties
    if (isEmpty()) {
      return Lists.newArrayList();
    }
    // don't suggest users to resize Composite, it is better to resize content
    if (getCreationSupport() instanceof ThisCreationSupport) {
      PropertyUtils.getByTitle(properties, "Size").setCategory(PropertyCategory.ADVANCED);
    }
    //
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected StatementTarget getMethodInvocationTarget(String newSignature) throws Exception {
    if (getCreationSupport() instanceof ThisCreationSupport) {
      ThisCreationSupport creationSupport = (ThisCreationSupport) getCreationSupport();
      MethodDeclaration constructor = creationSupport.getConstructor();
      return new StatementTarget(constructor, false);
    }
    return super.getMethodInvocationTarget(newSignature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // ensure Widget for not "this" Composite
    if (!(getCreationSupport() instanceof ThisCreationSupport)) {
      ensureWidget();
    }
  }

  @Override
  protected void attachAfterConstructor() throws Exception {
    ensureWidget();
    super.attachAfterConstructor();
  }

  private void ensureWidget() throws Exception {
    // may be placeholder
    if (isPlaceholder()) {
      m_empty = false;
      return;
    }
    // OK, real Composite
    m_empty = ReflectionUtils.invokeMethod(getObject(), "getWidget()") == null;
    if (m_empty) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "initWidget(com.google.gwt.user.client.ui.Widget)",
          createEmptyWidget());
    }
  }

  /**
   * @return the <code>Widget</code> to use as filler for empty <code>Composite</code>.
   */
  protected Object createEmptyWidget() throws Exception {
    return ScriptUtils.evaluate(GlobalState.getClassLoader(), CodeUtils.getSource(
        "import com.google.gwt.user.client.ui.*;",
        "return new Label('Empty Composite');"));
  }
}
