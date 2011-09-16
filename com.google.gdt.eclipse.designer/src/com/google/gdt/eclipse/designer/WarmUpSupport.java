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

import com.google.gdt.eclipse.designer.hosted.IHostedModeSupport;
import com.google.gdt.eclipse.designer.hosted.IModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.Version;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.osgi.framework.Bundle;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Performs warm up of GWT hosted mode.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public class WarmUpSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final boolean DEBUG = getBooleanProperty("gwtd.warmup.debug", false);
  private static final boolean ENABLED = getBooleanProperty("gwtd.warmup.enabled", true);
  private static final int MIN_CPU_NUMBER = getIntProperty("gwtd.warmup.mincpu", 2);

  private static boolean getBooleanProperty(String name, boolean def) {
    String s = System.getProperty(name);
    return s != null ? "true".equals(s) : def;
  }

  private static int getIntProperty(String name, int def) {
    String s = System.getProperty(name);
    try {
      return Integer.parseInt(s);
    } catch (Throwable e) {
      return def;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs warm up.
   */
  public static void doWarmUp() {
    // may be disabled manually
    if (!ENABLED) {
      return;
    }
    // may be not enough CPUs
    if (Runtime.getRuntime().availableProcessors() < MIN_CPU_NUMBER) {
      return;
    }
    // do warm up
    warmUpHostedMode();
  }

  /**
   * Initializes GWT 2.2 hosted mode using any existing GWT 2.2 project.
   * <p>
   * Gives <b>3</b> times speedup for first GWT editing opening in this Eclipse session.
   */
  private static void warmUpHostedMode() {
    try {
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : projects) {
        if (Utils.isGpeGwtProject(project)) {
          Version version = Utils.getVersion(project);
          if (version.isHigherOrSame(Utils.GWT_2_2)) {
            IJavaProject javaProject = JavaCore.create(project);
            List<ModuleDescription> modules = Utils.getModules(javaProject);
            if (!modules.isEmpty()) {
              ModuleDescription module = modules.get(0);
              warmUpHostedMode(module);
              warmUpHostedMode(module);
              warmUpHostedMode(module);
            }
            break;
          }
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private static void warmUpHostedMode(ModuleDescription module) throws Exception {
    long startTime = System.nanoTime();
    Bundle hostedBundle = Platform.getBundle("com.google.gdt.eclipse.designer.hosted.2_2");
    Class<?> hmsClass =
        hostedBundle.loadClass("com.google.gdt.eclipse.designer.hosted.tdt.HostedModeSupport");
    Constructor<?> hmsConstructor = hmsClass.getConstructor(IModuleDescription.class);
    IHostedModeSupport hms = (IHostedModeSupport) hmsConstructor.newInstance(module);
    //
    try {
      ReflectionUtils.invokeMethod(
          hms,
          "createModuleSpaceHost(java.lang.String)",
          "com.google.gwt.user.User");
    } finally {
      hms.dispose();
    }
    if (DEBUG) {
      System.out.println("startup time: " + (System.nanoTime() - startTime) / 1000000.0);
    }
  }
}
