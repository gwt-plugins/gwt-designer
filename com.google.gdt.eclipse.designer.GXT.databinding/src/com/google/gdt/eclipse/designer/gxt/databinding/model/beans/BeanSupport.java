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
package com.google.gdt.eclipse.designer.gxt.databinding.model.beans;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.TypeImageProvider;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author lobas_av
 * 
 */
public final class BeanSupport {
  private final ClassLoader m_classLoader;
  private final AstEditor m_editor;
  private final IJavaProject m_javaProject;
  private final Class<?> m_ModelDataClass;
  private final Map<Class<?>, List<String>> m_properties = Maps.newHashMap();
  private final Map<Class<?>, Map<String, Class<?>>> m_classes = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanSupport(ClassLoader classLoader, AstEditor editor, IJavaProject javaProject)
      throws Exception {
    m_classLoader = classLoader;
    m_editor = editor;
    m_javaProject = javaProject == null ? editor.getJavaProject() : javaProject;
    m_ModelDataClass = classLoader.loadClass("com.extjs.gxt.ui.client.data.ModelData");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<BeanPropertyObserveInfo> getProperties(Class<?> beanClass,
      IObserveInfo parent,
      boolean addSelf) throws Exception {
    List<BeanPropertyObserveInfo> properties = Lists.newArrayList();
    //
    if (addSelf) {
      SimpleObservePresentation selfPresentation =
          new SimpleObservePresentation("<Self Object>",
              "",
              TypeImageProvider.SELF_OBJECT_PROPERTY_IMAGE);
      properties.add(new BeanPropertyObserveInfo(null,
          parent,
          StringReferenceProvider.EMPTY,
          selfPresentation,
          IObserveDecorator.BOLD));
    }
    //
    for (String propertyName : getStringProperties(beanClass)) {
      Class<?> propertyClass = m_classes.get(beanClass).get(propertyName);
      if (propertyClass == null) {
        propertyClass = Object.class;
      }
      IReferenceProvider referenceProvider =
          new StringReferenceProvider("\"" + propertyName + "\"");
      IObservePresentation presentation =
          new SimpleObservePresentation(propertyName,
              propertyName,
              TypeImageProvider.getImage(propertyClass));
      IObserveDecorator decorator = IObserveDecorator.DEFAULT;
      properties.add(new BeanPropertyObserveInfo(propertyClass,
          parent,
          referenceProvider,
          presentation,
          decorator));
    }
    return properties;
  }

  private List<String> getStringProperties(Class<?> beanClass) throws Exception {
    List<String> properties = m_properties.get(beanClass);
    if (properties != null) {
      return properties;
    }
    if (m_ModelDataClass.isAssignableFrom(beanClass)) {
      IType type = m_javaProject.findType(beanClass.getName());
      if (type != null) {
        ICompilationUnit compilationUnit = type.getCompilationUnit();
        if (compilationUnit != null) {
          properties = Lists.newArrayList();
          m_properties.put(beanClass, properties);
          //
          Map<String, Class<?>> classes = Maps.newHashMap();
          m_classes.put(beanClass, classes);
          //
          Set<String> uniqueProperties = Sets.newHashSet();
          AstEditor editor = new AstEditor(compilationUnit);
          editor.getAstUnit().accept(new PropertiesVisitor(uniqueProperties, classes));
          //
          Class<?> superClass = beanClass.getSuperclass();
          if (superClass != null) {
            uniqueProperties.addAll(getStringProperties(superClass));
            //
            Map<String, Class<?>> superClasses = m_classes.get(superClass);
            if (superClasses != null) {
              for (Map.Entry<String, Class<?>> entry : superClasses.entrySet()) {
                if (!classes.containsKey(entry.getKey())) {
                  classes.put(entry.getKey(), entry.getValue());
                }
              }
            }
          }
          //
          properties.addAll(uniqueProperties);
          Collections.sort(properties);
          //
          return properties;
        }
      }
    }
    return Collections.emptyList();
  }

  private class PropertiesVisitor extends ASTVisitor {
    private final Set<String> m_properties;
    private final Map<String, Class<?>> m_classes;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PropertiesVisitor(Set<String> properties, Map<String, Class<?>> classes) {
      m_properties = properties;
      m_classes = classes;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ASTVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void endVisit(MethodInvocation invocation) {
      try {
        String signature = AstNodeUtils.getMethodSignature(invocation);
        List<Expression> arguments = DomGenerics.arguments(invocation);
        if (signature.equals("get(java.lang.String)")
            || signature.startsWith("set(java.lang.String,")
            && arguments.size() == 2) {
          String className;
          if (arguments.size() == 1) {
            className =
                AstNodeUtils.getFullyQualifiedName(
                    AstNodeUtils.getMethodBinding(invocation).getReturnType(),
                    true);
          } else {
            className = AstNodeUtils.getFullyQualifiedName(arguments.get(1), true);
          }
          String propertyName = null;
          if (m_editor == null) {
            Expression expression = arguments.get(0);
            if (expression instanceof StringLiteral) {
              StringLiteral literal = (StringLiteral) expression;
              propertyName = literal.getLiteralValue();
            }
          } else {
            propertyName = CoreUtils.evaluate(String.class, m_editor, arguments.get(0));
          }
          if (!StringUtils.isEmpty(propertyName)) {
            m_properties.add(propertyName);
            if (!className.equals("java.lang.Object") && !m_classes.containsKey(propertyName)) {
              try {
                m_classes.put(
                    propertyName,
                    ReflectionUtils.getClassByName(m_classLoader, className));
              } catch (Throwable e) {
                System.out.println(className + " = " + e); // XXX
              }
            }
          }
        }
      } catch (Throwable e) {
        System.out.println(e); // XXX
      }
    }
  }
}