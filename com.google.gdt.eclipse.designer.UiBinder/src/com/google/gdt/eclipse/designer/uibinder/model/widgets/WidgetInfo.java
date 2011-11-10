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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.uibinder.refactoring.MorphingSupport;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.Widget</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class WidgetInfo extends UIObjectInfo implements IWidgetInfo {
  private final WidgetInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    // contribute context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          MorphingSupport.contribute(m_this, manager);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new WidgetTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_create() throws Exception {
    super.refresh_create();
    attachAfterConstructor();
  }

  /**
   * This method is invoked directly after creation of "this" object.
   */
  protected void attachAfterConstructor() throws Exception {
    {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("model", this);
      variables.put("widget", getObject());
      String name = "attachAfterConstructorScript";
      String script = XmlObjectUtils.getParameter(this, name);
      Assert.isNotNull2(name, "No {0}", name);
      getUIObjectUtils().executeScript(script, variables);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image m_liveDefaultImage = new Image(null, 1, 1);
  /**
   * We set this flag during requesting live image because:
   * <ul>
   * <li>Getting live image performs refresh();</li>
   * <li>refresh() may run messages loop;</li>
   * <li>during messages loop user may move mouse again and request live image again;</li>
   * <li>we don't support recursive live image requests.</li>
   * </ul>
   */
  private boolean m_liveInProgress;

  @Override
  protected Image getLiveImage() {
    // live image is supported only for component from palette
    if (getArbitraryValue(FLAG_MANUAL_COMPONENT) != Boolean.TRUE) {
      return null;
    }
    // prevent recursive live image requests
    if (m_liveInProgress) {
      return m_liveDefaultImage;
    }
    // OK, get live image
    m_liveInProgress = true;
    try {
      return getLiveComponentsManager().getImage();
    } finally {
      m_liveInProgress = false;
    }
  }

  /**
   * @return the {@link GwtLiveManager} instance.
   */
  protected GwtLiveManager getLiveComponentsManager() {
    return new GwtLiveManager(this);
  }

  public boolean shouldSetReasonableSize() {
    return getLiveComponentsManager().shouldSetSize();
  }
}
