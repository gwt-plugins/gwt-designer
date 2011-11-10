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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveManager;
import com.google.gdt.eclipse.designer.palette.ImageBundleUseEntryInfo;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ICreationSupportPermissions;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Description for GWT <code>AbstractImagePrototype</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class ImageBundlePrototypeDescription {
  /**
   * Implementation of {@link ICreationSupportPermissions} for <code>Image</code> created using
   * <code>AbstractImagePrototype.createImage()</code>.
   */
  public static final ICreationSupportPermissions PERMISSIONS = new ICreationSupportPermissions() {
    public boolean canDelete(JavaInfo javaInfo) {
      return true;
    }

    public void delete(JavaInfo javaInfo) throws Exception {
      JavaInfoUtils.deleteJavaInfo(javaInfo, true);
    }

    public boolean canReorder(JavaInfo javaInfo) {
      return true;
    }

    public boolean canReparent(JavaInfo javaInfo) {
      return true;
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ImageBundleInfo m_bundle;
  private final Method m_method;
  private final String m_methodSignature;
  private final EditorState m_editorState;
  private final ClassLoader m_loader;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageBundlePrototypeDescription(ImageBundleInfo bundle, Method method) throws Exception {
    m_bundle = bundle;
    m_method = method;
    m_methodSignature = ReflectionUtils.getMethodSignature(method);
    m_editorState = EditorState.get(m_bundle.getEditor());
    m_loader = m_editorState.getEditorLoader();
    prepareImages();
    // contribute to palette
    m_bundle.addBroadcastListener(new PaletteEventListener() {
      @Override
      public void entries(CategoryInfo category, List<EntryInfo> entries) throws Exception {
        if (category.getId().equals("com.google.gdt.eclipse.designer.ImageBundle")) {
          entries.add(new ImageBundleUseEntryInfo(ImageBundlePrototypeDescription.this));
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source {@link ImageBundleInfo}.
   */
  public ImageBundleInfo getBundle() {
    return m_bundle;
  }

  /**
   * @return the {@link Method} from <code>ImageBundle</code>.
   */
  public Method getMethod() {
    return m_method;
  }

  /**
   * @return <code>true</code> if given {@link Expression} represents this
   *         {@link ImageBundlePrototypeDescription}.
   */
  public boolean isRepresentedBy(Expression expression) {
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      return AstNodeUtils.isMethodInvocation(
          invocation,
          m_bundle.getDescription().getComponentClass().getName(),
          m_methodSignature);
    }
    return false;
  }

  /**
   * @return the new <code>Image</code> widget from this <code>AbstractImagePrototype</code>.
   */
  public ImageInfo createImageWidget() throws Exception {
    AstEditor editor = m_bundle.getEditor();
    // prepare CreationSupport
    OpaqueCreationSupport creationSupport;
    {
      String source = getImageSource();
      creationSupport = new OpaqueCreationSupport(source);
      creationSupport.setPermissions(PERMISSIONS);
    }
    // create Image model
    ImageInfo newImage =
        (ImageInfo) JavaInfoUtils.createJavaInfo(
            editor,
            "com.google.gwt.user.client.ui.Image",
            creationSupport);
    // set "live" properties
    GwtLiveManager.setEntry(newImage, m_image, false);
    // final Image model
    return newImage;
  }

  /**
   * @return the source for creating new image using <code>createImage()</code>.
   */
  public String getImageSource() throws Exception {
    return TemplateUtils.format("{0}.{1}().createImage()", m_bundle, m_method.getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image/icon
  //
  ////////////////////////////////////////////////////////////////////////////
  private Image m_image;
  private Image m_icon;

  /**
   * @return the {@link Image} of this {@link ImageBundlePrototypeDescription}.
   */
  public Image getImage() {
    return m_image;
  }

  /**
   * @return the icon (16x16) to show for this {@link ImageBundlePrototypeDescription} to user.
   */
  public Image getIcon() {
    return m_icon;
  }

  /**
   * Loads {@link #m_image}, scales it down to produce {@link #m_icon}.
   */
  private void prepareImages() throws Exception {
    if (m_image == null) {
      String location = getImageClasspathLocation();
      if (location != null) {
        InputStream inputStream = m_loader.getResourceAsStream(location);
        try {
          if (inputStream != null) {
            // load image
            {
              m_image = new Image(null, inputStream);
              m_editorState.addDisposableImage(m_image);
            }
            // prepare icon
            {
              m_icon = DrawUtils.getThubmnail(m_image, 16, 16, 32, 32);
              m_editorState.addDisposableImage(m_icon);
            }
          }
        } finally {
          IOUtils.closeQuietly(inputStream);
        }
      }
    }
  }

  private String getImageClasspathLocation() throws Exception {
    String packagePath =
        CodeUtils.getPackage(m_method.getDeclaringClass().getName()).replace('.', '/');
    // check for @Resource annotation
    for (Annotation annotation : m_method.getAnnotations()) {
      if (annotation.annotationType().getName().equals(
          "com.google.gwt.user.client.ui.ImageBundle$Resource")) {
        String location = (String) ReflectionUtils.invokeMethod(annotation, "value()");
        if (location.contains("/")) {
          return location;
        } else {
          return packagePath + "/" + location;
        }
      }
    }
    // no annotation, check files in this package
    for (String extension : ImmutableList.of(".png", ".gif", ".jpg")) {
      String location = packagePath + "/" + m_method.getName() + extension;
      InputStream stream = m_loader.getResourceAsStream(location);
      try {
        if (stream != null) {
          return location;
        }
      } finally {
        IOUtils.closeQuietly(stream);
      }
    }
    // not found
    return null;
  }
}
