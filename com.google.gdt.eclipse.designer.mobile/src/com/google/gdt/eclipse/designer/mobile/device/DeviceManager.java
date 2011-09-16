/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.mobile.device;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.mobile.Activator;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryAddCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryMoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryNameCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.CategoryRemoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.Command;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceAddCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceEditCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceMoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceRemoveCommand;
import com.google.gdt.eclipse.designer.mobile.device.command.ElementVisibilityCommand;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Manager for accessing {@link DeviceInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DeviceManager() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_DEVICES = "com.google.gdt.eclipse.designer.mobile.devices";
  private static List<CategoryInfo> m_caterogies;

  /**
   * Specifies that devices configuration should be reloaded.
   */
  public static void forceReload() {
    m_caterogies = null;
  }

  /**
   * Removes all applied {@link Command}'s.
   */
  public static void resetToDefaults() {
    m_commands.clear();
    commandsWrite();
    forceReload();
  }

  /**
   * @return the {@link List} of {@link CategoryInfo}'s existing in configuration.
   */
  public static List<CategoryInfo> getCategories() {
    if (m_caterogies == null) {
      m_caterogies = Lists.newArrayList();
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          // load from plugins
          List<IConfigurationElement> categoryElements =
              ExternalFactoriesHelper.getElements(POINT_DEVICES, "category");
          for (IConfigurationElement categoryElement : categoryElements) {
            CategoryInfo category = new CategoryInfo(categoryElement);
            // add this category
            m_caterogies.add(category);
            // add devices
            for (IConfigurationElement deviceElement : categoryElement.getChildren("device")) {
              category.addDevice(new DeviceInfo(deviceElement));
            }
          }
          // apply commands
          commandsApply();
        }
      });
    }
    return m_caterogies;
  }

  /**
   * @return the {@link CategoryInfo} with given id, or <code>null</code> if no such
   *         {@link CategoryInfo} found.
   */
  public static CategoryInfo getCategory(String id) {
    for (CategoryInfo category : getCategories()) {
      if (category.getId().equals(id)) {
        return category;
      }
    }
    return null;
  }

  /**
   * @return the {@link DeviceInfo} with given id, or <code>null</code> if no such
   *         {@link DeviceInfo} found.
   */
  public static DeviceInfo getDevice(String id) {
    for (CategoryInfo category : getCategories()) {
      for (DeviceInfo device : category.getDevices()) {
        if (device.getId().equals(id)) {
          return device;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final List<Class<? extends Command>> m_commandClasses = Lists.newArrayList();
  static {
    m_commandClasses.add(ElementVisibilityCommand.class);
    m_commandClasses.add(CategoryAddCommand.class);
    m_commandClasses.add(CategoryNameCommand.class);
    m_commandClasses.add(CategoryMoveCommand.class);
    m_commandClasses.add(CategoryRemoveCommand.class);
    m_commandClasses.add(DeviceAddCommand.class);
    m_commandClasses.add(DeviceEditCommand.class);
    m_commandClasses.add(DeviceMoveCommand.class);
    m_commandClasses.add(DeviceRemoveCommand.class);
  }
  private static Map<String, Class<? extends Command>> m_idToCommandClass;
  private static List<Command> m_commands;

  /**
   * Applies commands for modifying palette.
   */
  private static void commandsApply() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        commandsApplyEx();
      }
    });
  }

  /**
   * Implementation for {@link #commandsApply()}.
   */
  private static void commandsApplyEx() throws Exception {
    // prepare mapping: id -> command class
    if (m_idToCommandClass == null) {
      m_idToCommandClass = Maps.newTreeMap();
      for (Class<? extends Command> commandClass : m_commandClasses) {
        String id = (String) commandClass.getField("ID").get(null);
        m_idToCommandClass.put(id, commandClass);
      }
    }
    // read commands
    m_commands = Lists.newArrayList();
    File commandsFile = getCommandsFile();
    if (commandsFile.exists()) {
      FileInputStream inputStream = new FileInputStream(commandsFile);
      try {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(inputStream, new DefaultHandler() {
          @Override
          public void startElement(String uri,
              String localName,
              final String name,
              final Attributes attributes) {
            ExecutionUtils.runIgnore(new RunnableEx() {
              public void run() throws Exception {
                commandsApplySingleEx(name, attributes);
              }
            });
          }

          private void commandsApplySingleEx(String name, Attributes attributes) throws Exception {
            // prepare command class
            Class<? extends Command> commandClass;
            {
              commandClass = m_idToCommandClass.get(name);
              if (commandClass == null) {
                return;
              }
            }
            // create command
            Command command;
            {
              Constructor<? extends Command> constructor =
                  commandClass.getConstructor(new Class[]{Attributes.class});
              command = constructor.newInstance(new Object[]{attributes});
            }
            // add command
            commandsAdd(command);
          }
        });
      } finally {
        inputStream.close();
      }
    }
  }

  /**
   * Adds given {@link Command} to the list (and executes it).
   */
  public static void commandsAdd(final Command command) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        command.execute();
        command.addToCommandList(m_commands);
      }
    });
  }

  /**
   * Stores current {@link Command}'s {@link List}.
   */
  public static void commandsWrite() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        File commandsFile = getCommandsFile();
        PrintWriter writer = new PrintWriter(new FileOutputStream(commandsFile));
        try {
          writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          writer.println("<commands>");
          // write separate commands
          for (Command command : m_commands) {
            writer.println(command.toString());
          }
          // close
          writer.println("</commands>");
        } finally {
          writer.close();
        }
      }
    });
  }

  /**
   * @return the {@link File} with {@link Command}'s.
   */
  private static File getCommandsFile() {
    File stateDirectory = Activator.getDefault().getStateLocation().toFile();
    return new File(stateDirectory, "devices.commands");
  }
}
