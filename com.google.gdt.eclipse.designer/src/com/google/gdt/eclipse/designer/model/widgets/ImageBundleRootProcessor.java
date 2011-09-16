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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.gdt.eclipse.designer.IExceptionConstants;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * Support for {@link ImageBundleInfo} features.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class ImageBundleRootProcessor implements IRootProcessor {
  private static final String IMAGE_BUNDLE = "%ImageBundle%";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new ImageBundleRootProcessor();

  private ImageBundleRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    processRoot(root);
    processComponents(root, components);
  }

  private void processRoot(final JavaInfo root) {
    root.addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy_Argument(JavaInfo javaInfo,
          ParameterDescription parameter,
          Expression argument,
          String[] source) throws Exception {
        if (ReflectionUtils.isSuccessorOf(
            parameter.getType(),
            "com.google.gwt.user.client.ui.ImageBundle")) {
          JavaInfo imageBundle = root.getChildRepresentedBy(argument);
          if (imageBundle instanceof ImageBundleInfo) {
            source[0] =
                IMAGE_BUNDLE
                    + imageBundle.getDescription().getComponentClass().getCanonicalName()
                    + "|";
          }
        }
      }

      @Override
      public void associationTemplate(JavaInfo component, String[] source_) throws Exception {
        String source = source_[0];
        while (true) {
          int index = source.indexOf(IMAGE_BUNDLE);
          if (index == -1) {
            break;
          }
          int indexEnd = source.indexOf('|', index);
          Assert.isTrue2(indexEnd != -1, "Can not find end of name in {0}", source);
          // prepare reference on ImageBundle
          String bundleSource;
          {
            String className = source.substring(index + IMAGE_BUNDLE.length(), indexEnd);
            ImageBundleInfo bundle = getBundle(className);
            TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
            NodeTarget target = new NodeTarget(new BodyDeclarationTarget(typeDeclaration, false));
            bundleSource = bundle.getVariableSupport().getReferenceExpression(target);
          }
          // replace
          source = source.substring(0, index) + bundleSource + source.substring(indexEnd + 1);
        }
        source_[0] = source;
      }

      /**
       * @return the {@link ImageBundleInfo} with given class name, existing or newly added. Throws
       *         {@link IllegalArgumentException} if no such <code>ImageBundle</code> in project.
       */
      private ImageBundleInfo getBundle(String className) throws Exception {
        // try to find existing instance
        for (ImageBundleInfo bundle : ImageBundleContainerInfo.getBundles(root)) {
          Class<?> bundleClass = bundle.getDescription().getComponentClass();
          if (ReflectionUtils.isSuccessorOf(bundleClass, className)) {
            return bundle;
          }
        }
        // try to add new instance
        IType type = root.getEditor().getJavaProject().findType(className);
        if (type != null) {
          return ImageBundleContainerInfo.add(root, className);
        }
        // no such ImageBundle
        throw new DesignerException(IExceptionConstants.NO_SUCH_IMAGE_BUNDLE, className);
      }
    });
  }

  private void processComponents(final JavaInfo root, final List<JavaInfo> components)
      throws Exception {
    // bind {@link ImageBundle_Info}'s into hierarchy.
    for (JavaInfo javaInfo : components) {
      if (javaInfo instanceof ImageBundleInfo) {
        ImageBundleInfo imageBundleInfo = (ImageBundleInfo) javaInfo;
        imageBundleInfo.setAssociation(new EmptyAssociation());
        ImageBundleContainerInfo.get(root).addChild(imageBundleInfo);
      }
    }
  }
}
