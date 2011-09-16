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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.uibinder.Activator;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenTree;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.DirectAssociation;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Support for @UiChild positions.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class UiChildSupport {
  private static final String UI_CHILD = "com.google.gwt.uibinder.client.UiChild";
  private final UiBinderContext m_context;
  private final ClassMap<List<Description>> m_descriptions = ClassMap.create();
  private final Map<WidgetInfo, Map<String, Position>> m_positions =
      new MapMaker().weakKeys().makeMap();
  private final Map<XmlObjectInfo, Property> m_properties = new MapMaker().weakKeys().makeMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiChildSupport(UiBinderContext context) {
    m_context = context;
    m_context.getBroadcastSupport().addListener(null, new ObjectInfoChildrenTree() {
      public void invoke(ObjectInfo parent, List<ObjectInfo> children) throws Exception {
        processTreeChildren(parent, children);
      }
    });
    m_context.getBroadcastSupport().addListener(null, new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        Property property = getProperty(object);
        if (property != null) {
          properties.add(property);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void processTreeChildren(ObjectInfo parent, List<ObjectInfo> children) throws Exception {
    // exclude @UiChild widgets from parent
    if (parent instanceof WidgetInfo) {
      WidgetInfo widget = (WidgetInfo) parent;
      // include Position objects
      {
        Map<String, Position> tagToPosition = getPositions(widget);
        for (Position position : tagToPosition.values()) {
          children.add(position);
          position.setParent(parent);
        }
      }
      // exclude widgets
      for (WidgetInfo child : widget.getChildren(WidgetInfo.class)) {
        Position position = getPosition(widget, child);
        if (position != null) {
          children.remove(child);
        }
      }
    }
  }

  /**
   * @return the {@link Position} to which given child is bound, may be <code>null</code>.
   */
  private Position getPosition(WidgetInfo widget, WidgetInfo child) throws Exception {
    Map<String, Position> tagToPosition = getPositions(widget);
    DocumentElement childElementParent = child.getElement().getParent();
    String tag = childElementParent.getTagLocal();
    return tagToPosition.get(tag);
  }

  /**
   * @return the {@link Position} to which given object is bound as {@link WidgetInfo}, may be
   *         <code>null</code> if not {@link WidgetInfo} or not bound.
   */
  private Position getPosition(XmlObjectInfo object) throws Exception {
    if (object instanceof WidgetInfo && object.getParent() instanceof WidgetInfo) {
      return getPosition((WidgetInfo) object.getParent(), (WidgetInfo) object);
    }
    return null;
  }

  /**
   * Prepare information for {@link WidgetInfo}.
   */
  private Map<String, Position> getPositions(WidgetInfo widget) throws Exception {
    Map<String, Position> tagToPosition = m_positions.get(widget);
    if (tagToPosition == null) {
      // remember positions for Widget
      tagToPosition = Maps.newTreeMap();
      m_positions.put(widget, tagToPosition);
      // fill positions
      for (Description description : getDescriptions(widget)) {
        String tag = description.getTag();
        Position position = new Position(widget, description);
        tagToPosition.put(tag, position);
      }
    }
    return tagToPosition;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "UiChild" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the complex {@link Property} for parent {@link Position}.
   */
  private Property getProperty(XmlObjectInfo object) throws Exception {
    Property property = m_properties.get(object);
    if (property == null) {
      property = createProperty(object);
      if (property != null) {
        m_properties.put(object, property);
      }
    }
    return property;
  }

  /**
   * Creates {@link Property} for {@link #getProperty(XmlObjectInfo)}, may be <code>null</code>.
   */
  private Property createProperty(XmlObjectInfo object) throws Exception {
    // prepare @UiChild method
    Method method;
    {
      Position position = getPosition(object);
      if (position == null) {
        return null;
      }
      method = position.getMethod();
    }
    // if no other parameters than Widget, then no property
    if (method.getParameterTypes().length < 2) {
      return null;
    }
    // prepare @UiChild complex property
    ComplexProperty methodProperty = new ComplexProperty("UiChild", "(Properties)");
    methodProperty.setCategory(PropertyCategory.system(3));
    methodProperty.setModified(true);
    methodProperty.setTooltip("Properties for @UiChild arguments.");
    // prepare sub-properties
    List<Property> subPropertiesList = Lists.newArrayList();
    String[] parameterNames = getMethodParameterNames(method);
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (int i = 1; i < parameterTypes.length; i++) {
      Property property = createProperty(object, parameterNames[i], parameterTypes[i]);
      if (property != null) {
        subPropertiesList.add(property);
      }
    }
    // set sub-properties
    methodProperty.setProperties(subPropertiesList);
    return methodProperty;
  }

  /**
   * @return the {@link Property} for single @UiChild parameter.
   */
  private static Property createProperty(XmlObjectInfo object, String name, Class<?> type)
      throws Exception {
    ExpressionConverter converter = DescriptionPropertiesHelper.getConverterForType(type);
    PropertyEditor editor = DescriptionPropertiesHelper.getEditorForType(type);
    ExpressionAccessor accessor = new ExpressionAccessor(name) {
      @Override
      protected DocumentElement getElement(XmlObjectInfo object) {
        return object.getElement().getParent();
      }

      @Override
      public Object getValue(XmlObjectInfo object) throws Exception {
        UiBinderContext context = (UiBinderContext) object.getContext();
        return context.getAttributeValue(getElement(object), m_attribute);
      }
    };
    GenericPropertyDescription description =
        new GenericPropertyDescription(name, name, type, accessor);
    description.setConverter(converter);
    description.setEditor(editor);
    return new GenericPropertyImpl(object, description);
  }

  /**
   * @return the names of {@link Method} parameters, not <code>null</code>.
   */
  private String[] getMethodParameterNames(Method reflectionMethod) throws Exception {
    IJavaProject javaProject = m_context.getJavaProject();
    IMethod javaMethod =
        CodeUtils.findMethod(
            javaProject,
            reflectionMethod.getDeclaringClass().getName(),
            ReflectionUtils.getMethodSignature(reflectionMethod));
    return javaMethod.getParameterNames();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Description}s for methods of given {@link WidgetInfo}.
   */
  private List<Description> getDescriptions(WidgetInfo widget) throws Exception {
    Class<?> componentClass = widget.getDescription().getComponentClass();
    List<Description> descriptions = m_descriptions.get(componentClass);
    if (descriptions == null) {
      descriptions = Lists.newArrayList();
      for (Method method : componentClass.getMethods()) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
          if (ReflectionUtils.isSuccessorOf(annotation, UI_CHILD)) {
            descriptions.add(new Description(method, annotation));
          }
        }
      }
      m_descriptions.put(componentClass, descriptions);
    }
    return descriptions;
  }

  /**
   * Description for single @UiChild annotated method.
   */
  private static final class Description {
    private final Method m_method;
    private final String m_tag;
    private final int m_limit;
    private final Class<?> m_widgetClass;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Description(Method method, Annotation annotation) throws Exception {
      m_method = method;
      m_tag = getTag(method, annotation);
      m_limit = getLimit(annotation);
      m_widgetClass = method.getParameterTypes()[0];
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Private access
    //
    ////////////////////////////////////////////////////////////////////////////
    private static String getTag(Method method, Annotation annotation) throws Exception {
      String tag = (String) ReflectionUtils.invokeMethod(annotation, "tagname()");
      if (StringUtils.isEmpty(tag)) {
        tag = StringUtils.removeStart(method.getName(), "add").toLowerCase();
      }
      return tag;
    }

    private static Integer getLimit(Annotation annotation) throws Exception {
      return (Integer) ReflectionUtils.invokeMethod(annotation, "limit()");
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Method getMethod() {
      return m_method;
    }

    public String getTag() {
      return m_tag;
    }

    public int getLimit() {
      return m_limit;
    }

    public Class<?> getWidgetClass() {
      return m_widgetClass;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Position
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Model for @UiChild element.
   */
  public final class Position extends ObjectInfo {
    private final WidgetInfo m_widget;
    private final Description m_description;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Position(WidgetInfo widget, Description description) throws Exception {
      m_widget = widget;
      m_description = description;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Method getMethod() {
      return m_description.getMethod();
    }

    public String getTag() {
      return m_description.getTag();
    }

    public Class<?> getWidgetClass() {
      return m_description.getWidgetClass();
    }

    public boolean canAddChild() {
      return getWidgets().size() < m_description.getLimit();
    }

    private List<WidgetInfo> getWidgets() {
      List<WidgetInfo> widgets = Lists.newArrayList();
      for (WidgetInfo child : m_widget.getChildren(WidgetInfo.class)) {
        DocumentElement childElementParent = child.getElement().getParent();
        String tag = childElementParent.getTagLocal();
        String positionTag = m_description.getTag();
        if (tag.equals(positionTag)) {
          widgets.add(child);
        }
      }
      return widgets;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public IObjectPresentation getPresentation() {
      return new DefaultObjectPresentation(this) {
        public String getText() throws Exception {
          return m_description.getTag();
        }

        @Override
        public Image getIcon() throws Exception {
          return Activator.getImage("info/UiChild.png");
        }

        @Override
        public List<ObjectInfo> getChildrenTree() throws Exception {
          List<WidgetInfo> widgets = getWidgets();
          return Lists.<ObjectInfo>newArrayList(widgets);
        }
      };
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Commands
    //
    ////////////////////////////////////////////////////////////////////////////
    public void command_CREATE(WidgetInfo widget, WidgetInfo reference) throws Exception {
      XmlObjectUtils.add(widget, getAssociation(), m_widget, reference);
    }

    public void command_MOVE(WidgetInfo widget, WidgetInfo reference) throws Exception {
      XmlObjectUtils.move(widget, getAssociation(), m_widget, reference);
    }

    private Association getAssociation() {
      return new DirectAssociation() {
        @Override
        public void add(XmlObjectInfo object, ElementTarget target) throws Exception {
          target = prepareTarget(target);
          super.add(object, target);
        }

        @Override
        //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
        public void move(XmlObjectInfo object,
            ElementTarget target,
            XmlObjectInfo oldParent,
            XmlObjectInfo newParent) throws Exception {
          WidgetInfo widget = (WidgetInfo) object;
          // reorder, use same "position" element
          {
            Position position = getPosition(m_widget, widget);
            if (position == Position.this) {
              DocumentElement targetElement = target.getElement();
              int targetIndex = target.getIndex();
              targetElement.moveChild(object.getElement().getParent(), targetIndex);
              return;
            }
          }
          // create new "position" element
          target = prepareTarget(target);
          super.move(object, target, oldParent, newParent);
        }

        private ElementTarget prepareTarget(ElementTarget target) {
          // prepare "position" element
          String tag = m_widget.getElement().getTagNS() + m_description.getTag();
          DocumentElement positionElement = new DocumentElement(tag);
          // add "position" element
          DocumentElement targetElement = target.getElement();
          int targetIndex = target.getIndex();
          targetElement.addChild(positionElement, targetIndex);
          // prepare new target
          return new ElementTarget(positionElement, 0);
        }
      };
    }
  }
}
