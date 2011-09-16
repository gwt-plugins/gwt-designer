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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveManager;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.model.util.factory.FactoryActionsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;
import java.util.Map;

/**
 * Model for any GWT <code>Widget</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class WidgetInfo extends UIObjectInfo implements IWidgetInfo, IThisMethodParameterEvaluator {
  private final WidgetInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    constributeContextMenu();
    dontBindToHierarchyIfThis();
    attachThisWidgetToRootPanel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new WidgetTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void constributeContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          ExposeComponentSupport.contribute(m_this, manager, "Expose widget...");
          MorphingSupport.contribute("com.google.gwt.user.client.ui.Widget", m_this, manager);
          FactoryActionsSupport.contribute(m_this, manager);
          RenameConvertSupport.contribute(objects, manager);
        }
      }
    });
  }

  /**
   * Some (stupid) users try to use "this" <code>Widget</code> in combination with
   * <code>RootPanel</code>. This causes unexpected binding of <code>Widget</code> to
   * <code>RootPanel</code>. We should break this binding.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44635
   */
  private void dontBindToHierarchyIfThis() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        if (getCreationSupport() instanceof ThisCreationSupport && !isRoot()) {
          getParent().removeChild(WidgetInfo.this);
        }
      }
    });
  }

  /**
   * In GWT method <code>Widget.onLoad()</code> is often used to perform some rendering operations.
   * And during this rendering super class may call some methods of this {@link CompilationUnit}.
   * And these methods may create some widgets. So, to visit them during parsing, we use known
   * solution from our RCP support -
   * {@link JavaInfoUtils#scheduleSpecialRendering(JavaInfo, IJavaInfoRendering)}.
   */
  private void attachThisWidgetToRootPanel() {
    if (getCreationSupport() instanceof ThisCreationSupport) {
      JavaInfoUtils.scheduleSpecialRendering(this, new IJavaInfoRendering() {
        public void render() throws Exception {
          attachAfterConstructor();
        }
      });
    }
  }

  protected void attachAfterConstructor() throws Exception {
    getBroadcast(WidgetAttachAfterConstructor.class).invoke();
    {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("model", this);
      variables.put("widget", getObject());
      String name = "attachAfterConstructorScript";
      String script = JavaInfoUtils.getParameter(this, name);
      Assert.isNotNull2(name, "No {0}", name);
      getUIObjectUtils().executeScript(script, variables);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    {
      ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> widgetClass = editorLoader.loadClass("com.google.gwt.user.client.ui.Widget");
      JavaInfoUtils.addExposedChildren(this, new Class<?>[]{widgetClass});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image m_liveDefaultImage = new Image(null, 1, 1);
  /**
   * We set this flag during requesting live image because:
   * <ul>
   * <li>Getting live image performs refresh();</li>
   * <li>refresh() may run messages loop;</li>
   * <li>during messages loop user may move mouse again and request live image again;</li>
   * <li>we don't support recursive live image requests.</li>
   * </ul>
   */
  private boolean m_liveInProgress;

  @Override
  protected Image getLiveImage() {
    // live image is supported only for component from palette
    if (getArbitraryValue(FLAG_MANUAL_COMPONENT) != Boolean.TRUE) {
      return null;
    }
    // prevent recursive live image requests
    if (m_liveInProgress) {
      return m_liveDefaultImage;
    }
    // OK, get live image
    m_liveInProgress = true;
    try {
      return getLiveComponentsManager().getImage();
    } finally {
      m_liveInProgress = false;
    }
  }

  /**
   * @return the {@link GwtLiveManager} instance.
   */
  protected GwtLiveManager getLiveComponentsManager() {
    return new GwtLiveManager(this);
  }

  public boolean shouldSetReasonableSize() {
    return getLiveComponentsManager().shouldSetSize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IThisMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(parameter);
    // special support for:
    //    com.google.gwt.user.client.ui.ImageBundle
    //    com.google.gwt.i18n.client.Constants
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.user.client.ui.ImageBundle")
        || AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.i18n.client.Constants")) {
      String bundleClassName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
      // special tweak for GWT "Showcase" sample
      if (bundleClassName.startsWith("com.google.gwt.sample.showcase.")) {
        bundleClassName = "com.google.gwt.sample.showcase.client.ShowcaseConstants";
      }
      // do create
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> bundleClass = classLoader.loadClass(bundleClassName);
      Class<?> c_DOM = classLoader.loadClass("com.google.gwt.core.client.GWT");
      return ReflectionUtils.invokeMethod(c_DOM, "create(java.lang.Class)", bundleClass);
    }
    // unknown
    return AstEvaluationEngine.UNKNOWN;
  }
}
