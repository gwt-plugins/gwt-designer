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
package com.google.gdt.eclipse.designer.tests;

import org.eclipse.wb.internal.core.BundleResourceProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "org.eclipse.wb.tests";
  private static Activator m_plugin;
  private BundleContext m_context;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Activator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
    m_context = context;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Activator getDefault() {
    return m_plugin;
  }

  public BundleContext getContext() {
    return m_context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Window/page/shell operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public static IWorkbenchWindow getActiveWindow() {
    return getDefault().getWorkbench().getActiveWorkbenchWindow();
  }

  public static IWorkbenchPage getActivePage() {
    return getActiveWindow().getActivePage();
  }

  public static Shell getShell() {
    return getActiveWindow().getShell();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final BundleResourceProvider m_resourceProvider =
      BundleResourceProvider.get(PLUGIN_ID);

  /**
   * @return the {@link InputStream} for file from plugin directory.
   */
  public static InputStream getFile(String path) {
    return m_resourceProvider.getFile(path);
  }

  /**
   * @return the {@link Image} from "icons" directory, with caching.
   */
  public static Image getImage(String path) {
    return m_resourceProvider.getImage("icons/" + path);
  }

  /**
   * @return the {@link ImageDescriptor} from "icons" directory.
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return m_resourceProvider.getImageDescriptor("icons/" + path);
  }
}
