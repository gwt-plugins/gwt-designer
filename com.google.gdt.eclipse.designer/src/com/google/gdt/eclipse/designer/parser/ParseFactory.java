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
package com.google.gdt.eclipse.designer.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelCreationSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelCreationSupport;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.preferences.IPreferenceConstants;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.IJavaInfoParseResolver;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.parser.ParseRootContext;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

import java.util.List;

/**
 * {@link IParseFactory} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.parser
 */
public class ParseFactory extends AbstractParseFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    // check for GWT type
    {
      // check if there are com.google.gwt.user.client.ui.UIObject successors
      final boolean[] isGWT = new boolean[1];
      typeDeclaration.accept(new ASTVisitor() {
        @Override
        public void postVisit(ASTNode node) {
          if (!isGWT[0] && node instanceof Expression) {
            Expression expression = (Expression) node;
            ITypeBinding expressionBinding = AstNodeUtils.getTypeBinding(expression);
            if (AstNodeUtils.isSuccessorOf(
                expressionBinding,
                "com.google.gwt.user.client.ui.UIObject")) {
              isGWT[0] = true;
            }
          }
        }
      });
      // not a GWT, stop
      if (!isGWT[0]) {
        return null;
      }
    }
    // special unsupported classes
    {
      ITypeBinding superClass = typeBinding.getSuperclass();
      String superClassName = AstNodeUtils.getFullyQualifiedName(superClass, false);
      if (superClassName.equals("com.google.gwt.user.client.ui.Widget")) {
        throw new DesignerException(IExceptionConstants.NO_DESIGN_WIDGET);
      }
    }
    // prepare class loader
    initializeClassLoader(editor);
    // check for @wbp.parser.entryPoint
    {
      MethodDeclaration method = ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration);
      if (method != null) {
        List<MethodDeclaration> rootMethods = Lists.newArrayList(method);
        return new ParseRootContext(null, new ExecutionFlowDescription(rootMethods));
      }
    }
    // support for EntryPoint
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.core.client.EntryPoint")) {
      MethodDeclaration onModuleLoadMethod =
          AstNodeUtils.getMethodBySignature(typeDeclaration, "onModuleLoad()");
      if (onModuleLoadMethod != null) {
        List<MethodDeclaration> rootMethods = Lists.newArrayList(onModuleLoadMethod);
        return new ParseRootContext(null, new ExecutionFlowDescription(rootMethods));
      }
    }
    // support for com.google.gwt.user.client.ui.UIObject
    if (AstNodeUtils.isSuccessorOf(typeBinding, "com.google.gwt.user.client.ui.UIObject")) {
      ITypeBinding typeBinding_super = typeBinding.getSuperclass();
      // prepare class of component
      Class<?> superClass = getSuperClass(editor, typeBinding_super);
      // prepare creation
      MethodDeclaration constructor = getConstructor(editor, typeDeclaration);
      ThisCreationSupport creationSupport = new ThisCreationSupport(constructor);
      // create JavaInfo
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, superClass, creationSupport);
      if (javaInfo != null) {
        javaInfo.setVariableSupport(new ThisVariableSupport(javaInfo, constructor));
        // prepare root context
        List<MethodDeclaration> rootMethods = Lists.newArrayList();
        rootMethods.add(constructor);
        return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
      }
    }
    // no root found
    return null;
  }

  @Override
  public JavaInfo create(AstEditor editor,
      ClassInstanceCreation creation,
      IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression arguments[],
      JavaInfo argumentInfos[]) throws Exception {
    if (!hasGWT(editor)) {
      return null;
    }
    if (creation.getAnonymousClassDeclaration() != null) {
      typeBinding = typeBinding.getSuperclass();
    }
    // check "super"
    {
      JavaInfo javaInfo =
          super.create(editor, creation, methodBinding, typeBinding, arguments, argumentInfos);
      if (javaInfo != null) {
        return javaInfo;
      }
    }
    // GWT object
    if (isGWTObject(typeBinding)) {
      // prepare class of component
      Class<?> componentClass = getClass(editor, typeBinding);
      if (componentClass == null) {
        return null;
      }
      // create JavaInfo
      CreationSupport creationSupport = new ConstructorCreationSupport(creation);
      return JavaInfoUtils.createJavaInfo(editor, componentClass, creationSupport);
    }
    // unknown class
    return null;
  }

  @Override
  public JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression arguments[],
      JavaInfo expressionInfo,
      JavaInfo argumentInfos[],
      IJavaInfoParseResolver javaInfoResolver) throws Exception {
    if (!hasGWT(editor)) {
      return null;
    }
    // check "super"
    {
      JavaInfo javaInfo =
          super.create(
              editor,
              invocation,
              methodBinding,
              arguments,
              expressionInfo,
              argumentInfos,
              javaInfoResolver);
      if (javaInfo != null) {
        return javaInfo;
      }
    }
    // RootPanel.get()
    if (AstNodeUtils.isMethodInvocation(
        invocation,
        "com.google.gwt.user.client.ui.RootPanel",
        new String[]{"get()", "get(java.lang.String)"})) {
      JavaInfo rootPanel = javaInfoResolver.getJavaInfo(invocation);
      if (rootPanel == null) {
        rootPanel =
            JavaInfoUtils.createJavaInfo(
                editor,
                getClass(editor, methodBinding.getReturnType()),
                new RootPanelCreationSupport(invocation));
      }
      return rootPanel;
    }
    // RootLayoutPanel.get()
    if (AstNodeUtils.isMethodInvocation(
        invocation,
        "com.google.gwt.user.client.ui.RootLayoutPanel",
        "get()")) {
      JavaInfo rootPanel = javaInfoResolver.getJavaInfo(invocation);
      if (rootPanel == null) {
        rootPanel =
            JavaInfoUtils.createJavaInfo(
                editor,
                getClass(editor, methodBinding.getReturnType()),
                new RootLayoutPanelCreationSupport(invocation));
      }
      return rootPanel;
    }
    // GWT.create(classLiteral)
    if (arguments.length == 1
        && arguments[0] instanceof TypeLiteral
        && AstNodeUtils.isMethodInvocation(
            invocation,
            "com.google.gwt.core.client.GWT",
            "create(java.lang.Class)")) {
      return createJavaInfo_forGWTCreate(editor, invocation, arguments);
    }
    // AbstractImagePrototype#createImage()
    if (arguments.length == 0
        && AstNodeUtils.isMethodInvocation(
            invocation,
            "com.google.gwt.user.client.ui.AbstractImagePrototype",
            "createImage()")) {
      Class<?> imageClass = getClass(editor, methodBinding.getReturnType());
      OpaqueCreationSupport creationSupport = new OpaqueCreationSupport(invocation);
      creationSupport.setPermissions(ImageBundlePrototypeDescription.PERMISSIONS);
      return JavaInfoUtils.createJavaInfo(editor, imageClass, creationSupport);
    }
    // no JavaInfo for MethodInvocation
    return null;
  }

  @Override
  public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    return isGWTObject(typeBinding);
  }

  private static JavaInfo createJavaInfo_forGWTCreate(final AstEditor editor,
      final MethodInvocation invocation,
      Expression[] arguments) throws Exception {
    TypeLiteral typeLiteral = (TypeLiteral) arguments[0];
    final Class<?> classLiteral = getClass(editor, typeLiteral.getType().resolveBinding());
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<JavaInfo>() {
      public JavaInfo runObject() throws Exception {
        return JavaInfoUtils.createJavaInfo(
            editor,
            classLiteral,
            new OpaqueCreationSupport(invocation));
      }
    }, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sharing GWTState
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_sharedUse = false;
  private static GwtState m_shared_GWTState;

  /**
   * Specifies if next parsing should use shared {@link GwtState}.
   */
  public static void setUseSharedGWTState(boolean use) {
    m_sharedUse = use;
  }

  /**
   * Disposes existing shared {@link GwtState}.
   */
  public static void disposeSharedGWTState() {
    if (m_shared_GWTState != null) {
      m_shared_GWTState.setShared(false);
      m_shared_GWTState.dispose();
      m_shared_GWTState = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_HAS_GWT = ParseFactory.class.getName();

  /**
   * @return <code>true</code> if given {@link AstEditor} can use GWT.
   */
  private static boolean hasGWT(AstEditor editor) {
    Boolean result = (Boolean) editor.getGlobalValue(KEY_HAS_GWT);
    if (result == null) {
      IJavaProject javaProject = editor.getJavaProject();
      try {
        result = javaProject.findType("com.google.gwt.user.client.ui.UIObject") != null;
      } catch (Throwable e) {
        result = false;
      }
      editor.putGlobalValue(KEY_HAS_GWT, result);
    }
    return result.booleanValue();
  }

  /**
   * @return <code>true</code> if given {@link ITypeBinding} is GWT object.
   */
  private static boolean isGWTObject(ITypeBinding typeBinding) throws Exception {
    if (typeBinding == null) {
      return false;
    }
    // check UIObject
    if (AstNodeUtils.isSuccessorOf(
        typeBinding,
        "com.google.gwt.user.client.ui.UIObject",
        "com.google.gwt.user.cellview.client.Column",
        "com.gwtext.client.widgets.layout.ContainerLayout",
        "com.gwtext.client.widgets.layout.LayoutData",
        "com.gwtext.client.widgets.grid.ColumnConfig",
        "com.gwtext.client.data.Node",
        "com.extjs.gxt.ui.client.widget.Layout",
        "com.extjs.gxt.ui.client.widget.layout.LayoutData",
        "com.extjs.gxt.ui.client.widget.grid.ColumnConfig",
        "com.smartgwt.client.widgets.layout.SectionStackSection",
        "com.smartgwt.client.widgets.grid.ListGridField",
        "com.smartgwt.client.widgets.viewer.DetailViewerField",
        "com.smartgwt.client.widgets.menu.MenuItem",
        "com.smartgwt.client.widgets.form.fields.FormItem",
        "com.smartgwt.client.widgets.tab.Tab",
        "com.smartgwt.client.widgets.toolbar.ToolStripSpacer",
        "com.smartgwt.client.data.DataSource",
        "com.smartgwt.client.data.DataSourceField")) {
      return true;
    }
    //
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getToolkitId() {
    return IPreferenceConstants.TOOLKIT_ID;
  }

  @Override
  protected ClassLoader getClassLoader(AstEditor editor) throws Exception {
    EditorState editorState = EditorState.get(editor);
    IJavaProject javaProject = editor.getJavaProject();
    ICompilationUnit modelUnit = editor.getModelUnit();
    // check for shared GWTState
    if (m_sharedUse && m_shared_GWTState != null) {
      editor.putGlobalValue(UIObjectInfo.STATE_KEY, m_shared_GWTState);
      rememberVariable_isStrictMode(editorState, m_shared_GWTState);
      m_shared_GWTState.activate();
      return m_shared_GWTState.getClassLoader();
    }
    // use same GwtState for same editor
    boolean hasCurrentEditor = GwtEditorLifeCycleListener.hasCurrentEditor();
    if (hasCurrentEditor) {
      GwtState state = GwtEditorLifeCycleListener.getCurrentEditorState();
      if (state != null) {
        editor.putGlobalValue(UIObjectInfo.STATE_KEY, state);
        rememberVariable_isStrictMode(editorState, state);
        state.activate();
        return state.getClassLoader();
      }
    }
    // prepare module
    ModuleDescription moduleDescription = Utils.getSingleModule(modelUnit);
    if (moduleDescription == null) {
      throw new DesignerException(IExceptionConstants.NO_MODULE_FILE);
    }
    // always include standard D2 ClassLoader's
    CompositeClassLoader parentClassLoader;
    {
      parentClassLoader = createClassLoader_parent(editor);
      initializeClassLoader_parent(editor, parentClassLoader);
      // add ClassLoader to use only for loading resources
      {
        ClassLoader resourcesClassLoader = moduleDescription.getClassLoader();
        parentClassLoader.add(resourcesClassLoader, ImmutableList.<String>of(), null);
      }
    }
    // initialize GWTState
    final GwtState state = new GwtState(parentClassLoader, moduleDescription);
    try {
      state.initialize();
      editor.putGlobalValue(UIObjectInfo.STATE_KEY, state);
      rememberVariable_isStrictMode(editorState, state);
    } catch (Throwable e) {
      state.dispose();
      ReflectionUtils.propagate(e);
    }
    // remember shared state
    if (m_sharedUse) {
      state.setShared(true);
      m_shared_GWTState = state;
    }
    // remember editor GwtState
    GwtEditorLifeCycleListener.setCurrentEditorState(state);
    // remember ClassLoader
    ClassLoader gwtClassLoader = state.getClassLoader();
    // validate
    try {
      validate(javaProject, state);
    } catch (Throwable e) {
      state.dispose();
      ReflectionUtils.propagate(e);
    }
    // dispose GWTState during hierarchy dispose
    if (!hasCurrentEditor) {
      editorState.getBroadcast().addListener(null, new ObjectEventListener() {
        @Override
        public void dispose() throws Exception {
          state.dispose();
        }
      });
    }
    // final result
    return gwtClassLoader;
  }

  /**
   * Remembers "isStrictMode" variable.
   */
  private void rememberVariable_isStrictMode(EditorState editorState, GwtState state) {
    editorState.addVersions(ImmutableMap.of("gwt_isStrictMode", state.isStrictMode()));
  }

  private static void validate(IJavaProject javaProject, GwtState state) throws Exception {
    List<IClassLoaderValidator> validators =
        ExternalFactoriesHelper.getElementsInstances(
            IClassLoaderValidator.class,
            "com.google.gdt.eclipse.designer.classLoaderValidators",
            "validator");
    for (IClassLoaderValidator validator : validators) {
      validator.validate(javaProject, state);
    }
  }
}
