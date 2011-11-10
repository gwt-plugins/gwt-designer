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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.mobile.Activator;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.IDeviceView;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IJavaInfoInitializationParticipator} that provides device selection
 * action on editor toolbar.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceSelectionSupport
    implements
      org.eclipse.wb.core.model.IRootProcessor,
      org.eclipse.wb.internal.core.xml.model.IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new DeviceSelectionSupport();

  private DeviceSelectionSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_DEVICE_ID = new QualifiedName(Activator.PLUGIN_ID,
      "deviceId");
  private static final QualifiedName KEY_ORIENTATION_ID = new QualifiedName(Activator.PLUGIN_ID,
      "deviceOrientation");

  public static enum Orientation {
    PORTRAIT, LANDSCAPE
  };

  private static Map<Image, Image> m_rotatedImages = Maps.newHashMap();

  /**
   * @return the {@link IDeviceView} for given object.
   */
  public static IDeviceView getDeviceView(IUIObjectInfo object) {
    DeviceInfo device = getDevice(object);
    Orientation orientation = getOrientation(object);
    if (device == null) {
      return null;
    }
    // prepare view parts
    final Image image;
    final org.eclipse.wb.draw2d.geometry.Rectangle displayBounds;
    if (orientation == Orientation.PORTRAIT) {
      image = device.getImage();
      displayBounds = device.getDisplayBounds();
    } else {
      image = getRotatedImage(device.getImage());
      //displayBounds = device.getDisplayBounds().getTransposed();
      Rectangle ib = image.getBounds();
      org.eclipse.wb.draw2d.geometry.Rectangle db = device.getDisplayBounds();
      int y = ib.height - db.right();
      int x = db.y;
      displayBounds = new org.eclipse.wb.draw2d.geometry.Rectangle(x, y, db.height, db.width);
    }
    // return view
    return new IDeviceView() {
      public Image getImage() {
        return image;
      }

      public org.eclipse.wb.draw2d.geometry.Rectangle getDisplayBounds() {
        return displayBounds;
      }
    };
  }

  private static Image getRotatedImage(Image image) {
    Image rotated = m_rotatedImages.get(image);
    if (rotated == null) {
      rotated = DrawUtils.createRotatedImage(image);
      m_rotatedImages.put(image, rotated);
    }
    return rotated;
  }

  /**
   * @return the {@link Orientation} of device for given object.
   */
  public static Orientation getOrientation(final IUIObjectInfo object) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Orientation>() {
      public Orientation runObject() throws Exception {
        String name = getResource(object).getPersistentProperty(KEY_ORIENTATION_ID);
        if (name == null) {
          return Orientation.PORTRAIT;
        }
        return Enum.valueOf(Orientation.class, name);
      }
    }, Orientation.PORTRAIT);
  }

  /**
   * Sets the {@link DeviceInfo} for given object.
   */
  public static void setOrientation(final IUIObjectInfo object, final Orientation orientation) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        getResource(object).setPersistentProperty(KEY_ORIENTATION_ID, orientation.name());
        applyDevice(object);
      }
    });
  }

  /**
   * @return the {@link DeviceInfo} for given object.
   */
  public static DeviceInfo getDevice(final IUIObjectInfo object) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<DeviceInfo>() {
      public DeviceInfo runObject() throws Exception {
        String id = getResource(object).getPersistentProperty(KEY_DEVICE_ID);
        return DeviceManager.getDevice(id);
      }
    }, null);
  }

  /**
   * Sets the {@link DeviceInfo} for given object.
   */
  public static void setDevice(final IUIObjectInfo object, final DeviceInfo device) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // remember in resource
        {
          String id = device != null ? device.getId() : null;
          getResource(object).setPersistentProperty(KEY_DEVICE_ID, id);
        }
        // apply
        applyDevice(object);
      }
    });
  }

  /**
   * Applies {@link DeviceInfo} invocation into hierarchy and refresh.
   */
  private static void applyDevice(IUIObjectInfo object) throws Exception {
    IDeviceView device = getDeviceView(object);
    // set size
    if (device != null) {
      Dimension size = device.getDisplayBounds().getSize();
      object.getTopBoundsSupport().setSize(size.width, size.height);
    }
    // refresh
    object.getUnderlyingModel().refresh();
  }

  /**
   * @return the underlying {@link IResource} which was was parsed.
   */
  private static IResource getResource(IUIObjectInfo object) throws Exception {
    if (object instanceof JavaInfo) {
      return ((JavaInfo) object).getEditor().getModelUnit().getUnderlyingResource();
    }
    return ((XmlObjectInfo) object).getContext().getFile();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    processRoot(root);
  }

  public void process(XmlObjectInfo root) throws Exception {
    processRoot(root);
  }

  private void processRoot(ObjectInfo root) {
    if (root instanceof IUIObjectInfo) {
      final IUIObjectInfo rootObject = (IUIObjectInfo) root;
      root.addBroadcastListener(new ObjectEventListener() {
        private DeviceSelectionItem m_deviceSelectionItem;
        private DeviceOrientationItem m_deviceOrientationItem;

        @Override
        public void addHierarchyActions(List<Object> actions) throws Exception {
          {
            if (m_deviceSelectionItem == null) {
              m_deviceSelectionItem = new DeviceSelectionItem(rootObject);
            }
            actions.add(m_deviceSelectionItem);
            m_deviceSelectionItem.updateActions();
          }
          {
            if (m_deviceOrientationItem == null) {
              m_deviceOrientationItem = new DeviceOrientationItem(rootObject);
            }
            actions.add(m_deviceOrientationItem);
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DeviceOrientationItem
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class DeviceOrientationItem extends ContributionItem {
    private static final Image PORTRAIT = Activator.getImage("device/portrait.png");
    private static final Image LANDSCAPE = Activator.getImage("device/landscape.png");
    private final IUIObjectInfo m_rootObject;
    private ToolItem m_toolItem;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceOrientationItem(IUIObjectInfo rootObject) {
      m_rootObject = rootObject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ContributionItem
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void fill(ToolBar toolBar, int index) {
      m_toolItem = new ToolItem(toolBar, SWT.PUSH);
      m_toolItem.setImage(PORTRAIT);
      m_toolItem.setToolTipText("Flip orientation");
      m_toolItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          flipOrientation();
          updateImage();
        }
      });
      updateImage();
    }

    private void flipOrientation() {
      Orientation orientation = getOrientation(m_rootObject);
      if (orientation == Orientation.PORTRAIT) {
        setOrientation(m_rootObject, Orientation.LANDSCAPE);
      } else {
        setOrientation(m_rootObject, Orientation.PORTRAIT);
      }
    }

    private void updateImage() {
      Orientation orientation = getOrientation(m_rootObject);
      if (orientation == Orientation.PORTRAIT) {
        m_toolItem.setImage(PORTRAIT);
      } else {
        m_toolItem.setImage(LANDSCAPE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DeviceSelectionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link ContributionItem} with drop down menu of accessible devices.
   */
  private static final class DeviceSelectionItem extends ContributionItem {
    private final IUIObjectInfo m_rootObject;
    private ToolItem m_toolItem;
    private Menu m_menu;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceSelectionItem(IUIObjectInfo rootObject) {
      m_rootObject = rootObject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ContributionItem
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void fill(final ToolBar toolBar, int index) {
      m_toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
      m_toolItem.setImage(Activator.getImage("device/device.png"));
      // bind menu
      createMenu(toolBar);
      m_toolItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          createMenu(toolBar);
          // prepare location
          Point menuLocation;
          {
            Rectangle bounds = m_toolItem.getBounds();
            menuLocation = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
          }
          // show device images
          new DeviceFloatingPreview(m_menu, menuLocation);
          // show menu
          m_menu.setLocation(menuLocation);
          if (EnvironmentUtils.isTestingTime()) {
            m_toolItem.setData("designTimeMenu", m_menu);
          } else {
            m_menu.setVisible(true);
          }
        }
      });
      // update now
      updateActions();
    }

    @Override
    public void dispose() {
      disposeMenu();
      super.dispose();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Actions
    //
    ////////////////////////////////////////////////////////////////////////////
    private final List<DeviceAction> m_deviceActions = Lists.newArrayList();

    /**
     * Disposes drop-down {@link Menu}.
     */
    private void disposeMenu() {
      if (m_menu != null) {
        m_menu.dispose();
        m_menu = null;
      }
    }

    /**
     * Creates drop down {@link Menu} with {@link Action}'s for device selection.
     */
    private void createMenu(Control parent) {
      disposeMenu();
      // create new menu
      m_menu = new Menu(parent);
      // no device
      {
        addDeviceAction(m_menu, new DeviceAction(m_rootObject, null, null));
        new Separator().fill(m_menu, -1);
      }
      // add categories
      for (CategoryInfo category : DeviceManager.getCategories()) {
        if (category.isVisible()) {
          MenuItem categoryItem = new MenuItem(m_menu, SWT.CASCADE);
          categoryItem.setText(category.getName());
          //
          Menu categoryMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
          categoryItem.setMenu(categoryMenu);
          // add devices
          for (DeviceInfo device : category.getDevices()) {
            if (device.isVisible()) {
              DeviceAction action = new DeviceAction(m_rootObject, category, device);
              addDeviceAction(categoryMenu, action);
            }
          }
        }
      }
    }

    /**
     * Adds single {@link DeviceAction} to the menu.
     */
    private void addDeviceAction(Menu menu, final DeviceAction deviceAction) {
      m_deviceActions.add(deviceAction);
      //
      MenuItem menuItem = new MenuItem(menu, SWT.NONE);
      menuItem.setText(deviceAction.getText());
      menuItem.setData(deviceAction.m_device);
      // add listeners
      menuItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          deviceAction.run();
        }
      });
    }

    /**
     * Updates this item and {@link DeviceAction}'s.
     */
    private void updateActions() {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          DeviceInfo currentDevice = getDevice(m_rootObject);
          for (DeviceAction deviceAction : m_deviceActions) {
            if (deviceAction.m_device == currentDevice) {
              String text;
              if (deviceAction.m_device != null) {
                text = deviceAction.m_category.getName() + " - " + deviceAction.m_device.getName();
              } else {
                text = deviceAction.getText();
              }
              m_toolItem.setText(text);
            }
          }
        }
      });
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DeviceAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link Action} for selection single {@link DeviceInfo} for current {@link ControlInfo}.
   */
  private static final class DeviceAction extends Action {
    private final IUIObjectInfo m_rootObject;
    private final CategoryInfo m_category;
    private final DeviceInfo m_device;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceAction(IUIObjectInfo rootObject, CategoryInfo category, DeviceInfo device) {
      m_rootObject = rootObject;
      m_category = category;
      m_device = device;
      setText(getDeviceTitle(device));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the title text for device action.
     */
    private static String getDeviceTitle(DeviceInfo device) {
      if (device != null) {
        return device.getName()
            + "\t"
            + device.getDisplayBounds().width
            + "x"
            + device.getDisplayBounds().height;
      } else {
        return "No device";
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Action
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      setDevice(m_rootObject, m_device);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DevicePreview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper that display {@link DeviceInfo} image in floating window.
   */
  private static final class DeviceFloatingPreview {
    private final Shell m_shell;
    private DeviceInfo m_armedDevice;

    public DeviceFloatingPreview(Menu menu, final Point menuLocation) {
      // prepare preview Shell
      {
        m_shell = new Shell(SWT.SINGLE);
        m_shell.addListener(SWT.Paint, new Listener() {
          public void handleEvent(Event event) {
            if (m_armedDevice != null) {
              Rectangle clientArea = m_shell.getClientArea();
              Image image = m_armedDevice.getImage();
              Rectangle imageBounds = image.getBounds();
              event.gc.drawImage(
                  image,
                  0,
                  0,
                  imageBounds.width,
                  imageBounds.height,
                  clientArea.x,
                  clientArea.y,
                  clientArea.width,
                  clientArea.height);
            }
          }
        });
      }
      // listen for Menu hide
      {
        menu.addListener(SWT.Hide, new Listener() {
          public void handleEvent(Event event) {
            if (event.type == SWT.Hide) {
              m_shell.dispose();
            }
          }
        });
      }
      // listen for MenuItem's
      {
        Listener itemListener = new Listener() {
          public void handleEvent(Event event) {
            m_armedDevice = (DeviceInfo) event.widget.getData();
            if (m_armedDevice != null) {
              Rectangle imageBounds = m_armedDevice.getImage().getBounds();
              int width = imageBounds.width / 3;
              int height = imageBounds.height / 3;
              m_shell.setBounds(menuLocation.x - width - 5, menuLocation.y + 5, width, height);
              m_shell.setVisible(true);
            } else {
              m_shell.setVisible(false);
            }
            m_shell.redraw();
          }
        };
        addMenuItemListener(menu, SWT.Arm, itemListener);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Adds {@link Listener} with given types for all {@link MenuItem} in given {@link Menu} and
     * sub-menus.
     */
    private static void addMenuItemListener(Menu menu, int type, Listener listener) {
      for (MenuItem menuItem : menu.getItems()) {
        if (menuItem.getStyle() == SWT.CASCADE) {
          addMenuItemListener(menuItem.getMenu(), type, listener);
        } else {
          menuItem.addListener(type, listener);
        }
      }
    }
  }
}
