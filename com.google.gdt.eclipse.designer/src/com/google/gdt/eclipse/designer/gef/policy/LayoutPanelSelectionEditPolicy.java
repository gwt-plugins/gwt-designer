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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.Anchor;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelAlignmentSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelInfo;

import org.eclipse.wb.core.gef.policy.helpers.SelectionEditPolicyRefreshHelper;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteLayoutSelectionEditPolicy;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link LayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public class LayoutPanelSelectionEditPolicy<W extends IWidgetInfo>
    extends
      AbsoluteLayoutSelectionEditPolicy<W> {
  private final ILayoutPanelInfo<W> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutPanelSelectionEditPolicy(ILayoutPanelInfo<W> panel) {
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setHost(EditPart host) {
    super.setHost(host);
    new SelectionEditPolicyRefreshHelper(this);
    installSupport_AlignmentFigures();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment figures
  //
  ////////////////////////////////////////////////////////////////////////////
  private void installSupport_AlignmentFigures() {
    new AlignmentActionsHelper<W>(this) {
      @Override
      protected Figure createAlignmentFigure(final W widget, final boolean horizontal) {
        IEditPartViewer viewer = getHost().getViewer();
        final Anchor anchor = m_panel.getAnchor(widget, horizontal);
        if (horizontal) {
          return new AbstractPopupFigure(viewer, 16, 8) {
            @Override
            protected Image getImage() {
              return anchor != null ? anchor.getSmallImage(horizontal) : null;
            }

            @Override
            protected void fillMenu(IMenuManager manager) {
              addAlignmentActions(manager, widget, horizontal);
            }
          };
        } else {
          return new AbstractPopupFigure(viewer, 8, 16) {
            @Override
            protected Image getImage() {
              return anchor != null ? anchor.getSmallImage(horizontal) : null;
            }

            @Override
            protected void fillMenu(IMenuManager manager) {
              addAlignmentActions(manager, widget, horizontal);
            }
          };
        }
      }

      private void addAlignmentActions(IMenuManager manager, W widget, boolean horizontal) {
        List<Object> actionObjects = Lists.newArrayList();
        {
          LayoutPanelAlignmentSupport<W> alignmentSupport = m_panel.getAlignmentSupport();
          List<W> widgets = ImmutableList.of(widget);
          if (horizontal) {
            alignmentSupport.addAnchorActions_horizontal(actionObjects, widgets);
          } else {
            alignmentSupport.addAnchorActions_vertical(actionObjects, widgets);
          }
        }
        for (Object actionObject : actionObjects) {
          if (actionObject instanceof IAction) {
            IAction action = (IAction) actionObject;
            action.setText(action.getToolTipText());
            manager.add(action);
          }
        }
      }
    };
  }
}
