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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;

import org.eclipse.swt.graphics.Image;

/**
 * Interface model for <code>com.google.gwt.user.client.ui.LayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public interface ILayoutPanelInfo<W extends IWidgetInfo> extends IComplexPanelInfo<W> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the provider for managing "anchor".
   */
  public LayoutPanelAlignmentSupport<W> getAlignmentSupport();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location hint in units for given location in pixels.
   */
  public String getLocationHint(W widget, int x, int y);

  /**
   * @return <code>true</code> if {@link W} is attached to trailing size of panel.
   */
  public boolean getLocationHint_isTrailing(W widget, boolean horizontal);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds: location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_LOCATION(W widget, Point location) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds: size
  //
  ////////////////////////////////////////////////////////////////////////////
  public enum ResizeDirection {
    NONE, LEADING, TRAILING
  }

  public void command_SIZE(W widget,
      Dimension size,
      ResizeDirection hDirection,
      ResizeDirection vDirection) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  public enum Anchor {
    NONE {
      @Override
      public String getTitle(boolean horizontal) {
        return "none";
      }
    },
    LEADING {
      @Override
      public String getTitle(boolean horizontal) {
        return horizontal ? "left + width" : "top + height";
      }
    },
    TRAILING {
      @Override
      public String getTitle(boolean horizontal) {
        return horizontal ? "right + width" : "bottom + height";
      }
    },
    BOTH {
      @Override
      public String getTitle(boolean horizontal) {
        return horizontal ? "left + right" : "top + bottom";
      }
    };
    public abstract String getTitle(boolean horizontal);

    public Image getImage(boolean horizontal) {
      String name = name() + (horizontal ? "_h" : "_v") + ".png";
      return getImage(name);
    }

    public Image getSmallImage(boolean horizontal) {
      String name = name() + (horizontal ? "_h" : "_v") + "_small.png";
      return getImage(name);
    }

    private Image getImage(String name) {
      return Activator.getImage("info/LayoutPanel/" + name);
    }
  };

  /**
   * @return the {@link Anchor} type for given {@link W}.
   */
  public Anchor getAnchor(W widget, boolean horizontal);

  /**
   * Sets anchor for given {@link W}.
   */
  public void command_ANCHOR(W widget, boolean horizontal, Anchor anchor) throws Exception;
}
