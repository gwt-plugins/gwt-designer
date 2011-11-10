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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.Anchor;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;

import java.util.List;
import java.util.Set;

/**
 * Helper for adding alignment actions for {@link LayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class LayoutPanelAlignmentSupport<W extends IWidgetInfo> extends ObjectEventListener {
  private final ILayoutPanelInfo<W> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutPanelAlignmentSupport(ILayoutPanelInfo<W> panel) {
    m_panel = panel;
    m_panel.addBroadcastListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions) throws Exception {
    if (objects.isEmpty()) {
      return;
    }
    // prepare widgets
    List<W> widgets = Lists.newArrayList();
    for (ObjectInfo object : objects) {
      if (!isWidget(object)) {
        return;
      }
      widgets.add((W) object);
    }
    //
    actions.add(new Separator());
    addAnchorActions_horizontal(actions, widgets);
    actions.add(new Separator());
    addAnchorActions_vertical(actions, widgets);
  }

  private boolean isWidget(ObjectInfo object) {
    return object instanceof IWidgetInfo && object.getParent() == m_panel.getUnderlyingModel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AnchorAction
  //
  ////////////////////////////////////////////////////////////////////////////
  public Anchor getAnchor(List<W> widgets, boolean horizontal) {
    Set<Anchor> anchors = Sets.newHashSet();
    for (W widget : widgets) {
      Anchor anchor = m_panel.getAnchor(widget, horizontal);
      anchors.add(anchor);
    }
    return anchors.size() == 1 ? anchors.iterator().next() : null;
  }

  public void addAnchorActions_horizontal(List<Object> actions, List<W> widgets) {
    Anchor anchor = getAnchor(widgets, true);
    actions.add(new AnchorAction(widgets, Anchor.NONE, true, "none", anchor));
    actions.add(new AnchorAction(widgets, Anchor.LEADING, true, "left + width", anchor));
    actions.add(new AnchorAction(widgets, Anchor.TRAILING, true, "right + width", anchor));
    actions.add(new AnchorAction(widgets, Anchor.BOTH, true, "left + right", anchor));
  }

  public void addAnchorActions_vertical(List<Object> actions, List<W> widgets) {
    Anchor anchor = getAnchor(widgets, false);
    actions.add(new AnchorAction(widgets, Anchor.NONE, false, "none", anchor));
    actions.add(new AnchorAction(widgets, Anchor.LEADING, false, "top + height", anchor));
    actions.add(new AnchorAction(widgets, Anchor.TRAILING, false, "bottom + height", anchor));
    actions.add(new AnchorAction(widgets, Anchor.BOTH, false, "top + bottom", anchor));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractAction
  //
  ////////////////////////////////////////////////////////////////////////////
  private abstract class AbstractAction extends ObjectInfoAction {
    private final List<W> m_widgets;
    protected final boolean m_horizontal;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractAction(int style,
        List<W> widgets,
        boolean horizontal,
        String tooltip,
        boolean checked) {
      super(m_panel.getUnderlyingModel(), "", style);
      m_widgets = widgets;
      m_horizontal = horizontal;
      setToolTipText(tooltip);
      setChecked(checked);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      for (W widget : m_widgets) {
        handle(widget);
      }
    }

    protected abstract void handle(W widget) throws Exception;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // AnchorAction
  //
  ////////////////////////////////////////////////////////////////////////////
  private class AnchorAction extends AbstractAction {
    private final Anchor m_anchor;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AnchorAction(List<W> widgets,
        Anchor anchor,
        boolean horizontal,
        String tooltip,
        Anchor currentAnchor) {
      super(IAction.AS_RADIO_BUTTON,
          widgets,
          horizontal,
          anchor.getTitle(horizontal),
          currentAnchor == anchor);
      setIcon(anchor.getImage(horizontal));
      m_anchor = anchor;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void handle(W widget) throws Exception {
      m_panel.command_ANCHOR(widget, m_horizontal, m_anchor);
    }
  }
}