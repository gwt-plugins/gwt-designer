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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Utils for GXT objects.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class GxtUtils {
  public static Rectangle getAbsoluteBounds(Object el) throws Exception {
    Object box = ReflectionUtils.invokeMethod(el, "getBounds()");
    int x = ReflectionUtils.getFieldInt(box, "x");
    int y = ReflectionUtils.getFieldInt(box, "y");
    int width = ReflectionUtils.getFieldInt(box, "width");
    int height = ReflectionUtils.getFieldInt(box, "height");
    return new Rectangle(x, y, width, height);
  }

  /**
   * @return the {@link Insets} of given <code>El</code> element.
   */
  public static Insets getBorders(Object el) throws Exception {
    int top = getBorderWidth(el, "top");
    int left = getBorderWidth(el, "left");
    int bottom = getBorderWidth(el, "bottom");
    int right = getBorderWidth(el, "right");
    return new Insets(top, left, bottom, right);
  }

  /**
   * @return the value of "border-x-width" attribute in pixels.
   */
  private static int getBorderWidth(Object el, String side) throws Exception {
    String widthString;
    {
      String attributeName = "border-" + side + "-width";
      widthString =
          (String) ReflectionUtils.invokeMethod(
              el,
              "getStyleAttribute(java.lang.String)",
              attributeName);
    }
    return GwtState.getValuePx(widthString);
  }
}
