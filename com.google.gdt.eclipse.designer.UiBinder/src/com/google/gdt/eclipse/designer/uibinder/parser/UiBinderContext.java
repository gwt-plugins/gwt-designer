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
package com.google.gdt.eclipse.designer.uibinder.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.IClassLoaderValidator;
import com.google.gdt.eclipse.designer.uibinder.IExceptionConstants;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.ILiveEditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.apache.commons.lang.StringUtils;

import java.beans.Beans;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link EditorContext} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.parser
 */
public class UiBinderContext extends EditorContext {
  private String m_binderClassName;
  private String m_binderResourceName;
  private String m_formClassName;
  private IType m_formType;
  private IFile m_formFile;
  private AstEditor m_formEditor;
  private long m_formFileModification;
  private ModuleDescription m_module;
  private GwtState m_state;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderContext(IFile file, IDocument document) throws Exception {
    super(GwtToolkitDescription.INSTANCE, file, document);
    prepareBinderNames();
    configureDescriptionVersionsProviders();
    addVersions(ImmutableMap.of("isUiBinder", "true"));
  }

  public UiBinderContext(GwtState state, ClassLoader classLoader, IFile file, IDocument document)
      throws Exception {
    this(file, document);
    m_state = state;
    m_classLoader = classLoader;
    updateFromGWTState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getBinderClassName() {
    return m_binderClassName;
  }

  /**
   * @return the {@link IType} of class with "Binder".
   */
  public IType getFormType() {
    return m_formType;
  }

  /**
   * @return the {@link AstEditor} for Java source.
   */
  public AstEditor getFormEditor() throws Exception {
    long currentModificationStamp = m_formFile.getModificationStamp();
    if (m_formEditor == null || currentModificationStamp != m_formFileModification) {
      m_formEditor = new AstEditor(m_formType.getCompilationUnit());
      m_formFileModification = currentModificationStamp;
    }
    return m_formEditor;
  }

  /**
   * Saves and clears {@link AstEditor} for Java source.
   */
  public void saveFormEditor() throws Exception {
    if (m_formEditor != null) {
      m_formEditor.saveChanges(true);
      m_formEditor = null;
    }
  }

  /**
   * @return the {@link GwtState} of this editor.
   */
  public GwtState getState() {
    return m_state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that UiBinder is configured for design time.
   */
  public void runDesignTime(RunnableEx runnable) throws Exception {
    String isKey = "gwt.UiBinder.isDesignTime " + m_binderClassName.replace('$', '.');
    String resKey = "gwt.UiBinder.designTime " + m_binderResourceName;
    boolean old_designTime = Beans.isDesignTime();
    try {
      Beans.setDesignTime(true);
      // mark "Binder" as design time
      System.setProperty(isKey, "true");
      // put current document content into System, to make it available to UiBinderGenerator
      {
        String content = getContent();
        content = removeWbpNameAttributes(content);
        System.setProperty(resKey, content);
      }
      // do run
      runnable.run();
    } finally {
      m_state.getDevModeBridge().invalidateRebind(m_binderClassName);
      Beans.setDesignTime(old_designTime);
      System.clearProperty(isKey);
      System.clearProperty(resKey);
    }
  }

  /**
   * In tests we use "wbp:name" attribute to access widgets by such "internal" names, but UiBinder
   * does not like when it sees unknown attributes, so we should remove them.
   */
  private static String removeWbpNameAttributes(String content) {
    Matcher matcher = Pattern.compile("wbp:name=\"[^\"]*\"").matcher(content);
    // process each match
    int last = 0;
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      // not matched part
      sb.append(content.substring(last, start));
      last = end;
      // replace matched part with spaces
      for (int i = start; i < end; i++) {
        sb.append(' ');
      }
    }
    // append tail
    sb.append(content.substring(last));
    return sb.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Object> m_attributeValues = Maps.newHashMap();

  /**
   * Registers values for attributes, during rendering.
   */
  public void setAttributeValues(Map<String, Object> attributes) {
    m_attributeValues.clear();
    m_attributeValues.putAll(attributes);
  }

  /**
   * Registers value for attribute.
   */
  public void setAttributeValue(DocumentElement element, String name, Object value) {
    String key = UiBinderParser.getPath(element) + " " + name;
    m_attributeValues.put(key, value);
  }

  /**
   * @return the attribute value, remembered earlier during rendering. Value <code>null</code> is
   *         just value, not flag that there are no value. If no value for attribute, then
   *         {@link Property#UNKNOWN_VALUE} will be returned.
   */
  public Object getAttributeValue(DocumentElement element, String name) {
    String key = UiBinderParser.getPath(element) + " " + name;
    if (m_attributeValues.containsKey(key)) {
      return m_attributeValues.get(key);
    }
    return Property.UNKNOWN_VALUE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createClassLoader() throws Exception {
    if (m_classLoader == null) {
      m_module = Utils.getSingleModule(m_file);
      super.createClassLoader();
      executeClassLoaderInitializationScripts();
    }
  }

  /**
   * Allows users to perform {@link ClassLoader} initialization actions, to prepare environment.
   */
  private void executeClassLoaderInitializationScripts() {
    IResource resource = m_file;
    while (true) {
      if (resource.getParent() instanceof IFolder) {
        IFolder folder = (IFolder) resource.getParent();
        resource = folder;
        // use script in current folder
        final IFile scriptFile = folder.getFile("ClassLoaderInitializer.gwtd.mvel");
        if (scriptFile.exists()) {
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              String script = IOUtils2.readString(scriptFile);
              ScriptUtils.evaluate(m_classLoader, script);
            }
          });
        }
      } else {
        break;
      }
    }
  }

  @Override
  protected void addParentClassLoaders(CompositeClassLoader parentClassLoader) throws Exception {
    super.addParentClassLoaders(parentClassLoader);
    // add ClassLoader to use only for loading resources
    {
      ClassLoader resourcesClassLoader = m_module.getClassLoader();
      parentClassLoader.add(resourcesClassLoader, ImmutableList.of(), null);
    }
  }

  @Override
  protected ClassLoader createProjectClassLoader(CompositeClassLoader parentClassLoader)
      throws Exception {
    createGWTState(parentClassLoader);
    {
      // process ClassLoader validators
      List<IClassLoaderValidator> validators =
          ExternalFactoriesHelper.getElementsInstances(
              IClassLoaderValidator.class,
              "com.google.gdt.eclipse.designer.classLoaderValidators",
              "validator");
      for (IClassLoaderValidator validator : validators) {
        validator.validate(m_javaProject, m_state);
      }
    }
    return m_state.getClassLoader();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareBinderNames() throws Exception {
    // template
    IPath templatePath = m_file.getFullPath();
    String templatePathString = templatePath.toPortableString();
    // package
    IPackageFragment uiPackage;
    {
      if (!(m_file.getParent() instanceof IFolder)) {
        throw new DesignerException(IExceptionConstants.NO_FORM_PACKAGE, templatePathString);
      }
      // prepare package
      IFolder uiFolder = (IFolder) m_file.getParent();
      IJavaElement uiElement = JavaCore.create(uiFolder);
      if (!(uiElement instanceof IPackageFragment)) {
        throw new DesignerException(IExceptionConstants.NO_FORM_PACKAGE, templatePathString);
      }
      uiPackage = (IPackageFragment) uiElement;
      // has client package
      if (!Utils.isModuleSourcePackage(uiPackage)) {
        throw new DesignerException(IExceptionConstants.NOT_CLIENT_PACKAGE, templatePathString);
      }
    }
    // binder resource
    m_binderResourceName = uiPackage.getElementName().replace('.', '/') + "/" + m_file.getName();
    // try current package
    {
      String formName = StringUtils.removeEnd(m_file.getName(), ".ui.xml");
      m_formClassName = uiPackage.getElementName() + "." + formName;
      m_formType = m_javaProject.findType(m_formClassName);
      if (m_formType != null) {
        m_formFile = (IFile) m_formType.getCompilationUnit().getUnderlyingResource();
        prepareBinderClass();
        if (m_binderClassName != null) {
          return;
        }
      }
    }
    // try @UiTemplate
    IType uiTemplateType = m_javaProject.findType("com.google.gwt.uibinder.client.UiTemplate");
    List<IJavaElement> references = CodeUtils.searchReferences(uiTemplateType);
    for (IJavaElement reference : references) {
      if (reference instanceof IAnnotation) {
        IAnnotation annotation = (IAnnotation) reference;
        IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
        if (valuePairs.length == 1 && valuePairs[0].getValue() instanceof String) {
          String templateName = (String) valuePairs[0].getValue();
          // prepare ICompilationUnit
          ICompilationUnit compilationUnit =
              (ICompilationUnit) annotation.getAncestor(IJavaElement.COMPILATION_UNIT);
          // prepare qualified template name
          templateName = StringUtils.removeEnd(templateName, ".ui.xml");
          if (templateName.contains(".")) {
            templateName = templateName.replace('.', '/');
          } else {
            String packageName = compilationUnit.getPackageDeclarations()[0].getElementName();
            templateName = packageName.replace('.', '/') + "/" + templateName;
          }
          templateName += ".ui.xml";
          // if found, initialize "form" element
          if (m_binderResourceName.equals(templateName)) {
            m_formType = (IType) annotation.getParent().getParent();
            m_formClassName = m_formType.getFullyQualifiedName();
            m_formFile = (IFile) m_formType.getCompilationUnit().getUnderlyingResource();
            prepareBinderClass();
            if (m_binderClassName != null) {
              return;
            }
          }
        }
      }
    }
    // no Java form
    throw new DesignerException(IExceptionConstants.NO_FORM_TYPE, m_binderResourceName);
  }

  /**
   * Attempts for put into {@link #m_binderClassName} the "UiBinder" inner {@link IType} from
   * {@link #m_formType}.
   */
  private void prepareBinderClass() throws Exception {
    for (IType innerType : m_formType.getTypes()) {
      if (CodeUtils.isSuccessorOf(innerType, "com.google.gwt.uibinder.client.UiBinder")) {
        IType binderType = innerType;
        assertWidgetBased(binderType);
        m_binderClassName = m_formClassName + "$" + innerType.getElementName();
      }
    }
  }

  /**
   * Asserts that given <code>binderType</code> generates <code>Widget</code> when rendered.
   */
  private void assertWidgetBased(IType binderType) throws Exception {
    String[] superSignatures = binderType.getSuperInterfaceTypeSignatures();
    for (String superSignature : superSignatures) {
      int binderIndex = superSignature.indexOf("UiBinder<");
      if (binderIndex != -1) {
        int objectTypeBegin = binderIndex + "UiBinder<".length();
        int objectTypeEnd = superSignature.indexOf(";", binderIndex);
        String objectTypeSignature = superSignature.substring(objectTypeBegin, objectTypeEnd + 1);
        String objectTypeName = CodeUtils.getResolvedTypeName(m_formType, objectTypeSignature);
        if (objectTypeName != null) {
          IType objectType = m_javaProject.findType(objectTypeName);
          if (!CodeUtils.isSuccessorOf(objectType, "com.google.gwt.user.client.ui.Widget")) {
            throw new DesignerException(IExceptionConstants.ONLY_WIDGET_BASED,
                m_formClassName,
                objectTypeName);
          }
        }
      }
    }
  }

  private void createGWTState(CompositeClassLoader parentClassLoader) throws Exception {
    if (m_sharedUse && m_shared_GWTState != null) {
      m_state = m_shared_GWTState;
    } else {
      // initialize GWTState
      m_state = new GwtState(parentClassLoader, m_module);
      m_state.initialize();
      // remember shared state
      if (m_sharedUse) {
        m_state.setShared(true);
        m_shared_GWTState = m_state;
      }
    }
    updateFromGWTState();
  }

  /**
   * Updates this {@link UiBinderContext} from {@link #m_state}.
   */
  private void updateFromGWTState() {
    addVersions(ImmutableMap.of("gwt_isStrictMode", m_state.isStrictMode()));
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
  // Live support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ILiveEditorContext getLiveContext() {
    return m_liveEditorContext;
  }

  private final ILiveEditorContext m_liveEditorContext = new ILiveEditorContext() {
    public XmlObjectInfo parse(String[] sourceLines) throws Exception {
      // prepare document
      String source = StringUtils.join(sourceLines, "\n");
      IDocument document = new Document(source);
      // prepare context
      UiBinderContext context = new UiBinderContext(m_state, m_classLoader, m_file, document);
      context.setLiveComponent(true);
      // do parse
      UiBinderParser parser = new UiBinderParser(context);
      return parser.parse();
    }

    public void dispose() throws Exception {
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Installs {@link IDescriptionVersionsProvider}'s.
   */
  private void configureDescriptionVersionsProviders() throws Exception {
    List<IDescriptionVersionsProviderFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IDescriptionVersionsProviderFactory.class,
            "org.eclipse.wb.core.descriptionVersionsProviderFactories",
            "factory");
    for (IDescriptionVersionsProviderFactory factory : factories) {
      // versions
      addVersions(factory.getVersions(m_javaProject, m_classLoader));
      // version providers
      {
        IDescriptionVersionsProvider provider = factory.getProvider(m_javaProject, m_classLoader);
        if (provider != null) {
          addDescriptionVersionsProvider(provider);
        }
      }
    }
  }
}
