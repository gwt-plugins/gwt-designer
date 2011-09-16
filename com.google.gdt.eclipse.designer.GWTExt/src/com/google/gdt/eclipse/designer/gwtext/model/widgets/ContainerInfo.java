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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import com.google.gdt.eclipse.designer.gwtext.GwtExtToolkitDescription;
import com.google.gdt.eclipse.designer.gwtext.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.ImplicitLayoutCreationSupport;
import com.google.gdt.eclipse.designer.gwtext.model.layout.ImplicitLayoutVariableSupport;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import java.util.List;

/**
 * Model for <code>Container</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public class ContainerInfo extends BoxComponentInfo {
  /**
   * We set this key during {@link #setLayout(LayoutInfo)} to prevent implicit {@link LayoutInfo}
   * activation during layout replacement.
   */
  public static final String KEY_DONT_SET_IMPLICIT_LAYOUT = "KEY_DONT_SET_IMPLICIT_LAYOUT";
  private final ContainerInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContainerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    initialize_createImplicitLayout();
    // context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          fillContextMenu(manager);
        }
      }
    });
  }

  /**
   * Fill context menu {@link IMenuManager}.
   */
  protected void fillContextMenu(IMenuManager manager) throws Exception {
    if (hasLayout()) {
      // add "Set layout"
      IMenuManager layoutsManager = new MenuManager("Set layout");
      manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, layoutsManager);
      fillLayoutsManager(layoutsManager);
    }
  }

  /**
   * Fills given {@link IMenuManager} with {@link IAction}s for setting new {@link LayoutInfo} on
   * this {@link CompositeInfo}.
   */
  public void fillLayoutsManager(IMenuManager layoutsManager) throws Exception {
    // add layout items
    final AstEditor editor = getEditor();
    ClassLoader editorLoader = EditorState.get(editor).getEditorLoader();
    List<LayoutDescription> descriptions =
        LayoutDescriptionHelper.get(GwtExtToolkitDescription.INSTANCE);
    for (final LayoutDescription description : descriptions) {
      final Class<?> layoutClass = editorLoader.loadClass(description.getLayoutClassName());
      final String creationId = description.getCreationId();
      ComponentDescription layoutComponentDescription =
          ComponentDescriptionHelper.getDescription(editor, layoutClass);
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          description.ensureLibraries(editor.getJavaProject());
          LayoutInfo layout =
              (LayoutInfo) JavaInfoUtils.createJavaInfo(
                  getEditor(),
                  layoutClass,
                  new ConstructorCreationSupport(creationId, true));
          setLayout(layout);
        }
      };
      action.setText(description.getName());
      action.setImageDescriptor(new ImageImageDescriptor(layoutComponentDescription.getIcon()));
      layoutsManager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_HAS = "layout.has";

  /**
   * Prepares {@link LayoutInfo} for any layout existing by default for this container.
   */
  private void initialize_createImplicitLayout() throws Exception {
    if (hasLayout()) {
      AstEditor editor = getEditor();
      Object layout = ReflectionUtils.invokeMethod(getObject(), "getLayout()");
      // create implicit layout model
      LayoutInfo implicitLayout;
      CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
      if (layout == null) {
        implicitLayout = createDefaultImplicitLayout(creationSupport);
      } else {
        implicitLayout =
            (LayoutInfo) JavaInfoUtils.createJavaInfo(editor, layout.getClass(), creationSupport);
      }
      // initialize layout model
      {
        // set variable support
        {
          VariableSupport variableSupport = new ImplicitLayoutVariableSupport(implicitLayout);
          implicitLayout.setVariableSupport(variableSupport);
        }
        // set association
        implicitLayout.setAssociation(new ImplicitObjectAssociation(this));
        // add as child
        addChild(implicitLayout);
      }
    }
  }

  /**
   * In GWT-Ext method <code>Container.getLayout()</code> returns layout only if it was set
   * explicitly. However such components as <code>FormPanel</code> set layout internally, so we need
   * special way to access default implicit layout.
   */
  protected LayoutInfo createDefaultImplicitLayout(CreationSupport creationSupport)
      throws Exception {
    return new DefaultLayoutInfo(getEditor(), creationSupport);
  }

  /**
   * @return <code>true</code> if this {@link ContainerInfo} can have {@link LayoutInfo}.
   */
  public final boolean hasLayout() {
    if (isPlaceholder()) {
      return false;
    }
    return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_HAS);
  }

  /**
   * @return the current {@link LayoutInfo} for this container.
   */
  public final LayoutInfo getLayout() {
    Assert.isTrue(hasLayout());
    // try to find layout
    for (ObjectInfo child : getChildren()) {
      if (child instanceof LayoutInfo) {
        return (LayoutInfo) child;
      }
    }
    // container that has layout, should always have some layout model
    throw new IllegalStateException("Container should always have layout");
  }

  /**
   * Sets new {@link LayoutInfo}.
   */
  public final void setLayout(LayoutInfo newLayout) throws Exception {
    putArbitraryValue(KEY_DONT_SET_IMPLICIT_LAYOUT, Boolean.TRUE);
    startEdit();
    try {
      // remove old layout
      {
        LayoutInfo oldLayout = getLayout();
        oldLayout.delete();
      }
      // set new layout
      VariableSupport variableSupport =
          new EmptyInvocationVariableSupport(newLayout, "%parent%.setLayout(%child%)", 0);
      JavaInfoUtils.add(
          newLayout,
          variableSupport,
          PureFlatStatementGenerator.INSTANCE,
          AssociationObjects.invocationChildNull(),
          this,
          null);
      newLayout.onSet();
    } finally {
      endEdit();
      putArbitraryValue(KEY_DONT_SET_IMPLICIT_LAYOUT, Boolean.FALSE);
    }
  }

  /*////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(Widget_Info component, Widget_Info nextComponent) throws Exception {
  	AssociationObject association = getAssociation_CREATE(component, nextComponent);
  	StatementTarget target = getTarget_CREATE(component, nextComponent);
  	if (target != null) {
  		JavaInfoUtils.addTarget(component, association, this, target);
  	} else {
  		JavaInfoUtils.add(component, association, this, nextComponent);
  	}
  }
  public void command_MOVE(final Widget_Info component, final Widget_Info nextComponent) throws Exception {
  	AssociationObject association_CREATE = getAssociation_CREATE(component, nextComponent);
  	// do move
  	IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
  		public void add() throws Exception {
  			addChild(component, nextComponent);
  		}
  		public void move() throws Exception {
  			moveChild(component, nextComponent);
  		}
  		public StatementTarget getTarget() throws Exception {
  			return getTarget_CREATE(component, nextComponent);
  		}
  	};
  	JavaInfoUtils.moveProvider(component, association_CREATE, this, targetProvider);
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<WidgetInfo> getChildrenWidgets() {
    return getChildren(WidgetInfo.class);
  }

  /*protected AssociationObject getAssociation_CREATE(Widget_Info component, Widget_Info nextComponent) {
  	AssociationObject association =
  			AssociationObjects.invocationChild("%parent%.add(%child%)", false);
  	return association;
  }
  protected StatementTarget getTarget_CREATE(Widget_Info component, Widget_Info nextComponent)
  		throws Exception {
  	StatementTarget target = null;
  	if (nextComponent == null) {
  		Association association_this = getAssociation();
  		if (association_this != null) {
  			target = new StatementTarget(association_this.getStatement(), true);
  		}
  	} else {
  		target = JavaInfoUtils.getTarget(this, nextComponent);
  	}
  	return target;
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the direct child {@link WidgetInfo} with given ID.
   */
  public final WidgetInfo getWidgetByID(String id) throws Exception {
    for (WidgetInfo widget : getChildrenWidgets()) {
      String widgetId = getID(widget);
      if (id.equals(widgetId)) {
        return widget;
      }
    }
    return null;
  }

  /**
   * @return the ID of given {@link ComponentInfo}, from its {@link Object}.
   */
  public static String getID(WidgetInfo widget) throws Exception {
    Class<?> class_DOMUtil =
        JavaInfoUtils.getClassLoader(widget).loadClass("com.gwtext.client.util.DOMUtil");
    return (String) ReflectionUtils.invokeMethod(
        class_DOMUtil,
        "getID(com.google.gwt.user.client.ui.Widget)",
        widget.getObject());
  }
}
