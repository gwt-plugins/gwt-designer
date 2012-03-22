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

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Support for @UiConstructor properties.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class UiConstructorSupport {
  private static final String UI_CONSTRUCTOR = "com.google.gwt.uibinder.client.UiConstructor";
  private final UiBinderContext m_context;
  private final Map<XmlObjectInfo, Property> m_properties = new MapMaker().weakKeys().makeMap();
  private final Map<XmlObjectInfo, Boolean> m_propertyNo = new MapMaker().weakKeys().makeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiConstructorSupport(UiBinderContext context) throws Exception {
    m_context = context;
    // add properties
    m_context.getBroadcastSupport().addListener(null, new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        Property property = getProperty(object);
        if (property != null) {
          properties.add(property);
        }
      }
    });
    // set attributes during create
    m_context.getBroadcastSupport().addListener(null, new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        addMissingConstructorAttributes(child);
      }
    });
  }

  private void addMissingConstructorAttributes(XmlObjectInfo object) throws Exception {
    // may be we know that we will create UiField(provided)
    if (object.getDescription().hasTrueParameter("UiBinder.createFieldProvided")) {
      return;
    }
    // prepare UiConstructor
    Constructor<?> constructor = getUiConstructor(object);
    if (constructor == null) {
      return;
    }
    // check parameter attributes
    String[] parameterNames = getConstructorParameterNames(constructor);
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      String name = parameterNames[i];
      Class<?> type = parameterTypes[i];
      if (object.getAttribute(name) == null) {
        String defaultAttributeValue = getDefaultAttributeValue(type);
        if (defaultAttributeValue != null) {
          object.setAttribute(name, defaultAttributeValue);
        }
      }
    }
  }

  /**
   * @return the value for attribute which represents default value for given type.
   */
  private static String getDefaultAttributeValue(Class<?> type) {
    if (type == String.class) {
      return "String";
    }
    if (type == boolean.class) {
      return "false";
    }
    if (type == int.class || type == double.class) {
      return "0";
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the complex {@link Property} for constructor marked with @UiConstructor, or
   *         <code>null</code> if no such constructor.
   */
  private Property getProperty(XmlObjectInfo object) throws Exception {
    // may be we don't want to have @UiConstructor property
    if (XmlObjectUtils.hasTrueParameter(object, "UiConstructor.disabled")) {
      return null;
    }
    // may be we already know that there are no @UiConstructor property
    if (m_propertyNo.containsKey(object)) {
      return null;
    }
    // try to get cached or create new
    Property constructorProperty = m_properties.get(object);
    if (constructorProperty == null) {
      constructorProperty = createProperty(object);
      if (constructorProperty != null) {
        m_properties.put(object, constructorProperty);
      } else {
        m_propertyNo.put(object, Boolean.TRUE);
      }
    }
    // done
    return constructorProperty;
  }

  /**
   * Creates {@link Property} for {@link #getProperty(XmlObjectInfo)}, may be <code>null</code>.
   */
  private Property createProperty(XmlObjectInfo object) throws Exception {
    Constructor<?> constructor = getUiConstructor(object);
    if (constructor == null) {
      return null;
    }
    // prepare @UiConstructor complex property
    ComplexProperty constructorProperty = new ComplexProperty("UiConstructor", "(Properties)");
    constructorProperty.setCategory(PropertyCategory.system(3));
    constructorProperty.setModified(true);
    constructorProperty.setTooltip("Properties for @UiConstructor arguments.");
    // prepare sub-properties
    List<Property> subPropertiesList = Lists.newArrayList();
    String[] parameterNames = getConstructorParameterNames(constructor);
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      Property property = createProperty(object, parameterNames[i], parameterTypes[i]);
      if (property != null) {
        subPropertiesList.add(property);
      }
    }
    // set sub-properties
    if (!subPropertiesList.isEmpty()) {
      constructorProperty.setProperties(subPropertiesList);
      return constructorProperty;
    }
    return null;
  }

  /**
   * @return the {@link Property} for single constructor parameter.
   */
  private static Property createProperty(XmlObjectInfo object, String name, Class<?> type)
      throws Exception {
    ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(type);
    PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(type);
    ExpressionAccessor accessor = new ExpressionAccessor(name) {
      @Override
      public void setExpression(XmlObjectInfo object, String expression) throws Exception {
        if (expression != null) {
          super.setExpression(object, expression);
        }
      }
    };
    GenericPropertyDescription description =
        new GenericPropertyDescription(name, name, type, accessor);
    description.setConverter(converter);
    description.setEditor(editor);
    return new GenericPropertyImpl(object, description);
  }

  /**
   * @return the {@link Constructor} marker with @UiConstructor, may be <code>null</code>.
   */
  private Constructor<?> getUiConstructor(XmlObjectInfo object) throws Exception {
    Class<?> componentClass = object.getDescription().getComponentClass();
    for (Constructor<?> constructor : componentClass.getConstructors()) {
      Annotation[] annotations = constructor.getAnnotations();
      for (Annotation annotation : annotations) {
        if (ReflectionUtils.isSuccessorOf(annotation, UI_CONSTRUCTOR)) {
          return constructor;
        }
      }
    }
    return null;
  }

  /**
   * @return the names of {@link Constructor} parameters, not <code>null</code>.
   */
  private String[] getConstructorParameterNames(Constructor<?> constructor) throws Exception {
    IJavaProject javaProject = m_context.getJavaProject();
    IMethod method =
        CodeUtils.findMethod(
            javaProject,
            constructor.getDeclaringClass().getName(),
            ReflectionUtils.getConstructorSignature(constructor));
    return method.getParameterNames();
  }
}
