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
package com.google.gdt.eclipse.designer.smart.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Statement;

import java.util.Iterator;
import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.tab.TabSet</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class TabSetInfo extends CanvasInfo {
  private final StackContainerSupport<TabInfo> m_stackContainer =
      new StackContainerSupport<TabInfo>(this) {
        @Override
        protected List<TabInfo> getChildren() {
          return getTabs();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabSetInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the children {@link TabInfo}'s.
   */
  public List<TabInfo> getTabs() {
    return getChildren(TabInfo.class);
  }

  /**
   * @return the selected {@link TabItem_Info}.
   */
  public TabInfo getSelectedTab() {
    return m_stackContainer.getActive();
  }

  /**
   * Sets the selected {@link TabItem_Info}.
   */
  public void setSelectedTab(TabInfo item) {
    m_stackContainer.setActive(item);
  }

  /**
   * @return the thickness of tabBar, applies to either orientation.
   */
  public Integer getTabBarThickness() {
    return (Integer) ReflectionUtils.invokeMethodEx(getObject(), "getTabBarThickness()");
  }

  /**
   * @return the side of the TabSet the TabBar should appear on.
   */
  public String getTabBarPosition() {
    String positionString = null;
    Object positionObject = ReflectionUtils.invokeMethodEx(getObject(), "getTabBarPosition()");
    if (positionObject != null) {
      positionString = (String) ReflectionUtils.invokeMethodEx(positionObject, "getValue()");
    }
    return positionString;
  }

  /**
   * @return the children tabs insets size.
   */
  public Insets getTabInsets() {
    return new Insets(5, 5, 5, 5);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenGraphical() throws Exception {
      List<ObjectInfo> children = super.getChildrenGraphical();
      // remove all Tabs's except of active
      TabInfo selectedTab = getSelectedTab();
      for (Iterator<ObjectInfo> I = children.iterator(); I.hasNext();) {
        ObjectInfo child = I.next();
        if (child instanceof TabInfo && child != selectedTab) {
          I.remove();
        }
      }
      // OK, show these children
      return children;
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_dispose_detach() throws Exception {
    // detach tabs
    if (isCreated()) {
      for (TabInfo tab : getTabs()) {
        if (tab.isCreated()) {
          ReflectionUtils.invokeMethod(
              getObject(),
              "removeTab(com.smartgwt.client.widgets.tab.Tab)",
              tab.getObject());
        }
      }
    }
    super.refresh_dispose_detach();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // select tab
    {
      TabInfo selectedTabInfo = getSelectedTab();
      if (selectedTabInfo != null) {
        ReflectionUtils.invokeMethodEx(
            getObject(),
            "selectTab(com.smartgwt.client.widgets.tab.Tab)",
            selectedTabInfo.getObject());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: <code>com.smartgwt.client.widgets.Canvas</code>
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(CanvasInfo newCanvas, TabInfo referenceTab) throws Exception {
    TabInfo newTab = createTab(referenceTab);
    AssociationObject createAssociation = createCanvasAssociationObject();
    StatementTarget statementTarget = createStatementTarget(newTab);
    JavaInfoUtils.addTarget(newCanvas, createAssociation, newTab, statementTarget);
  }

  public void command_MOVE(CanvasInfo moveCanvas, TabInfo referenceTab) throws Exception {
    ObjectInfo parent = moveCanvas.getParent();
    if (parent instanceof TabInfo) {
      TabInfo moveTab = (TabInfo) parent;
      AssociationObject associationObject = createTabAssociationObject();
      JavaInfoUtils.move(moveTab, associationObject, this, referenceTab);
      return;
    }
    TabInfo newTab = createTab(referenceTab);
    AssociationObject associationObject = createCanvasAssociationObject();
    StatementTarget statementTarget = createStatementTarget(newTab);
    JavaInfoUtils.moveTarget(moveCanvas, associationObject, newTab, null, statementTarget);
  }

  /**
   * @return new {@link TabInfo}.
   */
  private TabInfo createTab(TabInfo referenceTab) throws Exception {
    TabInfo newTab =
        (TabInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.smartgwt.client.widgets.tab.Tab",
            new ConstructorCreationSupport());
    JavaInfoUtils.add(newTab, createTabAssociationObject(), this, referenceTab);
    return newTab;
  }

  /**
   * @return the {@link AssociationObject} for standard association.
   */
  private static AssociationObject createTabAssociationObject() {
    return AssociationObjects.invocationChild("%parent%.addTab(%child%)", false);
  }

  private static AssociationObject createCanvasAssociationObject() {
    return AssociationObjects.invocationChild("%parent%.setPane(%child%)", false);
  }

  /**
   * @return the {@link StatementTarget} for association invocation.
   */
  private static StatementTarget createStatementTarget(TabInfo tabInfo) {
    Statement targetStatement =
        AstNodeUtils.getEnclosingStatement(tabInfo.getCreationSupport().getNode());
    return new StatementTarget(targetStatement, false);
  }
}
