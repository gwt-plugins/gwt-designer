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

import com.google.gdt.eclipse.designer.Activator;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ImageBundleInfo}, direct child of root {@link JavaInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class ImageBundleContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{" + getClass().getName() + "}";
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
        return "(ImageBundle's)";
      }

      @Override
      public Image getIcon() throws Exception {
        return Activator.getImage("info/ImageBundle/container.gif");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing or new {@link ImageBundleContainerInfo} for given root.
   */
  public static ImageBundleContainerInfo get(JavaInfo root) throws Exception {
    ImageBundleContainerInfo container = findContainer(root);
    if (container == null) {
      container = new ImageBundleContainerInfo();
      root.addChild(container);
    }
    return container;
  }

  /**
   * @return all {@link ImageBundleInfo}'s for given root.
   */
  public static List<ImageBundleInfo> getBundles(JavaInfo root) {
    ImageBundleContainerInfo container = findContainer(root);
    if (container != null) {
      return container.getChildren(ImageBundleInfo.class);
    }
    return Collections.emptyList();
  }

  /**
   * @return find the existing {@link ImageBundleContainerInfo} for given root.
   */
  private static ImageBundleContainerInfo findContainer(JavaInfo root) {
    for (ObjectInfo child : root.getChildren()) {
      if (child instanceof ImageBundleContainerInfo) {
        return (ImageBundleContainerInfo) child;
      }
    }
    return null;
  }

  /**
   * Adds new {@link ImageBundleInfo} and to the {@link ImageBundleContainerInfo}.
   * 
   * @param bundleClassName
   *          the fully qualified name of <code>ImageBundle</code>.
   * @return the added {@link ImageBundleInfo}.
   */
  public static ImageBundleInfo add(JavaInfo root, String bundleClassName) throws Exception {
    ImageBundleInfo bundle;
    {
      OpaqueCreationSupport creationSupport =
          new OpaqueCreationSupport("com.google.gwt.core.client.GWT.create("
              + bundleClassName
              + ".class)");
      bundle =
          (ImageBundleInfo) JavaInfoUtils.createJavaInfo(
              root.getEditor(),
              bundleClassName,
              creationSupport);
    }
    // do add
    FieldInitializerVariableSupport variableSupport = new FieldInitializerVariableSupport(bundle);
    variableSupport.setForceStaticModifier(true);
    JavaInfoUtils.add(
        bundle,
        variableSupport,
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.empty(),
        root,
        null);
    root.removeChild(bundle);
    ImageBundleContainerInfo.get(root).addChild(bundle);
    // done
    return bundle;
  }
}