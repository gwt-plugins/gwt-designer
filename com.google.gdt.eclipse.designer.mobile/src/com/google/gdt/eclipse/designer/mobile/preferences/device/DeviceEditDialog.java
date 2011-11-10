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
package com.google.gdt.eclipse.designer.mobile.preferences.device;

import com.google.gdt.eclipse.designer.mobile.device.command.Command;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceEditCommand;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.widgets.Composite;

/**
 * Dialog for modifying {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceEditDialog extends DeviceAbstractDialog {
  private final DeviceInfo m_device;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceEditDialog(DeviceInfo device) {
    super("Edit mobile device", "Edit a mobile device.");
    m_device = device;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ///////////////////////////////////////////////////////////////////////////
  /**
   * Creates controls on this dialog.
   */
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_nameField.setText(m_device.getName());
    m_imageField.setText(m_device.getImagePath());
    m_displayField_x.setText("" + m_device.getDisplayBounds().x);
    m_displayField_y.setText("" + m_device.getDisplayBounds().y);
    m_displayField_width.setText("" + m_device.getDisplayBounds().width);
    m_displayField_height.setText("" + m_device.getDisplayBounds().height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public Command getCommand() {
    Rectangle displayBounds =
        new Rectangle(Integer.parseInt(m_displayField_x.getText()),
            Integer.parseInt(m_displayField_y.getText()),
            Integer.parseInt(m_displayField_width.getText()),
            Integer.parseInt(m_displayField_height.getText()));
    return new DeviceEditCommand(m_device.getId(),
        m_nameField.getText(),
        m_imageField.getText(),
        displayBounds);
  }
}
