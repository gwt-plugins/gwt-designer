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
package com.google.gdt.eclipse.designer.model.property;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * {@link PropertyEditor} for editing alignment string.
 * 
 * @author sablin_aa
 * @coverage gwt.model.property
 */
public final class AlignStringPropertyEditor extends AbstractTextPropertyEditor
    implements
      IConfigurablePropertyObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new AlignStringPropertyEditor();

  private AlignStringPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditorPresentation getPresentation() {
    return PRESENTATION;
  }

  /**
   * Presentation with button
   */
  private static ButtonPropertyEditorPresentation PRESENTATION =
      new ButtonPropertyEditorPresentation() {
        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
          // create dialog
          AlignDialog dialog = new AlignDialog(DesignerPlugin.getShell());
          dialog.setValue((String) property.getValue());
          // open dialog
          if (dialog.open() == Window.OK) {
            property.setValue(dialog.getValue());
          }
        }
      };

  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  @Override
  protected String getEditorText(Property property) throws Exception {
    return getText(property);
  }

  @Override
  protected boolean setEditorText(Property property, String text) throws Exception {
    // check for delete
    if (text.length() == 0) {
      property.setValue(Property.UNKNOWN_VALUE);
    }
    // modify property
    property.setValue(text);
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // NONE
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class AlignDialog extends ReusableDialog {
    private String m_value;
    private boolean m_changed;
    private Group m_elementAnchorGroup;
    private Group m_targetAnchorGroup;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AlignDialog(Shell parentShell) {
      super(parentShell);
    }

    @Override
    protected void onBeforeOpen() {
      super.onBeforeOpen();
      // parse current value
      {
        String[] splitValue = StringUtils.split(m_value, '-');
        if (splitValue != null) {
          if (splitValue.length > 0) {
            setSelectionState(m_elementAnchorGroup, splitValue[0].trim());
          }
          if (splitValue.length > 1) {
            setSelectionState(m_targetAnchorGroup, splitValue[1].trim());
          }
        }
      }
      m_changed = false;
    }

    public void setValue(String value) {
      m_value = value;
    }

    public String getValue() {
      if (isChanged()) {
        m_value =
            getSelectionState(m_elementAnchorGroup) + "-" + getSelectionState(m_targetAnchorGroup);
      }
      return m_value;
    }

    public boolean isChanged() {
      return m_changed;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Contents
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Align chooser");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite area = (Composite) super.createDialogArea(parent);
      GridLayoutFactory.create(area).columns(2);
      SelectionListener listener = new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
          m_changed = true;
        }

        public void widgetSelected(SelectionEvent e) {
          m_changed = true;
        }
      };
      {
        m_targetAnchorGroup = createAlignGroup(area, listener);
        m_targetAnchorGroup.setText("Target anchor");
      }
      {
        m_elementAnchorGroup = createAlignGroup(area, listener);
        m_elementAnchorGroup.setText("Element anchor");
      }
      return area;
    }

    /**
     * @return {@link Group} with radio-grid 9x9 + ?.
     */
    private static Group createAlignGroup(Composite parent, SelectionListener listener) {
      Group group = new Group(parent, SWT.NONE);
      GridLayoutFactory.create(group).columns(3);
      createButton(group, "top left", "tl", listener);
      createButton(group, "top", "t", listener);
      createButton(group, "top right", "tr", listener);
      createButton(group, "left", "l", listener);
      createButton(group, "center", "c", listener);
      createButton(group, "right", "r", listener);
      createButton(group, "bottom left", "bl", listener);
      createButton(group, "bottom", "b", listener);
      createButton(group, "bottom right", "br", listener);
      {
        Button check = new Button(group, SWT.CHECK);
        check.setToolTipText("adjust to viewport");
        check.setText("?");
        check.addSelectionListener(listener);
        GridDataFactory.create(check).spanH(3);
      }
      return group;
    }

    private static Button createButton(Group parent,
        String toolTip,
        String text,
        SelectionListener listener) {
      Button button = new Button(parent, SWT.RADIO);
      button.setToolTipText(toolTip);
      button.setText(text);
      button.addSelectionListener(listener);
      return button;
    }

    /**
     * @return {@link String} according for current selected anchor
     */
    private static String getSelectionState(Group stateGroup) {
      String align = StringUtils.EMPTY;
      String adjust = StringUtils.EMPTY;
      Control[] children = stateGroup.getChildren();
      for (Control control : children) {
        if (control instanceof Button) {
          Button button = (Button) control;
          if ((button.getStyle() & SWT.RADIO) != 0) {
            align = button.getSelection() ? button.getText() : align;
          }
          if ((button.getStyle() & SWT.CHECK) != 0) {
            adjust = button.getSelection() ? "?" : adjust;
          }
        }
      }
      return align + adjust;
    }

    /**
     * Set selected anchor according specified {@link String} state.
     */
    private static void setSelectionState(Group stateGroup, String state) {
      String align = StringUtils.replace(state, "?", StringUtils.EMPTY);
      boolean adjust = state.endsWith("?");
      Control[] children = stateGroup.getChildren();
      for (Control control : children) {
        if (control instanceof Button) {
          Button button = (Button) control;
          if ((button.getStyle() & SWT.RADIO) != 0) {
            if (align.equals(button.getText())) {
              button.setSelection(true);
            }
          }
          if ((button.getStyle() & SWT.CHECK) != 0) {
            button.setSelection(adjust);
          }
        }
      }
    }
  }
}
