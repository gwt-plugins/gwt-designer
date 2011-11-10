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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveCacheEntry;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.AbstractLiveManager;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

/**
 * Default live components manager for GWT.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class GwtLiveManager extends AbstractLiveManager {
  protected boolean m_shouldSetSize = false;
  private final UIObjectUtils m_utils;
  private String m_formSourceOriginal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtLiveManager(WidgetInfo widget) {
    super(widget);
    m_utils = widget.getState().getUIObjectUtils();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractLiveComponentsManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected WidgetInfo createLiveComponent() throws Exception {
    m_utils.setLiveManager(true);
    // remember form source
    {
      UiBinderContext context = (UiBinderContext) m_component.getContext();
      m_formSourceOriginal = context.getFormEditor().getSource();
    }
    // prepare empty AbsolutePanel
    AbsolutePanelInfo panel =
        (AbsolutePanelInfo) parse(new String[]{
            "<ui:UiBinder xmlns:ui='urn:ui:com.google.gwt.uibinder'"
                + " xmlns:g='urn:import:com.google.gwt.user.client.ui'>",
            "  <g:AbsolutePanel title='__wbp_liveWidget'/>",
            "</ui:UiBinder>"});
    // prepare component
    CreationSupport creationSupport =
        ((ILiveCreationSupport) m_component.getCreationSupport()).getLiveComponentCreation();
    WidgetInfo widget =
        (WidgetInfo) XmlObjectUtils.createObject(
            panel.getContext(),
            m_component.getDescription(),
            creationSupport);
    // add component to AbsolutePanel
    panel.command_CREATE2(widget, null);
    panel.command_BOUNDS(widget, new Point(100, 100), null);
    // check for forced size
    {
      String width = getForcedWidth();
      String height = getForcedHeight();
      if (hasForcedSizePart(width) && hasForcedSizePart(height)) {
        m_shouldSetSize = true;
        widget.getSizeSupport().setSize(width, height);
      }
    }
    // ready to get live values
    return widget;
  }

  @Override
  protected void cleanupLiveComponent(AbstractComponentInfo liveComponentInfo) throws Exception {
    super.cleanupLiveComponent(liveComponentInfo);
    // restore form source
    if (liveComponentInfo != null) {
      UiBinderContext context = (UiBinderContext) liveComponentInfo.getContext();
      context.getFormEditor().setSource(m_formSourceOriginal);
      context.saveFormEditor();
    }
    // reset "live" state of UIObjectManipulations
    m_utils.setLiveWidget(null);
    m_utils.setLiveManager(false);
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo component) {
    WidgetInfo widget = (WidgetInfo) component;
    // check for size
    setDefaultSizeIfEmpty(widget);
    // do create cache entry
    GwtLiveCacheEntry entry = new GwtLiveCacheEntry();
    entry.setImage(getWidgetImage(widget));
    entry.shouldSetSize(m_shouldSetSize);
    return entry;
  }

  /**
   * If widget has no size and no forced size (i.e. most probably custom component), then set some
   * reasonable default size.
   */
  private void setDefaultSizeIfEmpty(final WidgetInfo widget) {
    Rectangle bounds = widget.getBounds();
    if (bounds.width == 0 || bounds.height == 0) {
      m_shouldSetSize = true;
      ExecutionUtils.run(widget, new RunnableEx() {
        public void run() throws Exception {
          widget.getSizeSupport().setSize(100, 100);
        }
      });
    }
  }

  /**
   * @return the {@link Image} of {@link WidgetInfo} that is direct child of
   *         <code>AbsolutePanel</code>.
   */
  private Image getWidgetImage(WidgetInfo widget) {
    Rectangle bounds = widget.getAbsoluteBounds();
    if (bounds.width == 0 || bounds.height == 0) {
      Dimension forcedSize = getForcedSize();
      return new Image(null, forcedSize.width, forcedSize.height);
    } else {
      AbsolutePanelInfo absolutePanel = (AbsolutePanelInfo) widget.getRoot();
      return UiUtils.getCroppedImage(absolutePanel.getImage(), bounds.getSwtRectangle());
    }
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntryEx(Throwable e) {
    GwtLiveCacheEntry cacheEntry = new GwtLiveCacheEntry();
    // set image
    {
      Image image = createImageForException(e);
      cacheEntry.setImage(image);
    }
    // done
    return cacheEntry;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Forced size
  //
  ////////////////////////////////////////////////////////////////////////////
  private String getForcedWidth() {
    return getForcedSizePart("width");
  }

  private String getForcedHeight() {
    return getForcedSizePart("height");
  }

  private String getForcedSizePart(String name) {
    if (!useForcedSize()) {
      return null;
    }
    return m_component.getDescription().getParameter("liveComponent.forcedSize." + name);
  }

  /**
   * @return <code>true</code> if specified forced size should be used.
   */
  private boolean useForcedSize() {
    String script = m_component.getDescription().getParameter("liveComponent.forcedSize.use");
    if (StringUtils.isEmpty(script)) {
      return true;
    }
    return m_utils.evaluateScriptBoolean(script);
  }

  private static boolean hasForcedSizePart(String size) {
    return !StringUtils.isEmpty(size);
  }

  /**
   * @return the forced size from description.
   */
  private Dimension getForcedSize() {
    String width = getForcedWidth();
    String height = getForcedHeight();
    Assert.isTrue(
        hasForcedSizePart(width) && hasForcedSizePart(height),
        "Forced size was requested, but no liveComponent.forcedSize.width/height specified.");
    int w = parseSize(width);
    int h = parseSize(height);
    return new Dimension(w, h);
  }

  /**
   * @return the integer value of size {@link String}, that should be some integer like
   *         <code>100</code> or pixel value <code>100px</code> (preferable).
   */
  private static int parseSize(String size) {
    if (size.endsWith("px")) {
      size = size.replaceAll("px", "").trim();
    }
    return Integer.parseInt(size);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of "live" {@link Image} of component, must be disposed by caller.
   */
  public Image getImage() {
    return getEntry().getImage();
  }

  /**
   * See {@link WidgetInfo#shouldSetReasonableSize()}.
   */
  public boolean shouldSetSize() {
    return getEntry().shouldSetSize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LiveComponentsCacheEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies "live" {@link WidgetInfo} properties externally, to avoid parsing (when
   * {@link CreationSupport} is not {@link ILiveCreationSupport}).
   */
  public static void setEntry(WidgetInfo widget, Image image, boolean shouldSetSize) {
    GwtLiveCacheEntry entry = new GwtLiveCacheEntry();
    entry.setImage(image);
    entry.shouldSetSize(shouldSetSize);
    widget.putArbitraryValue(GwtLiveCacheEntry.class, entry);
  }

  private GwtLiveCacheEntry getEntry() {
    // check if we have LiveComponentsCacheEntry set externally
    {
      GwtLiveCacheEntry entry =
          (GwtLiveCacheEntry) m_component.getArbitraryValue(GwtLiveCacheEntry.class);
      if (entry != null) {
        return entry;
      }
    }
    // check if no entry
    if (XmlObjectUtils.hasTrueParameter(m_component, "liveComponent.no")) {
      Dimension forcedSize = getForcedSize();
      setEntry(
          (WidgetInfo) m_component,
          new Image(null, forcedSize.width, forcedSize.height),
          false);
      return getEntry();
    }
    // use code parsing
    return (GwtLiveCacheEntry) getCachedEntry();
  }
}
