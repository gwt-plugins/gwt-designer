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
package com.google.gdt.eclipse.designer.hosted.tdt;

import com.google.gdt.eclipse.designer.hosted.IModuleDescription;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A copy of com.google.gdt.eclipse.designer.util.Utils due to build issues.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwtHosted
 */
final class Utils {
  static String getDevLibLocation(final IModuleDescription moduleDescription) {
    String userJarFolder = getUserJarFolder(moduleDescription);
    // try to find gwt-dev.jar in classpath
    {
      String devLocation = ExecutionUtils.runObject(new RunnableObjectEx<String>() {
        public String runObject() throws Exception {
          List<String> locations = moduleDescription.getLocations();
          return getDevJarLocation(locations);
        }
      });
      if (devLocation != null) {
        return devLocation;
      }
    }
    // Maven
    if (userJarFolder.contains("/gwt/gwt-user/")) {
      String gwtFolder = StringUtils.substringBefore(userJarFolder, "/gwt-user/");
      String versionString = StringUtils.substringAfter(userJarFolder, "/gwt/gwt-user/");
      String devFolder = gwtFolder + "/gwt-dev/" + versionString;
      String devFileName = "gwt-dev-" + versionString + ".jar";
      String devLocation = devFolder + "/" + devFileName;
      if (new File(devLocation).exists()) {
        return devLocation;
      }
    }
    // gwt-dev in same folder as gwt-user.jar
    String path = userJarFolder + "/gwt-dev.jar";
    if (new File(path).exists()) {
      return path;
    }
    // no gwt-dev
    return null;
  }

  private static String getUserJarFolder(IModuleDescription moduleDescription) {
    String userJarPath = getUserJarLocation(moduleDescription);
    if (userJarPath != null) {
      return StringUtils.substringBeforeLast(userJarPath, "/");
    } else {
      return null;
    }
  }

  private static String getUserJarLocation(final IModuleDescription moduleDescription) {
    return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        List<String> locations = moduleDescription.getLocations();
        // try to find gwt-user.jar by name
        String userJarEntry = getUserJarLocation(locations);
        if (userJarEntry != null) {
          return userJarEntry;
        }
        // not found
        return null;
      }
    });
  }

  /**
   * Lookup for "gwt-user.jar" entry; it can be "gwt-user-1.5.3.jar", so use regexp.
   */
  private static String getUserJarLocation(List<String> locations) {
    Pattern p = Pattern.compile(".*gwt-user.*\\.jar");
    for (String location : locations) {
      Matcher m = p.matcher(location);
      if (m.matches()) {
        return location;
      }
    }
    return null;
  }

  /**
   * Lookup for "gwt-dev.jar" entry; it can be "gwt-dev-1.5.3.jar", so use regexp.
   */
  private static String getDevJarLocation(List<String> locations) {
    Pattern p = Pattern.compile(".*gwt-dev.*\\.jar");
    for (String location : locations) {
      Matcher m = p.matcher(location);
      if (m.matches()) {
        return location;
      }
    }
    return null;
  }
}