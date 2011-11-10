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
package com.google.gdt.eclipse.designer.util.ui;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * Dialog for selecting image from public folders of modules.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.ui
 */
public final class ImageSelectionDialog extends ResourceSelectionDialog {
  private static final int ICON_SIZE = 16;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageSelectionDialog(Shell parentShell,
      IResourcesProvider provider,
      ModuleDescription moduleDescription,
      String title) throws Exception {
    super(parentShell, provider, moduleDescription, title);
    addFilter("GIF Files", "*.gif");
    addFilter("JPG Files", "*.jpg");
    addFilter("PNG Files", "*.png");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icons support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getIcon(ResourceFile file) {
    return getFileIcon(file);
  }

  @Override
  public boolean close() {
    disposeImages();
    return super.close();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void previewFile(Composite parent, ResourceFile file) {
    Image image = getFileImage(file);
    if (image != null) {
      GridLayoutFactory.create(parent);
      Canvas canvas = new ImageCanvas(parent, SWT.NONE, image);
      GridDataFactory.create(canvas).grab().fill();
      parent.layout();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images access
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<ResourceFile, Image> m_fileToImage = Maps.newHashMap();
  private final Map<ResourceFile, Image> m_fileToIcon = Maps.newHashMap();

  /**
   * @return the icon size {@link Image} (16x16) corresponding to the given {@link ResourceFile}.
   */
  private Image getFileIcon(ResourceFile file) {
    Image icon = m_fileToIcon.get(file);
    if (icon == null) {
      // prepare image
      Image image = getFileImage(file);
      if (image == null) {
        return super.getIcon(file);
      }
      // prepare icon
      icon = new Image(Display.getCurrent(), ICON_SIZE, ICON_SIZE);
      m_fileToIcon.put(file, icon);
      // draw image on icon
      GC gc = new GC(icon);
      DrawUtils.drawScaledImage(gc, image, new Rectangle(0, 0, ICON_SIZE, ICON_SIZE));
      gc.dispose();
    }
    return icon;
  }

  /**
   * @return the full size {@link Image} loaded from given {@link ResourceFile}.
   */
  private Image getFileImage(ResourceFile file) {
    Image image = m_fileToImage.get(file);
    if (image == null) {
      InputStream is = null;
      try {
        is = getResourceAsStream(file);
        image = new Image(Display.getCurrent(), is);
        m_fileToImage.put(file, image);
      } catch (Throwable e) {
        return null;
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return image;
  }

  /**
   * Disposes creates {@link Image}'s.
   */
  private void disposeImages() {
    disposeImagesMap(m_fileToImage);
    disposeImagesMap(m_fileToIcon);
  }

  /**
   * Disposes given {@link Image}'s map.
   */
  private static void disposeImagesMap(Map<?, Image> fileToImage) {
    for (Image image : fileToImage.values()) {
      image.dispose();
    }
  }
}