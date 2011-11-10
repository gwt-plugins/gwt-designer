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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.ExtGwtToolkitDescription;
import com.google.gdt.eclipse.designer.gxt.IExceptionConstants;
import com.google.gdt.eclipse.designer.gxt.model.layout.DefaultLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.ImplicitLayoutCreationSupport;
import com.google.gdt.eclipse.designer.gxt.model.layout.ImplicitLayoutVariableSupport;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutDataInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import java.util.List;

/**
 * Model for <code>LayoutContainer</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class LayoutContainerInfo extends ScrollContainerInfo {
  private final LayoutContainerInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutContainerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroacastListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addBroacastListeners() throws Exception {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == m_this) {
          clipboardCopy_addCommands(commands);
        }
      }
    });
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
        LayoutDescriptionHelper.get(ExtGwtToolkitDescription.INSTANCE);
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

  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    initialize_createImplicitLayout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_HAS = "layout.has";
  /**
   * We set this key during {@link #setLayout(LayoutInfo)} to prevent implicit {@link LayoutInfo}
   * activation during layout replacement.
   */
  public static final String KEY_DONT_SET_IMPLICIT_LAYOUT = "KEY_DONT_SET_IMPLICIT_LAYOUT";

  /**
   * @return <code>true</code> if {@link WidgetInfo} of this {@link LayoutContainerInfo} can have
   *         {@link LayoutDataInfo}.
   */
  public boolean hasLayoutData() {
    return hasLayout();
  }

  /**
   * @return <code>true</code> if this {@link LayoutContainerInfo} can have {@link LayoutInfo}.
   */
  public final boolean hasLayout() {
    if (isPlaceholder()) {
      return false;
    }
    return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_HAS);
  }

  private void initialize_createImplicitLayout() throws Exception {
    if (hasLayout()) {
      if (initialize_hasExplicitLayout()) {
        return;
      }
      // prepare for creation
      AstEditor editor = getEditor();
      Object layout = ReflectionUtils.invokeMethod(getObject(), "getLayout()");
      // check if same implicit already exists
      if (initialize_removeImplicitLayout(layout)) {
        return;
      }
      // create layout model
      LayoutInfo implicitLayout;
      CreationSupport creationSupport = new ImplicitLayoutCreationSupport(this);
      if (layout == null) {
        implicitLayout = createDefaultImplicitLayout(editor, creationSupport);
      } else {
        Class<?> layoutClass = layout.getClass();
        implicitLayout =
            (LayoutInfo) JavaInfoUtils.createJavaInfo(editor, layoutClass, creationSupport);
      }
      // set variable support
      VariableSupport variableSupport = new ImplicitLayoutVariableSupport(implicitLayout);
      implicitLayout.setVariableSupport(variableSupport);
      // set association
      implicitLayout.setAssociation(new ImplicitObjectAssociation(this));
      // add as child
      addChildFirst(implicitLayout);
    }
  }

  /**
   * @return the implicit {@link LayoutInfo} for this container. Usually {@link DefaultLayoutInfo} ,
   *         so some containers (such as FormPanel) may not have implicit layout set in constructor,
   *         but we still know that some more specific {@link LayoutInfo} should be used.
   */
  protected LayoutInfo createDefaultImplicitLayout(AstEditor editor, CreationSupport creationSupport)
      throws Exception {
    return new DefaultLayoutInfo(editor, creationSupport);
  }

  /**
   * @return <code>true</code> if explicit layout was already set, so we should not try to find
   *         implicit layout anymore.
   */
  private boolean initialize_hasExplicitLayout() {
    List<LayoutInfo> layouts = getChildren(LayoutInfo.class);
    return !layouts.isEmpty()
        && !(layouts.get(0).getCreationSupport() instanceof ImplicitLayoutCreationSupport);
  }

  /**
   * We may call {@link #initialize_createImplicitLayout()} many times, may be after each
   * {@link Statement}, so before adding new implicit layout we should remove existing one.
   * 
   * @return <code>true</code> if {@link LayoutInfo} with same object already exists, so it was not
   *         removed and no need for creating new implicit {@link LayoutInfo}.
   */
  private boolean initialize_removeImplicitLayout(Object layoutObject) throws Exception {
    for (JavaInfo child : getChildrenJava()) {
      if (child.getCreationSupport() instanceof ImplicitLayoutCreationSupport) {
        if (child.getObject() != layoutObject) {
          return true;
        }
        ImplicitLayoutCreationSupport creationSupport =
            (ImplicitLayoutCreationSupport) child.getCreationSupport();
        creationSupport.removeForever();
        break;
      }
    }
    return false;
  }

  /**
   * @return the current {@link LayoutInfo} for this composite. Can not return <code>null</code>.
   * 
   * @throws IllegalStateException
   *           if no {@link LayoutInfo} found.
   */
  public final LayoutInfo getLayout() {
    Assert.isTrueException(hasLayout(), IExceptionConstants.NO_LAYOUT_EXPECTED, this);
    // try to find layout
    for (ObjectInfo child : getChildren()) {
      if (child instanceof LayoutInfo) {
        return (LayoutInfo) child;
      }
    }
    // composite that has layout, should always have some layout model
    throw new IllegalStateException("Composite should always have layout");
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
      // prepare StatementGenerator
      StatementGenerator statementGenerator;
      if (JavaInfoUtils.hasTrueParameter(newLayout, "layout.setInBlock")) {
        statementGenerator = BlockStatementGenerator.INSTANCE;
      } else {
        statementGenerator = PureFlatStatementGenerator.INSTANCE;
      }
      // set new layout
      VariableSupport variableSupport =
          new EmptyInvocationVariableSupport(newLayout, "%parent%.setLayout(%child%)", 0);
      JavaInfoUtils.add(
          newLayout,
          variableSupport,
          statementGenerator,
          AssociationObjects.invocationChildNull(),
          this,
          null);
    } finally {
      endEdit();
      putArbitraryValue(KEY_DONT_SET_IMPLICIT_LAYOUT, Boolean.FALSE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds commands for coping this {@link LayoutContainerInfo}.
   */
  protected void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
    if (hasLayout()) {
      LayoutInfo layout = getLayout();
      if (layout.getCreationSupport() instanceof IImplicitCreationSupport) {
        // no need to set implicit layout
      } else {
        final JavaInfoMemento layoutMemento = JavaInfoMemento.createMemento(layout);
        commands.add(new ClipboardCommand() {
          private static final long serialVersionUID = 0L;

          @Override
          public void execute(JavaInfo javaInfo) throws Exception {
            LayoutContainerInfo composite = (LayoutContainerInfo) javaInfo;
            LayoutInfo newLayout = (LayoutInfo) layoutMemento.create(javaInfo);
            composite.setLayout(newLayout);
            layoutMemento.apply();
          }
        });
      }
    }
  }
}
