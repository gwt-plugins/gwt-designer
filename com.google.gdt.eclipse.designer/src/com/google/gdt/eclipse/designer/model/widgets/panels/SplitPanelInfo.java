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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for GWT <code>SplitPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class SplitPanelInfo extends PanelInfo implements ISplitPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SplitPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // decorate Widget's text with direction
    addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == SplitPanelInfo.this) {
          String region = getRegion((WidgetInfo) object);
          text[0] = region + " - " + text[0];
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
   * @return the {@link WidgetInfo} associated using {@link MethodInvocation} with given name.
   */
  protected final WidgetInfo getWidgetAssociatedByMethod(String... names) {
    List<WidgetInfo> widgets = getChildrenWidgets();
    for (WidgetInfo widget : widgets) {
      String methodName = getWidgetAssociationMethod(widget);
      if (ArrayUtils.contains(names, methodName)) {
        return widget;
      }
    }
    return null;
  }

  /**
   * @return the name of method used to associate given {@link WidgetInfo}, may be <code>null</code>
   *         .
   */
  private static String getWidgetAssociationMethod(WidgetInfo widget) {
    InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
    return association.getDescription().getName();
  }

  /**
   * @return the name of region used to associate given {@link WidgetInfo}.
   */
  private static String getRegion(WidgetInfo widget) {
    String region = getWidgetAssociationMethod(widget);
    region = StringUtils.substringBetween(region, "set", "Widget");
    region = StringUtils.uncapitalize(region);
    return region;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public abstract String getEmptyRegion();

  public void command_CREATE(WidgetInfo widget, String region) throws Exception {
    JavaInfoUtils.add(widget, getWidgetAssociation(region), this, null);
  }

  public void command_MOVE(WidgetInfo widget, WidgetInfo next) throws Exception {
    JavaInfoUtils.move(widget, null, this, next);
  }

  public void command_MOVE(WidgetInfo widget, String region) throws Exception {
    if (widget.getParent() != this) {
      JavaInfoUtils.move(widget, getWidgetAssociation(region), this, null);
    } else {
      command_REGION(widget, region);
    }
  }

  private void command_REGION(WidgetInfo widget, String region) throws Exception {
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      String methodName = getRegionMethod(region);
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      getEditor().replaceInvocationName(invocation, methodName);
    }
  }

  private static AssociationObject getWidgetAssociation(String region) {
    String methodName = getRegionMethod(region);
    String associationSource = "%parent%." + methodName + "(%child%)";
    return AssociationObjects.invocationChild(associationSource, false);
  }

  private static String getRegionMethod(String region) {
    return "set" + StringUtils.capitalize(region) + "Widget";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final String region = getRegion(widget);
    commands.add(new PanelClipboardCommand<SplitPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(SplitPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE(widget, region);
      }
    });
  }
}
