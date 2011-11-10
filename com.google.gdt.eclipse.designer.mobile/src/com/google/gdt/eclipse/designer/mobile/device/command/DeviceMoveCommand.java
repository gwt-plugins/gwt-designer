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
package com.google.gdt.eclipse.designer.mobile.device.command;

import com.google.gdt.eclipse.designer.mobile.device.DeviceManager;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * Implementation of {@link Command} that moves {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceMoveCommand extends Command {
  public static final String ID = "deviceMove";
  private final String m_id;
  private final String m_categoryId;
  private final String m_nextDeviceId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceMoveCommand(DeviceInfo device, CategoryInfo category, DeviceInfo nextDevice) {
    m_id = device.getId();
    m_categoryId = category.getId();
    m_nextDeviceId = nextDevice != null ? nextDevice.getId() : null;
  }

  public DeviceMoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_categoryId = attributes.getValue("category");
    m_nextDeviceId = attributes.getValue("nextDevice");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    DeviceInfo device = DeviceManager.getDevice(m_id);
    CategoryInfo category = DeviceManager.getCategory(m_categoryId);
    if (device == null || category == null) {
      return;
    }
    // don't move before itself, this is no-op
    if (m_id.equals(m_nextDeviceId)) {
      return;
    }
    // remove source entry
    device.getCategory().removeDevice(device);
    // add to new location
    DeviceInfo nextDevice = DeviceManager.getDevice(m_nextDeviceId);
    if (nextDevice != null) {
      int index = category.getDevices().indexOf(nextDevice);
      category.addDevice(index, device);
    } else {
      category.addDevice(device);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
    addAttribute("category", m_categoryId);
    addAttribute("nextDevice", m_nextDeviceId);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    commands.add(this);
  }
}
