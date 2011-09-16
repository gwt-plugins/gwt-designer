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
package com.google.gdt.eclipse.designer.model.property;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * {@link PropertyEditor} for editing DateTimeFormat.
 * 
 * @author sablin_aa
 * @coverage gwt.model.property
 */
public final class DateTimeFormatPropertyEditor extends AbstractComboPropertyEditor
    implements
      IValueSourcePropertyEditor,
      IConfigurablePropertyObject {
  private CompositeClassLoader m_loader;
  private Class<?> m_formatClass;
  private List<Method> m_formatMethods;
  private String m_extractScript;
  private String m_sourceTemplate;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DateTimeFormatPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    String text = getPattern(property);
    if (text == null) {
      Object value = property.getValue();
      if (value != null && value != Property.UNKNOWN_VALUE) {
        text = value.getClass().getCanonicalName();
      }
    }
    return text;
  }

  private String getPattern(Property property) throws Exception {
    Object value = extractFormat(property);
    if (value != null && m_formatClass.isAssignableFrom(value.getClass())) {
      return (String) ReflectionUtils.invokeMethod(value, "getPattern()");
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    for (Method method : m_formatMethods) {
      combo.add(getMethodTitle(method));
    }
    combo.add("Custom...");
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    Object format = extractFormat(property);
    for (Method method : m_formatMethods) {
      if (format == method.invoke(m_formatClass)) {
        combo.setText(getMethodTitle(method));
        return;
      }
    }
    if (getPattern(property) != null) {
      combo.select(combo.getItemCount() - 1);
    }
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    if (index == m_formatMethods.size()) {
      // custom item selected
      InputDialog inputDialog =
          new InputDialog(combo.getShell(),
              "Date&time format",
              "Enter date & time format pattern",
              getPattern(property),
              null);
      if (inputDialog.open() == Window.OK) {
        String pattern = inputDialog.getValue();
        Object value =
            ReflectionUtils.invokeMethod(m_formatClass, "getFormat(java.lang.String)", pattern);
        setPropertyExpression((GenericProperty) property, "getFormat(\"" + pattern + "\")", value);
      }
    } else {
      // standard selected
      Method method = m_formatMethods.get(index);
      setPropertyExpression(
          (GenericProperty) property,
          method.getName() + "()",
          method.invoke(m_formatClass));
    }
  }

  private void setPropertyExpression(GenericProperty property, String invocation, Object value)
      throws Exception {
    String source = ReflectionUtils.getFullyQualifiedName(m_formatClass, false) + "." + invocation;
    property.setExpression(getSourceByTemplate(source), value);
  }

  private String getSourceByTemplate(String source) {
    return (StringUtils.isEmpty(m_sourceTemplate) ? source : StringUtils.replace(
        m_sourceTemplate,
        "%value%",
        source)).trim();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueSource(Object value) throws Exception {
    if (value != null && m_formatClass.isAssignableFrom(value.getClass())) {
      String formatFullyQualifiedName = ReflectionUtils.getFullyQualifiedName(m_formatClass, false);
      // check for static formats
      for (Method method : m_formatMethods) {
        if (value == method.invoke(m_formatClass)) {
          return getSourceByTemplate(formatFullyQualifiedName + "." + method.getName() + "()");
        }
      }
      // get custom format
      String customPattern = (String) ReflectionUtils.invokeMethod(value, "getPattern()");
      return getSourceByTemplate(formatFullyQualifiedName + ".getFormat(\"" + customPattern + "\")");
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    m_loader = new CompositeClassLoader() {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (ReflectionUtils.class.getCanonicalName().equals(name)) {
          return ReflectionUtils.class;
        }
        return super.loadClass(name);
      }
    };
    m_loader.add(state.getEditorLoader(), null);
    // get static formats
    {
      m_formatClass = m_loader.loadClass("com.google.gwt.i18n.client.DateTimeFormat");
      m_formatMethods = Lists.newArrayList();
      for (Method method : m_formatClass.getMethods()) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)
            && Modifier.isPublic(modifiers)
            && method.getParameterTypes().length == 0
            && method.getName().endsWith("Format")) {
          m_formatMethods.add(method);
        }
      }
    }
    // extract script
    {
      final String EXTRACT_PARAM = "extract";
      if (parameters.containsKey(EXTRACT_PARAM)) {
        m_extractScript = (String) parameters.get(EXTRACT_PARAM);
      } else {
        m_extractScript = "";
      }
    }
    // source template
    {
      final String SOURCE_PARAM = "source";
      if (parameters.containsKey(SOURCE_PARAM)) {
        m_sourceTemplate = (String) parameters.get(SOURCE_PARAM);
      } else {
        m_sourceTemplate = "";
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return Extracted by script (if specified) date-time format object
   */
  private Object extractFormat(Property property) throws Exception {
    Object value = property.getValue();
    if (Property.UNKNOWN_VALUE == value) {
      return null;
    }
    if (!StringUtils.isEmpty(m_extractScript)) {
      Map<String, Object> variables = Maps.newTreeMap();;
      variables.put("value", value);
      if (property instanceof GenericProperty) {
        variables.put("control", ((GenericProperty) property).getJavaInfo().getObject());
      }
      value = evaluate(m_extractScript, variables);
    }
    return value;
  }

  /**
   * @return evaluated object
   */
  private Object evaluate(String script, Map<String, Object> variables) throws Exception {
    if (m_loader == null) {
      return ScriptUtils.evaluate(script, variables);
    } else {
      return ScriptUtils.evaluate(m_loader, script, variables);
    }
  }

  /**
   * @return displaying title for format getter method
   */
  private String getMethodTitle(Method method) {
    return StringUtils.replace(method.getName(), "get", "");
  }
}
