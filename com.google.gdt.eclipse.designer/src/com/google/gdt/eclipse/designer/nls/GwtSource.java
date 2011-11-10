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
package com.google.gdt.eclipse.designer.nls;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.ExtendPropertyElement;
import com.google.gdt.eclipse.designer.model.module.InheritsElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.bundle.IPropertiesAccessor;
import org.eclipse.wb.internal.core.nls.bundle.pure.AbstractPureBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstVisitorEx;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Source for GWT Constants class.
 * 
 * @author scheglov_ke
 * @coverage gwt.nls
 */
public class GwtSource extends AbstractPureBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static fields
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Key generator for GWT sources.
   */
  public static final IKeyGeneratorStrategy GWT_KEY_GENERATOR = new IKeyGeneratorStrategy() {
    public final String generateBaseKey(JavaInfo component, GenericProperty property) {
      String propertyTitle = property.getTitle().replace(' ', '_');
      String componentName = component.getVariableSupport().getComponentName();
      return componentName + "_" + propertyTitle;
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BUNDLE_COMMENT = "GWT variable: ";

  @Override
  protected String getBundleComment() {
    return BUNDLE_COMMENT + m_fieldName;
  }

  /**
   * Return "possible" sources that exist in given package.
   * 
   * "Possible" source is source that exists in current package, but is not used in current unit. We
   * show "possible" sources only if there are no "real" sources.
   */
  public static List<AbstractSource> getPossibleSources(JavaInfo root, IPackageFragment pkg)
      throws Exception {
    List<AbstractSource> sources = Lists.newArrayList();
    for (IJavaElement packageElement : pkg.getChildren()) {
      ICompilationUnit unit = (ICompilationUnit) packageElement;
      // prepare IType
      IType type = CodeUtils.findPrimaryType(unit);
      if (type == null) {
        continue;
      }
      // check that type is is successor of Constants
      if (!CodeUtils.isSuccessorOf(type, Constants.CLASS_CONSTANTS)) {
        continue;
      }
      // prepare field name for Constants instance
      // it should be on the first line of default *.properties file
      String fieldName;
      {
        IFolder folder = (IFolder) type.getPackageFragment().getUnderlyingResource();
        IFile defaultPropertiesFile = folder.getFile(type.getElementName() + ".properties");
        if (!defaultPropertiesFile.exists()) {
          continue;
        }
        // check first line for required comment
        InputStream is = defaultPropertiesFile.getContents(true);
        String firstLine = IOUtils2.readFirstLine(is);
        if (firstLine != null && firstLine.startsWith("#" + BUNDLE_COMMENT)) {
          fieldName = firstLine.substring(1 + BUNDLE_COMMENT.length());
        } else {
          fieldName = "CONSTANTS";
        }
      }
      // OK, this is probably correct source
      try {
        String bundleName = type.getFullyQualifiedName();
        AbstractSource source = new GwtSource(root, bundleName, fieldName);
        sources.add(source);
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
    return sources;
  }

  @Override
  public void attachPossible() throws Exception {
    addField(m_root, m_bundleName, m_fieldName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse given expression and return NLSSource for it (new or existing from list).
   */
  public static AbstractSource get(JavaInfo component,
      GenericProperty property,
      Expression expression,
      List<AbstractSource> sources) throws Exception {
    ExpressionInfo expressionInfo = getExpressionInfo(component, expression);
    if (expressionInfo != null) {
      GwtSource source = getNewOrExistingSource(component, expressionInfo, sources);
      source.onKeyAdd(component, expressionInfo.m_key);
      return source;
    }
    return null;
  }

  /**
   * Find existing source with same field or create new one.
   */
  private static GwtSource getNewOrExistingSource(JavaInfo component,
      ExpressionInfo expressionInfo,
      List<AbstractSource> sources) throws Exception {
    for (AbstractSource abstractSource : sources) {
      if (abstractSource instanceof GwtSource) {
        GwtSource source = (GwtSource) abstractSource;
        if (source.m_bundleName.equals(expressionInfo.m_bundleName)) {
          return source;
        }
      }
    }
    //
    return new GwtSource(component.getRootJava(),
        expressionInfo.m_bundleName,
        expressionInfo.m_fieldName);
  }

  /**
   * Parse given expression and if it is valid GWT NLS expression, extract required information.
   */
  private static ExpressionInfo getExpressionInfo(JavaInfo component, Expression expression) {
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      // check invocation
      if (invocation.arguments().size() != 0) {
        return null;
      }
      if (!(invocation.getExpression() instanceof SimpleName)) {
        return null;
      }
      //
      SimpleName field = (SimpleName) invocation.getExpression();
      String bundleName = AstNodeUtils.getFullyQualifiedName(field, true);
      String fieldName = field.getIdentifier();
      //
      SimpleName keyExpression = invocation.getName();
      String key = keyExpression.getIdentifier();
      //
      ExpressionInfo expressionInfo =
          new ExpressionInfo(expression, bundleName, fieldName, keyExpression, key);
      expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
      return expressionInfo;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression information
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information about expression. We store it in expression property to avoid parsing every time.
   */
  protected static class ExpressionInfo extends BasicExpressionInfo {
    private final String m_bundleName;
    private final String m_fieldName;

    public ExpressionInfo(Expression expression,
        String bundleName,
        String fieldName,
        Expression keyExpression,
        String key) {
      super(expression, keyExpression, key);
      m_bundleName = bundleName;
      m_fieldName = fieldName;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_fieldName;
  private final AstEditor m_accessorEditor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtSource(JavaInfo root, String bundleName, String fieldName) throws Exception {
    super(root, bundleName);
    m_fieldName = fieldName;
    // prepare AST editor for Constants class
    {
      IType constants_type = m_root.getEditor().getJavaProject().findType(m_bundleName);
      ICompilationUnit constants_unit = constants_type.getCompilationUnit();
      m_accessorEditor = new AstEditor(constants_unit);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTypeTitle() throws Exception {
    return "Constants in variable/field '" + m_fieldName + "'";
  }

  @Override
  protected IPropertiesAccessor getPropertiesAccessor() {
    return GwtPropertiesAccessor.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IKeyGeneratorStrategy getKeyGeneratorStrategy() {
    return GWT_KEY_GENERATOR;
  }

  @Override
  public void apply_addKey(String key) throws Exception {
    addKey(key);
    super.apply_addKey(key);
  }

  @Override
  protected BasicExpressionInfo apply_externalize_replaceExpression(GenericProperty property,
      String key) throws Exception {
    // replace expression
    Expression expression = property.getExpression();
    key = key.replace('.', '_');
    String newSource = m_fieldName + "." + key + "()";
    Expression newExpression = m_root.getEditor().replaceExpression(expression, newSource);
    // side effect of this invocation is that ExpressionInfo placed in newExpression 
    return getExpressionInfo(m_root, newExpression);
  }

  @Override
  protected void apply_renameKeys_pre(final Map<String, String> oldToNew) throws Exception {
    m_accessorEditor.getAstUnit().accept(new AstVisitorEx() {
      @Override
      public void postVisitEx(ASTNode node) throws Exception {
        if (node instanceof MethodDeclaration) {
          MethodDeclaration methodDeclaration = (MethodDeclaration) node;
          String methodName = methodDeclaration.getName().getIdentifier();
          String newMethodName = oldToNew.get(methodName);
          if (newMethodName != null) {
            m_accessorEditor.setIdentifier(methodDeclaration.getName(), newMethodName);
          }
        }
      }
    });
    commitAccessorChanges();
  }

  @Override
  protected Expression apply_renameKey_replaceKeyExpression(AstEditor editor,
      Expression keyExpression,
      String newKey) throws Exception {
    editor.replaceInvocationName((MethodInvocation) keyExpression.getParent(), newKey);
    return keyExpression;
  }

  @Override
  protected void apply_internalizeKeys_post(final Set<String> keys) throws Exception {
    m_accessorEditor.getAstUnit().accept(new AstVisitorEx() {
      @Override
      public void postVisitEx(ASTNode node) throws Exception {
        if (node instanceof MethodDeclaration) {
          MethodDeclaration methodDeclaration = (MethodDeclaration) node;
          String methodName = methodDeclaration.getName().getIdentifier();
          if (keys.contains(methodName)) {
            m_accessorEditor.removeBodyDeclaration(methodDeclaration);
          }
        }
      }
    });
  }

  /**
   * Create NLS source for given root and parameters.
   */
  public static GwtSource apply_create(IEditableSource editable, JavaInfo root, Object o)
      throws Exception {
    // prepare parameters
    SourceParameters parameters = (SourceParameters) o;
    String fullClassName = parameters.m_constant.m_fullClassName;
    String fieldName = parameters.m_fieldName;
    // create class if it does not exist already
    if (!parameters.m_constant.m_exists) {
      // create Constants class
      {
        ensureI18NModule(root);
        // prepare class source
        String template;
        {
          template = IOUtils.toString(GwtSource.class.getResourceAsStream("newConstants.jvt"));
          template =
              StringUtils.replace(template, "%PACKAGE_NAME%", parameters.m_constant.m_packageName);
          template =
              StringUtils.replace(template, "%CLASS_NAME%", parameters.m_constant.m_className);
        }
        // create class file
        {
          String fileName = parameters.m_constant.m_className + ".java";
          createFileIfDoesNotExist(parameters.m_constant.m_packageFolder, fileName, template);
        }
      }
      // create property bundle
      {
        String propertyFileName = parameters.m_constant.m_className + ".properties";
        createPropertyBundleFile(parameters.m_constant.m_package, propertyFileName, "UTF-8");
      }
    }
    // add field
    addField(root, fullClassName, fieldName);
    // create source
    return new GwtSource(root, fullClassName, fieldName);
  }

  @Override
  public void apply_addLocale(LocaleInfo locale, Map<String, String> values) throws Exception {
    super.apply_addLocale(locale, values);
    modifyLocalesSet(locale, true);
  }

  @Override
  public void apply_removeLocale(LocaleInfo locale) throws Exception {
    super.apply_removeLocale(locale);
    modifyLocalesSet(locale, false);
  }

  @Override
  protected String getCharsetForBundleFiles() {
    return "UTF-8";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String I18N_MODULE = "com.google.gwt.i18n.I18N";

  /**
   * Add field with <code>Constants</code> definition.
   */
  private static void addField(JavaInfo root, String fullClassName, String fieldName)
      throws Exception {
    String code =
        "private static final "
            + fullClassName
            + " "
            + fieldName
            + " = com.google.gwt.core.client.GWT.create("
            + fullClassName
            + ".class);";
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, true);
    root.getEditor().addFieldDeclaration(code, target);
  }

  /**
   * Ensures that module for given compilation unit contains import for module
   * "com.google.gwt.i18n.I18N".
   */
  private static void ensureI18NModule(JavaInfo root) throws Exception {
    ModuleDescription module = getModule(root);
    DefaultModuleProvider.modify(module, new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        // check, may be already import for I18N
        for (InheritsElement inheritsElement : moduleElement.getInheritsElements()) {
          if (inheritsElement.getName().equals(I18N_MODULE)) {
            return;
          }
        }
        // import I18N
        {
          InheritsElement inheritsElement = new InheritsElement();
          moduleElement.addChild(inheritsElement);
          inheritsElement.setName(I18N_MODULE);
        }
      }
    });
  }

  /**
   * Adds or removes (depending on value of "add" parameter) given locale into module.
   */
  private void modifyLocalesSet(final LocaleInfo locale, final boolean add) throws Exception {
    // don't add default locale
    if (locale.isDefault()) {
      return;
    }
    //
    ModuleDescription module = getModule(m_root);
    DefaultModuleProvider.modify(module, new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        // build list of locales in <extent-property name='locale'> elements, remove these elements
        List<String> locales = Lists.newArrayList();
        for (ExtendPropertyElement extendPropertyElement : moduleElement.getExtendPropertyElements()) {
          if (extendPropertyElement.getName().equals("locale")) {
            extendPropertyElement.remove();
            String[] parts = StringUtils.split(extendPropertyElement.getValues(), ", ");
            CollectionUtils.addAll(locales, parts);
          }
        }
        // add/remove new locale
        {
          String localeName = locale.getTitle();
          if (add) {
            locales.add(localeName);
          } else {
            locales.remove(localeName);
          }
        }
        // sort locales
        Collections.sort(locales);
        // add new <extent-property> with all locales
        {
          ExtendPropertyElement extendPropertyElement = new ExtendPropertyElement();
          moduleElement.addChild(extendPropertyElement);
          extendPropertyElement.setName("locale");
          extendPropertyElement.setValues(StringUtils.join(locales.iterator(), ","));
        }
      }
    });
  }

  /**
   * @return the {@link ModuleDescription}, not <code>null</code>.
   */
  private static ModuleDescription getModule(JavaInfo root) throws Exception {
    ICompilationUnit compilationUnit = root.getEditor().getModelUnit();
    return Utils.getSingleModule(compilationUnit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addKey(String key) throws Exception {
    TypeDeclaration typeDeclaration =
        (TypeDeclaration) m_accessorEditor.getAstUnit().types().get(0);
    m_accessorEditor.addInterfaceMethodDeclaration(
        "String " + key + "()",
        new BodyDeclarationTarget(typeDeclaration, false));
    commitAccessorChanges();
  }

  /**
   * Commits changes in accessor {@link AstEditor}, including saving {@link ICompilationUnit}
   * buffer.
   */
  private void commitAccessorChanges() throws Exception {
    m_accessorEditor.commitChanges();
    m_accessorEditor.getModelUnit().save(null, true);
  }
}
