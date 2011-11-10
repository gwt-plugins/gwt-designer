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
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.swt.graphics.Image;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Command} that edits {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceEditCommand extends DeviceAbstractCommand {
  public static final String ID = "deviceEdit";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceEditCommand(String id, String name, String imagePath, Rectangle displayBounds) {
    super(id, name, imagePath, displayBounds);
  }

  public DeviceEditCommand(Attributes attributes) {
    super(attributes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        DeviceInfo device = DeviceManager.getDevice(m_id);
        device.setName(m_name);
        device.setDisplayBounds(m_displayBounds);
        // update image
        Image image = SwtResourceManager.getImage(m_imagePath);
        device.setImage(m_imagePath, image);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addToCommandList(List<Command> commands) {
    // remove other DeviceEditCommand's for this device
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof DeviceEditCommand) {
        DeviceEditCommand editCommand = (DeviceEditCommand) command;
        if (editCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    // do add
    commands.add(this);
  }
}
