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
package com.google.gdt.eclipse.designer;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.launch.GwtLaunchConfigurationConfigurator;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.preferences.PreferenceToSystemForwarder;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.collections.CollectionUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public class Activator extends AbstractUIPlugin implements Constants, IStartup {
  public static final String PLUGIN_ID = "com.google.gdt.eclipse.designer";
  private static Activator m_plugin;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
    // install preference forwarder for WebKit
    // don't follow with Mac
    if (!EnvironmentUtils.IS_MAC) {
      new PreferenceToSystemForwarder(getPreferenceStore(),
          Constants.P_GWT_USE_WEBKIT,
          "__wbp.gwt.useWebKit");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the shared instance.
   */
  public static Activator getDefault() {
    return m_plugin;
  }

  /**
   * @return this {@link Bundle}, can be used even without starting this plugin.
   */
  private static Bundle getBundleStatic() {
    return Platform.getBundle(PLUGIN_ID);
  }

  /**
   * @return the {@link String} from bundle localization.
   */
  public static String getResourceString(String key) {
    return Platform.getResourceString(getBundleStatic(), key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return array of entry sub-paths in given path.
   */
  @SuppressWarnings("unchecked")
  public static String[] getEntriesPaths(String path) {
    List<String> entryPaths;
    {
      entryPaths = Lists.newArrayList();
      Enumeration<String> entryPathsEnumeration = getBundleStatic().getEntryPaths(path);
      CollectionUtils.addAll(entryPaths, entryPathsEnumeration);
    }
    // remove ".svn" files (for case when we use runtime workbench)
    for (Iterator<String> I = entryPaths.iterator(); I.hasNext();) {
      String entryPath = I.next();
      if (entryPath.indexOf(".svn") != -1) {
        I.remove();
      }
    }
    // convert to array
    return entryPaths.toArray(new String[entryPaths.size()]);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns preference store for GWT plugin.
   */
  public static IPreferenceStore getStore() {
    return m_plugin.getPreferenceStore();
  }

  /**
   * Returns location of GWT installation directory.
   */
  public static String getGWTLocation() {
    return getStore().getString(P_GWT_LOCATION);
  }

  /**
   * Sets location of GWT installation directory.
   */
  public static void setGWTLocation(String location) {
    getStore().setValue(P_GWT_LOCATION, location);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IStartup
  //
  ////////////////////////////////////////////////////////////////////////////
  public void earlyStartup() {
    new GwtLaunchConfigurationConfigurator();
    WarmUpSupport.doWarmUp();
  }
}
