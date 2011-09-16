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
package com.google.gdt.eclipse.designer.gxt.databinding.model.beans;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.Activator;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.JavaInfoReferenceProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author lobas_av
 * 
 */
public class BeansObserveTypeContainer extends ObserveTypeContainer {
  private List<BeanObserveInfo> m_observables = Collections.emptyList();
  private BeanSupport m_beanSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeansObserveTypeContainer() {
    super(ObserveType.BEANS, false, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveTypeContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getObservables() {
    return CoreUtils.cast(m_observables);
  }

  @Override
  public void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    m_observables = Lists.newArrayList();
    ClassLoader classLoader = CoreUtils.classLoader(root);
    m_beanSupport = new BeanSupport(classLoader, editor, null);
    //
    Image beanImage = Activator.getImage("javabean.gif");
    //
    for (VariableDeclarationFragment fragment : CoreUtils.getFieldFragments(rootNode)) {
      try {
        // prepare bean class
        Type type = CoreUtils.getType(fragment, true);
        Class<?> beanClass =
            CoreUtils.load(classLoader, AstNodeUtils.getFullyQualifiedName(type, true));
        // prepare association widget
        JavaInfo widget = getJavaInfoRepresentedBy(root, fragment.getName().getIdentifier());
        //
        IReferenceProvider referenceProvider = new FragmentReferenceProvider(fragment);
        //
        IObservePresentation presentation =
            new BeanObservePresentation(beanClass, referenceProvider, widget, beanImage);
        //
        m_observables.add(new BeanObserveInfo(m_beanSupport,
            beanClass,
            referenceProvider,
            presentation));
      } catch (ClassNotFoundException e) {
        AbstractParser.addError(editor, "ClassNotFoundException: " + fragment, new Throwable());
      } catch (Throwable e) {
        AbstractParser.addError(editor, "Error during load bean field: " + fragment, e);
      }
    }
  }

  @Override
  public void synchronizeObserves(final JavaInfo root,
      final AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    SynchronizeManager.synchronizeObjects(
        m_observables,
        CoreUtils.getFieldFragments(rootNode),
        new ISynchronizeProcessor<VariableDeclarationFragment, BeanObserveInfo>() {
          public boolean handleObject(BeanObserveInfo object) {
            return true;
          }

          public VariableDeclarationFragment getKeyObject(BeanObserveInfo observe) {
            FragmentReferenceProvider provider =
                (FragmentReferenceProvider) observe.getReferenceProvider();
            return provider.getFragment();
          }

          public boolean equals(VariableDeclarationFragment key0, VariableDeclarationFragment key1) {
            return key0 == key1;
          }

          public BeanObserveInfo findObject(Map<VariableDeclarationFragment, BeanObserveInfo> keyObjectToObject,
              VariableDeclarationFragment key) throws Exception {
            return null;
          }

          public BeanObserveInfo createObject(VariableDeclarationFragment fragment)
              throws Exception {
            try {
              // prepare bean class
              Type type = CoreUtils.getType(fragment, true);
              Class<?> beanClass =
                  CoreUtils.load(
                      CoreUtils.classLoader(root),
                      AstNodeUtils.getFullyQualifiedName(type, true));
              // prepare association widget
              JavaInfo widget = getJavaInfoRepresentedBy(root, fragment.getName().getIdentifier());
              //
              IReferenceProvider referenceProvider = new FragmentReferenceProvider(fragment);
              //
              IObservePresentation presentation =
                  new BeanObservePresentation(beanClass,
                      referenceProvider,
                      widget,
                      Activator.getImage("javabean.gif"));
              //
              return new BeanObserveInfo(m_beanSupport, beanClass, referenceProvider, presentation);
            } catch (ClassNotFoundException e) {
              AbstractParser.addError(
                  editor,
                  "ClassNotFoundException: " + fragment,
                  new Throwable());
              return null;
            }
          }

          public void update(BeanObserveInfo object) throws Exception {
          }
        });
  }

  private static JavaInfo getJavaInfoRepresentedBy(JavaInfo rootJavaInfo, final String variable) {
    final JavaInfo result[] = new JavaInfo[1];
    rootJavaInfo.accept(new ObjectInfoVisitor() {
      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        if (result[0] == null && objectInfo instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) objectInfo;
          if (variable.equals(JavaInfoReferenceProvider.getReference(javaInfo))) {
            result[0] = javaInfo;
          }
        }
        return result[0] == null;
      }
    });
    return result[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanSupport getBeanSupport() {
    return m_beanSupport;
  }

  public BeanObserveInfo getBeanObserveObject(Expression expression) throws Exception {
    // prepare reference
    String reference = CoreUtils.getNodeReference(expression);
    // find object
    for (BeanObserveInfo observeObject : m_observables) {
      if (reference.equals(observeObject.getReference())) {
        return observeObject;
      }
    }
    //
    return null;
  }

  /**
   * @return {@link BeanPropertyObserveInfo} property that association with given {@link Expression}
   *         .
   */
  public BeanPropertyObserveInfo getBeanObserveProperty(BeanObserveInfo observeObject,
      AstEditor editor,
      Expression expression) throws Exception {
    // prepare reference
    String propertyName = CoreUtils.evaluate(String.class, editor, expression);
    String propertyReference = "\"" + propertyName + "\"";
    // find property
    Boolean[] error = new Boolean[1];
    BeanPropertyObserveInfo observeProperty =
        observeObject.resolvePropertyReference(propertyReference, error);
    if (error[0]) {
      AbstractParser.addError(editor, "Property '"
          + expression
          + "' for bean object '"
          + observeObject.getReference()
          + "' not found", new Throwable());
    }
    //
    return observeProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    return null;
  }
}