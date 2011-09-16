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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.uibinder.IExceptionConstants;
import com.google.gdt.eclipse.designer.uibinder.model.util.EventHandlersSupport;
import com.google.gdt.eclipse.designer.uibinder.model.util.NameSupport;
import com.google.gdt.eclipse.designer.uibinder.model.util.StylePropertySupport;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiBinderStaticFieldSupport;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiBinderTagResolver;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiChildSupport;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiConstructorSupport;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.IsWidgetInfo;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Parser for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.parser
 */
public final class UiBinderParser {
  private final UiBinderContext m_context;
  private final Map<String, DocumentElement> m_pathToElementMap = Maps.newLinkedHashMap();
  private final Map<String, XmlObjectInfo> m_pathToModelMap = Maps.newLinkedHashMap();
  private XmlObjectInfo m_rootModel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderParser(UiBinderContext context) throws Exception {
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse <code>*.ui.xml</code> files and return root {@link XmlObjectInfo}.
   */
  public XmlObjectInfo parse() throws Exception {
    // validate GWT version
    {
      IJavaProject javaProject = m_context.getJavaProject();
      // has UiBinder support at all
      if (!hasUiBinderSupport(javaProject)) {
        throw new DesignerException(IExceptionConstants.WRONG_VERSION);
      }
      // has support for @UiField(provided) and @UiFactory
      if (!hasUiFieldUiFactorySupport(javaProject)) {
        String formSource = m_context.getFormType().getSource();
        if (formSource.contains("@UiField(provided")) {
          throw new DesignerException(IExceptionConstants.UI_FIELD_FACTORY_FEATURE);
        }
        if (formSource.contains("@UiFactory")) {
          throw new DesignerException(IExceptionConstants.UI_FIELD_FACTORY_FEATURE);
        }
      }
    }
    // prepare for parsing
    GlobalStateXml.setEditorContext(m_context);
    m_context.initialize();
    // parse
    m_context.runDesignTime(new RunnableEx() {
      public void run() throws Exception {
        try {
          parse0();
        } catch (Throwable e) {
          m_context.getBroadcastSupport().getListener(ObjectEventListener.class).dispose();
          ReflectionUtils.propagate(e);
        }
      }
    });
    return m_rootModel;
  }

  /**
   * Implementation of {@link #parse()}.
   */
  private void parse0() throws Exception {
    m_context.setParsing(true);
    fillMap_pathToElement();
    // load Binder
    Object createdBinder;
    {
      ClassLoader classLoader = m_context.getClassLoader();
      String binderClassName = m_context.getBinderClassName();
      Class<?> binderClass = classLoader.loadClass(binderClassName);
      Class<?> classGWT = classLoader.loadClass("com.google.gwt.core.client.GWT");
      createdBinder =
          ReflectionUtils.invokeMethod(classGWT, "create(java.lang.Class)", binderClass);
      createModels(createdBinder);
    }
    // render Widget(s)
    ReflectionUtils.invokeMethod(createdBinder, "createAndBindUi(java.lang.Object)", (Object) null);
    buildHierarchy();
    // done
    m_context.setParsing(false);
    NameSupport.decoratePresentationWithName(m_rootModel);
    XmlObjectUtils.callRootProcessors(m_rootModel);
    new UiBinderTagResolver(m_rootModel);
    new UiBinderStaticFieldSupport(m_rootModel);
    NameSupport.removeName_onDelete(m_rootModel);
    NameSupport.ensureFieldProvided_onCreate(m_rootModel);
    new EventHandlersSupport(m_rootModel);
    new UiConstructorSupport(m_context);
    new UiChildSupport(m_context);
    new StylePropertySupport(m_context);
    GlobalStateXml.activate(m_rootModel);
    m_rootModel.getBroadcast(ObjectInfoTreeComplete.class).invoke();
    m_rootModel.refresh_dispose();
  }

  /**
   * Handle object before setting attributes, so get default property values.
   */
  private void createModels(Object binder) throws Exception {
    ClassLoader classLoader = binder.getClass().getClassLoader();
    String handlerClassName = binder.getClass().getName() + "$DTObjectHandler";
    Class<?> handlerClass = classLoader.loadClass(handlerClassName);
    Object handler =
        Proxy.newProxyInstance(classLoader, new Class[]{handlerClass}, new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("handle")) {
              String path = (String) args[0];
              Object object = args[1];
              createModel(path, object);
            }
            if (method.getName().equals("provideFactory")) {
              Class<?> factoryType = (Class<?>) args[0];
              String methodName = (String) args[1];
              Object[] factoryArgs = (Object[]) args[2];
              return createProvidedFactory(m_context, factoryType, methodName, factoryArgs);
            }
            if (method.getName().equals("provideField")) {
              Class<?> fieldType = (Class<?>) args[0];
              String fieldName = (String) args[1];
              return createProvidedField(m_context, fieldType, fieldName);
            }
            return null;
          }
        });
    ReflectionUtils.setField(binder, "dtObjectHandler", handler);
  }

  private void createModel(String path, Object object) throws Exception {
    DocumentElement xmlElement = m_pathToElementMap.get(path);
    XmlObjectInfo objectInfo =
        XmlObjectUtils.createObject(
            m_context,
            object.getClass(),
            new ElementCreationSupport(xmlElement));
    GlobalStateXml.activate(objectInfo);
    objectInfo.setObject(object);
    m_pathToModelMap.put(path, objectInfo);
  }

  private void buildHierarchy() throws Exception {
    for (Map.Entry<String, XmlObjectInfo> entry : m_pathToModelMap.entrySet()) {
      String path = entry.getKey();
      XmlObjectInfo object = entry.getValue();
      if (object instanceof IsWidgetInfo) {
        object = ((IsWidgetInfo) object).getWrapped();
      }
      XmlObjectInfo parent = findParent(path);
      if (parent != null) {
        parent.addChild(object);
      } else {
        m_rootModel = object;
      }
    }
  }

  /**
   * @return the parent for given "child" path, may be <code>null</code> if root.
   */
  private XmlObjectInfo findParent(String childPath) {
    String parentPath = StringUtils.substringBeforeLast(childPath, "/");
    for (Map.Entry<String, XmlObjectInfo> entry : m_pathToModelMap.entrySet()) {
      String path = entry.getKey();
      XmlObjectInfo parent = entry.getValue();
      if (path.equals(parentPath)) {
        return parent;
      }
    }
    if (childPath.contains("/")) {
      return findParent(parentPath);
    }
    return null;
  }

  /**
   * Visits all {@link DocumentElement}s and remembers all of them with path.
   */
  private void fillMap_pathToElement() {
    m_context.getRootElement().accept(new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        m_pathToElementMap.put(getPath(element), element);
      }
    });
  }

  /**
   * @return the path of our {@link DocumentElement}.
   */
  public static String getPath(DocumentElement element) {
    DocumentElement parent = element.getParent();
    if (parent == null) {
      return "0";
    } else {
      int index = parent.indexOf(element);
      return getPath(parent) + "/" + index;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instance of given {@link Class} created using factory method.
   */
  static Object createProvidedFactory(UiBinderContext context,
      Class<?> factoryType,
      String methodName,
      Object[] args) throws Exception {
    try {
      return createObjectInstance(context, factoryType, args);
    } catch (Throwable e) {
      throw new DesignerException(IExceptionConstants.UI_FACTORY_EXCEPTION,
          e,
          factoryType.getName(),
          methodName);
    }
  }

  /**
   * @return the instance of given {@link Class}.
   */
  static Object createProvidedField(UiBinderContext context, Class<?> fieldType, String fieldName)
      throws Exception {
    try {
      return createObjectInstance(context, fieldType, ArrayUtils.EMPTY_OBJECT_ARRAY);
    } catch (Throwable e) {
      throw new DesignerException(IExceptionConstants.UI_FIELD_EXCEPTION,
          e,
          fieldType.getName(),
          fieldName);
    }
  }

  /**
   * @return the instance of given {@link Class}, created using the most specific method.
   */
  private static Object createObjectInstance(UiBinderContext context, Class<?> clazz, Object[] args)
      throws Exception {
    // try "UiBinder.createInstance" script
    {
      ComponentDescription description = ComponentDescriptionHelper.getDescription(context, clazz);
      String script = description.getParameter("UiBinder.createInstance");
      if (script != null) {
        ClassLoader classLoader = context.getClassLoader();
        Map<String, Object> variables = Maps.newTreeMap();
        variables.put("wbpClassLoader", UiBinderParser.class.getClassLoader());
        variables.put("classLoader", classLoader);
        variables.put("componentClass", clazz);
        variables.put("args", args);
        return ScriptUtils.evaluate(classLoader, script, variables);
      }
    }
    // default constructor
    {
      Constructor<?> constructor = ReflectionUtils.getConstructorBySignature(clazz, "<init>()");
      if (constructor != null) {
        return constructor.newInstance();
      }
    }
    // shortest constructor
    {
      Constructor<?> constructor = ReflectionUtils.getShortestConstructor(clazz);
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      Object[] argumentValues = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        Class<?> parameterType = parameterTypes[i];
        argumentValues[i] = getDefaultValue(parameterType);
      }
      return constructor.newInstance(argumentValues);
    }
  }

  /**
   * @return the argument value for parameter of given type.
   */
  private static Object getDefaultValue(Class<?> parameterType) throws Exception {
    // try INSTANCE field
    {
      Field instanceField = ReflectionUtils.getFieldByName(parameterType, "INSTANCE");
      if (instanceField != null
          && ReflectionUtils.isStatic(instanceField)
          && parameterType.isAssignableFrom(instanceField.getType())) {
        return instanceField.get(null);
      }
    }
    // use default value
    return ReflectionUtils.getDefaultValue(parameterType);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Versions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link IJavaProject} uses at least GWT 2.1, which contains
   *         design-time tweaks for using UiBinder in GWT Designer.
   */
  public static boolean hasUiBinderSupport(IJavaProject javaProject) {
    return Utils.getVersion(javaProject).isHigherOrSame(Utils.GWT_2_1);
  }

  /**
   * @return <code>true</code> if given {@link IJavaProject} uses at least GWT 2.1.1, which contains
   *         design-time tweaks for using UiBinder's <code>@UiField(provided)</code> and
   *         <code>@UiFactory</code> in GWT Designer.
   */
  public static boolean hasUiFieldUiFactorySupport(IJavaProject javaProject) {
    return Utils.getVersion(javaProject).isHigherOrSame(Utils.GWT_2_1_1);
  }
}
