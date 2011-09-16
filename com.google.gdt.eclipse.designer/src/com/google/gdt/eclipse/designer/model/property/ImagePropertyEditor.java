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

import com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleInfo;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.text.MessageFormat;

/**
 * {@link PropertyEditor} for selecting <code>Image</code> from some URL, or from
 * <code>ImageBundle</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public final class ImagePropertyEditor extends TextDisplayPropertyEditor
    implements
      IComplexPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ImagePropertyEditor();

  private ImagePropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    for (Property subProperty : getProperties(property)) {
      if (subProperty.isModified()) {
        String text =
            (String) ReflectionUtils.invokeMethod2(
                subProperty.getEditor(),
                "getText",
                Property.class,
                subProperty);
        return subProperty.getTitle() + ": " + text;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    JavaInfo javaInfo = property.getJavaInfo();
    Property[] properties = (Property[]) javaInfo.getArbitraryValue(this);
    if (properties == null) {
      Shell shell = Display.getCurrent().getActiveShell();
      GwtState state = getState(javaInfo);
      properties = new Property[2];
      properties[0] = new UrlProperty(shell, property, state);
      properties[1] = new Bundle_Property(shell, property);
      javaInfo.putArbitraryValue(this, properties);
    }
    return properties;
  }

  /**
   * @return the {@link GwtState} for hierarchy of given {@link JavaInfo}.
   */
  private static GwtState getState(JavaInfo javaInfo) {
    for (; javaInfo != null; javaInfo = javaInfo.getParentJava()) {
      if (javaInfo instanceof UIObjectInfo) {
        return ((UIObjectInfo) javaInfo).getState();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // URL_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for selecting <code>Image</code> as URL in public resources.
   */
  private static class UrlProperty extends Property {
    private final GenericProperty m_property;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public UrlProperty(Shell shell, GenericProperty property, GwtState state) {
      super(new ImageUrlPropertyEditor(shell, state));
      m_property = property;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "as URL";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      Expression expression = m_property.getExpression();
      if (AstNodeUtils.isCreation(
          expression,
          "com.google.gwt.user.client.ui.Image",
          "<init>(java.lang.String)")) {
        ClassInstanceCreation creation = (ClassInstanceCreation) expression;
        Expression urlExpression = DomGenerics.arguments(creation).get(0);
        return JavaInfoEvaluationHelper.getValue(urlExpression);
      }
      return null;
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value == Property.UNKNOWN_VALUE) {
        m_property.setValue(Property.UNKNOWN_VALUE);
      } else if (value instanceof String) {
        String url = (String) value;
        String source =
            MessageFormat.format(
                "new com.google.gwt.user.client.ui.Image({0})",
                StringConverter.INSTANCE.toJavaSource(null, url));
        m_property.setExpression(source, Property.UNKNOWN_VALUE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle_Property XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for selecting <code>Image</code> as created from
   * <code>AbstractImagePrototype</code>.
   */
  private static class Bundle_Property extends Property {
    private final GenericProperty m_property;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Bundle_Property(Shell shell, GenericProperty property) {
      super(new ImagePrototypePropertyEditor(shell, property.getJavaInfo().getRootJava()));
      m_property = property;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "as Bundle";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      Expression expression = m_property.getExpression();
      if (AstNodeUtils.isMethodInvocation(
          expression,
          "com.google.gwt.user.client.ui.AbstractImagePrototype",
          "createImage()")) {
        MethodInvocation createImageInvocation = (MethodInvocation) expression;
        Expression prototypeExpression = createImageInvocation.getExpression();
        JavaInfo rootJava = m_property.getJavaInfo().getRootJava();
        for (ImageBundleInfo bundle : ImageBundleContainerInfo.getBundles(rootJava)) {
          for (ImageBundlePrototypeDescription prototype : bundle.getPrototypes()) {
            if (prototype.isRepresentedBy(prototypeExpression)) {
              return prototype;
            }
          }
        }
      }
      return null;
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value == Property.UNKNOWN_VALUE) {
        m_property.setValue(Property.UNKNOWN_VALUE);
      } else if (value instanceof ImageBundlePrototypeDescription) {
        ImageBundlePrototypeDescription prototype = (ImageBundlePrototypeDescription) value;
        m_property.setExpression(prototype.getImageSource(), Property.UNKNOWN_VALUE);
      }
    }
  }
}
