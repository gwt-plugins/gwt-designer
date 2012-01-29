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
package com.google.gdt.eclipse.designer.uibinder.parser;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.IsWidgetWrappedInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Renderer for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.parser
 */
public final class UiBinderRenderer {
  private final XmlObjectInfo m_rootModel;
  private final UiBinderContext m_context;
  private final Map<String, XmlObjectInfo> m_pathToModelMap = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderRenderer(XmlObjectInfo rootModel) throws Exception {
    m_rootModel = rootModel;
    m_context = (UiBinderContext) m_rootModel.getContext();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Render
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Renders current content of {@link EditorContext} and fill objects for {@link XmlObjectInfo}s.
   */
  public void render() throws Exception {
    m_context.runDesignTime(new RunnableEx() {
      public void run() throws Exception {
        render0();
      }
    });
  }

  /**
   * Implementation of {@link #render()}.
   */
  private void render0() throws Exception {
    GlobalStateXml.activate(m_rootModel);
    fillMap_pathToModel();
    // load Binder
    Object createdBinder;
    {
      ClassLoader classLoader = m_context.getClassLoader();
      String binderClassName = m_context.getBinderClassName();
      m_context.getState().getDevModeBridge().invalidateRebind(binderClassName);
      Class<?> binderClass = classLoader.loadClass(binderClassName);
      Class<?> classGWT = classLoader.loadClass("com.google.gwt.core.client.GWT");
      createdBinder =
          ReflectionUtils.invokeMethod(classGWT, "create(java.lang.Class)", binderClass);
      setObjects(createdBinder);
    }
    // render Widget(s)
    ReflectionUtils.invokeMethod(createdBinder, "createAndBindUi(java.lang.Object)", (Object) null);
    setAttributes(createdBinder);
  }

  /**
   * Visits all {@link XmlObjectInfo} and remembers all of them with path.
   */
  private void fillMap_pathToModel() {
    m_rootModel.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof XmlObjectInfo) {
          XmlObjectInfo xmlObjectInfo = (XmlObjectInfo) objectInfo;
          CreationSupport creationSupport = xmlObjectInfo.getCreationSupport();
          if (!XmlObjectUtils.isImplicit(xmlObjectInfo)) {
            DocumentElement element = creationSupport.getElement();
            String path = UiBinderParser.getPath(element);
            if (xmlObjectInfo instanceof IsWidgetWrappedInfo) {
              xmlObjectInfo = ((IsWidgetWrappedInfo) xmlObjectInfo).getWrapper();
            }
            m_pathToModelMap.put(path, xmlObjectInfo);
          }
        }
      }
    });
  }

  private void setObjects(Object binder) throws Exception {
    ClassLoader classLoader = binder.getClass().getClassLoader();
    String handlerClassName = binder.getClass().getName() + "$DTObjectHandler";
    Class<?> handlerClass = classLoader.loadClass(handlerClassName);
    Object handler =
        Proxy.newProxyInstance(classLoader, new Class[]{handlerClass}, new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("handle")) {
              String path = (String) args[0];
              Object object = args[1];
              XmlObjectInfo objectInfo = m_pathToModelMap.get(path);
              objectInfo.setObject(object);
            }
            if (method.getName().equals("provideFactory")) {
              Class<?> factoryType = (Class<?>) args[0];
              String methodName = (String) args[1];
              Object[] factoryArgs = (Object[]) args[2];
              return UiBinderParser.createProvidedFactory(
                  m_context,
                  factoryType,
                  methodName,
                  factoryArgs);
            }
            if (method.getName().equals("provideField")) {
              Class<?> fieldType = (Class<?>) args[0];
              String fieldName = (String) args[1];
              return UiBinderParser.createProvidedField(m_context, fieldType, fieldName);
            }
            return null;
          }
        });
    ReflectionUtils.setField(binder, "dtObjectHandler", handler);
  }

  private void setAttributes(Object binder) throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, Object> attributes =
        (Map<String, Object>) ReflectionUtils.getFieldObject(binder, "dtAttributes");
    // individual attributes for models
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String path;
      String attribute;
      {
        String key = entry.getKey();
        String[] keyParts = StringUtils.split(key);
        path = keyParts[0];
        attribute = keyParts[1];
      }
      XmlObjectInfo objectInfo = m_pathToModelMap.get(path);
      if (objectInfo != null) {
        Object value = entry.getValue();
        objectInfo.registerAttributeValue(attribute, value);
      }
    }
    // attributes for all elements
    m_context.setAttributeValues(attributes);
  }
}
