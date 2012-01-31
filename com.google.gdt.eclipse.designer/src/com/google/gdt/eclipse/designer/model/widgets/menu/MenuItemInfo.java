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
package com.google.gdt.eclipse.designer.model.widgets.menu;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AnonymousTypeDeclaration;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for <code>MenuItem</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class MenuItemInfo extends UIObjectInfo implements IAdaptable {
  private final MenuItemInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ignoreSpecialParameterDuringCopy();
    addSubMenuOnItemAdding();
    addContextMenu_openCommand();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ignoreSpecialParameterDuringCopy() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy_Argument(JavaInfo javaInfo,
          ParameterDescription parameter,
          Expression argument,
          String[] source) throws Exception {
        if (javaInfo == m_this) {
          if (parameter.hasTrueTag("MenuBar.command")) {
            source[0] = "(com.google.gwt.user.client.Command) null";
          }
          if (parameter.hasTrueTag("MenuBar.subMenu")) {
            source[0] = "(com.google.gwt.user.client.ui.MenuBar) null";
          }
        }
      }
    });
  }

  private void addSubMenuOnItemAdding() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this && JavaInfoUtils.hasTrueParameter(m_this, "MenuItem.withSubMenu")) {
          if (getCreationSupport() instanceof ConstructorCreationSupport) {
            addNewSubMenu();
          }
        }
      }
    });
  }

  private void addNewSubMenu() throws Exception {
    ConstructorCreationSupport creationSupport = (ConstructorCreationSupport) getCreationSupport();
    ClassInstanceCreation creation = creationSupport.getCreation();
    Statement creationStatement = AstNodeUtils.getEnclosingStatement(creation);
    for (ParameterDescription parameter : creationSupport.getDescription().getParameters()) {
      if (parameter.hasTrueTag("MenuBar.subMenu")) {
        // create "vertical" MenuBar
        MenuBarInfo subMenu =
            (MenuBarInfo) JavaInfoUtils.createJavaInfo(
                getEditor(),
                "com.google.gwt.user.client.ui.MenuBar",
                new ConstructorCreationSupport("vertical", false));
        // add "subMenu" directly before this MenuItem
        JavaInfoUtils.add(
            subMenu,
            new LocalUniqueVariableSupport(subMenu),
            PureFlatStatementGenerator.INSTANCE,
            AssociationObjects.constructorChild(),
            m_this,
            null,
            new StatementTarget(creationStatement, true));
        // update "subMenu" association
        {
          Expression subMenuReference =
              replaceExpression(
                  DomGenerics.arguments(creation).get(parameter.getIndex()),
                  TemplateUtils.getExpression(subMenu));
          subMenu.addRelatedNode(subMenuReference);
        }
      }
    }
  }

  private void addContextMenu_openCommand() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          Action action = new Action("Open Command") {
            @Override
            public void run() {
              openCommand();
            }
          };
          manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS, action);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuBarInfo} child or <code>null</code>.
   */
  public MenuBarInfo getSubMenu() {
    List<MenuBarInfo> menuChildren = getChildren(MenuBarInfo.class);
    return GenericsUtils.getFirstOrNull(menuChildren);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open command
  //
  ////////////////////////////////////////////////////////////////////////////
  public void openCommand() {
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      AbstractInvocationDescription description = creationSupport.getDescription();
      List<Expression> arguments = DomGenerics.arguments(creation);
      openCommand(description, arguments);
    }
    if (getCreationSupport() instanceof ImplicitFactoryCreationSupport) {
      ImplicitFactoryCreationSupport creationSupport =
          (ImplicitFactoryCreationSupport) getCreationSupport();
      MethodInvocation invocation = creationSupport.getInvocation();
      AbstractInvocationDescription description = creationSupport.getDescription();
      List<Expression> arguments = DomGenerics.arguments(invocation);
      openCommand(description, arguments);
    }
  }

  private void openCommand(AbstractInvocationDescription description, List<Expression> arguments) {
    for (ParameterDescription parameter : description.getParameters()) {
      if (parameter.hasTrueTag("MenuBar.command")) {
        int index = parameter.getIndex();
        final Expression oldCommandExpression = arguments.get(index);
        ExecutionUtils.run(this, new RunnableEx() {
          public void run() throws Exception {
            ASTNode nodeToOpen = getCommandNodeToOpen(oldCommandExpression);
            JavaInfoUtils.scheduleOpenNode(m_this, nodeToOpen);
          }
        });
      }
    }
  }

  private ASTNode getCommandNodeToOpen(Expression commandExpression) throws Exception {
    if (isNullCommandExpression(commandExpression)) {
      commandExpression =
          getEditor().replaceExpression(
              commandExpression,
              ImmutableList.of(
                  "new com.google.gwt.user.client.Command() {",
                  "\tpublic void execute() {",
                  "\t}",
                  "}"));
    }
    if (commandExpression instanceof ClassInstanceCreation) {
      ClassInstanceCreation newCommandExpression = (ClassInstanceCreation) commandExpression;
      AnonymousClassDeclaration classDeclaration =
          newCommandExpression.getAnonymousClassDeclaration();
      if (classDeclaration != null) {
        TypeDeclaration typeDeclaration = AnonymousTypeDeclaration.create(classDeclaration);
        return typeDeclaration.getMethods()[0];
      }
    }
    return commandExpression;
  }

  private static boolean isNullCommandExpression(Expression commandExpression) {
    return commandExpression.toString().endsWith("null");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(MenuItemInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return MenuItemInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return null;
    }

    public Rectangle getBounds() {
      return MenuItemInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      MenuBarInfo menu = getSubMenu();
      if (menu != null) {
        return MenuObjectInfoUtils.getMenuInfo(menu);
      } else {
        return null;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return IMenuPolicy.NOOP;
    }
  }
}
