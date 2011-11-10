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
package com.google.gdt.eclipse.designer.mobile.device;

import com.google.gdt.eclipse.designer.mobile.device.command.CategoryAddCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryMoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryNameCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryRemoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceAddCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceEditCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceMoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceRemoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.ElementVisibilityCommand;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.core.resources.IFile;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link DeviceManager}.
 * 
 * @author scheglov_ke
 */
public class DeviceManagerTest extends AbstractJavaProjectTest {
  protected static final String DEVICES_ID = "com.google.gdt.eclipse.designer.mobile.devices";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    do_projectCreate();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    do_projectDispose();
    DeviceManager.resetToDefaults();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DeviceManager#getCategories()}.
   */
  public void test_DeviceManager_categoriesAndDevices() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile("icons/devA.png", TestUtils.createImagePNG(1, 1));
      testBundle.setFile("icons/devB.png", TestUtils.createImagePNG(2, 2));
      testBundle.addExtension(DEVICES_ID, new String[]{
          "<category id='catA' name='Category A'>",
          "  <device id='devA' name='Device A' image='icons/devA.png'>",
          "    <display x='1' y='2' width='10' height='20'/>",
          "  </device>",
          "  <device id='devB' name='Device B' image='icons/devB.png'>",
          "    <display x='10' y='20' width='100' height='200'/>",
          "  </device>",
          "</category>"});
      testBundle.install();
      // work with Bundle
      {
        List<CategoryInfo> categories = DeviceManager.getCategories();
        assertThat(categories.size()).isGreaterThan(1);
        {
          CategoryInfo category = DeviceManager.getCategory("catA");
          assertEquals("catA", category.getId());
          assertEquals("Category A", category.getName());
          {
            List<DeviceInfo> devices = category.getDevices();
            assertThat(devices).hasSize(2);
            // devA
            {
              DeviceInfo device = devices.get(0);
              assertTrue(device.isContributed());
              assertSame(category, device.getCategory());
              // id
              assertEquals("devA", device.getId());
              assertSame(device, DeviceManager.getDevice("devA"));
              // name
              assertEquals("Device A", device.getName());
              // image
              assertEquals(new Rectangle(1, 2, 10, 20), device.getDisplayBounds());
              assertEquals("icons/devA.png", device.getImagePath());
              assertEquals(1, device.getImage().getBounds().width);
            }
            // devB
            {
              DeviceInfo device = devices.get(1);
              assertTrue(device.isContributed());
              assertSame(category, device.getCategory());
              // id
              assertEquals("devB", device.getId());
              assertSame(device, DeviceManager.getDevice("devB"));
              // name
              assertEquals("Device B", device.getName());
              // image
              assertEquals(new Rectangle(10, 20, 100, 200), device.getDisplayBounds());
              assertEquals("icons/devB.png", device.getImagePath());
              assertEquals(2, device.getImage().getBounds().width);
            }
          }
        }
        // not found
        assertSame(null, DeviceManager.getCategory("noSuchCategory"));
        assertSame(null, DeviceManager.getDevice("noSuchDevice"));
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ElementVisibilityCommand}.
   */
  public void test_ElementVisibilityCommand_Category() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("newCat", "New category"));
    // visible initially
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertTrue(category.isVisible());
      // show (will be removed by second command)
      DeviceManager.commandsAdd(new ElementVisibilityCommand(category, true));
      // hide
      DeviceManager.commandsAdd(new ElementVisibilityCommand(category, false));
      assertFalse(category.isVisible());
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still hidden
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertFalse(category.isVisible());
    }
  }

  public void test_ElementVisibilityCommand_Device() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    // add Device
    addDevice(category, "dev", "device");
    // visible initially
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertTrue(device.isVisible());
      // show (will be removed by second command)
      DeviceManager.commandsAdd(new ElementVisibilityCommand(device, true));
      // hide
      DeviceManager.commandsAdd(new ElementVisibilityCommand(device, false));
      assertFalse(device.isVisible());
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still hidden
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertFalse(device.isVisible());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for Device
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addDevice(CategoryInfo category, String id, String name) throws Exception {
    IFile imageFile = TestUtils.createImagePNG(m_testProject, "device.png", 10, 20);
    String imagePath = imageFile.getLocation().toPortableString();
    Rectangle displayBounds = new Rectangle(1, 2, 10, 20);
    addDevice(category, id, name, imagePath, displayBounds);
  }

  private void addDevice(CategoryInfo category,
      String id,
      String name,
      String imagePath,
      Rectangle displayBounds) {
    DeviceManager.commandsAdd(new DeviceAddCommand(category, id, name, imagePath, displayBounds));
  }

  /**
   * Test for {@link DeviceAddCommand}.
   */
  public void test_DeviceAddCommand() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    // add Device
    addDevice(category, "dev", "device");
    // can use new Device
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertEquals("device", device.getName());
      assertEquals(new Rectangle(1, 2, 10, 20), device.getDisplayBounds());
      assertEquals(10, device.getImage().getBounds().width);
      assertEquals(20, device.getImage().getBounds().height);
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still can use new Device
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertEquals("device", device.getName());
      assertEquals(new Rectangle(1, 2, 10, 20), device.getDisplayBounds());
      assertEquals(10, device.getImage().getBounds().width);
      assertEquals(20, device.getImage().getBounds().height);
    }
  }

  /**
   * Test for {@link DeviceMoveCommand}.
   */
  public void test_DeviceMoveCommand() throws Exception {
    {
      CategoryInfo category = DeviceManager.getCategories().get(0);
      List<DeviceInfo> devices = category.getDevices();
      // add Device
      {
        addDevice(category, "devA", "a");
        addDevice(category, "devB", "b");
      }
      // has expected order
      DeviceInfo deviceA = DeviceManager.getDevice("devA");
      DeviceInfo deviceB = DeviceManager.getDevice("devB");
      assertEquals(devices.indexOf(deviceA), devices.indexOf(deviceB) - 1);
      // do move
      DeviceManager.commandsAdd(new DeviceMoveCommand(deviceB, category, deviceA));
      // new order
      assertEquals(devices.indexOf(deviceB), devices.indexOf(deviceA) - 1);
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still has expected order
    {
      CategoryInfo category = DeviceManager.getCategories().get(0);
      List<DeviceInfo> devices = category.getDevices();
      DeviceInfo deviceA = DeviceManager.getDevice("devA");
      DeviceInfo deviceB = DeviceManager.getDevice("devB");
      assertEquals(devices.indexOf(deviceB), devices.indexOf(deviceA) - 1);
    }
  }

  /**
   * Test for {@link DeviceMoveCommand}.
   */
  public void test_DeviceMoveCommand_asLast() throws Exception {
    {
      CategoryInfo category = DeviceManager.getCategories().get(0);
      List<DeviceInfo> devices = category.getDevices();
      // add Device
      {
        addDevice(category, "devA", "a");
        addDevice(category, "devB", "b");
      }
      // has expected order
      DeviceInfo deviceA = DeviceManager.getDevice("devA");
      assertEquals(devices.indexOf(deviceA), devices.size() - 2);
      // do move
      DeviceManager.commandsAdd(new DeviceMoveCommand(deviceA, category, null));
      // new order
      assertEquals(devices.indexOf(deviceA), devices.size() - 1);
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still has expected order
    {
      CategoryInfo category = DeviceManager.getCategories().get(0);
      List<DeviceInfo> devices = category.getDevices();
      DeviceInfo deviceA = DeviceManager.getDevice("devA");
      assertEquals(devices.indexOf(deviceA), devices.size() - 1);
    }
  }

  /**
   * Test for {@link DeviceMoveCommand}.
   */
  public void test_DeviceMoveCommand_moveBeforeItself() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    List<DeviceInfo> devices = category.getDevices();
    // add Device
    addDevice(category, "devA", "a");
    // has expected order
    DeviceInfo deviceA = DeviceManager.getDevice("devA");
    int index = devices.indexOf(deviceA);
    // ignored - move before itself
    DeviceManager.commandsAdd(new DeviceMoveCommand(deviceA, category, deviceA));
    // no change
    assertEquals(index, devices.indexOf(deviceA));
  }

  /**
   * Test for {@link DeviceMoveCommand}.
   */
  public void test_DeviceMoveCommand_noSource() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    List<DeviceInfo> devices = category.getDevices();
    // add Device
    addDevice(category, "dev", "device");
    DeviceInfo device = DeviceManager.getDevice("dev");
    // ignored - we remove device before executing 
    {
      DeviceMoveCommand command = new DeviceMoveCommand(device, category, null);
      devices.remove(device);
      DeviceManager.commandsAdd(command);
    }
  }

  /**
   * Test for {@link DeviceRemoveCommand}.
   */
  public void test_DeviceRemoveCommand() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    // add Device
    addDevice(category, "dev", "device");
    // remove Device
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertNotNull(device);
      // do remove
      DeviceManager.commandsAdd(new DeviceRemoveCommand(device));
      assertNull(DeviceManager.getDevice("dev"));
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still no Device
    assertNull(DeviceManager.getDevice("dev"));
  }

  /**
   * Test for {@link DeviceEditCommand}.
   */
  public void test_DeviceEditCommand() throws Exception {
    CategoryInfo category = DeviceManager.getCategories().get(0);
    // add Device
    {
      String imagePath =
          TestUtils.createImagePNG(m_testProject, "imA.png", 1, 2).getLocation().toPortableString();
      addDevice(category, "dev", "name", imagePath, new Rectangle(1, 2, 10, 20));
    }
    // initial Device state
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertEquals("name", device.getName());
      assertEquals(new Rectangle(1, 2, 10, 20), device.getDisplayBounds());
      assertEquals(1, device.getImage().getBounds().width);
      assertEquals(2, device.getImage().getBounds().height);
    }
    // edit
    {
      String imagePath =
          TestUtils.createImagePNG(m_testProject, "imB.png", 10, 20).getLocation().toPortableString();
      // this command will be ignored
      DeviceManager.commandsAdd(new DeviceEditCommand("dev",
          "otherName",
          imagePath,
          new Rectangle(0, 0, 0, 0)));
      // we will use this command
      DeviceManager.commandsAdd(new DeviceEditCommand("dev",
          "newName",
          imagePath,
          new Rectangle(10, 20, 100, 200)));
    }
    // Device was updated
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertEquals("newName", device.getName());
      assertEquals(new Rectangle(10, 20, 100, 200), device.getDisplayBounds());
      assertEquals(10, device.getImage().getBounds().width);
      assertEquals(20, device.getImage().getBounds().height);
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still updated Device
    {
      DeviceInfo device = DeviceManager.getDevice("dev");
      assertEquals("newName", device.getName());
      assertEquals(new Rectangle(10, 20, 100, 200), device.getDisplayBounds());
      assertEquals(10, device.getImage().getBounds().width);
      assertEquals(20, device.getImage().getBounds().height);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for Category
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CategoryAddCommand}.
   */
  public void test_CategoryAddCommand() throws Exception {
    DeviceManager.commandsAdd(new CategoryAddCommand("newCat", "New\r\ncategory"));
    // can use new Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNotNull(category);
      assertEquals("New\r\ncategory", category.getName());
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still can use new Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNotNull(category);
      assertEquals("New\r\ncategory", category.getName());
    }
  }

  /**
   * Test for {@link CategoryAddCommand}.
   */
  public void test_CategoryAddCommand_specialCharacters() throws Exception {
    DeviceManager.commandsAdd(new CategoryAddCommand("newCat", "New\r\ncategory"));
    // can use new Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNotNull(category);
      assertEquals("New\r\ncategory", category.getName());
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still can use new Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNotNull(category);
      assertEquals("New\r\ncategory", category.getName());
    }
  }

  /**
   * Test for {@link CategoryNameCommand}.
   */
  public void test_CategoryNameCommand() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("newCat", "foo"));
    // rename Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertEquals("foo", category.getName());
      DeviceManager.commandsAdd(new CategoryNameCommand(category, "bar"));
      assertEquals("bar", category.getName());
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // Category has new name
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertEquals("bar", category.getName());
    }
  }

  /**
   * Test for {@link CategoryRemoveCommand}.
   */
  public void test_CategoryRemoveCommand() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("newCat", "New category"));
    // remove Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNotNull(category);
      DeviceManager.commandsAdd(new CategoryRemoveCommand(category));
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still no Category
    {
      CategoryInfo category = DeviceManager.getCategory("newCat");
      assertNull(category);
    }
  }

  /**
   * Test for {@link CategoryMoveCommand}.
   */
  public void test_CategoryMoveCommand() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("catA", "a"));
    DeviceManager.commandsAdd(new CategoryAddCommand("catB", "b"));
    // original order
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      CategoryInfo categoryA = DeviceManager.getCategory("catA");
      CategoryInfo categoryB = DeviceManager.getCategory("catB");
      assertEquals(categories.indexOf(categoryA), categories.indexOf(categoryB) - 1);
      // do move
      DeviceManager.commandsAdd(new CategoryMoveCommand(categoryB, categoryA));
    }
    // new order
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      CategoryInfo categoryA = DeviceManager.getCategory("catA");
      CategoryInfo categoryB = DeviceManager.getCategory("catB");
      assertEquals(categories.indexOf(categoryB), categories.indexOf(categoryA) - 1);
    }
    // reload
    DeviceManager.commandsWrite();
    DeviceManager.forceReload();
    // still new order
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      CategoryInfo categoryA = DeviceManager.getCategory("catA");
      CategoryInfo categoryB = DeviceManager.getCategory("catB");
      assertEquals(categories.indexOf(categoryB), categories.indexOf(categoryA) - 1);
    }
  }

  /**
   * Test for {@link CategoryMoveCommand}.
   */
  public void test_CategoryMoveCommand_moveBeforeItself() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("catA", "a"));
    DeviceManager.commandsAdd(new CategoryAddCommand("catB", "b"));
    CategoryInfo categoryA = DeviceManager.getCategory("catA");
    CategoryInfo categoryB = DeviceManager.getCategory("catB");
    // ignored - move before itself
    DeviceManager.commandsAdd(new CategoryMoveCommand(categoryA, categoryA));
    // original order
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      assertEquals(categories.indexOf(categoryA), categories.indexOf(categoryB) - 1);
    }
  }

  /**
   * Test for {@link CategoryMoveCommand}.
   */
  public void test_CategoryMoveCommand_moveToTheEnd() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("catA", "a"));
    DeviceManager.commandsAdd(new CategoryAddCommand("catB", "b"));
    CategoryInfo categoryA = DeviceManager.getCategory("catA");
    // original order
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      assertEquals(categories.size() - 2, categories.indexOf(categoryA));
    }
    // do move
    DeviceManager.commandsAdd(new CategoryMoveCommand(categoryA, null));
    {
      List<CategoryInfo> categories = DeviceManager.getCategories();
      assertEquals(categories.size() - 1, categories.indexOf(categoryA));
    }
  }

  /**
   * Test for {@link CategoryMoveCommand}.
   */
  public void test_CategoryMoveCommand_noSource() throws Exception {
    // add Category
    DeviceManager.commandsAdd(new CategoryAddCommand("catA", "a"));
    DeviceManager.commandsAdd(new CategoryAddCommand("catB", "b"));
    CategoryInfo categoryA = DeviceManager.getCategory("catA");
    CategoryInfo categoryB = DeviceManager.getCategory("catB");
    // ignored - we delete "categoryB" before executing command
    {
      CategoryMoveCommand command = new CategoryMoveCommand(categoryB, categoryA);
      DeviceManager.getCategories().remove(categoryB);
      DeviceManager.commandsAdd(command);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Devices extension operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds dynamic "devices" extension.
   */
  protected static void addDevicesExtension(String... lines) {
    TestUtils.addDynamicExtension(DEVICES_ID, getSource(lines));
  }

  /**
   * Removes dynamic "devices" extension.
   */
  protected static void removeDevicesExtension() {
    TestUtils.removeDynamicExtension(DEVICES_ID);
  }
}