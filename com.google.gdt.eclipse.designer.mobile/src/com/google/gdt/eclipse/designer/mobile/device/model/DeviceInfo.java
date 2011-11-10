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
package com.google.gdt.eclipse.designer.mobile.device.model;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;

import org.osgi.framework.Bundle;

/**
 * Description for mobile device.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceInfo extends AbstractDeviceInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final boolean m_contributed;
  private final Bundle m_contributionBundle;
  private String m_imagePath;
  private Image m_image;
  private Rectangle m_displayBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceInfo(String id, String name, String imagePath, Image image, Rectangle displayBounds)
      throws Exception {
    m_contributed = false;
    m_contributionBundle = null;
    //
    m_id = id;
    m_name = name;
    m_imagePath = imagePath;
    m_image = image;
    m_displayBounds = displayBounds;
  }

  public DeviceInfo(IConfigurationElement element) throws Exception {
    m_contributed = true;
    m_contributionBundle = ExternalFactoriesHelper.getExtensionBundle(element);
    //
    m_id = ExternalFactoriesHelper.getRequiredAttribute(element, "id");
    m_name = ExternalFactoriesHelper.getRequiredAttribute(element, "name");
    m_imagePath = ExternalFactoriesHelper.getRequiredAttribute(element, "image");
    // display
    {
      IConfigurationElement[] displayElements = element.getChildren("display");
      Assert.equals(1, displayElements.length, "Exactly one \"display\" element expected, but "
          + displayElements.length
          + " found.");
      IConfigurationElement displayElement = displayElements[0];
      m_displayBounds =
          new Rectangle(ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "x"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "y"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "width"),
              ExternalFactoriesHelper.getRequiredAttributeInteger(displayElement, "height"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link DeviceInfo} can be modified by user.
   */
  public boolean isContributed() {
    return m_contributed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of display on image.
   */
  public Rectangle getDisplayBounds() {
    return m_displayBounds;
  }

  /**
   * @return the bounds of display on image.
   */
  public void setDisplayBounds(Rectangle displayBounds) {
    m_displayBounds = displayBounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the path to the image.
   */
  public String getImagePath() {
    return m_imagePath;
  }

  /**
   * @return the {@link Image} of this device.
   */
  public Image getImage() {
    if (m_image == null) {
      m_image = getImage(m_contributionBundle, m_imagePath);
    }
    return m_image;
  }

  /**
   * Sets the {@link Image} and path to this image.
   */
  public void setImage(String imagePath, Image image) {
    m_imagePath = imagePath;
    m_image = image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  private CategoryInfo m_category;

  /**
   * @return the parent {@link CategoryInfo}.
   */
  public CategoryInfo getCategory() {
    return m_category;
  }

  /**
   * Sets the new parent {@link CategoryInfo}.
   */
  public void setCategory(CategoryInfo category) {
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for {@link DeviceInfo} from {@link Bundle}.
   */
  private static Image getImage(Bundle bundle, String path) {
    return BundleResourceProvider.get(bundle).getImage(path);
  }
}
