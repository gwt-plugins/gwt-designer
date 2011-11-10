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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link Property} for single UiBinder event.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class EventHandlerProperty extends AbstractListenerProperty {
  private final EventHandlerDescription m_handler;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventHandlerProperty(XmlObjectInfo object, EventHandlerDescription handler) {
    super(object, handler.getMethodName(), EventHandlerPropertyEditor.INSTANCE);
    m_handler = handler;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return getMethodDeclaration(false) != null;
  }

  @Override
  public void setValue(Object value) throws Exception {
    if (value == UNKNOWN_VALUE && isModified()) {
      if (MessageDialog.openConfirm(
          DesignerPlugin.getShell(),
          "Confirm",
          "Do you really want delete handle '" + m_handler.getMethodName() + "'?")) {
        ExecutionUtils.run(m_object, new RunnableEx() {
          public void run() throws Exception {
            removeListener();
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void removeListener() throws Exception {
    prepareAST();
    try {
      MethodDeclaration method = getMethodDeclaration0();
      m_editor.removeBodyDeclaration(method);
    } finally {
      clearAST();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
      throws Exception {
    IAction[] actions = createListenerMethodActions();
    // append existing stub action
    if (actions[0] != null) {
      manager.appendToGroup(DesignContextMenuProvider.GROUP_EVENTS, actions[0]);
    }
    // append existing or new method action
    implementMenuManager.add(actions[0] != null ? actions[0] : actions[1]);
  }

  /**
   * For given {@link ListenerMethodProperty} creates two {@link Action}'s:
   * 
   * [0] - for existing stub method, may be <code>null</code>;<br>
   * [1] - for creating new stub method.
   */
  private IAction[] createListenerMethodActions() throws Exception {
    String name = m_handler.getMethodName();
    IAction[] actions = new IAction[2];
    // try to find existing stub method
    {
      MethodDeclaration method = getMethodDeclaration(false);
      if (method != null) {
        actions[0] = new ObjectInfoAction(m_object) {
          @Override
          protected void runEx() throws Exception {
            openListener();
          }
        };
        actions[0].setText(name + " -> " + method.getName().getIdentifier());
        actions[0].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
      }
    }
    // in any case prepare action for creating new stub method
    {
      actions[1] = new ObjectInfoAction(m_object) {
        @Override
        protected void runEx() throws Exception {
          openListener();
        }
      };
      actions[1].setText(name);
      actions[1].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
    }
    //
    return actions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openListener() throws Exception {
    final MethodDeclaration method = getMethodDeclaration(true);
    if (method != null) {
      ExecutionUtils.runAsync(new RunnableEx() {
        public void run() throws Exception {
          openMethod_inEditor(method);
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST
  //
  ////////////////////////////////////////////////////////////////////////////
  private IFile m_javaFile;
  private AstEditor m_editor;
  private TypeDeclaration m_typeDeclaration;

  /**
   * Prepares {@link #m_editor} for {@link #m_javaFile}.
   */
  private void prepareAST() throws Exception {
    m_editor = ((UiBinderContext) m_object.getContext()).getFormEditor();
    m_javaFile = (IFile) m_editor.getModelUnit().getUnderlyingResource();
    m_typeDeclaration = m_editor.getPrimaryType();
  }

  /**
   * Clears {@link #m_editor} after finishing AST operations.
   */
  private void clearAST() {
    m_editor = null;
    m_typeDeclaration = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  MethodDeclaration getMethodDeclaration(boolean addNew) throws Exception {
    if (canHaveHandler()) {
      prepareAST();
      try {
        MethodDeclaration method = getMethodDeclaration0();
        if (method != null || !addNew) {
          return method;
        }
      } finally {
        clearAST();
      }
    }
    // new is not requested
    if (!addNew) {
      return null;
    }
    // ensure method
    prepareAST();
    try {
      return createMethodDeclaration0();
    } finally {
      clearAST();
      ExecutionUtils.refresh(m_object);
    }
  }

  /**
   * This method is used for optimization - parsing AST is fairly costly operation, so we try to
   * check quickly at first and do exact check only if needed. Alternative approach is remembering
   * {@link AstEditor} somewhere in context and track Java file changes. But for now we use simplest
   * solution.
   * 
   * @return <code>true</code> if it is possible that there is handler method, or <code>false</code>
   *         if there is definitely no such handler.
   */
  private boolean canHaveHandler() throws Exception {
    String name = NameSupport.getName(m_object);
    if (name == null) {
      return false;
    }
    // prepare Java source
    String formSource;
    {
      UiBinderContext context = (UiBinderContext) m_object.getContext();
      formSource = getTypeSource(context.getFormType());
    }
    // widget name
    if (!formSource.contains("\"" + name + "\"")) {
      return false;
    }
    // handler type name
    {
      String typeName = m_handler.getEventTypeName();
      String shortTypeName = CodeUtils.getShortClass(typeName);
      if (!formSource.contains(shortTypeName)) {
        return false;
      }
    }
    // may be there is handler
    return true;
  }

  /**
   * @return the source of {@link IType}. In theory first call of {@link IType#getSource()} should
   *         be successful, however when we change {@link IType} opened in Java editor there is some
   *         delay between change and time when source will be visible again (reconciling?).
   */
  private static String getTypeSource(IType type) throws Exception {
    String source = null;
    for (int i = 0; i < 5000; i++) {
      source = type.getSource();
      if (source != null) {
        break;
      }
      ExecutionUtils.waitEventLoop(5);
    }
    return source;
  }

  /**
   * @return the existing or new {@link MethodDeclaration} for this event.
   */
  private MethodDeclaration getMethodDeclaration0() throws Exception {
    String name = NameSupport.getName(m_object);
    // try to find existing
    for (MethodDeclaration methodDeclaration : m_typeDeclaration.getMethods()) {
      if (isEventHandler(methodDeclaration) && isObjectHandler(methodDeclaration, name)) {
        return methodDeclaration;
      }
    }
    // no method
    return null;
  }

  /**
   * @return <code>true</code> if {@link MethodDeclaration} has required event type parameter.
   */
  private boolean isEventHandler(MethodDeclaration methodDeclaration) {
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    if (parameters.size() == 1) {
      SingleVariableDeclaration parameter = parameters.get(0);
      String parameterTypeName = AstNodeUtils.getFullyQualifiedName(parameter, false);
      return m_handler.getEventTypeName().equals(parameterTypeName);
    }
    return false;
  }

  /**
   * @return <code>true</code> if {@link MethodDeclaration} has "@UiHandler" annotation with
   *         required name.
   */
  static boolean isObjectHandler(MethodDeclaration methodDeclaration, String name) {
    SingleMemberAnnotation handlerAnnotation = getHandlerAnnotation(methodDeclaration);
    if (handlerAnnotation != null && handlerAnnotation.getValue() instanceof StringLiteral) {
      StringLiteral handlerLiteral = (StringLiteral) handlerAnnotation.getValue();
      String handlerName = handlerLiteral.getLiteralValue();
      return name.equals(handlerName);
    }
    return false;
  }

  /**
   * @return the existing or new {@link MethodDeclaration} for this event.
   */
  private MethodDeclaration createMethodDeclaration0() throws Exception {
    String name = NameSupport.ensureName(m_object);
    String eventTypeName = m_handler.getEventTypeName();
    // prepare name of method
    String methodName;
    {
      String eventName = StringUtils.removeStart(m_handler.getMethodName(), "on");
      String baseName = "on" + StringUtils.capitalize(name) + eventName;
      methodName = m_editor.getUniqueMethodName(baseName);
    }
    // add method
    String uiHandlerAnnotation = "@com.google.gwt.uibinder.client.UiHandler(\"" + name + "\")";
    String header = "void " + methodName + "(" + eventTypeName + " event)";
    MethodDeclaration method =
        m_editor.addMethodDeclaration(
            ImmutableList.<String>of(uiHandlerAnnotation),
            header,
            ImmutableList.<String>of(),
            new BodyDeclarationTarget(m_typeDeclaration, false));
    // done
    return method;
  }

  /**
   * @return the "@UiHandler" {@link SingleMemberAnnotation}, may be <code>null</code>.
   */
  private static SingleMemberAnnotation getHandlerAnnotation(MethodDeclaration methodDeclaration) {
    for (IExtendedModifier modifier : DomGenerics.modifiers(methodDeclaration)) {
      if (modifier instanceof SingleMemberAnnotation) {
        SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
        if (AstNodeUtils.isSuccessorOf(annotation, "com.google.gwt.uibinder.client.UiHandler")) {
          return annotation;
        }
      }
    }
    return null;
  }

  /**
   * Opens source of given Java {@link IFile} at position that corresponds {@link MethodDeclaration}
   * .
   */
  private void openMethod_inEditor(MethodDeclaration method) throws Exception {
    IEditorPart javaEditor = IDE.openEditor(DesignerPlugin.getActivePage(), m_javaFile);
    if (javaEditor instanceof ITextEditor) {
      ((ITextEditor) javaEditor).selectAndReveal(method.getStartPosition(), 0);
    }
  }
}
